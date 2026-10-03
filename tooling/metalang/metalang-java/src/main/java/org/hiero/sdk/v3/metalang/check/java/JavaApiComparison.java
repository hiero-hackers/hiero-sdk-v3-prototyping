package org.hiero.sdk.v3.metalang.check.java;

import org.hiero.sdk.v3.metalang.check.ApiDifference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.hiero.sdk.v3.metalang.check.java.JavaApi.ApiMember;
import org.hiero.sdk.v3.metalang.check.java.JavaApi.ApiModule;
import org.hiero.sdk.v3.metalang.check.java.JavaApi.ApiType;

/**
 * Compares the API of a project with the expected API. Everything the expected API declares must exist unchanged;
 * additions are allowed: more types, members, implemented interfaces, permitted subtypes, enum constants,
 * annotations, exported packages and required modules. Method bodies, private members, formatting, imports and
 * documentation are not compared, and whether a method is abstract, a default method or implemented is the choice of
 * the implementation.
 */
public final class JavaApiComparison {

    private JavaApiComparison() {
    }

    /**
     * Compares a project with the expected API.
     *
     * @param expected the expected API
     * @param actual   the API of the project
     * @return the differences, sorted by file and line; empty if the project provides the expected API
     */
    public static List<ApiDifference> compare(final JavaApi expected, final JavaApi actual) {
        Objects.requireNonNull(expected, "expected must not be null");
        Objects.requireNonNull(actual, "actual must not be null");
        final List<ApiDifference> differences = new ArrayList<>(actual.problems());
        expected.modules().forEach((name, module) -> {
            final ApiModule found = actual.modules().get(name);
            if (found == null) {
                differences.add(new ApiDifference("", 0, "Module " + name + " is missing (expected in "
                        + module.file() + ")"));
            } else {
                compareModule(module, found, differences);
            }
        });
        expected.types().forEach((name, type) -> {
            final ApiType found = actual.types().get(name);
            if (found == null) {
                differences.add(new ApiDifference("", 0, capitalize(type.kind()) + " " + name
                        + " is missing (expected in " + type.file() + ")"));
            } else {
                compareType(type, found, differences);
            }
        });
        return differences.stream().sorted().toList();
    }

    private static void compareModule(final ApiModule expected, final ApiModule actual,
                                      final List<ApiDifference> differences) {
        final String element = "Module " + expected.name();
        expected.requires().forEach((module, flags) -> {
            final String found = actual.requires().get(module);
            if (found == null) {
                add(differences, actual.file(), actual.line(), element + " must require " + module);
            } else if (!found.equals(flags)) {
                add(differences, actual.file(), actual.line(), element + " must require " + module + " as '"
                        + ("requires " + flags).strip() + "', found '" + ("requires " + found).strip() + "'");
            }
        });
        missing(expected.exports(), actual.exports()).forEach(p -> add(differences, actual.file(), actual.line(),
                element + " must export " + p));
        missing(expected.annotations(), actual.annotations()).forEach(a -> add(differences, actual.file(),
                actual.line(), element + " must be annotated with " + a));
    }

    private static void compareType(final ApiType expected, final ApiType actual,
                                    final List<ApiDifference> differences) {
        final String file = actual.file();
        final long line = actual.line();
        final String element = capitalize(expected.kind()) + " " + expected.name();
        if (!expected.kind().equals(actual.kind())) {
            add(differences, file, line, "Type " + expected.name() + " must be " + article(expected.kind()) + ", found "
                    + article(actual.kind()));
            return;
        }
        same(differences, file, line, element, "modifiers", String.join(" ", expected.modifiers()),
                String.join(" ", actual.modifiers()));
        same(differences, file, line, element, "type parameters", expected.typeParameters().strip(),
                actual.typeParameters().strip());
        same(differences, file, line, element, "superclass", expected.superclass(), actual.superclass());
        same(differences, file, line, element, "record components", String.join(", ", expected.recordComponents()),
                String.join(", ", actual.recordComponents()));
        missing(expected.interfaces(), actual.interfaces()).forEach(i -> add(differences, file, line,
                element + " must implement " + i));
        missing(expected.permits(), actual.permits()).forEach(p -> add(differences, file, line,
                element + " must permit " + p));
        missing(expected.enumConstants(), actual.enumConstants()).forEach(c -> add(differences, file, line,
                element + " must declare the constant " + c));
        missing(expected.annotations(), actual.annotations()).forEach(a -> add(differences, file, line,
                element + " must be annotated with " + a));
        for (final Map.Entry<String, ApiMember> entry : expected.members().entrySet()) {
            final ApiMember member = entry.getValue();
            final ApiMember found = actual.members().get(entry.getKey());
            final String memberElement = capitalize(member.kind()) + " " + expected.name() + "."
                    + entry.getKey();
            if (found == null) {
                add(differences, file, line, memberElement + " is missing");
                continue;
            }
            same(differences, found.file(), found.line(), memberElement, "declaration", member.signature(),
                    found.signature());
            missing(member.annotations(), found.annotations()).stream()
                    .filter(a -> !JavaApi.isNullnessAnnotation(a)) // part of the declaration, compared above
                    .forEach(a -> add(differences, found.file(),
                    found.line(), memberElement + " must be annotated with " + a));
        }
    }

    private static void same(final List<ApiDifference> differences, final String file, final long line,
                             final String element, final String aspect, final String expected, final String actual) {
        if (!expected.equals(actual)) {
            add(differences, file, line, element + " has different " + aspect + ": expected '" + expected
                    + "', found '" + actual + "'");
        }
    }

    private static Set<String> missing(final Collection<String> expected, final Collection<String> actual) {
        final Set<String> result = new TreeSet<>(expected);
        result.removeAll(actual);
        return result;
    }

    private static void add(final List<ApiDifference> differences, final String file, final long line,
                            final String message) {
        differences.add(new ApiDifference(file, line, message));
    }

    private static String capitalize(final String kind) {
        return Character.toUpperCase(kind.charAt(0)) + kind.substring(1);
    }

    private static String article(final String kind) {
        return (kind.equals("interface") || kind.equals("enum") || kind.equals("annotation") ? "an " : "a ") + kind;
    }
}
