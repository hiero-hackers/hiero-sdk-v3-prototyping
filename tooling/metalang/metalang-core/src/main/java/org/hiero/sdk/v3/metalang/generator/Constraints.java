package org.hiero.sdk.v3.metalang.generator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.hiero.sdk.v3.metalang.ast.Annotation;
import org.hiero.sdk.v3.metalang.ast.Literal;
import org.hiero.sdk.v3.metalang.model.Type;
import org.hiero.sdk.v3.metalang.model.TypeDefinition;
import org.hiero.sdk.v3.metalang.model.TypeParameterDefinition;
import org.hiero.sdk.v3.metalang.semantic.BuiltinType;
import org.jspecify.annotations.Nullable;

/**
 * The language-independent part of the test values of the generated tests: what the validation annotations of an
 * attribute or parameter allow ({@code @@min}/{@code @@max} with the range of the integer type, lengths, sizes,
 * patterns, URLs), the type arguments used for type parameters, and whether a default instance fits a required type.
 * The generators of the languages render the values in their syntax.
 */
public final class Constraints {

    /** The {@code string} type, the default for type variables without bound. */
    public static final Type STRING = new Type.BasicType(BuiltinType.lookup("string").orElseThrow(), List.of());

    private Constraints() {
    }

    /**
     * Returns the type arguments the tests use for type parameters: the bound, or {@code string} without bound.
     *
     * @param parameters the type parameters
     * @return the type argument of every type variable; empty if a bound refers to a type variable (e.g. a self
     *         type), which has no simple type argument
     */
    public static Optional<Map<Type.TypeVariable, Type>> defaults(final List<TypeParameterDefinition> parameters) {
        final Map<Type.TypeVariable, Type> result = new HashMap<>();
        for (final TypeParameterDefinition parameter : parameters) {
            if (parameter.bound() == null) {
                result.put(parameter.variable(), STRING);
            } else if (containsVariable(parameter.bound())) {
                return Optional.empty();
            } else {
                result.put(parameter.variable(), parameter.bound());
            }
        }
        return Optional.of(result);
    }

    /**
     * The values of an integer type with its {@code @@min}/{@code @@max}: the range of the type intersected with the
     * annotations.
     *
     * @param builtin     the integer type
     * @param annotations the annotations
     * @return the range; {@code min > max} if the constraints contradict each other
     */
    public static IntegerRange range(final BuiltinType builtin, final List<Annotation> annotations) {
        IntegerRange range = IntegerRange.of(builtin);
        final Optional<BigDecimal> min = bound(annotations, "min");
        final Optional<BigDecimal> max = bound(annotations, "max");
        if (min.isPresent()) {
            range = range.intersect(new IntegerRange(min.get().setScale(0, java.math.RoundingMode.CEILING)
                    .toBigIntegerExact(), range.max()));
        }
        if (max.isPresent()) {
            range = range.intersect(new IntegerRange(range.min(), max.get()
                    .setScale(0, java.math.RoundingMode.FLOOR).toBigIntegerExact()));
        }
        return range;
    }

    public static Optional<BigDecimal> decimal(final List<Annotation> annotations, final int variant) {
        BigDecimal value = new BigDecimal("1.5").add(BigDecimal.valueOf(variant));
        final Optional<BigDecimal> min = bound(annotations, "min");
        final Optional<BigDecimal> max = bound(annotations, "max");
        if (min.isPresent() && max.isPresent() && min.get().compareTo(max.get()) > 0) {
            return Optional.empty();
        }
        if (min.isPresent() && value.compareTo(min.get()) < 0) {
            value = min.get();
        }
        if (max.isPresent() && value.compareTo(max.get()) > 0) {
            value = max.get();
        }
        return Optional.of(value);
    }

    /**
     * Returns a string that fulfils the string constraints ({@code @@pattern}, {@code @@urlPattern},
     * {@code @@minLength}, {@code @@maxLength}), verified like the generated checks.
     *
     * @param annotations the annotations
     * @param variant     the variant
     * @return the string, empty if none was found
     */
    public static Optional<String> string(final List<Annotation> annotations, final int variant) {
        final Optional<String> pattern = stringArgument(annotations, "pattern");
        final boolean url = annotations.stream().anyMatch(a -> a.name().equals("urlPattern"));
        final List<String> bases = new ArrayList<>();
        if (url) {
            bases.add("https://example.com/v" + variant);
        }
        if (pattern.isPresent()) {
            final Optional<String> accepted = RegexSamples.accepted(pattern.get());
            accepted.ifPresent(a -> bases.add(a + suffix(variant)));
            accepted.ifPresent(bases::add);
        }
        bases.add("value" + suffix(variant));
        bases.add("a" + suffix(variant));
        return bases.stream().map(b -> fitLength(b, annotations))
                .filter(c -> isValidString(c, annotations)).findFirst();
    }

    private static String fitLength(final String value, final List<Annotation> annotations) {
        final int min = size(annotations, "minLength").orElse(0);
        final int max = size(annotations, "maxLength").orElse(Integer.MAX_VALUE);
        String result = value;
        if (result.length() < min) {
            result = result + "a".repeat(min - result.length());
        }
        if (result.length() > max) {
            result = result.substring(0, max);
        }
        return result;
    }

