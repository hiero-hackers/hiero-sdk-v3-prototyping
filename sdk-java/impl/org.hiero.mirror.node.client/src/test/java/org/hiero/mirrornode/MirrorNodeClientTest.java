package org.hiero.mirrornode;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.hiero.authority.AuthorityFactory;
import org.hiero.http.HttpClient;
import org.hiero.http.HttpConfiguration;
import org.hiero.http.HttpFactory;
import org.hiero.http.HttpResponse;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.mirrornode.account.AccountFactory;
import org.hiero.mirrornode.account.AccountRepository;
import org.hiero.mirrornode.contract.ContractFactory;
import org.hiero.mirrornode.contract.ContractRepository;
import org.hiero.mirrornode.http.MirrorNodeHttpClient;
import org.hiero.mirrornode.http.MirrorNodeHttpRequest;
import org.hiero.mirrornode.network.NetworkFactory;
import org.hiero.mirrornode.network.NetworkRepository;
import org.hiero.mirrornode.nft.NftFactory;
import org.hiero.mirrornode.nft.NftRepository;
import org.hiero.mirrornode.token.TokenFactory;
import org.hiero.mirrornode.token.TokenRepository;
import org.hiero.mirrornode.topic.TopicFactory;
import org.hiero.mirrornode.topic.TopicRepository;
import org.hiero.mirrornode.transaction.TransactionFactory;
import org.hiero.mirrornode.transaction.TransactionRepository;
import org.hiero.sdk.annotation.ThreadSafe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `MirrorNodeClient`, generated from the specs.
@DisplayName("MirrorNodeClient")
class MirrorNodeClientTest {

