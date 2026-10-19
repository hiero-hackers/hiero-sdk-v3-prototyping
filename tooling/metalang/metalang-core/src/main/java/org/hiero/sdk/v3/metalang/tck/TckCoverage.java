package org.hiero.sdk.v3.metalang.tck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Compares the bindings with the JSON-RPC methods of the TCK test specifications: methods without binding, TCK
 * parameters and result fields that no binding uses (and that are not declared {@code unsupported}), and bound
 * parameters or result fields that the TCK does not have. Parameters of nested objects (the elements of an
 * {@code each} over objects, a nested object with a path) are not compared; the TCK specifies them in shared files.
 * The variants of a method are compared together. Of the alternative sources of an attribute
 * ({@code a : c | b : c}) only one has to exist in the TCK: the others may be spellings the TCK tests send although
 * its specification names another one.
 */
public final class TckCoverage {

    /** The methods that every TCK server runtime implements itself (no binding). */
    public static final Set<String> RUNTIME_METHODS = Set.of("setup", "reset", "generateKey");

    /**
     * The common transaction parameters the runtime handles itself: the additional {@code signers} change how a
     * transaction is signed, not the transaction.
     */
    public static final Set<String> RUNTIME_PARAMETERS = Set.of("signers");

    /** The parameter of every transaction method that holds the common transaction parameters. */
    public static final String COMMON_PARAMETER = TckSpecifications.COMMON;

    /**
     * The result of a comparison.
     *
     * @param bound    the TCK methods with binding (or provided by the runtime)
     * @param unbound  the TCK methods without binding, sorted
     * @param findings the differences of the bound methods, sorted
     */
    public record Report(List<String> bound, List<String> unbound, List<String> findings) {

        /**
         * Creates a report.
         *
         * @param bound    the bound methods
         * @param unbound  the unbound methods
         * @param findings the findings
         */
        public Report {
            bound = bound.stream().sorted().toList();
            unbound = unbound.stream().sorted().toList();
            findings = findings.stream().sorted().toList();
        }
    }

    private TckCoverage() {
    }

    /**
     * Compares the bindings with the TCK.
     *
     * @param bindings the resolved bindings
     * @param methods  the methods of the TCK specifications by name
     * @return the report
     */
    public static Report check(final TckBindings.Bindings bindings, final Map<String, TckSpecifications.Method> methods) {
        final List<String> bound = new ArrayList<>();
        final List<String> unbound = new ArrayList<>();
        final List<String> findings = new ArrayList<>();
        for (final TckSpecifications.Method method : methods.values()) {
            if (method.name().equals(TckSpecifications.COMMON)) {
                continue;
            }
            final List<TckBindings.Resolved> variants = bindings.bindings().stream()
                    .filter(b -> b.binding().name().equals(method.name())).toList();
            if (RUNTIME_METHODS.contains(method.name())) {
                bound.add(method.name());
            } else if (variants.isEmpty()) {
                unbound.add(method.name());
            } else {
                bound.add(method.name());
                compare(method, variants, findings);
            }
        }
        final Set<String> reported = new HashSet<>();
        for (final TckBindings.Resolved binding : bindings.bindings()) {
            if (!methods.containsKey(binding.binding().name()) && reported.add(binding.binding().name())) {
                findings.add("Method " + binding.binding().name() + " (" + binding.file()
                        + ") does not exist in the TCK");
            }
        }
        final TckSpecifications.Method common = methods.get(TckSpecifications.COMMON);
        if (common != null && bindings.common().isPresent()) {
            compare(common, List.of(bindings.common().get()), findings);
        }
        return new Report(bound, unbound, findings);
    }

    /** The parameters and result fields a binding uses. */
    private static final class Usage {
        private final Set<String> inputs = new HashSet<>();
        private final List<Set<String>> alternatives = new ArrayList<>();
        private final Set<String> outputs = new HashSet<>();
        private final Set<String> unsupportedInputs = new HashSet<>();
        private final Set<String> unsupportedOutputs = new HashSet<>();
        /** Results the TCK tests read although the test specification does not name them. */
        private final Set<String> undocumentedOutputs = new HashSet<>();

        void input(final Set<String> names) {
            if (!names.isEmpty()) {
                inputs.addAll(names);
                alternatives.add(names);
            }
        }

