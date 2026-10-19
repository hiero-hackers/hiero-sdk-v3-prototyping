package org.hiero.enterprise.service.internal;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.consensusnode.client.HieroClient;
import org.hiero.consensusnode.client.TransactionSigner;
import org.hiero.consensusnode.queries.Query;
import org.hiero.consensusnode.queries.QueryResponse;
import org.hiero.consensusnode.transactions.Receipt;
import org.hiero.consensusnode.transactions.Record;
import org.hiero.consensusnode.transactions.Response;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.Session;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.mirrornode.MirrorNodeClient;
import org.hiero.mirrornode.MirrornodeFactory;
import org.hiero.mirrornode.transaction.TransactionInfo;
import org.jspecify.annotations.Nullable;

/// The only `Session`: a client of the consensus nodes, a lazily created Mirror Node client and the high-water-mark of
/// the writes executed through the services of the session.
///
/// The high-water-mark is tracked by transaction: every successful write is remembered until the Mirror Node knows its
/// transaction. A Mirror Node imports transactions in consensus order, so once it knows a transaction it reflects every
/// write that reached consensus before it. `awaitConsistency()` polls the Mirror Node for every remembered transaction
/// and advances the high-water-mark to the latest consensus timestamp seen.
public final class DefaultSession implements Session {

    /// The protocol value of `SUCCESS` (`BasicTransactionStatus` has no constant for it).
    private static final int SUCCESS = 22;

    private static final Duration CONSISTENCY_TIMEOUT = Duration.ofSeconds(30);

    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    private static final Executor EXECUTOR = task -> Thread.ofVirtual().name("hiero-session").start(task);

    private final NetworkSetting networkSetting;

    private final Account operatorAccount;

    private final HieroClient<?> client;

    private final Set<TransactionId> pending = ConcurrentHashMap.newKeySet();

    private final AtomicReference<@Nullable ZonedDateTime> highWaterMark = new AtomicReference<>();

    private final Object mirrorNodeLock = new Object();

    private volatile @Nullable MirrorNodeClient mirrorNodeClient;

    /// Creates a session that signs with the given signer.
    ///
    /// @param networkSetting    the network
    /// @param operatorAccount   the account that pays for the transactions
    /// @param transactionSigner the signer of the transactions, or `null` to sign with the operator's private key
    public DefaultSession(final NetworkSetting networkSetting, final Account operatorAccount,
            final @Nullable TransactionSigner transactionSigner) {
        this.networkSetting = Objects.requireNonNull(networkSetting, "networkSettings must not be null");
        this.operatorAccount = Objects.requireNonNull(operatorAccount, "operatorAccount must not be null");
        this.client = transactionSigner == null
                ? ClientFactory.createClient(networkSetting, operatorAccount)
                : ClientFactory.createClient(networkSetting, operatorAccount, transactionSigner);
    }

    /// Returns the session behind the given one.
    ///
    /// @param session a session created by `ServiceFactory`
    /// @return the session
    /// @throws NullPointerException     if `session` is `null`
    /// @throws IllegalArgumentException if the session was not created by `ServiceFactory`
    public static DefaultSession of(final Session session) {
        Objects.requireNonNull(session, "session must not be null");
        if (session instanceof DefaultSession defaultSession) {
            return defaultSession;
        }
        throw new IllegalArgumentException("The session was not created by ServiceFactory.createSession: " + session);
    }

    /// The client of the consensus nodes.
    ///
    /// @return the client
    public HieroClient<?> client() {
        return client;
    }

    /// The network of the session.
    ///
    /// @return the network
    public Network<?> network() {
        return networkSetting.network();
    }

    /// The account that pays for the transactions of the session.
    ///
    /// @return the id of the operator account
    public AccountId operatorAccountId() {
        return operatorAccount.accountId();
    }

    /// The authority of the operator account: its public key.
    ///
    /// @return the authority
    public Authority operatorAuthority() {
        return AuthorityFactory.of(operatorAccount.privateKey().createPublicKey());
    }

    /// The latest consensus timestamp the Mirror Node is known to reflect for this session.
    ///
    /// @return the high-water-mark, or `null` if no write has been confirmed by the Mirror Node yet
    public @Nullable ZonedDateTime highWaterMark() {
        return highWaterMark.get();
    }

    /// Signs a transaction with the operator, submits it, waits for its receipt and remembers it for the
    /// high-water-mark.
    ///
    /// @param transaction the transaction
    /// @param <R>         the type of the receipt
    /// @return the receipt, whose status is `SUCCESS`
    /// @throws ServiceException if the transaction cannot be submitted or does not succeed
    public <R extends Receipt> R execute(final Transaction<R, ?> transaction) {
        return execute(transaction, false).receipt();
    }

    /// Like `execute`, but also queries the record of the transaction for its consensus timestamp.
    ///
    /// @param transaction the transaction
    /// @param <R>         the type of the receipt
    /// @return the record, whose receipt has the status `SUCCESS`
    /// @throws ServiceException if the transaction cannot be submitted or does not succeed
    public <R extends Receipt> Record<R> executeWithRecord(final Transaction<R, ?> transaction) {
        final Executed<R> executed = execute(transaction, true);
        final Record<R> record = executed.record();
        if (record == null) {
            throw new ServiceException("No record of transaction " + executed.receipt().transactionId(), null);
        }
        return record;
    }

