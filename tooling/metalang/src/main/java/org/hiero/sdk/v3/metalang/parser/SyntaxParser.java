package org.hiero.sdk.v3.metalang.parser;

import java.util.List;
import java.util.Objects;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.grammar.MetaLangLexer;
import org.hiero.sdk.v3.metalang.grammar.MetaLangParser;
import org.hiero.sdk.v3.metalang.source.SchemaSource;

/**
 * Thin wrapper around the ANTLR-generated lexer and parser. Produces the raw parse tree plus syntax
 * diagnostics whose positions are mapped back to the Markdown file.
 */
final class SyntaxParser {

    /**
     * Raw result of a syntax parse.
     *
     * @param tree        the parse tree (may be partial if there were errors)
     * @param tokens      the token stream including hidden-channel comments
     * @param diagnostics syntax diagnostics
     */
    record Result(MetaLangParser.SchemaContext tree, CommonTokenStream tokens, List<Diagnostic> diagnostics) {
    }

    private SyntaxParser() {
    }

    static Result parse(final SchemaSource source) {
        Objects.requireNonNull(source, "source must not be null");
        final DiagnosticCollector diagnostics = new DiagnosticCollector();
        final BaseErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
                                    final int line, final int charPositionInLine, final String msg,
                                    final RecognitionException e) {
                diagnostics.report(Rule.SYNTAX_ERROR, msg,
                        new SourceLocation(source.file(), line + source.lineOffset(), charPositionInLine + 1));
            }
        };

        final MetaLangLexer lexer = new MetaLangLexer(CharStreams.fromString(source.text(), source.file()));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final MetaLangParser parser = new MetaLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        final MetaLangParser.SchemaContext tree = parser.schema();
        return new Result(tree, tokens, diagnostics.sorted());
    }
}
