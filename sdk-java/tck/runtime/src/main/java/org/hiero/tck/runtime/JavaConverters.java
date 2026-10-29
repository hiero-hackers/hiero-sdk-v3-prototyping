package org.hiero.tck.runtime;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityFactory;
import org.hiero.authority.AuthorityList;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.queries.receipts.UntypedReceipt;
import org.hiero.consensusnode.queries.tokens.TokenNftInfo;
import org.hiero.consensusnode.status.TransactionStatus;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.TransactionId;
import org.hiero.nativeToken.ExchangeRate;
import org.hiero.nativeToken.NativeToken;
import org.hiero.tck.contract.Converters;
import org.hiero.token.TokenSupplyType;
import org.hiero.token.TokenType;
import org.jspecify.annotations.Nullable;

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
                // the protobuf Key of a key list or threshold key (Authority has no byte form in the API)
                return KeyProto.decode(bytes);
            }
        }
    }

    @Override
    public byte[] hex(final Object json) {
        final String text = string(json);
        return HexFormat.of().parseHex(text.startsWith("0x") ? text.substring(2) : text);
    }

    @Override
    public byte[] utf8(final Object json) {
        return string(json).getBytes(StandardCharsets.UTF_8);
    }

    /** A JSON list of keys that must all sign; an empty list is no key ({@code null}). */
    @Override
    public Authority keyList(final Object json) {
        if (!(json instanceof List<?> list)) {
            throw new IllegalArgumentException("not a list of keys: " + json);
        }
        if (list.isEmpty()) {
            return null;
        }
        return AuthorityFactory.of(list.stream().map(this::key).toArray(Authority[]::new));
    }

    /** {@code ft} / {@code nft}, or the name of the constant. */
    @Override
    public TokenType tokenType(final Object json) {
        return switch (string(json).toLowerCase(Locale.ROOT)) {
            case "ft", "fungible_common" -> TokenType.FUNGIBLE_COMMON;
            case "nft", "non_fungible_unique" -> TokenType.NON_FUNGIBLE_UNIQUE;
            default -> throw new IllegalArgumentException("not a token type: " + json);
        };
    }

    @Override
    public TokenSupplyType tokenSupplyType(final Object json) {
        return TokenSupplyType.valueOf(string(json).toUpperCase(Locale.ROOT));
    }

    @Override
    public Address nftIdToken(final Object json) {
        return address(nftIdPart(json, 0));
    }

    @Override
    public Long nftIdSerial(final Object json) {
        return int64(nftIdPart(json, 1));
    }

    @Override
    public IpAddress ipAddress(final Object json) {
        return IpAddress.fromBytes(hex(json));
    }

    private static String nftIdPart(final Object json, final int index) {
        final String[] parts = String.valueOf(json).split("/", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("not an NFT ID (tokenId/serial): " + json);
        }
        return parts[index];
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
        return HexFormat.of().formatHex(KeyProto.encode(value));
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

    @Override
    public Object numberJson(final Integer value) {
        return value;
    }

    @Override
    public Object utf8Json(final byte[] value) {
        return new String(value, StandardCharsets.UTF_8);
    }

    /** The keys of a list that must all sign, or the single key. */
    @Override
    public Object keyListJson(final Authority value) {
        final List<Object> keys = new ArrayList<>();
        if (value instanceof AuthorityList(final List<Authority> children, final int threshold)
                && threshold == children.size()) {
            children.forEach(child -> keys.add(keyJson(child)));
        } else {
            keys.add(keyJson(value));
        }
        return keys;
    }

    @Override
    public Object publicKeyJson(final PublicKey value) {
        return HexFormat.of().formatHex(value.toBytes(KeyFormat.SPKI_WITH_DER));
    }

    @Override
    public Object tokenTypeJson(final TokenType value) {
        return value.name();
    }

    @Override
    public Object tokenSupplyTypeJson(final TokenSupplyType value) {
        return value.name();
    }

    @Override
    public Object nftIdJson(final TokenNftInfo value) {
        return value.tokenId() + "/" + value.serial();
    }

    @Override
    public Object transactionIdJson(final TransactionId value) {
        return value.toString();
    }

    @Override
    public TransactionId transactionId(final Object json) {
        return TransactionId.fromString(string(json));
    }

    @Override
    public Object contractIdJson(final ContractId value) {
        return value.toString();
    }

    /**
     * An exchange rate as the TCK writes it: {@code hbars} and {@code cents}. The API keeps the price of one hbar in
     * US cents, not the pair of the protocol, so {@code hbars} is 1.
     */
    @Override
    public Object exchangeRateJson(final ExchangeRate value) {
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("hbars", 1);
        json.put("cents", value.exchangeRateInUsdCents());
        json.put("expirationTime", value.expirationTime().toInstant().toString());
        return json;
    }

    /** An untyped receipt with the attributes of the result of {@code getTransactionReceipt}; absent ones omitted. */
    @Override
    public Object receiptJson(final UntypedReceipt value) {
        final Map<String, Object> json = new LinkedHashMap<>();
        put(json, "status", value.status(), this::statusJson);
        put(json, "accountId", value.accountId(), this::accountIdJson);
        put(json, "fileId", value.fileId(), this::addressJson);
        put(json, "contractId", value.contractId(), this::contractIdJson);
        put(json, "topicId", value.topicId(), this::addressJson);
        put(json, "tokenId", value.tokenId(), this::addressJson);
        put(json, "scheduleId", value.scheduleId(), this::addressJson);
        put(json, "exchangeRate", value.exchangeRate(), this::exchangeRateJson);
        put(json, "topicSequenceNumber", value.topicSequenceNumber(), this::int64Json);
        put(json, "topicRunningHash", value.topicRunningHash(), this::hexJson);
        put(json, "totalSupply", value.totalSupply(), this::int64Json);
        put(json, "scheduledTransactionId", value.scheduledTransactionId(), this::transactionIdJson);
        json.put("serials", value.serials().stream().map(this::int64Json).toList());
        put(json, "nodeId", value.nodeId(), this::int64Json);
        return json;
    }

    private static <T> void put(final Map<String, Object> json, final String name, final @Nullable T value,
                                final Function<? super T, Object> converter) {
        if (value != null) {
            json.put(name, converter.apply(value));
        }
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
