package org.hiero.sdk.v3.metalang.parser;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.grammar.MetaLangLexer;
import org.hiero.sdk.v3.metalang.grammar.MetaLangParser;
import org.hiero.sdk.v3.metalang.source.SchemaSource;
import org.jspecify.annotations.Nullable;

/**
 * Thin wrapper around the ANTLR-generated lexer and parser. Produces the raw parse tree plus syntax
 * diagnostics whose positions are mapped back to the Markdown file.
 */
final class SyntaxParser {

    /**
     * Raw result of a syntax parse.
     *
     * @param tree        the parse tree (may be partial if there were errors, {@code null} if not parsed)
     * @param tokens      the token stream including hidden-channel comments
     * @param diagnostics syntax diagnostics
     */
    record Result<T extends ParserRuleContext>(@Nullable T tree, CommonTokenStream tokens,
                                               List<Diagnostic> diagnostics) {
    }

    private SyntaxParser() {
    }

    /**
     * Maximum bracket nesting ({@code < ( [ {}) accepted by the parser. The parser and the validator are recursive;
     * the limit keeps them far away from stack exhaustion. Real specs use fewer than 10 levels.
     */
    static final int MAX_NESTING = 100;

    private static Token firstTokenBeyondMaxNesting(final CommonTokenStream tokens) {
        int depth = 0;
        for (final Token token : tokens.getTokens()) {
            switch (token.getType()) {
                case MetaLangLexer.LT, MetaLangLexer.LPAREN, MetaLangLexer.LBRACK, MetaLangLexer.LBRACE -> {
                    depth++;
                    if (depth > MAX_NESTING) {
                        return token;
                    }
                }
                case MetaLangLexer.GT, MetaLangLexer.RPAREN, MetaLangLexer.RBRACK, MetaLangLexer.RBRACE ->
                        depth = Math.max(0, depth - 1);
                default -> {
                    // other tokens do not change the nesting
                }
            }
        }
        return null;
    }

    /**
     * Location for errors at the end of input: directly behind the last character of the last line of the schema.
     * ANTLR places the EOF token after the final line break, i.e. on a line that may not exist in the Markdown file.
     */
    private static SourceLocation endOfText(final SchemaSource source) {
        final String[] lines = source.text().split("\\R");
        if (lines.length == 0 || source.text().isBlank()) {
            // nothing to point at inside the schema: use the line in front of it (the opening fence), if any
            return new SourceLocation(source.file(), Math.max(1, source.lineOffset()), 1);
        }
        return new SourceLocation(source.file(), lines.length + source.lineOffset(),
                lines[lines.length - 1].length() + 1);
    }

    static Result<MetaLangParser.SchemaContext> parse(final SchemaSource source) {
        return parse(source, MetaLangParser::schema);
    }

    /**
     * Parses a source with the given start rule.
     *
     * @param source the source
     * @param start  the start rule, e.g. {@code MetaLangParser::instances}
     * @param <T>    the type of the parse tree
     * @return the parse result
     */
    static <T extends ParserRuleContext> Result<T> parse(final SchemaSource source,
                                                         final Function<MetaLangParser, T> start) {
        Objects.requireNonNull(source, "source must not be null");
        final DiagnosticCollector diagnostics = new DiagnosticCollector();
        final BaseErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol,
                                    final int line, final int charPositionInLine, final String msg,
                                    final RecognitionException e) {
                diagnostics.report(Rule.SYNTAX_ERROR, msg, offendingSymbol instanceof Token token
                        && token.getType() == Token.EOF
                        ? endOfText(source)
                        : new SourceLocation(source.file(), line + source.lineOffset(), charPositionInLine + 1));
            }
        };

        final MetaLangLexer lexer = new MetaLangLexer(CharStreams.fromString(source.text(), source.file()));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        final Token tooDeep = firstTokenBeyondMaxNesting(tokens);
        if (tooDeep != null) {
            diagnostics.report(Rule.SYNTAX_NESTING_TOO_DEEP, "Brackets are nested deeper than " + MAX_NESTING
                    + " levels; the input is not parsed", new SourceLocation(source.file(),
                    tooDeep.getLine() + source.lineOffset(), tooDeep.getCharPositionInLine() + 1));
            return new Result<>(null, tokens, diagnostics.sorted());
        }
        final MetaLangParser parser = new MetaLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        final T tree = start.apply(parser);
        return new Result<>(tree, tokens, diagnostics.sorted());
    }
}