    @Test
    @DisplayName("creates a MirrorNodeClient and returns the values")
    @Tag("testId:mirrornode.MirrorNodeClient/create")
    void shouldCreateAndReturnTheValues() {
        final AccountRepository accounts = accountsValue();
        final ContractRepository contracts = contractsValue();
        final NetworkRepository network = networkValue();
        final NftRepository nft = nftValue();
        final TokenRepository token = tokenValue();
        final TopicRepository topic = topicValue();
        final TransactionRepository transaction = transactionValue();
        final MirrorNodeHttpClient mirrorNodeHttpClient = mirrorNodeHttpClientValue();
        final MirrorNodeClient subject = new MirrorNodeClient(accounts, contracts, network, nft, token, topic, transaction, mirrorNodeHttpClient);
        assertValue(accounts, subject.accounts());
        assertValue(contracts, subject.contracts());
        assertValue(network, subject.network());
        assertValue(nft, subject.nft());
        assertValue(token, subject.token());
        assertValue(topic, subject.topic());
        assertValue(transaction, subject.transaction());
        assertValue(mirrorNodeHttpClient, subject.mirrorNodeHttpClient());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.MirrorNodeClient/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountRepository accounts = accountsValue();
        final ContractRepository contracts = contractsValue();
        final NetworkRepository network = networkValue();
        final NftRepository nft = nftValue();
        final TokenRepository token = tokenValue();
        final TopicRepository topic = topicValue();
        final TransactionRepository transaction = transactionValue();
        final MirrorNodeHttpClient mirrorNodeHttpClient = mirrorNodeHttpClientValue();
        final MirrorNodeClient subject = MirrorNodeClient.builder()
                .accounts(accounts)
                .contracts(contracts)
                .network(network)
                .nft(nft)
                .token(token)
                .topic(topic)
                .transaction(transaction)
                .mirrorNodeHttpClient(mirrorNodeHttpClient)
                .build();
        assertValue(accounts, subject.accounts());
        assertValue(contracts, subject.contracts());
        assertValue(network, subject.network());
        assertValue(nft, subject.nft());
        assertValue(token, subject.token());
        assertValue(topic, subject.topic());
        assertValue(transaction, subject.transaction());
        assertValue(mirrorNodeHttpClient, subject.mirrorNodeHttpClient());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.MirrorNodeClient/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final MirrorNodeClient subject = MirrorNodeClient.builder()
                .accounts(accountsValue())
                .contracts(contractsValue())
                .network(networkValue())
                .nft(nftValue())
                .token(tokenValue())
                .topic(topicValue())
                .transaction(transactionValue())
                .mirrorNodeHttpClient(mirrorNodeHttpClientValue())
                .build();
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.MirrorNodeClient/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> MirrorNodeClient.builder().build());
    }

    @Test
    @DisplayName("rejects null for accounts")
    @Tag("testId:mirrornode.MirrorNodeClient/accounts/rejects-null")
    void shouldRejectNullAccounts() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient((AccountRepository) null, contractsValue(), networkValue(), nftValue(), tokenValue(), topicValue(), transactionValue(), mirrorNodeHttpClientValue()));
    }

    @Test
    @DisplayName("rejects null for contracts")
    @Tag("testId:mirrornode.MirrorNodeClient/contracts/rejects-null")
    void shouldRejectNullContracts() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient(accountsValue(), (ContractRepository) null, networkValue(), nftValue(), tokenValue(), topicValue(), transactionValue(), mirrorNodeHttpClientValue()));
    }

    @Test
    @DisplayName("rejects null for network")
    @Tag("testId:mirrornode.MirrorNodeClient/network/rejects-null")
    void shouldRejectNullNetwork() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient(accountsValue(), contractsValue(), (NetworkRepository) null, nftValue(), tokenValue(), topicValue(), transactionValue(), mirrorNodeHttpClientValue()));
    }

    @Test
    @DisplayName("rejects null for nft")
    @Tag("testId:mirrornode.MirrorNodeClient/nft/rejects-null")
    void shouldRejectNullNft() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient(accountsValue(), contractsValue(), networkValue(), (NftRepository) null, tokenValue(), topicValue(), transactionValue(), mirrorNodeHttpClientValue()));
    }

    @Test
    @DisplayName("rejects null for token")
    @Tag("testId:mirrornode.MirrorNodeClient/token/rejects-null")
    void shouldRejectNullToken() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient(accountsValue(), contractsValue(), networkValue(), nftValue(), (TokenRepository) null, topicValue(), transactionValue(), mirrorNodeHttpClientValue()));
    }

    @Test
    @DisplayName("rejects null for topic")
    @Tag("testId:mirrornode.MirrorNodeClient/topic/rejects-null")
    void shouldRejectNullTopic() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient(accountsValue(), contractsValue(), networkValue(), nftValue(), tokenValue(), (TopicRepository) null, transactionValue(), mirrorNodeHttpClientValue()));
    }

    @Test
    @DisplayName("rejects null for transaction")
    @Tag("testId:mirrornode.MirrorNodeClient/transaction/rejects-null")
    void shouldRejectNullTransaction() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient(accountsValue(), contractsValue(), networkValue(), nftValue(), tokenValue(), topicValue(), (TransactionRepository) null, mirrorNodeHttpClientValue()));
    }

    @Test
    @DisplayName("rejects null for mirrorNodeHttpClient")
    @Tag("testId:mirrornode.MirrorNodeClient/mirrorNodeHttpClient/rejects-null")
    void shouldRejectNullMirrorNodeHttpClient() {
        assertThrows(NullPointerException.class, () -> new MirrorNodeClient(accountsValue(), contractsValue(), networkValue(), nftValue(), tokenValue(), topicValue(), transactionValue(), (MirrorNodeHttpClient) null));
    }

    private static AccountRepository accountsValue() {
        return AccountFactory.createRepository(new MirrorNode("http://127.0.0.1:38081"));
    }

    private static ContractRepository contractsValue() {
        return new ContractRepository();
    }

    private static NetworkRepository networkValue() {
        return new NetworkRepository();
    }

    private static NftRepository nftValue() {
        return NftFactory.createRepository(new MirrorNode("http://127.0.0.1:38081"));
    }

    private static TokenRepository tokenValue() {
        return TokenFactory.createRepository(new MirrorNode("http://127.0.0.1:38081"));
    }

    private static TopicRepository topicValue() {
        return TopicFactory.createRepository(new MirrorNode("http://127.0.0.1:38081"));
    }

    private static TransactionRepository transactionValue() {
        return TransactionFactory.createRepository(new MirrorNode("http://127.0.0.1:38081"));
    }

    private static MirrorNodeHttpClient mirrorNodeHttpClientValue() {
        return new MirrorNodeHttpClient(new MirrorNode("http://127.0.0.1:38081"), HttpFactory.createHttpClient(new HttpConfiguration(Map.ofEntries(Map.entry("value", "value")), Duration.ofSeconds(1L), Duration.ofSeconds(1L)))) {
            @ThreadSafe(group = "client")
            public CompletionStage<HttpResponse> execute(final MirrorNodeHttpRequest request) {
                throw new UnsupportedOperationException("test double");
            }
        };
    }

    private static MirrorNodeClient create() {
        return new MirrorNodeClient(accountsValue(), contractsValue(), networkValue(), nftValue(), tokenValue(), topicValue(), transactionValue(), mirrorNodeHttpClientValue());
    }
}
