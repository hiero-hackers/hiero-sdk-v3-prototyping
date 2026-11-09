package org.hiero.evm;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `evm`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("evm")
class EvmSpecTest {

    /// EIP-55, section "Test Cases": addresses in their EIP-55 form (all caps, all lower and mixed case).
    private static List<String> EIP55_ADDRESSES() {
        return List.of("0x52908400098527886E0F7030069857D2E4169EE7", "0x8617E340B3D01FA5F11F306F4090FD50E238070D", "0xde709f2102306220921060314715629080e2fb77", "0x27b1fdb04752bbc536007a920d24acb045561c26", "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359", "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB", "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb");
    }

    /// Names that are no supported ABI type: widths that are no multiple of 8 or out of range, the unsupported types,
    /// malformed arrays and tuples.
    private static List<String> NO_ABI_TYPES() {
        return List.of("", "uint7", "uint0", "uint264", "int12", "bytes0", "bytes33", "uint256[0]", "uint256[", "uint256]", "(uint256", "(uint256,)", "fixed128x18", "ufixed128x18", "function", "address payable", "uint 256", "Uint256", "struct");
    }

    /// Function signatures that parse rejects: incomplete or misplaced parentheses, names that are no identifiers, spaces
    /// other than the two around "returns", parameter names, "indexed", empty arguments, unsupported types, widths with a
    /// leading zero, array lengths that are 0, negative, beyond uint32 or have a leading zero, and anything after the end.
    private static List<String> MALFORMED_FUNCTION_SIGNATURES() {
        return List.of("", "f", "f(", "f)", "()", "(uint256)", "1f()", "f-g()", "f.g()", "Fü()", "f (uint256)", "f( uint256)", "f(uint256 )", "f(uint256, bool)", "f(,)", "f(uint256,)", "f(,uint256)", "f(uint256))", "f()x", "f();", "f() returns", "f() returns ", "f()returns (bool)", "f() returns(bool)", "f() returns  (bool)", "f()  returns (bool)", "f() returns bool", "f() RETURNS (bool)", "f() returns (bool) returns (bool)", "f(address to)", "f(uint256 indexed)", "f(uint7)", "f(byte)", "f(uint08)", "f(bytes01)", "f(uint256[01])", "f(uint256[0])", "f(uint256[-1])", "f(uint256[4294967296])", "f(uint256[99999999999])", "f(uint256[)", "f((uint256)", "f(uint256))", "f(fixed128x18)", "f(function)");
    }

    /// Event signatures that parse rejects: the cases of a function that apply to events, "indexed" misspelt, doubled,
    /// without its space or before the type, and "returns".
    private static List<String> MALFORMED_EVENT_SIGNATURES() {
        return List.of("", "E", "E(", "1E()", "E (uint256)", "E( uint256)", "E(uint256 indexed, bool)", "E(uint256 indexed,)", "E(uint256 indexed indexed)", "E(uint256  indexed)", "E(uint256indexed)", "E(uint256 Indexed)", "E(indexed uint256)", "E(address indexed from)", "E(address from)", "E(uint256) returns (bool)", "E() anonymous", "E(uint7 indexed)", "E()x");
    }

    /// `evm/an-evm-address-is-read-from-40-hex-digits-with-or-without-0x`
    ///
    /// Source: the Swift, Rust, Python and C++ SDKs.
    @Test
    @DisplayName("an EVM address is read from 40 hex digits with or without 0x")
    @Tag("testId:evm/an-evm-address-is-read-from-40-hex-digits-with-or-without-0x")
    void anEVMAddressIsReadFrom40HexDigitsWithOrWithout0x() {
        // given
        final byte[] bytes = hex("1234567890abcdef1234567890abcdef12345678");

        // when
        final EvmAddress withPrefix = EvmAddress.fromString("0x1234567890abcdef1234567890abcdef12345678");
        final EvmAddress withoutPrefix = EvmAddress.fromString("1234567890abcdef1234567890abcdef12345678");

        // then
        assertEquals(bytes, withPrefix.bytes());
        assertEquals(bytes, withoutPrefix.bytes());
        assertEquals(withPrefix, EvmAddress.fromBytes(bytes));
    }

    /// `evm/an-evm-address-of-the-wrong-length-or-with-other-characters-is-rejected`
    ///
    /// Source: the Swift, Rust, Python and C++ SDKs.
    @Test
    @DisplayName("an EVM address of the wrong length or with other characters is rejected")
    @Tag("testId:evm/an-evm-address-of-the-wrong-length-or-with-other-characters-is-rejected")
    void anEVMAddressOfTheWrongLengthOrWithOtherCharactersIsRejected() {
        for (final String text : List.of("", "0x1234", "0x0f0e0d0c0b0a09080706050403020100", "0xGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }
        for (final long size : List.of(19L, 21L)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromBytes(repeat(hex("00"), size)));
        }
    }

    /// `evm/an-evm-address-is-the-same-in-upper-and-lower-case`
    ///
    /// Source: the Swift, Java, Rust and Go SDKs (hex digits in either case denote the same address).
    @Test
    @DisplayName("an EVM address is the same in upper and lower case")
    @Tag("testId:evm/an-evm-address-is-the-same-in-upper-and-lower-case")
    void anEVMAddressIsTheSameInUpperAndLowerCase() {
        // when
        final EvmAddress upper = EvmAddress.fromString("0x1234567890ABCDEF1234567890ABCDEF12345678");
        final EvmAddress lower = EvmAddress.fromString("1234567890abcdef1234567890abcdef12345678");

        // then
        assertEquals(lower, upper);
    }

    /// `evm/an-evm-address-of-39-or-42-digits-with-a-misplaced-or-doubled-0x-or-of-22-bytes-is-rejected`
    ///
    /// Source: the C++ SDK (39 digits, 0x inside the digits, 22 bytes) and the Python SDK (42 digits).
    @Test
    @DisplayName("an EVM address of 39 or 42 digits, with a misplaced or doubled 0x, or of 22 bytes is rejected")
    @Tag("testId:evm/an-evm-address-of-39-or-42-digits-with-a-misplaced-or-doubled-0x-or-of-22-bytes-is-rejected")
    void anEVMAddressOf39Or42DigitsWithAMisplacedOrDoubled0xOrOf22BytesIsRejected() {
        for (final String text : List.of("303132333435363738396162636465666768696", "1234567890abcdef1234567890abcdef1234567890", "3031323334353637383961626364650x66676869", "0x0x1234567890abcdef1234567890abcdef12345678")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }

        // when / then
        assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromBytes(repeat(hex("00"), 22L)));
    }