    private <R extends Receipt> Executed<R> execute(final Transaction<R, ?> transaction, final boolean withRecord) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        try {
            final Response<R> response = await(transaction.signWithOperatorAndSubmit(client));
            final R receipt = await(response.queryReceipt(client));
            final TransactionId transactionId = response.transactionId();
            // a transaction with a receipt reached consensus, successful or not: the Mirror Node will know it
            pending.add(transactionId);
            if (receipt.status().code() != SUCCESS) {
                throw new ServiceException("Transaction " + transactionId + " failed with status "
                        + receipt.status(), null);
            }
            if (!withRecord) {
                return new Executed<>(receipt, null);
            }
            return new Executed<>(receipt, await(response.queryRecord(client)));
        } catch (final ServiceException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new ServiceException("Executing " + transaction.getClass().getSimpleName() + " failed: "
                    + e.getMessage(), e);
        }
    }

    /// Submits a query to the consensus nodes and returns its value.
    ///
    /// @param query the query
    /// @param <T>   the type of the value
    /// @return the value
    /// @throws ServiceException if the query fails
    public <T> T query(final Query<T, ? extends QueryResponse<T>> query) {
        Objects.requireNonNull(query, "query must not be null");
        try {
            return await(query.submit(client)).value();
        } catch (final ServiceException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new ServiceException("Executing " + query.getClass().getSimpleName() + " failed: "
                    + e.getMessage(), e);
        }
    }

    /// Reads from the Mirror Node after it has caught up to the high-water-mark.
    ///
    /// @param read the read
    /// @param <T>  the type of the result
    /// @return the result
    /// @throws ServiceException if the Mirror Node does not catch up or the read fails
    public <T> T read(final Function<MirrorNodeClient, CompletionStage<T>> read) {
        Objects.requireNonNull(read, "read must not be null");
        awaitConsistencyNow();
        try {
            return await(read.apply(mirrorNode()));
        } catch (final ServiceException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new ServiceException("Reading from the Mirror Node failed: " + e.getMessage(), e);
        }
    }

    /// The Mirror Node client of the session, created on first use. Reads through it do not wait for consistency.
    ///
    /// @return the client
    /// @throws ServiceException if the network has no Mirror Node or the client cannot be created
    public MirrorNodeClient mirrorNode() {
        MirrorNodeClient result = mirrorNodeClient;
        if (result == null) {
            synchronized (mirrorNodeLock) {
                result = mirrorNodeClient;
                if (result == null) {
                    final MirrorNode mirrorNode = networkSetting.getMirrorNodes().stream().findFirst()
                            .orElseThrow(() -> new ServiceException("The network setting names no Mirror Node", null));
                    try {
                        result = MirrornodeFactory.createMirrorNodeClient(mirrorNode);
                    } catch (final RuntimeException e) {
                        throw new ServiceException("Cannot create the Mirror Node client: " + e.getMessage(), e);
                    }
                    mirrorNodeClient = result;
                }
            }
        }
        return result;
    }

    @Override
    public CompletionStage<Void> awaitConsistency() {
        if (pending.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(this::awaitConsistencyNow, EXECUTOR);
    }

    /// Blocks until the Mirror Node knows every write of this session.
    ///
    /// @throws ServiceException if the Mirror Node does not catch up within the timeout
    public void awaitConsistencyNow() {
        if (pending.isEmpty()) {
            return;
        }
        final long deadline = System.nanoTime() + CONSISTENCY_TIMEOUT.toNanos();
        for (final TransactionId transactionId : List.copyOf(pending)) {
            RuntimeException lastFailure = null;
            while (true) {
                try {
                    final TransactionInfo info = await(mirrorNode().transaction().findById(transactionId));
                    if (info != null) {
                        advance(info.consensusTimestamp());
                        pending.remove(transactionId);
                        break;
                    }
                } catch (final RuntimeException e) {
                    lastFailure = e;
                }
                if (System.nanoTime() > deadline) {
                    throw new ServiceException("The Mirror Node does not reflect transaction " + transactionId
                            + " within " + CONSISTENCY_TIMEOUT.toSeconds() + " s", lastFailure);
                }
                sleep();
            }
        }
    }

    private void advance(final ZonedDateTime consensusTimestamp) {
        highWaterMark.accumulateAndGet(consensusTimestamp,
                (current, update) -> current == null || update.isAfter(current) ? update : current);
    }

    private static void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Interrupted while waiting for the Mirror Node", e);
        }
    }

    /// Waits for a stage and maps every failure to a `ServiceException`.
    ///
    /// @param stage the stage
    /// @param <T>   the type of the result
    /// @return the result
    /// @throws ServiceException if the stage fails
    public static <T> T await(final CompletionStage<T> stage) {
        try {
            return stage.toCompletableFuture().join();
        } catch (final CompletionException | CancellationException e) {
            final Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof ServiceException serviceException) {
                throw serviceException;
            }
            throw new ServiceException(String.valueOf(cause.getMessage()), cause);
        } catch (final ServiceException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new ServiceException(String.valueOf(e.getMessage()), e);
        }
    }

    @Override
    public String toString() {
        return "DefaultSession[network=" + networkSetting.network().name() + ", operator="
                + operatorAccount.accountId() + ", highWaterMark=" + highWaterMark.get() + "]";
    }

    /// The receipt and, if queried, the record of an executed transaction.
    ///
    /// @param receipt the receipt
    /// @param record  the record, or `null`
    /// @param <R>     the type of the receipt
    private record Executed<R extends Receipt>(R receipt, @Nullable Record<R> record) {
    }
}
