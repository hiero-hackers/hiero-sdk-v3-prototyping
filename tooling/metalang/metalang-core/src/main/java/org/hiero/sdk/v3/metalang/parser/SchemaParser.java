package org.hiero.sdk.v3.metalang.parser;

import java.util.List;
import java.util.Objects;
import org.hiero.sdk.v3.metalang.ast.Expression;
import org.hiero.sdk.v3.metalang.ast.Instance;
import org.hiero.sdk.v3.metalang.ast.SchemaFile;
import org.hiero.sdk.v3.metalang.ast.Statement;
import org.hiero.sdk.v3.metalang.ast.TestCase;
import org.hiero.sdk.v3.metalang.ast.TestValues;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.DiagnosticCollector;
import org.hiero.sdk.v3.metalang.diagnostic.Rule;
import org.hiero.sdk.v3.metalang.grammar.MetaLangParser;
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
        final SyntaxParser.Result<MetaLangParser.SchemaContext> syntax = SyntaxParser.parse(source);
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

    /**
     * The result of parsing a "## Default Instances for Testing" block.
     *
     * @param instances   the default instances (empty if the block has syntax errors)
     * @param values      the named test values (empty if the block has syntax errors)
     * @param diagnostics the syntax errors
     */
    public record InstancesResult(List<Instance> instances, List<TestValues> values, List<Diagnostic> diagnostics) {

        /**
         * Creates a result.
         *
         * @param instances   the default instances
         * @param values      the named test values
         * @param diagnostics the syntax errors
         */
        public InstancesResult {
            instances = List.copyOf(instances);
            values = List.copyOf(values);
            diagnostics = List.copyOf(diagnostics);
        }
    }

    /**
     * Parses the code block of a "## Default Instances for Testing" section.
     *
     * @param source the code block
     * @return the default instances and the syntax errors
     */
    public InstancesResult parseInstances(final SchemaSource source) {
        Objects.requireNonNull(source, "source must not be null");
        final SyntaxParser.Result<MetaLangParser.InstancesContext> syntax = SyntaxParser.parse(source,
                MetaLangParser::instances);
        if (!syntax.diagnostics().isEmpty()) {
            return new InstancesResult(List.of(), List.of(), syntax.diagnostics());
        }
        final AstBuilder builder = new AstBuilder(source, syntax.tokens());
        return new InstancesResult(builder.instances(syntax.tree()), builder.values(syntax.tree()), List.of());
    }

    /**
     * The result of parsing a {@code tests} block.
     *
     * @param tests       the tests (empty if the block has syntax errors)
     * @param diagnostics the syntax errors
     */
    public record TestsResult(List<TestCase> tests, List<Diagnostic> diagnostics) {

        /**
         * Creates a result.
         *
         * @param tests       the tests
         * @param diagnostics the syntax errors
         */
        public TestsResult {
            tests = List.copyOf(tests);
            diagnostics = List.copyOf(diagnostics);
        }
    }

    /**
     * Parses a {@code tests} code block of a "## Testing" section.
     *
     * @param source the code block
     * @return the tests and the syntax errors
     */
    public TestsResult parseTests(final SchemaSource source) {
        Objects.requireNonNull(source, "source must not be null");
        final SyntaxParser.Result<MetaLangParser.TestsContext> syntax = SyntaxParser.parse(source,
                MetaLangParser::tests);
        if (!syntax.diagnostics().isEmpty()) {
            return new TestsResult(List.of(), syntax.diagnostics());
        }
        final List<TestCase> tests = new AstBuilder(source, syntax.tokens()).tests(syntax.tree());
        final DiagnosticCollector notCalls = new DiagnosticCollector();
        tests.forEach(t -> requireCalls(t.body(), notCalls));
        if (!notCalls.sorted().isEmpty()) {
            return new TestsResult(List.of(), notCalls.sorted());
        }
        return new TestsResult(tests, List.of());
    }

    /**
     * An expression statement must be a call. Statements are not separated by a token and the keywords are valid
     * names, so without this check a broken statement such as {@code let x 1} would be read as the three statements
     * {@code let}, {@code x} and {@code 1}.
     */
    private static void requireCalls(final List<Statement> statements, final DiagnosticCollector out) {
        for (final Statement statement : statements) {
            switch (statement) {
                case Statement.Evaluate evaluate when !(evaluate.expression() instanceof Expression.Call
                        || evaluate.expression() instanceof Expression.MethodCall) ->
                        out.report(Rule.SYNTAX_ERROR, "A statement must be 'let', 'if', 'for' or a call",
                                evaluate.location());
                case Statement.If conditional -> {
                    requireCalls(conditional.then(), out);
                    requireCalls(conditional.otherwise(), out);
                }
                case Statement.For loop -> requireCalls(loop.body(), out);
                default -> {
                    // a binding or a call
                }
            }
        }
    }
}
