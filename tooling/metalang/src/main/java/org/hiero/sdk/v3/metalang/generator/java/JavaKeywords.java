package org.hiero.sdk.v3.metalang.generator.java;

import java.util.Set;

/**
 * Java reserved words. A meta-language name that is a Java keyword gets a trailing {@code _}.
 */
final class JavaKeywords {

    private static final Set<String> KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "_");

    private JavaKeywords() {
    }

    /**
     * Returns a valid Java identifier for a meta-language name.
     *
     * @param name the name
     * @return the name, with a trailing {@code _} if it is a Java keyword
     */
    static String identifier(final String name) {
        return KEYWORDS.contains(name) ? name + "_" : name;
    }

    /**
     * Returns the getter name of an attribute ({@code symbol} becomes {@code getSymbol}).
     *
     * @param attribute the attribute name
     * @return the getter name
     */
    static String getter(final String attribute) {
        return "get" + Character.toUpperCase(attribute.charAt(0)) + attribute.substring(1);
    }
}
