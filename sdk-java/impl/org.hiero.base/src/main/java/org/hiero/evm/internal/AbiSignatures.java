package org.hiero.evm.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hiero.evm.AbiAddressType;
import org.hiero.evm.AbiArrayType;
import org.hiero.evm.AbiBoolType;
import org.hiero.evm.AbiBytesType;
import org.hiero.evm.AbiEvent;
import org.hiero.evm.AbiEventParameter;
import org.hiero.evm.AbiFixedBytesType;
import org.hiero.evm.AbiFunction;
import org.hiero.evm.AbiIntType;
import org.hiero.evm.AbiStringType;
import org.hiero.evm.AbiTupleType;
import org.hiero.evm.AbiType;
import org.hiero.evm.AbiUintType;

/// Reads ABI types, function signatures (`balanceOf(address) returns (uint256)`) and event signatures
/// (`Transfer(address indexed,address indexed,uint256)`). Names are canonical: no spaces inside a type, `uint` and `int`
/// are the only synonyms (of `uint256` and `int256`), and numbers have no leading zeros.
public final class AbiSignatures {

    private static final Pattern NAME = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    private static final Pattern ELEMENTARY = Pattern.compile("(uint|int|bytes)([1-9][0-9]*)?|address|bool|string");
    private static final String RETURNS = " returns ";
    private static final String INDEXED = " indexed";
    /// A log has at most four topics; the first is the hash of the signature of an event that is not anonymous.
    private static final int MAX_INDEXED = 3;

    private AbiSignatures() {
    }

    /// Reads a type from its name.
    ///
    /// @param text the name
    /// @return the type
    /// @throws IllegalArgumentException if the text is no supported ABI type
    public static AbiType parseType(final String text) {
        final Parser parser = new Parser(text);
        final AbiType type = parser.type();
        parser.end();
        return type;
    }

    /// Reads a function from its signature.
    ///
    /// @param signature the signature, optionally followed by ` returns ` and the output types in parentheses
    /// @return the function
    /// @throws IllegalArgumentException if the text is no such signature
    public static AbiFunction parseFunction(final String signature) {
        final Parser parser = new Parser(signature);
        final String name = parser.name();
        final List<AbiType> inputs = parser.tuple().components();
        List<AbiType> outputs = List.of();
        if (parser.consume(RETURNS)) {
            outputs = parser.tuple().components();
        }
        parser.end();
        return new AbiFunction(name, inputs, outputs);
    }

    /// Reads an event from its signature.
    ///
    /// @param signature the signature; an indexed parameter has ` indexed` after its type
    /// @return the event, not anonymous
    /// @throws IllegalArgumentException if the text is no such signature or has more than three indexed parameters
    public static AbiEvent parseEvent(final String signature) {
        final Parser parser = new Parser(signature);
        final String name = parser.name();
        final List<AbiEventParameter> parameters = new ArrayList<>();
        parser.expect('(');
        if (!parser.consume(")")) {
            do {
                final AbiType type = parser.type();
                parameters.add(new AbiEventParameter(type, parser.consume(INDEXED)));
            } while (parser.consume(","));
            parser.expect(')');
        }
        parser.end();
        if (parameters.stream().filter(AbiEventParameter::indexed).count() > MAX_INDEXED) {
            throw new IllegalArgumentException("'" + signature + "' has more than " + MAX_INDEXED
                    + " indexed parameters");
        }
        return new AbiEvent(name, parameters, false);
    }

    /// A recursive-descent reader of the grammar `type := (tuple | elementary) ("[" length? "]")*`.
    private static final class Parser {

        private final String text;
        private int position;

        Parser(final String text) {
            this.text = text;
        }

        String name() {
            final Matcher matcher = NAME.matcher(text).region(position, text.length());
            if (!matcher.lookingAt()) {
                throw error("a name");
            }
            position = matcher.end();
            return matcher.group();
        }

        AbiType type() {
            AbiType type = peek('(') ? tuple() : elementary();
            while (consume("[")) {
                final int start = position;
                while (position < text.length() && Character.isDigit(text.charAt(position))) {
                    position++;
                }
                final String digits = text.substring(start, position);
                expect(']');
                if (digits.isEmpty()) {
                    type = new AbiArrayType(type, null);
                } else if (digits.length() > 1 && digits.charAt(0) == '0' || digits.length() > 10) {
                    throw error("an array length");
                } else {
                    type = new AbiArrayType(type, Long.parseLong(digits));
                }
            }
            return type;
        }

        AbiTupleType tuple() {
            expect('(');
            final List<AbiType> components = new ArrayList<>();
            if (!consume(")")) {
                do {
                    components.add(type());
                } while (consume(","));
                expect(')');
            }
            return new AbiTupleType(components);
        }

        private AbiType elementary() {
            final Matcher matcher = ELEMENTARY.matcher(text).region(position, text.length());
            if (!matcher.lookingAt()) {
                throw error("a type");
            }
            position = matcher.end();
            final String base = matcher.group(1) == null ? matcher.group() : matcher.group(1);
            final String size = matcher.group(2);
            return switch (base) {
                case "address" -> new AbiAddressType();
                case "bool" -> new AbiBoolType();
                case "string" -> new AbiStringType();
                case "uint" -> new AbiUintType(size == null ? 256 : number(size, 256));
                case "int" -> new AbiIntType(size == null ? 256 : number(size, 256));
                case "bytes" -> size == null ? new AbiBytesType() : new AbiFixedBytesType((short) number(size, 32));
                default -> throw error("a type");
            };
        }

        /// A width; out of range is rejected here so that a large number cannot overflow the attribute.
        private int number(final String digits, final int max) {
            if (digits.length() > 3 || Integer.parseInt(digits) > max) {
                throw error("a width of at most " + max);
            }
            return Integer.parseInt(digits);
        }

        boolean peek(final char c) {
            return position < text.length() && text.charAt(position) == c;
        }

        boolean consume(final String token) {
            if (text.startsWith(token, position)) {
                position += token.length();
                return true;
            }
            return false;
        }

        void expect(final char c) {
            if (!consume(String.valueOf(c))) {
                throw error("'" + c + "'");
            }
        }

        void end() {
            if (position != text.length()) {
                throw error("the end");
            }
        }

        private IllegalArgumentException error(final String expected) {
            return new IllegalArgumentException("'" + text + "' is no valid ABI signature or type: expected " + expected
                    + " at position " + position);
        }
    }
}
