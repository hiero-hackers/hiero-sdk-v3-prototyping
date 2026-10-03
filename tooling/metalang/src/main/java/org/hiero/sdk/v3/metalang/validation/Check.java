package org.hiero.sdk.v3.metalang.validation;

import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;

/**
 * A group of related validation rules.
 */
interface Check {

    /**
     * Runs the check against the whole model.
     *
     * @param context the models to check
     * @param out     collector for findings
     */
    void run(ValidationContext context, DiagnosticCollector out);
}
