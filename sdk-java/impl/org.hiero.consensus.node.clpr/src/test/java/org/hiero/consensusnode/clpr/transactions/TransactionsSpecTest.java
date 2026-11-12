package org.hiero.consensusnode.clpr.transactions;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.clpr.ClprThrottles;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.consensusnode.transactions.Transaction;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions")
class TransactionsSpecTest {

    /// The Channel ID and the salt of the ownership test vectors of the SDK proposal for HIP-1535.
    private static byte[] CHANNEL_ID() {
        return hex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
    }

    private static byte[] SALT() {
        return hex("202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f");
    }

    /// The public test keys of the ownership test vectors: the secp256k1 scalar 1 and the Ed25519 seed of RFC 8032, test 1.
    private static PrivateKey ECDSA_KEY() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ECDSA, ByteImportEncoding.HEX, "0000000000000000000000000000000000000000000000000000000000000001");
    }

    private static PrivateKey ED25519_KEY() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60");
    }

    /// A Channel ID one byte short of 32 bytes.
    private static byte[] SHORT_CHANNEL_ID() {
        return repeat(hex("01"), 31L);
    }

    /// `consensusnode.clpr.transactions/the-channel-commitment-is-the-keccak-256-hash-of-the-channel-id-and-the-public-key`
    ///
    /// Source: the ownership test vectors of the SDK proposal for HIP-1535 (channelCommitment of both vectors).
    @Test
    @DisplayName("the Channel commitment is the Keccak-256 hash of the Channel ID and the public key")
    @Tag("testId:consensusnode.clpr.transactions/the-channel-commitment-is-the-keccak-256-hash-of-the-channel-id-and-the-public-key")
    @Tag("hip:1535")
    void theChannelCommitmentIsTheKeccak256HashOfTheChannelIDAndThePublicKey() {
        // when
        final byte[] ecdsa = ClprRegisterChannelTransaction.computeOwnershipCommitment(CHANNEL_ID(), ECDSA_KEY().createPublicKey());
        final byte[] ed25519 = ClprRegisterChannelTransaction.computeOwnershipCommitment(CHANNEL_ID(), ED25519_KEY().createPublicKey());

        // then
        assertEquals(hex("f476ca287a171dd8aa94165fb27fadee1fc61f051840382aed326e1fd41d41b3"), ecdsa);
        assertEquals(hex("4816bb408545af511a22953026b40500890c7fa0a44c83682e251b114a0b6496"), ed25519);
    }

    /// `consensusnode.clpr.transactions/the-connector-id-and-commitment-are-the-keccak-256-hashes-of-the-protocol`
    ///
    /// Source: the ownership test vectors of the SDK proposal for HIP-1535 (connectorId and connectorCommitment of both
    /// vectors).
    @Test
    @DisplayName("the Connector ID and commitment are the Keccak-256 hashes of the protocol")
    @Tag("testId:consensusnode.clpr.transactions/the-connector-id-and-commitment-are-the-keccak-256-hashes-of-the-protocol")
    @Tag("hip:1535")
    void theConnectorIDAndCommitmentAreTheKeccak256HashesOfTheProtocol() {
        // when
        final byte[] ecdsaId = ClprRegisterConnectorTransaction.computeConnectorId(CHANNEL_ID(), ECDSA_KEY().createPublicKey(), SALT());
        final byte[] ed25519Id = ClprRegisterConnectorTransaction.computeConnectorId(CHANNEL_ID(), ED25519_KEY().createPublicKey(), SALT());

        // then
        assertEquals(hex("532a8331032028874f4d7fa1953636f8b076876c493606f93a86d8880058b4a6"), ecdsaId);
        assertEquals(hex("488d7b7fdcc061f1de9fe98666c1337f0fc90275889f2450d59e1e6e837e74b1"), ed25519Id);
        assertEquals(hex("647e3f78cfdbceb3714e6c4d043f60e3169cabae3d90be168aeb58796ed6533d"), ClprRegisterConnectorTransaction.computeOwnershipCommitment(ecdsaId, ECDSA_KEY().createPublicKey()));
        assertEquals(hex("a36b6836b720019a9fb1846df6bb4b76d13a1f276da0ebc2371b4d0eb8e30618"), ClprRegisterConnectorTransaction.computeOwnershipCommitment(ed25519Id, ED25519_KEY().createPublicKey()));
    }

    /// `consensusnode.clpr.transactions/a-channel-ownership-proof-signs-the-keccak-256-hash-of-the-channel-id`
    ///
    /// Source: the ownership test vectors of the SDK proposal for HIP-1535 (channelSignature of both vectors; the ECDSA
    /// reference uses RFC 6979 and a low s, as `PrivateKey.sign` of `keys`).
    @Test
    @DisplayName("a Channel ownership proof signs the Keccak-256 hash of the Channel ID")
    @Tag("testId:consensusnode.clpr.transactions/a-channel-ownership-proof-signs-the-keccak-256-hash-of-the-channel-id")
    @Tag("hip:1535")
    void aChannelOwnershipProofSignsTheKeccak256HashOfTheChannelID() {
        // when
        final ClprChannelOwnershipProof ecdsa = ClprChannelOwnershipProof.create(CHANNEL_ID(), ECDSA_KEY());
        final ClprChannelOwnershipProof ed25519 = ClprChannelOwnershipProof.create(CHANNEL_ID(), ED25519_KEY());

        // then
        assertEquals(CHANNEL_ID(), ecdsa.channelId());
        assertEquals(ECDSA_KEY().createPublicKey(), ecdsa.publicKey());
        assertEquals(hex("cd52448f226a599c8d61d568a6186d50bb263a2559e08c1c8389233e632d2a58" + "39232b91d7b8974b862ff5540bae836c08792936ebd708682413e0c8e2884086"), ecdsa.signature());
        assertEquals(ED25519_KEY().createPublicKey(), ed25519.publicKey());
        assertEquals(hex("8ef8546b7a464125e9bda37b52203c8a8f1c59c51deae85c6178632b40501412" + "26d6018ed52fcf8afd2a4c141a04109220eb8c7cd9d923a06a2fff23155cab0f"), ed25519.signature());
    }

    /// `consensusnode.clpr.transactions/a-connector-ownership-proof-derives-the-connector-id-and-signs-it-with-the-service-address`
    ///
    /// Source: the ownership test vectors of the SDK proposal for HIP-1535 (connectorId and connectorSignature of both
    /// vectors).
    @Test
    @DisplayName("a Connector ownership proof derives the Connector ID and signs it with the service address")
    @Tag("testId:consensusnode.clpr.transactions/a-connector-ownership-proof-derives-the-connector-id-and-signs-it-with-the-service-address")
    @Tag("hip:1535")
    void aConnectorOwnershipProofDerivesTheConnectorIDAndSignsItWithTheServiceAddress() {
        // when
        final ClprConnectorOwnershipProof ecdsa = ClprConnectorOwnershipProof.create(CHANNEL_ID(), SALT(), ECDSA_KEY());
        final ClprConnectorOwnershipProof ed25519 = ClprConnectorOwnershipProof.create(CHANNEL_ID(), SALT(), ED25519_KEY());

        // then
        assertEquals(CHANNEL_ID(), ecdsa.channelId());
        assertEquals(SALT(), ecdsa.salt());
        assertEquals(hex("532a8331032028874f4d7fa1953636f8b076876c493606f93a86d8880058b4a6"), ecdsa.connectorId());
        assertEquals(hex("4b133aadf1d8a8c8bc6509842e245e01f71639e6e09fa2ed8ddf315faa38926f" + "29e87b0cf850345f9cf74e82de367c7e31c96dfde6cb4603afb42750a551197d"), ecdsa.signature());
        assertEquals(hex("488d7b7fdcc061f1de9fe98666c1337f0fc90275889f2450d59e1e6e837e74b1"), ed25519.connectorId());
        assertEquals(hex("7374599d239cc8300e8f8f99057656a0c4b3e42f7fbaa99ab8cd3907da4d9fbe" + "1ef28f12b7af9cd3f126fa513e3d6661b24fe790c1bd8a80275e8eaf69f1c70f"), ed25519.signature());
    }

    /// `consensusnode.clpr.transactions/a-channel-id-that-is-not-32-bytes-long-is-rejected`
    ///
    /// Source: the SDK proposal for HIP-1535 (the helpers reject invalid ID lengths locally); `@@minSize(32) @@maxSize(32)`
    /// of this spec.
    @Test
    @DisplayName("a Channel ID that is not 32 bytes long is rejected")
    @Tag("testId:consensusnode.clpr.transactions/a-channel-id-that-is-not-32-bytes-long-is-rejected")
    @Tag("hip:1535")
    void aChannelIDThatIsNot32BytesLongIsRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> ClprRegisterChannelTransaction.computeOwnershipCommitment(SHORT_CHANNEL_ID(), ECDSA_KEY().createPublicKey()));
        assertThrows(IllegalArgumentException.class, () -> ClprChannelOwnershipProof.create(SHORT_CHANNEL_ID(), ECDSA_KEY()));
        assertThrows(IllegalArgumentException.class, () -> new ClprCloseChannelTransaction(SHORT_CHANNEL_ID(), null));
        assertThrows(IllegalArgumentException.class, () -> new ClprSubmitBundleTransaction(SHORT_CHANNEL_ID(), utf8("bundle")));
    }

    /// `consensusnode.clpr.transactions/a-complete-channel-transaction-keeps-its-proof-verifier-and-proofs`
    ///
    /// Source: the SDK proposal for HIP-1535 (the transactions of the Channel lifecycle and their fields); HAPI
    /// clpr_complete_channel.proto.
    @Test
    @DisplayName("a complete Channel transaction keeps its proof, verifier and proofs")
    @Tag("testId:consensusnode.clpr.transactions/a-complete-channel-transaction-keeps-its-proof-verifier-and-proofs")
    @Tag("hip:1535")
    void aCompleteChannelTransactionKeepsItsProofVerifierAndProofs() {
        // given
        final ClprChannelOwnershipProof proof = defaultClprChannelOwnershipProof();
        final ContractId verifier = ContractId.fromString("0.0.366");

        // when
        final ClprCompleteChannelTransaction transaction = new ClprCompleteChannelTransaction(proof, verifier, utf8("config proof"), utf8("manifest proof"));

        // then
        assertEquals(proof, transaction.ownershipProof());
        assertEquals(verifier, transaction.verifierContract());
        assertEquals(utf8("config proof"), transaction.configProof());
        assertEquals(utf8("manifest proof"), transaction.endpointManifestProof());
    }

    /// `consensusnode.clpr.transactions/a-close-channel-transaction-needs-the-commitment-only-for-an-incomplete-registration`
    ///
    /// Source: the SDK proposal for HIP-1535 (ClprCloseChannelTransaction: the ownership commitment is only needed for a
    /// registration that was never completed); `@@nullable` of this spec.
    @Test
    @DisplayName("a close Channel transaction needs the commitment only for an incomplete registration")
    @Tag("testId:consensusnode.clpr.transactions/a-close-channel-transaction-needs-the-commitment-only-for-an-incomplete-registration")
    @Tag("hip:1535")
    void aCloseChannelTransactionNeedsTheCommitmentOnlyForAnIncompleteRegistration() {
        // given
        final byte[] commitment = ClprRegisterChannelTransaction.computeOwnershipCommitment(CHANNEL_ID(), ECDSA_KEY().createPublicKey());

        // when
        final ClprCloseChannelTransaction close = new ClprCloseChannelTransaction(CHANNEL_ID(), null);
        final ClprCloseChannelTransaction sweep = new ClprCloseChannelTransaction(CHANNEL_ID(), commitment);

        // then
        assertEquals(CHANNEL_ID(), close.channelId());
        assertNull(close.ownershipCommitment());
        assertEquals(commitment, sweep.ownershipCommitment());
    }

    /// `consensusnode.clpr.transactions/the-connector-transactions-keep-their-attributes`
    ///
    /// Source: the SDK proposal for HIP-1535 (the transactions of the Connector lifecycle and their fields); HAPI
    /// clpr_complete_connector.proto and clpr_deregister_connector.proto.
    @Test
    @DisplayName("the Connector transactions keep their attributes")
    @Tag("testId:consensusnode.clpr.transactions/the-connector-transactions-keep-their-attributes")
    @Tag("hip:1535")
    void theConnectorTransactionsKeepTheirAttributes() {
        // given
        final ClprConnectorOwnershipProof proof = defaultClprConnectorOwnershipProof();
        final ContractId contract = ContractId.fromString("0.0.5007");
        final Authority adminAuthority = defaultAuthority();
        final NativeToken<?, ?> stake = defaultHbar();
        final AccountId recipient = defaultAccountId();

        // when
        final ClprCompleteConnectorTransaction complete = new ClprCompleteConnectorTransaction(proof, contract, adminAuthority, stake);
        final ClprDeregisterConnectorTransaction deregister = new ClprDeregisterConnectorTransaction(CHANNEL_ID(), proof.connectorId(), recipient);

        // then
        assertEquals(proof, complete.ownershipProof());
        assertEquals(contract, complete.connectorContract());
        assertEquals(adminAuthority, complete.adminAuthority());
        assertEquals(stake, complete.lockedStake());
        assertEquals(CHANNEL_ID(), deregister.channelId());
        assertEquals(proof.connectorId(), deregister.connectorId());
        assertEquals(recipient, deregister.stakeRecipient());
    }

    /// `consensusnode.clpr.transactions/an-update-ledger-configuration-transaction-keeps-the-throttles`
    ///
    /// Source: the SDK proposal for HIP-1535 (ClprUpdateLedgerConfigurationTransaction replaces the entire throttles object).
    @Test
    @DisplayName("an update ledger configuration transaction keeps the throttles")
    @Tag("testId:consensusnode.clpr.transactions/an-update-ledger-configuration-transaction-keeps-the-throttles")
    @Tag("hip:1535")
    void anUpdateLedgerConfigurationTransactionKeepsTheThrottles() {
        // given
        final ClprThrottles throttles = defaultClprThrottles();

        // when
        final ClprUpdateLedgerConfigurationTransaction transaction = new ClprUpdateLedgerConfigurationTransaction(throttles);

        // then
        assertEquals(throttles, transaction.throttles());
    }

    /// `consensusnode.clpr.transactions/every-clpr-transaction-survives-tobytes-and-frombytes`
    ///
    /// Source: this spec (a packed transaction survives toBytes and fromBytes, as every transaction of the SDK); the SDK
    /// proposal for HIP-1535 (Test Plan 24: a decoded completion transaction keeps its proof without a private key when it is
    /// re-serialized unchanged).
    @Test
    @DisplayName("every CLPR transaction survives toBytes and fromBytes")
    @Tag("testId:consensusnode.clpr.transactions/every-clpr-transaction-survives-tobytes-and-frombytes")
    @Tag("hip:1535")
    void everyCLPRTransactionSurvivesToBytesAndFromBytes() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006"));
        final ClprChannelOwnershipProof channelProof = ClprChannelOwnershipProof.create(CHANNEL_ID(), ED25519_KEY());
        final ClprConnectorOwnershipProof connectorProof = ClprConnectorOwnershipProof.create(CHANNEL_ID(), SALT(), ECDSA_KEY());
        final NativeToken<?, ?> stake = defaultHbar();
        final List<Transaction<?, ?>> transactions = List.of(new ClprRegisterChannelTransaction(ClprRegisterChannelTransaction.computeOwnershipCommitment(CHANNEL_ID(), ED25519_KEY().createPublicKey())), new ClprCompleteChannelTransaction(channelProof, ContractId.fromString("0.0.366"), utf8("config proof"), utf8("manifest proof")), new ClprCloseChannelTransaction(CHANNEL_ID(), null), new ClprSubmitBundleTransaction(CHANNEL_ID(), utf8("bundle")), new ClprRegisterConnectorTransaction(connectorProof.connectorId()), new ClprCompleteConnectorTransaction(connectorProof, ContractId.fromString("0.0.5007"), defaultAuthority(), stake), new ClprDeregisterConnectorTransaction(CHANNEL_ID(), connectorProof.connectorId(), defaultAccountId()), new ClprUpdateLedgerConfigurationTransaction(defaultClprThrottles()));
        for (final Transaction<?, ?> transaction : transactions) {
            // given
            final PackedTransaction<?, ?> packed = transaction.pack(payer, nodes);

            // when
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

            // then
            assertEquals(packed.toBytes(), restored.toBytes());
            assertEquals(packed.signableBodies(), restored.signableBodies());
        }
    }

    private static ClprChannelOwnershipProof defaultClprChannelOwnershipProof() {
        return ClprChannelOwnershipProof.create(new byte[] {(byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31}, defaultPrivateKey());
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static ClprConnectorOwnershipProof defaultClprConnectorOwnershipProof() {
        return ClprConnectorOwnershipProof.create(new byte[] {(byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31}, new byte[] {(byte) 32, (byte) 33, (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) 62, (byte) 63}, defaultPrivateKey());
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }

    private static Hbar defaultHbar() {
        return new Hbar(1L, HbarUnit.HBAR);
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static ClprThrottles defaultClprThrottles() {
        return new ClprThrottles(10L, 6144L, 1000000L, 100L, 71680L, 0L, 0L);
    }

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }
}
