package org.hiero.sdk.v3.metalang.validation;

import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.semantic.SpecModel;

/**
 * A group of related validation rules.
 */
interface Check {

    /**
     * Runs the check against the whole model.
     *
     * @param model the model
     * @param out   collector for findings
     */
    void run(SpecModel model, DiagnosticCollector out);
}
