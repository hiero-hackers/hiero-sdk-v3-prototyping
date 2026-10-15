package org.hiero.sdk.v3.metalang.validation;

import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;

/**
 * Reports what the linker found while resolving the tests of the specs (the {@code test.*} rules, see
 * {@code guidelines/test-language.md}).
 */
final class TestCheck implements Check {

    @Override
    public void run(final ValidationContext context, final DiagnosticCollector out) {
        out.addAll(context.linked().testDiagnostics());
    }
}
