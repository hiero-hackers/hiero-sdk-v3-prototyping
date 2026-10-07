package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityList;
import org.hiero.authority.ContractAuthority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.transactions.HapiTransactionStatus;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.Duration;
import com.hederahashgraph.api.proto.java.ExchangeRateSet;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.ThresholdKey;
import com.hederahashgraph.api.proto.java.Timestamp;
import com.hederahashgraph.api.proto.java.TransactionID;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.TransactionId;
import org.hiero.ledger.internal.DefaultTransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.hiero.nativeToken.internal.DefaultExchangeRate;

/// Converts between the API types and the protobuf messages of the Hiero API.
public final class Protobuf {

    private Protobuf() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /// Converts an account id.
    ///
    /// @param accountId the account id
    /// @return the message
    public static AccountID toProto(final AccountId accountId) {
        final Long num = accountId.num();
        final AccountID.Builder builder = AccountID.newBuilder()
                .setShardNum(accountId.shard())
                .setRealmNum(accountId.realm());
        if (num != null) {
            builder.setAccountNum(num);
        } else {
            final var evmAddress = accountId.evmAddress();
            final byte[] alias = evmAddress != null ? evmAddress.bytes() : accountId.alias();
            if (alias == null) {
                throw new IllegalArgumentException("The account id has neither a number nor an alias");
            }
            builder.setAlias(ByteString.copyFrom(alias));
        }
        return builder.build();
    }

    /// Converts an account id back.
    ///
    /// @param accountId the message
    /// @return the account id
    public static AccountId fromProto(final AccountID accountId) {
        return new AccountId(accountId.getShardNum(), accountId.getRealmNum(), "",
                accountId.getAccountNum(), null, null);
    }

    /// Converts a transaction id.
    ///
    /// @param transactionId the transaction id
    /// @return the message
    public static TransactionID toProto(final TransactionId transactionId) {
        final TransactionID.Builder builder = TransactionID.newBuilder()
                .setAccountID(toProto(transactionId.accountId()))
                .setTransactionValidStart(toProto(transactionId.validStart()));
        final Integer nonce = transactionId.nonce();
        if (nonce != null) {
            builder.setNonce(nonce);
        }
        return builder.build();
    }

    /// Converts a transaction id back.
    ///
    /// @param transactionId the message
    /// @return the transaction id
    public static TransactionId fromProto(final TransactionID transactionId) {
        final Timestamp start = transactionId.getTransactionValidStart();
        final ZonedDateTime validStart = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(start.getSeconds(), start.getNanos()), ZoneOffset.UTC);
        final int nonce = transactionId.getNonce();
        return new DefaultTransactionId(fromProto(transactionId.getAccountID()), validStart,
                nonce == 0 ? null : nonce);
    }

    /// Converts an instant.
    ///
    /// @param time the instant
    /// @return the message
    public static Timestamp toProto(final ZonedDateTime time) {
        final Instant instant = time.toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    /// Converts a duration.
    ///
    /// @param duration the duration
    /// @return the message
    public static Duration toProto(final java.time.Duration duration) {
        return Duration.newBuilder().setSeconds(duration.toSeconds()).build();
    }

    /// Converts an authority into the HAPI key structure.
    ///
    /// @param authority the authority
    /// @return the message
    public static Key toProto(final Authority authority) {
        return switch (authority) {
            case PublicKeyAuthority(PublicKey publicKey) -> toProto(publicKey);
            case AuthorityList(var children, var threshold) -> {
                final KeyList.Builder keys = KeyList.newBuilder();
                children.forEach(child -> keys.addKeys(toProto(child)));
                yield threshold == children.size()
                        ? Key.newBuilder().setKeyList(keys).build()
                        : Key.newBuilder().setThresholdKey(
                                ThresholdKey.newBuilder().setThreshold(threshold).setKeys(keys)).build();
            }
            case ContractAuthority contract -> throw new UnsupportedOperationException(
                    "Not implemented yet: a contract authority (" + contract + ") in a transaction");
        };
    }

    /// Converts a public key into the HAPI key structure.
    ///
    /// @param publicKey the key
    /// @return the message
    public static Key toProto(final PublicKey publicKey) {
        final ByteString raw = ByteString.copyFrom(publicKey.toRawBytes());
        return publicKey.algorithm() == KeyAlgorithm.ECDSA
                ? Key.newBuilder().setECDSASecp256K1(raw).build()
                : Key.newBuilder().setEd25519(raw).build();
    }

    /// Builds the signature pair of a signature, with the full public key as the prefix.
    ///
    /// @param publicKey the key that signed
    /// @param signature the signature
    /// @return the message
    public static SignaturePair toSignaturePair(final PublicKey publicKey, final byte[] signature) {
        final SignaturePair.Builder builder = SignaturePair.newBuilder()
                .setPubKeyPrefix(ByteString.copyFrom(publicKey.toRawBytes()));
        return publicKey.algorithm() == KeyAlgorithm.ECDSA
                ? builder.setECDSASecp256K1(ByteString.copyFrom(signature)).build()
                : builder.setEd25519(ByteString.copyFrom(signature)).build();
    }

    /// Converts a response code into a transaction status.
    ///
    /// @param code the code
    /// @return the status
    public static HapiTransactionStatus fromProto(final ResponseCodeEnum code) {
        return HapiTransactionStatus.ofCode(code.getNumber());
    }

    /// Returns the current rate of an exchange rate set.
    ///
    /// @param rates the set
    /// @return the current rate
    public static ExchangeRate currentRate(final ExchangeRateSet rates) {
        return toExchangeRate(rates.getCurrentRate());
    }

    /// Returns the next rate of an exchange rate set.
    ///
    /// @param rates the set
    /// @return the next rate
    public static ExchangeRate nextRate(final ExchangeRateSet rates) {
        return toExchangeRate(rates.getNextRate());
    }

    private static ExchangeRate toExchangeRate(final com.hederahashgraph.api.proto.java.ExchangeRate rate) {
        final ZonedDateTime expiration = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(rate.getExpirationTime().getSeconds()), ZoneOffset.UTC);
        final int hbarEquiv = rate.getHbarEquiv();
        final double centsPerHbar = hbarEquiv == 0 ? 0.0 : (double) rate.getCentEquiv() / hbarEquiv;
        return new DefaultExchangeRate(expiration, centsPerHbar);
    }
}