    /**
     * Whether the generated checks accept the string.
     *
     * @param value       the string
     * @param annotations the annotations of the attribute or parameter
     * @return {@code true} if every string constraint is fulfilled
     */
    public static boolean isValidString(final String value, final List<Annotation> annotations) {
        if (size(annotations, "minLength").filter(min -> value.length() < min).isPresent()
                || size(annotations, "maxLength").filter(max -> value.length() > max).isPresent()) {
            return false;
        }
        final Optional<String> pattern = stringArgument(annotations, "pattern");
        if (pattern.isPresent() && !Pattern.compile(pattern.get()).matcher(value).find()) {
            return false;
        }
        if (annotations.stream().anyMatch(a -> a.name().equals("urlPattern"))) {
            try {
                final URI uri = new URI(value);
                return uri.isAbsolute() && uri.getHost() != null;
            } catch (final URISyntaxException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * The number of elements of a value with {@code @@minSize}/{@code @@maxSize}: the preferred number within the
     * bounds.
     *
     * @param annotations the annotations
     * @param preferred   the preferred number
     * @return the number, -1 if the bounds contradict each other
     */
    public static int elementCount(final List<Annotation> annotations, final int preferred) {
        final int min = size(annotations, "minSize").orElse(0);
        final int max = size(annotations, "maxSize").orElse(Integer.MAX_VALUE);
        if (min > max) {
            return -1;
        }
        return Math.max(min, Math.min(max, preferred));
    }

    /**
     * The type arguments used for a generic type: the given ones, the defaults for wildcards and missing ones.
     *
     * @param definition the type
     * @param type       the type as used
     * @return the type argument of every type variable, empty if a default does not exist
     */
    public static Optional<Map<Type.TypeVariable, Type>> arguments(final TypeDefinition definition,
                                                           final Type.DeclaredType type) {
        final Optional<Map<Type.TypeVariable, Type>> defaults = defaults(definition.typeParameters());
        final Map<Type.TypeVariable, Type> result = new HashMap<>();
        final List<TypeParameterDefinition> parameters = definition.typeParameters();
        for (int i = 0; i < parameters.size(); i++) {
            final Type argument = i < type.arguments().size() ? type.arguments().get(i) : null;
            if (argument == null || argument instanceof Type.WildcardType) {
                if (defaults.isEmpty()) {
                    return Optional.empty();
                }
                result.put(parameters.get(i).variable(), defaults.get().get(parameters.get(i).variable()));
            } else {
                result.put(parameters.get(i).variable(), argument);
            }
        }
        return Optional.of(result);
    }

    /** Whether the default instance (e.g. of {@code HieroClient<ANY>}) can be used where the type is required. */
    public static boolean fitsInstance(final Type.DeclaredType instance, final Type.DeclaredType required) {
        for (int i = 0; i < required.arguments().size(); i++) {
            final Type argument = required.arguments().get(i);
            if (!(argument instanceof Type.WildcardType) && !(argument instanceof Type.AnyType)
                    && (i >= instance.arguments().size() || !argument.text().equals(instance.arguments().get(i)
                    .text()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The numeric argument of a {@code @@min}/{@code @@max}.
     *
     * @param annotations the annotations
     * @param name        {@code min} or {@code max}
     * @return the bound
     */
    public static Optional<BigDecimal> bound(final List<Annotation> annotations, final String name) {
        return annotations.stream().filter(a -> a.name().equals(name)).findFirst()
                .map(a -> a.arguments().getFirst())
                .filter(Literal.NumberLiteral.class::isInstance)
                .map(l -> ((Literal.NumberLiteral) l).value());
    }

    /**
     * The integer argument of a size or length annotation.
     *
     * @param annotations the annotations
     * @param name        e.g. {@code minSize}
     * @return the value
     */
    public static Optional<Integer> size(final List<Annotation> annotations, final String name) {
        return bound(annotations, name).map(BigDecimal::intValueExact);
    }

    /**
     * The string argument of an annotation.
     *
     * @param annotations the annotations
     * @param name        e.g. {@code pattern}
     * @return the value
     */
    public static Optional<String> stringArgument(final List<Annotation> annotations, final String name) {
        return annotations.stream().filter(a -> a.name().equals(name)).findFirst()
                .map(a -> a.arguments().getFirst())
                .map(l -> l instanceof Literal.StringLiteral string ? string.value() : l.text());
    }

    private static String suffix(final int variant) {
        return variant == 0 ? "" : String.valueOf(variant);
    }

    private static boolean containsVariable(final @Nullable Type type) {
        return switch (type) {
            case null -> false;
            case Type.TypeVariable ignored -> true;
            case Type.BasicType basic -> basic.arguments().stream().anyMatch(Constraints::containsVariable);
            case Type.DeclaredType declared -> declared.arguments().stream().anyMatch(Constraints::containsVariable);
            case Type.WildcardType wildcard -> containsVariable(wildcard.upperBound());
            case Type.FunctionType function -> containsVariable(function.returnType())
                    || function.parameters().stream().anyMatch(p -> containsVariable(p.type()));
            default -> false;
        };
    }
}