        /**
         * Collects the members.
         *
         * @param members the members
         * @param top     whether they read the JSON parameters of the method
         * @param names   the names bound by {@code each name in list}
         */
        void collect(final List<Binding.Member> members, final boolean top, final Set<String> names) {
            for (final Binding.Member member : members) {
                switch (member) {
                    case Binding.Assignment assignment -> {
                        final Set<String> sources = new LinkedHashSet<>();
                        assignment.sources().forEach(s -> {
                            if (s.path() != null) {
                                parameter(s.path(), top, names).ifPresent(sources::add);
                            }
                        });
                        input(sources);
                    }
                    case Binding.Each each -> {
                        boolean inner = top;
                        final Set<String> bound = new HashSet<>(names);
                        for (final Binding.Generator generator : each.generators()) {
                            parameter(generator.path(), inner, bound).ifPresent(p -> input(Set.of(p)));
                            if (generator.name() == null) {
                                inner = false;
                            } else {
                                bound.add(generator.name());
                            }
                        }
                        collect(each.members(), inner, bound);
                    }
                    case Binding.Construct construct -> {
                        if (construct.path() != null) {
                            parameter(construct.path(), top, names).ifPresent(p -> input(Set.of(p)));
                        }
                        final boolean inner = top && construct.path() == null;
                        for (final Binding.Variant variant : construct.variants()) {
                            if (variant.when() != null) {
                                parameter(variant.when(), inner, names).ifPresent(p -> input(Set.of(p)));
                            }
                            collect(variant.members(), inner, names);
                        }
                    }
                    case Binding.Request request ->
                            parameter(request.path(), top, names).ifPresent(p -> input(Set.of(p)));
                    case Binding.Result result -> {
                        outputs.add(result.name().first());
                        if (result.undocumented()) {
                            undocumentedOutputs.add(result.name().first());
                        }
                    }
                    case Binding.Unsupported unsupported -> {
                        if (unsupported.result()) {
                            unsupportedOutputs.add(unsupported.name());
                        } else if (top) {
                            unsupportedInputs.add(unsupported.name());
                        }
                    }
                }
            }
        }

        private static Optional<String> parameter(final Binding.Path path, final boolean top,
                                                  final Set<String> names) {
            return top && !path.parent() && !names.contains(path.first()) ? Optional.of(path.first())
                    : Optional.empty();
        }
    }

    private static void compare(final TckSpecifications.Method method, final List<TckBindings.Resolved> variants,
                                final List<String> findings) {
        final Usage usage = new Usage();
        for (final TckBindings.Resolved variant : variants) {
            if (variant.binding().when() != null) {
                Usage.parameter(variant.binding().when(), true, Set.of()).ifPresent(p -> usage.input(Set.of(p)));
            }
            usage.collect(variant.binding().members(), true, Set.of());
        }
        final String name = method.name().equals(TckSpecifications.COMMON) ? "the common transaction parameters"
                : "method " + method.name();
        for (final String input : method.inputs()) {
            final boolean runtime = method.name().equals(TckSpecifications.COMMON) && RUNTIME_PARAMETERS.contains(input);
            if (!input.equals(COMMON_PARAMETER) && !runtime && !usage.inputs.contains(input)
                    && !usage.unsupportedInputs.contains(input)) {
                findings.add("Parameter " + input + " of " + name + " has no binding");
            }
        }
        for (final String output : method.outputs()) {
            if (!usage.outputs.contains(output) && !usage.unsupportedOutputs.contains(output)) {
                findings.add("Result " + output + " of " + name + " has no binding");
            }
        }
        final Set<String> known = new HashSet<>(method.inputs());
        final Set<String> unknown = new java.util.TreeSet<>();
        for (final Set<String> alternatives : usage.alternatives) {
            if (alternatives.stream().noneMatch(known::contains)) {
                unknown.addAll(alternatives);
            }
        }
        usage.unsupportedInputs.stream().filter(i -> !known.contains(i)).forEach(unknown::add);
        unknown.forEach(input -> findings.add("Parameter " + input + " of " + name + " does not exist in the TCK"));
        final Set<String> knownOutputs = new HashSet<>(method.outputs());
        for (final String output : union(usage.outputs, usage.unsupportedOutputs)) {
            if (!knownOutputs.contains(output) && !usage.undocumentedOutputs.contains(output)) {
                findings.add("Result " + output + " of " + name + " does not exist in the TCK");
            }
        }
    }

    private static Set<String> union(final Set<String> first, final Set<String> second) {
        final Set<String> result = new HashSet<>(first);
        result.addAll(second);
        return result;
    }
}
