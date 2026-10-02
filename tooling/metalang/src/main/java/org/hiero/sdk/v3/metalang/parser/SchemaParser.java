package org.hiero.sdk.v3.metalang.parser;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.source.SchemaSource;

/**
 * Parses meta-language text into a {@link SchemaFile}.
 *
 * <p>If the text has syntax errors, no AST is produced; the result only contains the syntax
 * diagnostics. This keeps every later stage free of partial-tree special cases.
 */
public final class SchemaParser {

    /**
     * Parses the given schema.
     *
     * @param source the schema source
     * @return the parse result
     */
    public ParseResult parse(final SchemaSource source) {
        Objects.requireNonNull(source, "source must not be null");
        final SyntaxParser.Result syntax = SyntaxParser.parse(source);
        if (!syntax.diagnostics().isEmpty()) {
            return new ParseResult(null, syntax.diagnostics());
        }
        final SchemaFile file = new AstBuilder(source, syntax.tokens()).build(syntax.tree());
        return new ParseResult(file, List.of());
    }

    /**
     * Convenience method for parsing a plain meta-language text (mainly for tests).
     *
     * @param file the file name used in diagnostics
     * @param text the schema text
     * @return the parse result
     */
    public ParseResult parse(final String file, final String text) {
        return parse(SchemaSource.ofPlainText(file, text));
    }
}
