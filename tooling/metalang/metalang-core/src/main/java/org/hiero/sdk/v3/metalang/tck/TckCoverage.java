package org.hiero.sdk.v3.metalang.tck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Compares the bindings with the JSON-RPC methods of the TCK test specifications: methods without binding, TCK
 * parameters and result fields that no binding uses (and that are not declared {@code unsupported}), and bound
 * parameters or result fields that the TCK does not have. Parameters of nested objects (the elements of an
 * {@code each}) are not compared; the TCK specifies them in shared files.
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
            final Optional<TckBindings.Resolved> binding = bindings.bindings().stream()
                    .filter(b -> b.binding().name().equals(method.name())).findFirst();
            if (RUNTIME_METHODS.contains(method.name())) {
                bound.add(method.name());
            } else if (binding.isEmpty()) {
                unbound.add(method.name());
            } else {
                bound.add(method.name());
                compare(method, binding.get(), findings);
            }
        }
        for (final TckBindings.Resolved binding : bindings.bindings()) {
            if (!methods.containsKey(binding.binding().name())) {
                findings.add("Method " + binding.binding().name() + " (" + binding.file()
                        + ") does not exist in the TCK");
            }
        }
        final TckSpecifications.Method common = methods.get(TckSpecifications.COMMON);
        if (common != null && bindings.common().isPresent()) {
            compare(common, bindings.common().get(), findings);
        }
        return new Report(bound, unbound, findings);
    }

    private static void compare(final TckSpecifications.Method method, final TckBindings.Resolved binding,
                                final List<String> findings) {
        final Set<String> inputs = new HashSet<>();
        final Set<String> outputs = new HashSet<>();
        final Set<String> unsupportedInputs = new HashSet<>();
        final Set<String> unsupportedOutputs = new HashSet<>();
        for (final Binding.Member member : binding.binding().members()) {
            switch (member) {
                case Binding.Assignment assignment -> assignment.sources().forEach(s -> inputs.add(s.path().first()));
                case Binding.Each each -> inputs.add(each.source().first());
                case Binding.Result result -> outputs.add(result.name().first());
                case Binding.Unsupported unsupported ->
                        (unsupported.result() ? unsupportedOutputs : unsupportedInputs).add(unsupported.name());
            }
        }
        final String name = method.name().equals(TckSpecifications.COMMON) ? "the common transaction parameters"
                : "method " + method.name();
        for (final String input : method.inputs()) {
            final boolean runtime = method.name().equals(TckSpecifications.COMMON) && RUNTIME_PARAMETERS.contains(input);
            if (!input.equals(COMMON_PARAMETER) && !runtime && !inputs.contains(input)
                    && !unsupportedInputs.contains(input)) {
                findings.add("Parameter " + input + " of " + name + " has no binding");
            }
        }
        for (final String output : method.outputs()) {
            if (!outputs.contains(output) && !unsupportedOutputs.contains(output)) {
                findings.add("Result " + output + " of " + name + " has no binding");
            }
        }
        final Set<String> known = new HashSet<>(method.inputs());
        for (final String input : union(inputs, unsupportedInputs)) {
            if (!known.contains(input)) {
                findings.add("Parameter " + input + " of " + name + " does not exist in the TCK");
            }
        }
        final Set<String> knownOutputs = new HashSet<>(method.outputs());
        for (final String output : union(outputs, unsupportedOutputs)) {
            if (!knownOutputs.contains(output)) {
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
