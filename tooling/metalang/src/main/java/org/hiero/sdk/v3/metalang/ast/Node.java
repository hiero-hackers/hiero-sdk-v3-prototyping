package org.hiero.sdk.v3.metalang.ast;

import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;

/**
 * Common super type of all AST nodes.
 */
public interface Node {

    /**
     * Returns the location of the node in the Markdown spec file.
     *
     * @return the source location
     */
    SourceLocation location();
}
