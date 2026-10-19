package org.hiero.sdk.v3.metalang.tck;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.hiero.sdk.v3.metalang.model.QualifiedName;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;

/**
 * The fixed catalogue of conversions between the JSON conventions of the TCK and API values. Every converter is
 * implemented once per language (in the runtime of the TCK server); the bindings only name them. A converter is
 * inbound (JSON → API value), outbound (API value → JSON) or both, and it accepts certain meta-language types. The
 * first of them is its canonical type: the type of the value in the contract between the generated TCK server and the
 * runtime.
 */
public enum Converter {

    STRING("string", true, true, "A string.", "string"),
    BOOL("bool", true, true, "A boolean.", "bool"),
    INT32("int32", true, true, "A 32-bit integer (JSON number or decimal string; a decimal string in results).",
            "int32", "uint16"),
    INT64("int64", true, true, "A 64-bit integer (JSON number or decimal string; a decimal string in results).",
            "int64"),
    TINYBAR("tinybar", true, true,
            "An amount of the native token in its smallest unit, as decimal string (`\"100\"` tinybar).",
            "nativeToken.NativeToken"),
    SECONDS("seconds", true, true, "A duration in seconds, as decimal string.", "seconds", "duration"),
    TIMESTAMP("timestamp", true, true, "A point in time as seconds since the epoch, as decimal string.",
            "zonedDateTime"),
    ACCOUNT_ID("accountId", true, true, "An account ID (`\"0.0.1234\"`).", "ledger.AccountId"),
    EVM_ACCOUNT_ID("evmAccountId", true, false, "An account ID given as 20-byte EVM address in hex.",
            "ledger.AccountId"),
    EVM_ADDRESS("evmAddress", false, true, "An EVM address as hex string.", "ledger.EvmAddress"),
    ADDRESS("address", true, true, "An entity address (`\"0.0.1234\"`), e.g. a token ID.", "ledger.Address"),
    KEY("key", true, true, "A key as hex string: a DER-encoded private or public key, or the serialized protobuf "
            + "`Key` of a key list or threshold key.", "authority.Authority"),
    HEX("hex", true, true, "Bytes as hex string.", "bytes"),
    STATUS("status", false, true, "The name of a transaction status (`\"SUCCESS\"`).",
            "consensusnode.transactions.TransactionStatus"),
    NUMBER("number", false, true, "A 32-bit integer as JSON number (where the TCK expects a number, not a string).",
            "int32", "uint16"),
    UTF8("utf8", true, true, "Bytes as UTF-8 text.", "bytes"),
    KEY_LIST("keyList", true, true, "A JSON list of keys (each as for `key`) that must all sign; an empty list is "
            + "no key. In results the keys of a list that must all sign, or the single key.", "authority.Authority"),
    PUBLIC_KEY("publicKey", false, true, "A public key as DER-encoded hex string.", "keys.PublicKey"),
    TOKEN_TYPE("tokenType", true, true, "A token type: `ft` or `nft` (or the name of the constant); the name of the "
            + "constant in results (`FUNGIBLE_COMMON`).", "token.TokenType"),
    TOKEN_SUPPLY_TYPE("tokenSupplyType", true, true, "A token supply type: `infinite` or `finite` (any case); the "
            + "name of the constant in results (`INFINITE`).", "token.TokenSupplyType"),
    NFT_ID_TOKEN("nftIdToken", true, false, "The token ID of an NFT ID (`\"0.0.1234/1\"`).", "ledger.Address"),
    NFT_ID_SERIAL("nftIdSerial", true, false, "The serial number of an NFT ID (`\"0.0.1234/1\"`).", "int64"),
    NFT_ID("nftId", false, true, "The NFT ID (`\"0.0.1234/1\"`) of the information about an NFT.",
            "consensusnode.queries.tokens.TokenNftInfo"),
    TRANSACTION_ID("transactionId", true, true, "A transaction ID (`\"0.0.1234@1700000000.000000000\"`).",
            "ledger.TransactionId"),
    CONTRACT_ID("contractId", false, true, "A contract ID (`\"0.0.1234\"`).", "ledger.ContractId"),
    EXCHANGE_RATE("exchangeRate", false, true, "An exchange rate as JSON object: `hbars` and `cents` as JSON numbers "
            + "(one hbar and its price in US cents), `expirationTime` as ISO-8601 string.", "nativeToken.ExchangeRate"),
    RECEIPT("receipt", false, true, "An untyped receipt as JSON object with the attributes of the result of "
            + "`getTransactionReceipt` (without duplicates and children).",
            "consensusnode.queries.receipts.UntypedReceipt"),
    IP_ADDRESS("ipAddress", true, false, "An IP address as hex string of its bytes (`\"7f000001\"`).",
            "ledger.IpAddress");

    private final String id;
    private final boolean inbound;
    private final boolean outbound;
    private final String description;
    private final List<String> types;

    Converter(final String id, final boolean inbound, final boolean outbound, final String description,
              final String... types) {
        this.id = id;
        this.inbound = inbound;
        this.outbound = outbound;
        this.description = description;
        this.types = List.of(types);
    }

    /** Returns the name of the converter in the bindings and in the runtimes. */
    public String id() {
        return id;
    }

    /** Whether the converter turns JSON into an API value. */
    public boolean inbound() {
        return inbound;
    }

    /** Whether the converter turns an API value into JSON. */
    public boolean outbound() {
        return outbound;
    }

    /** Returns what the converter converts, for the documentation of the contract. */
    public String description() {
        return description;
    }

    /**
     * Whether the converter produces (or accepts) values of a type.
     *
     * @param type the meta-language type
     * @return {@code true} if the types match
     */
    public boolean accepts(final Type type) {
        return switch (type) {
            case Type.BasicType basic -> types.contains(basic.builtin().name());
            case Type.DeclaredType declared -> types.contains(declared.name().toString());
            default -> false;
        };
    }

    /**
     * Returns the canonical type of the converter, the first of its types: the type of the value it produces (inbound)
     * or accepts (outbound) in the contract with the runtime. A declared type has no type arguments here.
     *
     * @return the type
     */
    public Type type() {
        final String name = types.getFirst();
        final Optional<BuiltinType> builtin = BuiltinType.lookup(name);
        if (builtin.isPresent()) {
            return new Type.BasicType(builtin.get(), List.of());
        }
        final int dot = name.lastIndexOf('.');
        return new Type.DeclaredType(new QualifiedName(name.substring(0, dot), name.substring(dot + 1)), List.of());
    }

    /** Returns the meta-language types the converter accepts, for messages. */
    public String typeNames() {
        return String.join(" or ", types.stream().sorted().toList());
    }

    /**
     * Returns the converter with a name.
     *
     * @param id the name
     * @return the converter
     */
    public static Optional<Converter> of(final String id) {
        return Arrays.stream(values()).filter(c -> c.id.equals(id)).findFirst();
    }
}
