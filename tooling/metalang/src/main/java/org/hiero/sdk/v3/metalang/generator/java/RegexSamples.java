package org.hiero.sdk.v3.metalang.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Derives strings for a {@code @@pattern} (find semantics like the validator and the generated check): a string the
 * pattern accepts and one it rejects. The accepted string is built from the regular expression (literals, escapes,
 * character classes, groups, alternatives and quantifiers, each with its smallest repetition); constructs that are not
 * understood (back references, lookarounds, ...) give no sample. Every result is verified with
 * {@link java.util.regex.Pattern}, so a returned string is always correct.
 */
final class RegexSamples {

    /** Candidates for a string the pattern rejects, tried in this order. */
    private static final List<String> REJECTED = List.of("", " ", "!", "a b", "\t", "#", "0", "a", "/", "ä");

    /** Candidates for a character of a character class: printable ASCII, letters and digits first. */
    private static final String CLASS_CANDIDATES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            + "_-./:@#!$%&*+,;=?^`|~'\"()<>[]{}\\ \t\n\r";

    private RegexSamples() {
    }

    /**
     * Returns a string that the pattern accepts.
     *
     * @param regex the regular expression
     * @return the string, empty if none could be derived
     */
    static Optional<String> accepted(final String regex) {
        final Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (final PatternSyntaxException e) {
            return Optional.empty();
        }
        final List<String> candidates = new ArrayList<>();
        try {
            candidates.add(new Parser(regex).alternatives());
        } catch (final IllegalArgumentException e) {
            // not understood: only the fallback candidates
        }
        candidates.addAll(List.of("a", "0", "value"));
        return candidates.stream().filter(c -> pattern.matcher(c).find()).findFirst();
    }

    /**
     * Returns a string that the pattern rejects.
     *
     * @param regex the regular expression
     * @return the string, empty if the pattern accepts every candidate
     */
    static Optional<String> rejected(final String regex) {
        final Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (final PatternSyntaxException e) {
            return Optional.empty();
        }
        return REJECTED.stream().filter(c -> !pattern.matcher(c).find()).findFirst();
    }

    /** A recursive-descent reader of the regular expression that builds the smallest accepted string. */
    private static final class Parser {

        private final String regex;
        private int position;

        private Parser(final String regex) {
            this.regex = regex;
        }

        private String alternatives() {
            // the first alternative is enough
            final String first = sequence();
            while (position < regex.length() && regex.charAt(position) == '|') {
                position++;
                sequence();
            }
            if (position < regex.length() && regex.charAt(position) != ')') {
                throw new IllegalArgumentException("unexpected " + regex.charAt(position));
            }
            return first;
        }

        private String sequence() {
            final StringBuilder out = new StringBuilder();
            while (position < regex.length() && regex.charAt(position) != '|' && regex.charAt(position) != ')') {
                final String atom = atom();
                final int minimum = quantifier();
                out.append(atom.repeat(minimum));
            }
            return out.toString();
        }

        private String atom() {
            final char c = regex.charAt(position++);
            return switch (c) {
                case '^', '$' -> "";
                case '.' -> "a";
                case '(' -> {
                    if (regex.startsWith("?:", position)) {
                        position += 2;
                    } else if (position < regex.length() && regex.charAt(position) == '?') {
                        throw new IllegalArgumentException("unsupported group");
                    }
                    final String inner = alternatives();
                    expect(')');
                    yield inner;
                }
                case '[' -> String.valueOf(characterClass());
                case '\\' -> escape();
                case '*', '+', '?', '{', ')', '|' -> throw new IllegalArgumentException("unexpected " + c);
                default -> String.valueOf(c);
            };
        }

