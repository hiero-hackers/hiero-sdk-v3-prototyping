package org.hiero.tck.runtime;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityFactory;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.transactions.TransactionStatus;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.EvmAddress;
import org.hiero.nativeToken.NativeToken;
import org.hiero.tck.contract.Converters;

/**
 * The converter catalogue for Java: inbound converters turn a JSON value of the TCK into an API value
 * ({@code tinybar("100")}), outbound converters ({@code ...Json}) turn an API value into the JSON of the TCK. The
 * interface is generated from the catalogue, so a converter that is added there must be implemented here.
 */
final class JavaConverters implements Converters {

    // --- inbound -------------------------------------------------------------------------------------

    @Override
    public String string(final Object json) {
        return String.valueOf(json);
    }

    @Override
    public Boolean bool(final Object json) {
        return json instanceof Boolean value ? value : Boolean.valueOf(String.valueOf(json));
    }

    @Override
    public Integer int32(final Object json) {
        return number(json).intValueExact();
    }

    @Override
    public Long int64(final Object json) {
        return number(json).longValueExact();
    }

    @Override
    public NativeToken<?, ?> tinybar(final Object json) {
        return new Hbar(int64(json), HbarUnit.TINYBAR);
    }

    @Override
    public Duration seconds(final Object json) {
        return Duration.ofSeconds(int64(json));
    }

    @Override
    public ZonedDateTime timestamp(final Object json) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(int64(json)), ZoneOffset.UTC);
    }

    @Override
    public AccountId accountId(final Object json) {
        return AccountId.fromString(string(json));
    }

    @Override
    public AccountId evmAccountId(final Object json) {
        return AccountId.fromEvmAddress(0, 0, EvmAddress.fromBytes(hex(json)));
    }

    @Override
    public Address address(final Object json) {
        return Address.fromString(string(json));
    }

    /**
     * A key: a DER-encoded private key (its public key is used), a DER-encoded public key, or the protobuf
     * {@code Key} of a key list or threshold key.
     */
    @Override
    public Authority key(final Object json) {
        final byte[] bytes = hex(json);
        try {
            return AuthorityFactory.of(KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, bytes).createPublicKey());
        } catch (final IllegalArgumentException notPrivate) {
            try {
                return AuthorityFactory.of(KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_DER, bytes));
            } catch (final IllegalArgumentException notPublic) {
                throw RpcError.gap("a key list or threshold key cannot be read: Authority has no fromBytes");
            }
        }
    }

    @Override
    public byte[] hex(final Object json) {
        final String text = string(json);
        return HexFormat.of().parseHex(text.startsWith("0x") ? text.substring(2) : text);
    }

    // --- outbound ------------------------------------------------------------------------------------

    @Override
    public Object stringJson(final String value) {
        return value;
    }

    @Override
    public Object boolJson(final Boolean value) {
        return value;
    }

    @Override
    public Object int32Json(final Integer value) {
        return String.valueOf(value);
    }

    @Override
    public Object int64Json(final Long value) {
        return String.valueOf(value);
    }

    @Override
    public Object tinybarJson(final NativeToken<?, ?> value) {
        return String.valueOf(value.toBaseUnits());
    }

    @Override
    public Object secondsJson(final Duration value) {
        return String.valueOf(value.toSeconds());
    }

    @Override
    public Object timestampJson(final ZonedDateTime value) {
        return String.valueOf(value.toEpochSecond());
    }

    @Override
    public Object accountIdJson(final AccountId value) {
        return value.toString();
    }

    @Override
    public Object evmAddressJson(final EvmAddress value) {
        return HexFormat.of().formatHex(value.bytes());
    }

    @Override
    public Object addressJson(final Address value) {
        return value.toString();
    }

    @Override
    public Object keyJson(final Authority value) {
        if (value instanceof PublicKeyAuthority(final PublicKey publicKey)) {
            return HexFormat.of().formatHex(publicKey.toBytes(KeyFormat.SPKI_WITH_DER));
        }
        throw RpcError.gap("a key list or threshold key cannot be written: Authority has no toBytes");
    }

    @Override
    public Object hexJson(final byte[] value) {
        return HexFormat.of().formatHex(value);
    }

    /** The name of a status: the constant of the enum, otherwise the numeric code. */
    @Override
    public Object statusJson(final TransactionStatus value) {
        return value instanceof Enum<?> constant ? constant.name() : String.valueOf(value.code());
    }

    // --- used by the runtime itself ------------------------------------------------------------------

    /** A DER-encoded private key (for {@code setup} and signers). */
    PrivateKey privateKey(final Object json) {
        return KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, hex(json));
    }

    private static BigDecimal number(final Object json) {
        return json instanceof BigDecimal number ? number : new BigDecimal(String.valueOf(json));
    }
}
