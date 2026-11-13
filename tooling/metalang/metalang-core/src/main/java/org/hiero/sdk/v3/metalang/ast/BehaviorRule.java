package org.hiero.sdk.v3.metalang.ast;

import java.util.Objects;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * A behavior rule of a spec: a statement of the {@code ## Description} that starts with the marker
 * {@code `rule:<id>`} (see "Behavior rules" in {@code guidelines/api-guideline.md}). Tests reference it with
 * {@code @@rule("<id>")}.
 *
 * @param id       the id within the namespace of the spec, lowercase words joined by hyphens
 * @param location the location of the marker in the Markdown file
 */
public record BehaviorRule(String id, SourceLocation location) implements Node {

    /**
     * Creates a behavior rule.
     *
     * @param id       the id
     * @param location the location of the marker
     */
    public BehaviorRule {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(location, "location must not be null");
    }
}
