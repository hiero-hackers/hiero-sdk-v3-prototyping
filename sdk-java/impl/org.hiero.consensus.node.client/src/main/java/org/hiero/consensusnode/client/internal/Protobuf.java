package org.hiero.consensusnode.client.internal;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.ContractID;
import com.hederahashgraph.api.proto.java.Duration;
import com.hederahashgraph.api.proto.java.ExchangeRateSet;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.ThresholdKey;
import com.hederahashgraph.api.proto.java.Timestamp;
import com.hederahashgraph.api.proto.java.TransactionID;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityList;
import org.hiero.authority.ContractAuthority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;

/// Maps the types of the API to the HAPI messages and back.
public final class Protobuf {

    private Protobuf() {
    }

    /// Maps an account id.
    ///
    /// @param accountId the account id
    /// @return the message
    public static AccountID toProto(final AccountId accountId) {
        final AccountID.Builder builder = AccountID.newBuilder().setShardNum(accountId.shard())
                .setRealmNum(accountId.realm());
        final Long num = accountId.num();
        if (num != null) {
            return builder.setAccountNum(num).build();
        }
        final EvmAddress evmAddress = accountId.evmAddress();
        final byte[] alias = evmAddress != null ? evmAddress.bytes() : accountId.alias();
        if (alias == null) {
            throw new IllegalArgumentException("The account id has neither a number nor an alias");
        }
        return builder.setAlias(ByteString.copyFrom(alias)).build();
    }

    /// Maps an account id message.
    ///
    /// @param accountId the message
    /// @return the account id
    public static AccountId fromProto(final AccountID accountId) {
        return new AccountId(accountId.getShardNum(), accountId.getRealmNum(), "", accountId.getAccountNum(), null,
                null);
    }

    /// Maps a transaction id.
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

    /// Maps a transaction id message. The concrete `TransactionId` lives in `org.hiero.base`, which does not export
    /// it, so the id is built through its public text form.
    ///
    /// @param transactionId the message
    /// @return the transaction id
    public static TransactionId fromProto(final TransactionID transactionId) {
        final Timestamp start = transactionId.getTransactionValidStart();
        final int nonce = transactionId.getNonce();
        return TransactionId.fromString(fromProto(transactionId.getAccountID()) + "@" + start.getSeconds() + "."
                + String.format("%09d", start.getNanos()) + (nonce == 0 ? "" : "/" + nonce));
    }

    /// Maps a point in time.
    ///
    /// @param time the time
    /// @return the message
    public static Timestamp toProto(final ZonedDateTime time) {
        final Instant instant = time.toInstant();
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }

    /// Maps a duration.
    ///
    /// @param duration the duration, in whole seconds
    /// @return the message
    public static Duration toProto(final java.time.Duration duration) {
        return Duration.newBuilder().setSeconds(duration.toSeconds()).build();
    }

    /// Maps an authority to the key message of HAPI.
    ///
    /// @param authority the authority
    /// @return the key message
    public static Key toProto(final Authority authority) {
        return switch (authority) {
            case PublicKeyAuthority(PublicKey publicKey) -> toProto(publicKey);
            case AuthorityList(var children, var threshold) -> {
                final KeyList.Builder keys = KeyList.newBuilder();
                children.forEach(child -> keys.addKeys(toProto(child)));
                yield threshold == children.size() ? Key.newBuilder().setKeyList(keys).build()
                        : Key.newBuilder().setThresholdKey(ThresholdKey.newBuilder().setThreshold(threshold)
                        .setKeys(keys)).build();
            }
            case ContractAuthority(ContractId contractId, boolean delegatable) -> delegatable
                    ? Key.newBuilder().setDelegatableContractId(toProto(contractId)).build()
                    : Key.newBuilder().setContractID(toProto(contractId)).build();
        };
    }