        /** The smallest repetition of the quantifier after an atom (1 without quantifier). */
        private int quantifier() {
            if (position >= regex.length()) {
                return 1;
            }
            final char c = regex.charAt(position);
            final int minimum;
            switch (c) {
                case '*', '?' -> {
                    position++;
                    minimum = 0;
                }
                case '+' -> {
                    position++;
                    minimum = 1;
                }
                case '{' -> {
                    final int end = regex.indexOf('}', position);
                    if (end < 0) {
                        throw new IllegalArgumentException("unclosed quantifier");
                    }
                    final String bounds = regex.substring(position + 1, end);
                    final String lower = bounds.contains(",") ? bounds.substring(0, bounds.indexOf(',')) : bounds;
                    minimum = Integer.parseInt(lower.strip());
                    position = end + 1;
                }
                default -> {
                    return 1;
                }
            }
            // lazy and possessive markers do not change the smallest repetition
            if (position < regex.length() && (regex.charAt(position) == '?' || regex.charAt(position) == '+')) {
                position++;
            }
            return minimum;
        }

        private String escape() {
            final char c = regex.charAt(position++);
            return switch (c) {
                case 'd' -> "0";
                case 'D', 'w', 'S' -> "a";
                case 'W', 's' -> c == 's' ? " " : "!";
                case 'b', 'B', 'A', 'z', 'Z', 'G' -> "";
                case 't' -> "\t";
                case 'n' -> "\n";
                case 'r' -> "\r";
                default -> {
                    if (Character.isLetterOrDigit(c)) {
                        throw new IllegalArgumentException("unsupported escape \\" + c);
                    }
                    yield String.valueOf(c);
                }
            };
        }

        /** A character of a character class ({@code [...]}): the first one it accepts. */
        private char characterClass() {
            final boolean negated = position < regex.length() && regex.charAt(position) == '^';
            if (negated) {
                position++;
            }
            final List<char[]> ranges = new ArrayList<>();
            boolean first = true;
            while (position < regex.length() && (regex.charAt(position) != ']' || first)) {
                first = false;
                if (regex.charAt(position) == '[') {
                    throw new IllegalArgumentException("nested character class");
                }
                final char[] from = classCharacter();
                if (position + 1 < regex.length() && regex.charAt(position) == '-' && regex.charAt(position + 1) != ']'
                        && from.length == 1) {
                    position++;
                    final char[] to = classCharacter();
                    if (to.length != 1) {
                        throw new IllegalArgumentException("unsupported range");
                    }
                    ranges.add(new char[] {from[0], to[0]});
                } else {
                    ranges.add(from.length == 1 ? new char[] {from[0], from[0]} : from);
                }
            }
            expect(']');
            for (final char c : CLASS_CANDIDATES.toCharArray()) {
                if (contains(ranges, c) != negated) {
                    return c;
                }
            }
            throw new IllegalArgumentException("empty character class");
        }

        private static boolean contains(final List<char[]> ranges, final char c) {
            for (final char[] range : ranges) {
                for (int i = 0; i + 1 < range.length; i += 2) {
                    if (c >= range[i] && c <= range[i + 1]) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * One character of a class, or the ranges of a predefined class ({@code \d} = {@code 0-9}, ...) as pairs of
         * bounds.
         */
        private char[] classCharacter() {
            final char c = regex.charAt(position++);
            if (c != '\\') {
                return new char[] {c};
            }
            final char escaped = regex.charAt(position++);
            return switch (escaped) {
                case 'd' -> new char[] {'0', '9'};
                case 'w' -> new char[] {'a', 'z', 'A', 'Z', '0', '9', '_', '_'};
                case 's' -> new char[] {' ', ' ', '\t', '\r'};
                case 'D', 'W', 'S' -> throw new IllegalArgumentException("unsupported negated class in class");
                case 't' -> new char[] {'\t'};
                case 'n' -> new char[] {'\n'};
                case 'r' -> new char[] {'\r'};
                default -> {
                    if (Character.isLetterOrDigit(escaped)) {
                        throw new IllegalArgumentException("unsupported escape \\" + escaped);
                    }
                    yield new char[] {escaped};
                }
            };
        }

        private void expect(final char c) {
            if (position >= regex.length() || regex.charAt(position) != c) {
                throw new IllegalArgumentException("expected " + c);
            }
            position++;
        }
    }
}