    /// `evm/an-evm-address-is-written-in-its-eip-55-form`
    ///
    /// Source: EIP-55, section "Test Cases", written from the bytes of the address.
    @Test
    @DisplayName("an EVM address is written in its EIP-55 form")
    @Tag("testId:evm/an-evm-address-is-written-in-its-eip-55-form")
    void anEVMAddressIsWrittenInItsEIP55Form() {
        for (final String checksummed : EIP55_ADDRESSES()) {
            // when: built from the bytes, so the case of the text plays no part
            final EvmAddress evm = EvmAddress.fromBytes(hex(substring(checksummed, 2L, 42L)));

            // then
            assertEquals(checksummed, evm.toString());
            assertEquals(evm, EvmAddress.fromString(checksummed));
        }
    }

    /// `evm/an-evm-address-in-mixed-case-that-is-not-its-eip-55-form-is-rejected`
    ///
    /// Source: EIP-55 (mixed case carries a checksum); rejected by ethers.js, web3.js and viem. The second address is the
    /// one of the Go SDK's mirror node tests, which is not in EIP-55 form.
    @Test
    @DisplayName("an EVM address in mixed case that is not its EIP-55 form is rejected")
    @Tag("testId:evm/an-evm-address-in-mixed-case-that-is-not-its-eip-55-form-is-rejected")
    void anEVMAddressInMixedCaseThatIsNotItsEIP55FormIsRejected() {
        for (final String text : List.of("0x5aaeb6053F3E94C9b9A09f33669435E7Ef1BeAed", "7F2c5e0f4a3b9E8d1C6A5b4E3f2D1c0B9a8E7F6d")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> EvmAddress.fromString(text));
        }
    }

    /// `evm/the-call-of-baz-uint32-bool-with-69-and-true-is-the-one-of-the-abi-specification`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz).
    @Test
    @DisplayName("the call of baz(uint32,bool) with 69 and true is the one of the ABI specification")
    @Tag("testId:evm/the-call-of-baz-uint32-bool-with-69-and-true-is-the-one-of-the-abi-specification")
    void theCallOfBazUint32BoolWith69AndTrueIsTheOneOfTheABISpecification() {
        // given
        final AbiFunction baz = AbiFunction.parse("baz(uint32,bool) returns (bool)");

        // when
        final byte[] data = baz.encodeCall(List.of(new AbiUint(new BigInteger("69"), 32), new AbiBool(true)));

        // then
        assertEquals("baz(uint32,bool)", baz.signature());
        assertEquals(hex("cdcd77c0"), baz.selector());
        assertEquals(hex(("cdcd77c0" + "0000000000000000000000000000000000000000000000000000000000000045") + "0000000000000000000000000000000000000000000000000000000000000001"), data);
    }

    /// `evm/the-call-of-bar-bytes3-2-is-the-one-of-the-abi-specification`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (bar).
    @Test
    @DisplayName("the call of bar(bytes3[2]) is the one of the ABI specification")
    @Tag("testId:evm/the-call-of-bar-bytes3-2-is-the-one-of-the-abi-specification")
    void theCallOfBarBytes32IsTheOneOfTheABISpecification() {
        // given
        final AbiFunction bar = AbiFunction.parse("bar(bytes3[2])");
        final AbiArray pair = new AbiArray(new AbiArrayType(new AbiFixedBytesType((short) 3), 2L), List.of(new AbiFixedBytes(utf8("abc")), new AbiFixedBytes(utf8("def"))));

        // when
        final byte[] data = bar.encodeCall(List.of(pair));

        // then
        assertEquals(hex(("fce353f6" + "6162630000000000000000000000000000000000000000000000000000000000") + "6465660000000000000000000000000000000000000000000000000000000000"), data);
    }

    /// `evm/the-call-of-sam-bytes-bool-uint256-array-is-the-one-of-the-abi-specification`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (sam).
    @Test
    @DisplayName("the call of sam(bytes,bool,uint256[]) is the one of the ABI specification")
    @Tag("testId:evm/the-call-of-sam-bytes-bool-uint256-array-is-the-one-of-the-abi-specification")
    void theCallOfSamBytesBoolUint256IsTheOneOfTheABISpecification() {
        // given
        final AbiFunction sam = AbiFunction.parse("sam(bytes,bool,uint256[])");
        final AbiArray numbers = new AbiArray(new AbiArrayType(new AbiUintType(256), null), List.of(new AbiUint(new BigInteger("1"), 256), new AbiUint(new BigInteger("2"), 256), new AbiUint(new BigInteger("3"), 256)));
        final List<AbiValue> arguments = List.of(new AbiBytes(utf8("dave")), new AbiBool(true), numbers);

        // when
        final byte[] data = sam.encodeCall(arguments);

        // then
        assertEquals(hex((((((((("a5643bf2" + "0000000000000000000000000000000000000000000000000000000000000060") + "0000000000000000000000000000000000000000000000000000000000000001") + "00000000000000000000000000000000000000000000000000000000000000a0") + "0000000000000000000000000000000000000000000000000000000000000004") + "6461766500000000000000000000000000000000000000000000000000000000") + "0000000000000000000000000000000000000000000000000000000000000003") + "0000000000000000000000000000000000000000000000000000000000000001") + "0000000000000000000000000000000000000000000000000000000000000002") + "0000000000000000000000000000000000000000000000000000000000000003"), data);
        assertEquals(arguments, sam.decodeCall(data));
    }

    /// `evm/the-call-of-f-with-static-and-dynamic-arguments-is-the-one-of-the-abi-specification`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Use of Dynamic Types" (f).
    @Test
    @DisplayName("the call of f with static and dynamic arguments is the one of the ABI specification")
    @Tag("testId:evm/the-call-of-f-with-static-and-dynamic-arguments-is-the-one-of-the-abi-specification")
    void theCallOfFWithStaticAndDynamicArgumentsIsTheOneOfTheABISpecification() {
        // given
        final AbiFunction f = AbiFunction.parse("f(uint256,uint32[],bytes10,bytes)");
        final AbiArray numbers = new AbiArray(new AbiArrayType(new AbiUintType(32), null), List.of(new AbiUint(new BigInteger("1110"), 32), new AbiUint(new BigInteger("1929"), 32)));

        // when
        final byte[] data = f.encodeCall(List.of(new AbiUint(new BigInteger("291"), 256), numbers, new AbiFixedBytes(utf8("1234567890")), new AbiBytes(utf8("Hello, world!"))));

        // then
        assertEquals(hex((((((((("8be65246" + "0000000000000000000000000000000000000000000000000000000000000123") + "0000000000000000000000000000000000000000000000000000000000000080") + "3132333435363738393000000000000000000000000000000000000000000000") + "00000000000000000000000000000000000000000000000000000000000000e0") + "0000000000000000000000000000000000000000000000000000000000000002") + "0000000000000000000000000000000000000000000000000000000000000456") + "0000000000000000000000000000000000000000000000000000000000000789") + "000000000000000000000000000000000000000000000000000000000000000d") + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"), data);
    }

    /// `evm/the-call-of-g-with-nested-arrays-is-the-one-of-the-abi-specification`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Use of Dynamic Types" (g).
    @Test
    @DisplayName("the call of g with nested arrays is the one of the ABI specification")
    @Tag("testId:evm/the-call-of-g-with-nested-arrays-is-the-one-of-the-abi-specification")
    void theCallOfGWithNestedArraysIsTheOneOfTheABISpecification() {
        // given
        final AbiFunction g = AbiFunction.parse("g(uint256[][],string[])");
        final AbiArrayType numbersType = new AbiArrayType(new AbiUintType(256), null);
        final AbiArray numbers = new AbiArray(new AbiArrayType(numbersType, null), List.of(new AbiArray(numbersType, List.of(new AbiUint(new BigInteger("1"), 256), new AbiUint(new BigInteger("2"), 256))), new AbiArray(numbersType, List.of(new AbiUint(new BigInteger("3"), 256)))));
        final AbiArray words = new AbiArray(new AbiArrayType(new AbiStringType(), null), List.of(new AbiString("one"), new AbiString("two"), new AbiString("three")));

        // when
        final byte[] data = g.encodeCall(List.of(numbers, words));

        // then
        assertEquals(hex(((((((((((((((((((("2289b18c" + "0000000000000000000000000000000000000000000000000000000000000040") + "0000000000000000000000000000000000000000000000000000000000000140") + "0000000000000000000000000000000000000000000000000000000000000002") + "0000000000000000000000000000000000000000000000000000000000000040") + "00000000000000000000000000000000000000000000000000000000000000a0") + "0000000000000000000000000000000000000000000000000000000000000002") + "0000000000000000000000000000000000000000000000000000000000000001") + "0000000000000000000000000000000000000000000000000000000000000002") + "0000000000000000000000000000000000000000000000000000000000000001") + "0000000000000000000000000000000000000000000000000000000000000003") + "0000000000000000000000000000000000000000000000000000000000000003") + "0000000000000000000000000000000000000000000000000000000000000060") + "00000000000000000000000000000000000000000000000000000000000000a0") + "00000000000000000000000000000000000000000000000000000000000000e0") + "0000000000000000000000000000000000000000000000000000000000000003") + "6f6e650000000000000000000000000000000000000000000000000000000000") + "0000000000000000000000000000000000000000000000000000000000000003") + "74776f0000000000000000000000000000000000000000000000000000000000") + "0000000000000000000000000000000000000000000000000000000000000005") + "7468726565000000000000000000000000000000000000000000000000000000"), data);
    }

    /// `evm/a-result-of-integers-an-address-and-strings-is-decoded`
    ///
    /// Source: the Java SDK (ContractFunctionResultTest: a result of integers, an address and two strings).
    @Test
    @DisplayName("a result of integers, an address and strings is decoded")
    @Tag("testId:evm/a-result-of-integers-an-address-and-strings-is-decoded")
    void aResultOfIntegersAnAddressAndStringsIsDecoded() {
        // given
        final AbiFunction decoder = AbiFunction.parse("f() returns (uint256,int256,address,uint256,string,string)");

        // when
        final List<AbiValue> values = decoder.decodeResult(hex((((((((("00000000000000000000000000000000000000000000000000000000ffffffff" + "7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff") + "00000000000000000000000011223344556677889900aabbccddeeff00112233") + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff") + "00000000000000000000000000000000000000000000000000000000000000c0") + "0000000000000000000000000000000000000000000000000000000000000100") + "000000000000000000000000000000000000000000000000000000000000000d") + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000") + "0000000000000000000000000000000000000000000000000000000000000014") + "48656c6c6f2c20776f726c642c20616761696e21000000000000000000000000"));

        // then
        final List<AbiValue> expected = List.of(new AbiUint(new BigInteger("4294967295"), 256), new AbiInt(new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967"), 256), new AbiAddress(EvmAddress.fromString("11223344556677889900aabbccddeeff00112233")), new AbiUint(new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935"), 256), new AbiString("Hello, world!"), new AbiString("Hello, world, again!"));
        assertEquals(expected, values);
    }

    /// `evm/a-result-of-a-string-array-is-decoded`
    ///
    /// Source: the Java SDK (ContractFunctionResultTest: a string array).
    @Test
    @DisplayName("a result of a string array is decoded")
    @Tag("testId:evm/a-result-of-a-string-array-is-decoded")
    void aResultOfAStringArrayIsDecoded() {
        // given
        final AbiFunction decoder = AbiFunction.parse("f() returns (string[])");

        // when
        final List<AbiValue> values = decoder.decodeResult(hex((((((("0000000000000000000000000000000000000000000000000000000000000020" + "0000000000000000000000000000000000000000000000000000000000000002") + "0000000000000000000000000000000000000000000000000000000000000040") + "0000000000000000000000000000000000000000000000000000000000000080") + "000000000000000000000000000000000000000000000000000000000000000C") + "72616E646F6D2062797465730000000000000000000000000000000000000000") + "000000000000000000000000000000000000000000000000000000000000000C") + "72616E646F6D2062797465730000000000000000000000000000000000000000"));

        // then
        final List<AbiValue> expected = List.of(new AbiArray(new AbiArrayType(new AbiStringType(), null), List.of(new AbiString("random bytes"), new AbiString("random bytes"))));
        assertEquals(expected, values);
    }

    /// `evm/a-value-with-bits-its-type-does-not-have-is-rejected-when-decoded`
    ///
    /// Source: this spec (decoding is strict: a value with bits set that its type does not have is rejected). A word of
    /// 256 one bits is no bool, no uint32, no address and no bytes1 (its padding is not zero); 2 is no bool either.
    @Test
    @DisplayName("a value with bits its type does not have is rejected when decoded")
    @Tag("testId:evm/a-value-with-bits-its-type-does-not-have-is-rejected-when-decoded")
    void aValueWithBitsItsTypeDoesNotHaveIsRejectedWhenDecoded() {
        // given
        final byte[] word = hex("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        for (final String signature : List.of("f() returns (bool)", "f() returns (uint32)", "f() returns (address)", "f() returns (bytes1)")) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> AbiFunction.parse(signature).decodeResult(word));
        }

        // given
        final AbiFunction boolResult = AbiFunction.parse("f() returns (bool)");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> boolResult.decodeResult(hex("0000000000000000000000000000000000000000000000000000000000000002")));
    }

    /// `evm/data-that-is-too-short-or-points-outside-itself-is-rejected-when-decoded`
    ///
    /// Source: this spec (decoding is strict: data that is too short or an offset outside the data is rejected).
    @Test
    @DisplayName("data that is too short or points outside itself is rejected when decoded")
    @Tag("testId:evm/data-that-is-too-short-or-points-outside-itself-is-rejected-when-decoded")
    void dataThatIsTooShortOrPointsOutsideItselfIsRejectedWhenDecoded() {
        // given
        final AbiFunction uintResult = AbiFunction.parse("f() returns (uint256)");
        final AbiFunction stringResult = AbiFunction.parse("f() returns (string)");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> uintResult.decodeResult(hex("00")));
        assertThrows(IllegalArgumentException.class, () -> stringResult.decodeResult(hex("0000000000000000000000000000000000000000000000000000000000000040")));
    }

    /// `evm/call-data-of-another-function-is-rejected`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Function Selector" (the call data starts
    /// with the selector); this spec (call data with another selector is rejected).
    @Test
    @DisplayName("call data of another function is rejected")
    @Tag("testId:evm/call-data-of-another-function-is-rejected")
    void callDataOfAnotherFunctionIsRejected() {
        // given
        final AbiFunction baz = AbiFunction.parse("baz(uint32,bool)");
        final byte[] data = AbiFunction.parse("bar(bytes3[2])").encodeCall(List.of(new AbiArray(new AbiArrayType(new AbiFixedBytesType((short) 3), 2L), List.of(new AbiFixedBytes(utf8("abc")), new AbiFixedBytes(utf8("def"))))));

        // when / then
        assertThrows(IllegalArgumentException.class, () -> baz.decodeCall(data));
    }

    /// `evm/an-integer-that-does-not-fit-its-width-is-rejected-when-encoded`
    ///
    /// Source: this spec (a value that does not fit its width is rejected when it is encoded).
    @Test
    @DisplayName("an integer that does not fit its width is rejected when encoded")
    @Tag("testId:evm/an-integer-that-does-not-fit-its-width-is-rejected-when-encoded")
    void anIntegerThatDoesNotFitItsWidthIsRejectedWhenEncoded() {
        final List<AbiValue> tooLarge = List.of(new AbiUint(new BigInteger("256"), 8), new AbiInt(new BigInteger("128"), 8), new AbiInt(new BigInteger("-129"), 8));
        for (final AbiValue value : tooLarge) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> new AbiTuple(List.of(value)).encode());
        }

        // then: the limits themselves fit
        final AbiTuple limits = new AbiTuple(List.of(new AbiUint(new BigInteger("255"), 8), new AbiInt(new BigInteger("127"), 8), new AbiInt(new BigInteger("-128"), 8)));
        assertEquals(hex(("00000000000000000000000000000000000000000000000000000000000000ff" + "000000000000000000000000000000000000000000000000000000000000007f") + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff80"), limits.encode());
    }

    /// `evm/arguments-of-other-types-and-a-malformed-array-are-rejected-when-encoded`
    ///
    /// Source: this spec (arguments of other types than the inputs, and an array whose values do not match its type, are
    /// rejected when they are encoded).
    @Test
    @DisplayName("arguments of other types and a malformed array are rejected when encoded")
    @Tag("testId:evm/arguments-of-other-types-and-a-malformed-array-are-rejected-when-encoded")
    void argumentsOfOtherTypesAndAMalformedArrayAreRejectedWhenEncoded() {
        // given
        final AbiFunction baz = AbiFunction.parse("baz(uint32,bool)");
        final AbiArrayType pairType = new AbiArrayType(new AbiFixedBytesType((short) 3), 2L);

        // when / then: a uint256 instead of a uint32, a missing argument
        assertThrows(IllegalArgumentException.class, () -> baz.encodeCall(List.of(new AbiUint(new BigInteger("69"), 256), new AbiBool(true))));
        assertThrows(IllegalArgumentException.class, () -> baz.encodeCall(List.of(new AbiUint(new BigInteger("69"), 32))));

        // when / then: a fixed-size array with one value, an array with a value of another type
        assertThrows(IllegalArgumentException.class, () -> new AbiTuple(List.of(new AbiArray(pairType, List.of(new AbiFixedBytes(utf8("abc")))))).encode());
        assertThrows(IllegalArgumentException.class, () -> new AbiTuple(List.of(new AbiArray(pairType, List.of(new AbiFixedBytes(utf8("abc")), new AbiFixedBytes(utf8("de")))))).encode());
    }

    /// `evm/an-abi-type-is-read-from-and-written-as-its-canonical-name`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Types" (uint and int are synonyms for
    /// uint256 and int256; the canonical names are used in signatures).
    @Test
    @DisplayName("an ABI type is read from and written as its canonical name")
    @Tag("testId:evm/an-abi-type-is-read-from-and-written-as-its-canonical-name")
    void anABITypeIsReadFromAndWrittenAsItsCanonicalName() {
        for (final String name : List.of("uint8", "uint256", "int16", "int256", "address", "bool", "bytes1", "bytes32", "bytes", "string", "uint256[]", "bytes3[2]", "uint256[][3]", "(address,uint256)", "(address,uint256)[]", "(uint256,(bool,string[]))", "()")) {
            // when / then
            assertEquals(name, AbiType.parse(name).toString());
        }

        // given
        final AbiType uint256 = new AbiUintType(256);
        final AbiType int256 = new AbiIntType(256);
        final AbiType pair = new AbiArrayType(new AbiFixedBytesType((short) 3), 2L);

        // when / then
        assertEquals(uint256, AbiType.parse("uint"));
        assertEquals(int256, AbiType.parse("int"));
        assertEquals(pair, AbiType.parse("bytes3[2]"));
    }

    /// `evm/a-name-that-is-no-supported-abi-type-is-rejected`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Types" (uint<M> with 0 < M <= 256 and
    /// M % 8 == 0, bytes<M> with 0 < M <= 32); this spec (fixed, ufixed and function are not supported).
    @Test
    @DisplayName("a name that is no supported ABI type is rejected")
    @Tag("testId:evm/a-name-that-is-no-supported-abi-type-is-rejected")
    void aNameThatIsNoSupportedABITypeIsRejected() {
        for (final String name : NO_ABI_TYPES()) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> AbiType.parse(name));
        }
    }

    /// `evm/a-width-that-the-abi-does-not-have-is-rejected`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Types" (uint<M> with 0 < M <= 256 and
    /// M % 8 == 0, bytes<M> with 0 < M <= 32).
    @Test
    @DisplayName("a width that the ABI does not have is rejected")
    @Tag("testId:evm/a-width-that-the-abi-does-not-have-is-rejected")
    void aWidthThatTheABIDoesNotHaveIsRejected() {
        for (final long bits : List.of(0L, 7L, 12L, 264L)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> new AbiUintType(checked(integer(bits), "uint16").intValueExact()));
            assertThrows(IllegalArgumentException.class, () -> new AbiInt(new BigInteger("0"), checked(integer(bits), "uint16").intValueExact()));
        }
        for (final long size : List.of(0L, 33L)) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> new AbiFixedBytesType(checked(integer(size), "uint8").shortValueExact()));
        }
    }

    /// `evm/a-function-and-an-event-built-from-their-types-equal-the-parsed-ones`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz(uint32,bool) with the
    /// selector cdcd77c0); this spec (a function and an event are values of their name and types, parse only reads them
    /// from a signature).
    @Test
    @DisplayName("a function and an event built from their types equal the parsed ones")
    @Tag("testId:evm/a-function-and-an-event-built-from-their-types-equal-the-parsed-ones")
    void aFunctionAndAnEventBuiltFromTheirTypesEqualTheParsedOnes() {
        // when
        final AbiFunction baz = new AbiFunction("baz", List.of(new AbiUintType(32), new AbiBoolType()), List.of(new AbiBoolType()));
        final AbiEvent bazEvent = new AbiEvent("baz", List.of(new AbiEventParameter(new AbiUintType(32), true), new AbiEventParameter(new AbiBoolType(), false)), false);

        // then
        assertEquals(AbiFunction.parse("baz(uint32,bool) returns (bool)"), baz);
        assertEquals("baz(uint32,bool)", baz.signature());
        assertEquals(hex("cdcd77c0"), baz.selector());
        assertEquals(AbiEvent.parse("baz(uint32 indexed,bool)"), bazEvent);
        assertEquals(hex("cdcd77c0"), slice(bazEvent.topic(), 0L, 4L));
    }

    /// `evm/a-function-signature-is-read-with-its-name-arguments-and-results`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", sections "Function Selector" and "Types" (the
    /// signature is the name and the canonical types of the arguments, separated by commas without spaces; tuples are in
    /// parentheses); this spec (" returns " and the result types).
    @Test
    @DisplayName("a function signature is read with its name, arguments and results")
    @Tag("testId:evm/a-function-signature-is-read-with-its-name-arguments-and-results")
    void aFunctionSignatureIsReadWithItsNameArgumentsAndResults() {
        for (final String text : List.of("f()", "_()", "$()", "f_1$()", "transfer(address,uint256)", "f((uint256,address)[],bool)", "f(uint256[][3],bytes32[2][])", "f(())", "f(bytes,string,bool,address,int8,uint8,bytes1)")) {
            // when / then
            assertEquals(text, AbiFunction.parse(text).signature());
        }

        // when
        final AbiFunction transfer = AbiFunction.parse("transfer(address,uint256) returns (bool)");
        final AbiFunction noResults = AbiFunction.parse("f() returns ()");

        // then
        assertEquals("transfer", transfer.name());
        assertEquals(List.of(new AbiAddressType(), new AbiUintType(256)), transfer.inputs());
        assertEquals(List.of(new AbiBoolType()), transfer.outputs());
        assertEquals(AbiFunction.parse("f()"), noResults);
    }

    /// `evm/the-selector-of-supportsinterface-is-the-one-of-eip-165`
    ///
    /// Source: EIP-165, section "How a Contract will Publish the Interfaces it Implements" (the interface identifier of
    /// ERC165, the selector of supportsInterface(bytes4), is 0x01ffc9a7).
    @Test
    @DisplayName("the selector of supportsInterface is the one of EIP-165")
    @Tag("testId:evm/the-selector-of-supportsinterface-is-the-one-of-eip-165")
    void theSelectorOfSupportsInterfaceIsTheOneOfEIP165() {
        // when
        final AbiFunction supportsInterface = AbiFunction.parse("supportsInterface(bytes4) returns (bool)");

        // then
        assertEquals(hex("01ffc9a7"), supportsInterface.selector());
    }

    /// `evm/uint-and-int-in-a-signature-are-read-as-uint256-and-int256`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Function Selector" (uint and int are
    /// written as uint256 and int256 in the signature from which the selector is computed).
    @Test
    @DisplayName("uint and int in a signature are read as uint256 and int256")
    @Tag("testId:evm/uint-and-int-in-a-signature-are-read-as-uint256-and-int256")
    void uintAndIntInASignatureAreReadAsUint256AndInt256() {
        // when
        final AbiFunction short_ = AbiFunction.parse("f(uint,int[]) returns (uint)");

        // then
        assertEquals("f(uint256,int256[])", short_.signature());
        assertEquals(AbiFunction.parse("f(uint256,int256[]) returns (uint256)"), short_);
        assertEquals(AbiFunction.parse("f(uint256,int256[])").selector(), short_.selector());
    }

    /// `evm/a-function-with-100-arguments-and-results-and-deeply-nested-types-is-read`
    ///
    /// Source: this spec (the ABI limits neither the number of arguments and results nor the nesting of tuples and arrays,
    /// so parse does not either).
    @Test
    @DisplayName("a function with 100 arguments and results and deeply nested types is read")
    @Tag("testId:evm/a-function-with-100-arguments-and-results-and-deeply-nested-types-is-read")
    void aFunctionWith100ArgumentsAndResultsAndDeeplyNestedTypesIsRead() {
        // given
        final String hundred = repeat("uint256,", 99L) + "uint256";
        final String nestedTuple = (repeat("(", 32L) + "uint256") + repeat(")", 32L);
        final String nestedArray = "uint256" + repeat("[]", 32L);

        // when
        final AbiFunction wide = AbiFunction.parse(((("f(" + hundred) + ") returns (") + hundred) + ")");
        final AbiFunction deep = AbiFunction.parse(((("f(" + nestedTuple) + ",") + nestedArray) + ")");

        // then
        assertEquals(100L, length(wide.inputs()));
        assertEquals(100L, length(wide.outputs()));
        assertEquals(("f(" + hundred) + ")", wide.signature());
        assertEquals(((("f(" + nestedTuple) + ",") + nestedArray) + ")", deep.signature());
    }

    /// `evm/an-array-length-from-1-to-the-largest-uint32-is-read`
    ///
    /// Source: this spec (the length of a fixed-size array is a uint32 of at least 1, written without leading zeros).
    @Test
    @DisplayName("an array length from 1 to the largest uint32 is read")
    @Tag("testId:evm/an-array-length-from-1-to-the-largest-uint32-is-read")
    void anArrayLengthFrom1ToTheLargestUint32IsRead() {
        for (final String text : List.of("f(uint256[1])", "f(uint256[10])", "f(uint256[4294967295])")) {
            // when / then
            assertEquals(text, AbiFunction.parse(text).signature());
        }

        // when / then
        assertThrows(IllegalArgumentException.class, () -> AbiFunction.parse("f(uint256[4294967296])"));
    }

    /// `evm/a-malformed-function-signature-is-rejected`
    ///
    /// Source: this spec (parse reads only the canonical signature: no spaces except around "returns", no parameter names,
    /// no unsupported types, array lengths of a uint32 of at least 1).
    @Test
    @DisplayName("a malformed function signature is rejected")
    @Tag("testId:evm/a-malformed-function-signature-is-rejected")
    void aMalformedFunctionSignatureIsRejected() {
        for (final String text : MALFORMED_FUNCTION_SIGNATURES()) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> AbiFunction.parse(text));
        }
    }

    /// `evm/an-event-signature-is-read-with-its-name-and-indexed-parameters`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (the signature of an event does
    /// not contain "indexed"); this spec (" indexed" after the type of an indexed parameter).
    @Test
    @DisplayName("an event signature is read with its name and indexed parameters")
    @Tag("testId:evm/an-event-signature-is-read-with-its-name-and-indexed-parameters")
    void anEventSignatureIsReadWithItsNameAndIndexedParameters() {
        // when
        final AbiEvent empty = AbiEvent.parse("E()");
        final AbiEvent mixed = AbiEvent.parse("E((uint256,address) indexed,string,bytes32[2] indexed)");
        final AbiEvent wide = AbiEvent.parse(("E(" + repeat("uint256,", 99L)) + "uint256)");

        // then
        assertEquals("E()", empty.signature());
        assertEquals(0L, length(empty.parameters()));
        assertFalse(empty.anonymous());
        assertEquals("E((uint256,address),string,bytes32[2])", mixed.signature());
        assertEquals(List.of(true, false, true), List.of(get(mixed.parameters(), 0L).indexed(), get(mixed.parameters(), 1L).indexed(), get(mixed.parameters(), 2L).indexed()));
        assertEquals(100L, length(wide.parameters()));
    }

    /// `evm/an-event-with-three-indexed-parameters-is-read-and-one-with-four-is-rejected`
    ///
    /// Source: the Solidity documentation, "Events" (up to three parameters of an event can be indexed; a log has at most
    /// four topics, the first of which is the hash of the signature).
    @Test
    @DisplayName("an event with three indexed parameters is read and one with four is rejected")
    @Tag("testId:evm/an-event-with-three-indexed-parameters-is-read-and-one-with-four-is-rejected")
    void anEventWithThreeIndexedParametersIsReadAndOneWithFourIsRejected() {
        // when
        final AbiEvent three = AbiEvent.parse("E(uint256 indexed,address indexed,bool indexed,string)");

        // then
        assertEquals("E(uint256,address,bool,string)", three.signature());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> AbiEvent.parse("E(uint256 indexed,address indexed,bool indexed,string indexed)"));
    }

    /// `evm/a-malformed-event-signature-is-rejected`
    ///
    /// Source: this spec (parse reads only the canonical signature: " indexed" after the type, no other spaces, no
    /// parameter names, no "returns").
    @Test
    @DisplayName("a malformed event signature is rejected")
    @Tag("testId:evm/a-malformed-event-signature-is-rejected")
    void aMalformedEventSignatureIsRejected() {
        for (final String text : MALFORMED_EVENT_SIGNATURES()) {
            // when / then
            assertThrows(IllegalArgumentException.class, () -> AbiEvent.parse(text));
        }
    }

    /// `evm/the-topic-of-an-event-is-the-hash-whose-first-4-bytes-are-the-selector`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", sections "Function Selector" and "Events" (the
    /// selector is the first 4 bytes of the Keccak-256 hash of the signature, the topic of an event the whole hash); the
    /// selector of baz(uint32,bool) of section "Examples".
    @Test
    @DisplayName("the topic of an event is the hash whose first 4 bytes are the selector")
    @Tag("testId:evm/the-topic-of-an-event-is-the-hash-whose-first-4-bytes-are-the-selector")
    void theTopicOfAnEventIsTheHashWhoseFirst4BytesAreTheSelector() {
        // given
        final AbiEvent bazEvent = AbiEvent.parse("baz(uint32 indexed,bool)");

        // when
        final byte[] topic = bazEvent.topic();

        // then
        assertEquals("baz(uint32,bool)", bazEvent.signature());
        assertEquals(32L, length(topic));
        assertEquals(hex("cdcd77c0"), slice(topic, 0L, 4L));
        assertEquals(List.of(new AbiEventParameter(new AbiUintType(32), true), new AbiEventParameter(new AbiBoolType(), false)), bazEvent.parameters());
    }

    /// `evm/a-log-of-a-transfer-is-decoded`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (the first topic is the hash of
    /// the signature, an indexed value of at most 32 bytes is a topic of its own padded to 32 bytes, the other values are
    /// encoded in the data); the addresses of EIP55_ADDRESSES.
    @Test
    @DisplayName("a log of a transfer is decoded")
    @Tag("testId:evm/a-log-of-a-transfer-is-decoded")
    void aLogOfATransferIsDecoded() {
        // given
        final AbiEvent transfer = AbiEvent.parse("Transfer(address indexed,address indexed,uint256)");
        final EvmAddress from = EvmAddress.fromString(get(EIP55_ADDRESSES(), 4L));
        final EvmAddress to = EvmAddress.fromString(get(EIP55_ADDRESSES(), 5L));
        final List<byte[]> topics = List.of(transfer.topic(), concat(hex("000000000000000000000000"), from.bytes()), concat(hex("000000000000000000000000"), to.bytes()));

        // when
        final List<AbiValue> values = transfer.decodeLog(topics, hex("00000000000000000000000000000000000000000000000000000000000003e8"));

        // then
        final List<AbiValue> expected = List.of(new AbiAddress(from), new AbiAddress(to), new AbiUint(new BigInteger("1000"), 256));
        assertEquals(expected, values);
    }

    /// `evm/an-indexed-string-of-a-log-is-returned-as-its-hash`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (an indexed value of a dynamic
    /// type is stored as the Keccak-256 hash of its encoding); this spec (the hash is returned as an AbiFixedBytes).
    @Test
    @DisplayName("an indexed string of a log is returned as its hash")
    @Tag("testId:evm/an-indexed-string-of-a-log-is-returned-as-its-hash")
    void anIndexedStringOfALogIsReturnedAsItsHash() {
        // given
        final AbiEvent named = AbiEvent.parse("Named(string indexed,uint256)");
        final byte[] hash = hex(repeat("ab", 32L));

        // when
        final List<AbiValue> values = named.decodeLog(List.of(named.topic(), hash), hex("0000000000000000000000000000000000000000000000000000000000000007"));

        // then
        final List<AbiValue> expected = List.of(new AbiFixedBytes(hash), new AbiUint(new BigInteger("7"), 256));
        assertEquals(expected, values);
    }

    /// `evm/a-log-of-another-event-or-with-other-topics-is-rejected-and-an-anonymous-one-is-read-without-its-topic`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Events" (an anonymous event has no topic
    /// with its signature); this spec (a log of another event or with a missing topic is rejected).
    @Test
    @DisplayName("a log of another event or with other topics is rejected, and an anonymous one is read without its topic")
    @Tag("testId:evm/a-log-of-another-event-or-with-other-topics-is-rejected-and-an-anonymous-one-is-read-without-its-topic")
    void aLogOfAnotherEventOrWithOtherTopicsIsRejectedAndAnAnonymousOneIsReadWithoutItsTopic() {
        // given
        final AbiEvent transfer = AbiEvent.parse("Transfer(address indexed,address indexed,uint256)");
        final byte[] from = concat(hex("000000000000000000000000"), EvmAddress.fromString(get(EIP55_ADDRESSES(), 4L)).bytes());
        final byte[] amount = hex("00000000000000000000000000000000000000000000000000000000000003e8");
        final AbiEvent other = AbiEvent.parse("Approval(address indexed,address indexed,uint256)");

        // when / then: the topic of another event, a missing topic
        assertThrows(IllegalArgumentException.class, () -> transfer.decodeLog(List.of(other.topic(), from, from), amount));
        assertThrows(IllegalArgumentException.class, () -> transfer.decodeLog(List.of(transfer.topic(), from), amount));

        // when
        final AbiEvent anonymous = new AbiEvent("Moved", List.of(new AbiEventParameter(new AbiAddressType(), true), new AbiEventParameter(new AbiUintType(256), false)), true);
        final List<AbiValue> values = anonymous.decodeLog(List.of(from), amount);

        // then
        final List<AbiValue> expected = List.of(new AbiAddress(EvmAddress.fromString(get(EIP55_ADDRESSES(), 4L))), new AbiUint(new BigInteger("1000"), 256));
        assertEquals(expected, values);
    }

    /// `evm/a-value-knows-its-type`
    ///
    /// Source: this spec (a value knows its type).
    @Test
    @DisplayName("a value knows its type")
    @Tag("testId:evm/a-value-knows-its-type")
    void aValueKnowsItsType() {
        // given
        final AbiType uint32 = new AbiUintType(32);
        final AbiType bytes3 = new AbiFixedBytesType((short) 3);

        // when / then
        assertEquals(uint32, new AbiUint(new BigInteger("1"), 32).abiType());
        assertEquals(bytes3, new AbiFixedBytes(utf8("abc")).abiType());
        assertEquals(AbiType.parse("(bool,string)"), new AbiTuple(List.of(new AbiBool(true), new AbiString("x"))).abiType());
    }

    /// `evm/the-revert-data-of-error-string-and-panic-uint256-is-decoded`
    ///
    /// Source: the Solidity documentation, "Expressions and Control Structures", section "Error handling: Assert, Require,
    /// Revert and Exceptions": a revert with a reason is the call data of Error(string) (selector 0x08c379a0), the example
    /// "Not enough Ether provided."; a panic is Panic(uint256) (selector 0x4e487b71), code 0x11 for an arithmetic overflow.
    @Test
    @DisplayName("the revert data of Error(string) and Panic(uint256) is decoded")
    @Tag("testId:evm/the-revert-data-of-error-string-and-panic-uint256-is-decoded")
    void theRevertDataOfErrorStringAndPanicUint256IsDecoded() {
        // given
        final AbiFunction error = AbiFunction.parse("Error(string)");
        final AbiFunction panic = AbiFunction.parse("Panic(uint256)");
        final byte[] revert = hex((("08c379a0" + "0000000000000000000000000000000000000000000000000000000000000020") + "000000000000000000000000000000000000000000000000000000000000001a") + "4e6f7420656e6f7567682045746865722070726f76696465642e000000000000");
        final byte[] overflow = hex("4e487b71" + "0000000000000000000000000000000000000000000000000000000000000011");

        // when
        final List<AbiValue> reason = error.decodeCall(revert);
        final List<AbiValue> code = panic.decodeCall(overflow);

        // then
        assertEquals(hex("08c379a0"), error.selector());
        assertEquals(hex("4e487b71"), panic.selector());
        assertEquals(List.of(new AbiString("Not enough Ether provided.")), reason);
        assertEquals(List.of(new AbiUint(new BigInteger("17"), 256)), code);

        // when / then: revert data of the other one is rejected
        assertThrows(IllegalArgumentException.class, () -> error.decodeCall(overflow));
    }
}