    /// Maps a contract id.
    ///
    /// @param contractId the contract id
    /// @return the message
    public static ContractID toProto(final ContractId contractId) {
        final ContractID.Builder builder = ContractID.newBuilder().setShardNum(contractId.shard())
                .setRealmNum(contractId.realm());
        final Long num = contractId.num();
        final EvmAddress evmAddress = contractId.evmAddress();
        if (num != null) {
            return builder.setContractNum(num).build();
        }
        if (evmAddress == null) {
            throw new IllegalArgumentException("The contract id has neither a number nor an EVM address");
        }
        return builder.setEvmAddress(ByteString.copyFrom(evmAddress.bytes())).build();
    }

    /// Maps a public key to the key message of HAPI.
    ///
    /// @param publicKey the public key
    /// @return the key message
    public static Key toProto(final PublicKey publicKey) {
        final ByteString raw = ByteString.copyFrom(publicKey.toRawBytes());
        return publicKey.algorithm() == KeyAlgorithm.ECDSA ? Key.newBuilder().setECDSASecp256K1(raw).build()
                : Key.newBuilder().setEd25519(raw).build();
    }

    /// Maps a signature to a signature pair with the full public key as prefix.
    ///
    /// @param publicKey the key that signed
    /// @param signature the signature
    /// @return the signature pair
    public static SignaturePair toSignaturePair(final PublicKey publicKey, final byte[] signature) {
        final SignaturePair.Builder builder = SignaturePair.newBuilder()
                .setPubKeyPrefix(ByteString.copyFrom(publicKey.toRawBytes()));
        return publicKey.algorithm() == KeyAlgorithm.ECDSA
                ? builder.setECDSASecp256K1(ByteString.copyFrom(signature)).build()
                : builder.setEd25519(ByteString.copyFrom(signature)).build();
    }

    /// Reads the public key of a signature pair. Only pairs with the full public key as prefix can be read - which is
    /// what all Hiero SDKs write.
    ///
    /// @param pair the signature pair
    /// @return the public key
    /// @throws IllegalArgumentException if the pair holds no Ed25519 or ECDSA signature or only a key prefix
    public static PublicKey publicKey(final SignaturePair pair) {
        final byte[] prefix = pair.getPubKeyPrefix().toByteArray();
        return switch (pair.getSignatureCase()) {
            case ED25519 -> KeysFactory.createPublicKey(KeyAlgorithm.ED25519, prefix);
            case ECDSA_SECP256K1 -> KeysFactory.createPublicKey(KeyAlgorithm.ECDSA, prefix);
            default -> throw new IllegalArgumentException("Unsupported signature type " + pair.getSignatureCase());
        };
    }

    /// Reads the signature of a signature pair.
    ///
    /// @param pair the signature pair
    /// @return the signature bytes
    public static byte[] signature(final SignaturePair pair) {
        return switch (pair.getSignatureCase()) {
            case ED25519 -> pair.getEd25519().toByteArray();
            case ECDSA_SECP256K1 -> pair.getECDSASecp256K1().toByteArray();
            default -> throw new IllegalArgumentException("Unsupported signature type " + pair.getSignatureCase());
        };
    }

    /// The current exchange rate of a receipt.
    ///
    /// @param rates the exchange rates of a receipt
    /// @return the current rate
    public static ExchangeRate currentRate(final ExchangeRateSet rates) {
        return toExchangeRate(rates.getCurrentRate());
    }

    /// The next exchange rate of a receipt.
    ///
    /// @param rates the exchange rates of a receipt
    /// @return the next rate
    public static ExchangeRate nextRate(final ExchangeRateSet rates) {
        return toExchangeRate(rates.getNextRate());
    }

    private static ExchangeRate toExchangeRate(final com.hederahashgraph.api.proto.java.ExchangeRate rate) {
        final int hbarEquiv = rate.getHbarEquiv();
        return new DefaultExchangeRate(ZonedDateTime.ofInstant(Instant.ofEpochSecond(
                rate.getExpirationTime().getSeconds()), ZoneOffset.UTC),
                hbarEquiv == 0 ? 0.0 : (double) rate.getCentEquiv() / hbarEquiv);
    }
}
