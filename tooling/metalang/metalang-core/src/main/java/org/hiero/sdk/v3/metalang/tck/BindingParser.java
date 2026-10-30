package org.hiero.sdk.v3.metalang.tck;

import java.util.ArrayList;
import java.util.List;
import org.hiero.sdk.v3.metalang.diagnostic.Diagnostic;
import org.hiero.sdk.v3.metalang.diagnostic.Severity;
import org.hiero.sdk.v3.metalang.diagnostic.SourceLocation;
import org.hiero.sdk.v3.metalang.source.MarkdownSchemaExtractor;

/**
 * Reads the {@code bindings} code blocks of a Markdown file:
 *
 * <pre>
 * file         := (requires | binding)*
 * requires     := 'requires' '{' NAME (',' NAME)* '}' 'from' QNAME
 * binding      := ('binding' | 'common') NAME (construction | '-&gt;' QNAME '::' NAME body)
 * construction := '-&gt;' QNAME body
 *               | ('when' path '-&gt;' QNAME body)+ ('else' '-&gt;' QNAME body)?
 * body         := '{' member* '}'
 * member       := 'undocumented'? 'result' path '=' path ':' NAME
 *               | 'result' path '=' 'each' path body
 *               | 'take' path
 *               | 'unsupported' 'result'? NAME STRING ('unless' ('true' | 'false' | STRING))?
 *               | NAME '=' 'each' generator (',' generator)* '-&gt;' QNAME body
 *               | NAME '=' 'request' path
 *               | NAME '=' path? construction
 *               | NAME '=' source ('|' source)*
 * generator    := path | NAME 'in' path '?'?
 * source       := (path | STRING) ':' NAME
 * path         := '^'? NAME ('.' NAME)*
 * </pre>
 *
 * <p>A binding with variants ({@code when ... else ...}) becomes one {@link Binding} per variant, all with the same
 * name and documentation. Comments ({@code //}) directly above a binding become its documentation. A syntax error is reported as
 * diagnostic {@code tck.syntax} and ends the code block.
 */
public final class BindingParser {

    /** The info string of the code blocks that contain bindings. */
    public static final String INFO = "bindings";

    /**
     * The content of a bindings file.
     *
     * @param requires    the imported types: simple name to qualified name
     * @param bindings    the bindings in file order
     * @param diagnostics the syntax errors
     */
    public record Result(List<Requires> requires, List<Binding> bindings, List<Diagnostic> diagnostics) {
    }

    /**
     * An import: {@code requires {A, B} from namespace}.
     *
     * @param names     the simple names
     * @param namespace the namespace
     * @param location  the location
     */
    public record Requires(List<String> names, String namespace, SourceLocation location) {
    }

    /** A token with the comment lines directly before it. */
    private record Token(String text, Kind kind, int line, int column, List<String> comments) {
    }

    private enum Kind { NAME, STRING, SYMBOL, END }

    private static final class SyntaxError extends RuntimeException {

        private final transient Token token;

        SyntaxError(final String message, final Token token) {
            super(message);
            this.token = token;
        }
    }

    private final String file;
    private final List<Requires> requires = new ArrayList<>();
    private final List<Binding> bindings = new ArrayList<>();
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private List<Token> tokens = List.of();
    private int position;

    private BindingParser(final String file) {
        this.file = file;
    }

    /**
     * Parses the bindings of a Markdown file.
     *
     * @param file     the file name, for locations
     * @param markdown the content
     * @return the bindings and the syntax errors
     */
    public static Result parse(final String file, final String markdown) {
        final BindingParser parser = new BindingParser(file);
        for (final MarkdownSchemaExtractor.CodeBlock block : new MarkdownSchemaExtractor().codeBlocks(markdown)) {
            if (block.info().strip().equals(INFO)) {
                parser.block(block.content(), block.firstContentLine());
            }
        }
        return new Result(List.copyOf(parser.requires), List.copyOf(parser.bindings),
                List.copyOf(parser.diagnostics));
    }

    private void block(final String content, final int firstLine) {
        tokens = tokenize(content, firstLine);
        position = 0;
        try {
            while (peek().kind() != Kind.END) {
                if (peek().text().equals("requires")) {
                    requires();
                } else if (peek().text().equals("binding") || peek().text().equals("common")) {
                    bindings.addAll(binding());
                } else {
                    throw new SyntaxError("expected 'requires', 'binding' or 'common', found '" + peek().text() + "'",
                            peek());
                }
            }
        } catch (final SyntaxError e) {
            diagnostics.add(new Diagnostic(Severity.ERROR, "tck.syntax", e.getMessage(), location(e.token)));
        }
    }

    private void requires() {
        final Token start = next();
        expect("{");
        final List<String> names = new ArrayList<>();
        names.add(name());
        while (accept(",")) {
            names.add(name());
        }
        expect("}");
        expectName("from");
        requires.add(new Requires(names, qualifiedName(), location(start)));
    }

    private List<Binding> binding() {
        final Token start = next();
        final String documentation = String.join("\n", start.comments());
        final String name = name();
        final boolean common = start.text().equals("common");
        if (methodAhead()) {
            expect("->");
            final String type = qualifiedName();
            expect(":");
            expect(":");
            final String method = name();
            return List.of(new Binding(common, name, type, method, null, members(), documentation, location(start)));
        }
        final List<Binding> result = new ArrayList<>();
        for (final Binding.Variant variant : construction()) {
            result.add(new Binding(common, name, variant.type(), variant.when(), variant.members(), documentation,
                    result.isEmpty() ? location(start) : variant.location()));
        }
        return result;
    }

    /** Whether a call of a method follows: {@code -> Type::method} (the type possibly qualified). */
    private boolean methodAhead() {
        if (!peek().text().equals("->") || peek().kind() != Kind.SYMBOL) {
            return false;
        }
        int offset = 1;
        while (peekAt(offset).kind() == Kind.NAME && peekAt(offset + 1).text().equals(".")) {
            offset += 2;
        }
        return peekAt(offset).kind() == Kind.NAME && peekAt(offset + 1).text().equals(":")
                && peekAt(offset + 2).text().equals(":");
    }

    /** {@code -> Type { ... }} or the variants {@code when a -> A { ... } else -> B { ... }}. */
    private List<Binding.Variant> construction() {
        final List<Binding.Variant> variants = new ArrayList<>();
        if (!peek().text().equals("when") || peek().kind() != Kind.NAME) {
            final Token start = peek();
            expect("->");
            final String type = qualifiedName();
            variants.add(new Binding.Variant(null, type, members(), location(start)));
            return variants;
        }
        while (peek().text().equals("when") && peek().kind() == Kind.NAME) {
            final Token start = next();
            final Binding.Path when = path();
            expect("->");
            final String type = qualifiedName();
            variants.add(new Binding.Variant(when, type, members(), location(start)));
        }
        if (peek().text().equals("else") && peek().kind() == Kind.NAME) {
            final Token start = next();
            expect("->");
            final String type = qualifiedName();
            variants.add(new Binding.Variant(null, type, members(), location(start)));
        }
        return variants;
    }

    private List<Binding.Member> members() {
        expect("{");
        final List<Binding.Member> members = new ArrayList<>();
        while (!accept("}")) {
            members.add(member());
        }
        return members;
    }

    private Binding.Member member() {
        final Token start = peek();
        final boolean undocumented = start.text().equals("undocumented") && peekAt(1).text().equals("result")
                && peekAt(2).kind() == Kind.NAME;
        if (undocumented || start.text().equals("result") && peekAt(1).kind() == Kind.NAME) {
            if (undocumented) {
                next();
            }
            next();
            final Binding.Path name = path();
            expect("=");
            if (!undocumented && keyword("each")) {
                next();
                final Binding.Path value = path();
                return new Binding.ResultList(name, value, members(), location(start));
            }
            final Binding.Path value = path();
            expect(":");
            return new Binding.Result(name, value, name(), undocumented, location(start));
        }
        if (start.text().equals("take") && peekAt(1).kind() == Kind.NAME) {
            next();
            return new Binding.Take(path(), location(start));
        }
        if (start.text().equals("unsupported") && peekAt(1).kind() == Kind.NAME) {
            next();
            final boolean result = peek().text().equals("result") && peekAt(1).kind() == Kind.NAME && accept("result");
            final String name = name();
            final Token reason = next();
            if (reason.kind() != Kind.STRING) {
                throw new SyntaxError("expected the reason as string, found '" + reason.text() + "'", reason);
            }
            Object unless = null;
            if (!result && peek().text().equals("unless") && peek().kind() == Kind.NAME) {
                next();
                final Token value = next();
                if (value.kind() == Kind.STRING) {
                    unless = value.text();
                } else if (value.text().equals("true") || value.text().equals("false")) {
                    unless = Boolean.valueOf(value.text());
                } else {
                    throw new SyntaxError("expected true, false or a string after 'unless', found '" + value.text()
                            + "'", value);
                }
            }
            return new Binding.Unsupported(result, name, reason.text(), unless, location(start));
        }
        final String attribute = name();
        expect("=");
        if (keyword("each")) {
            next();
            final List<Binding.Generator> generators = new ArrayList<>();
            do {
                generators.add(generator());
            } while (accept(","));
            expect("->");
            final String type = qualifiedName();
            return new Binding.Each(attribute, generators, type, members(), location(start));
        }
        if (keyword("request")) {
            next();
            return new Binding.Request(attribute, path(), location(start));
        }
        if (keyword("when") || peek().text().equals("->") && peek().kind() == Kind.SYMBOL) {
            return new Binding.Construct(attribute, null, construction(), location(start));
        }
        final List<Binding.Source> sources = new ArrayList<>();
        do {
            if (peek().kind() == Kind.STRING) {
                final String literal = next().text();
                expect(":");
                sources.add(new Binding.Source(null, literal, name()));
                continue;
            }
            final Binding.Path path = path();
            if (sources.isEmpty() && (keyword("when") || peek().text().equals("->") && peek().kind() == Kind.SYMBOL)) {
                return new Binding.Construct(attribute, path, construction(), location(start));
            }
            expect(":");
            sources.add(new Binding.Source(path, name()));
        } while (accept("|"));
        return new Binding.Assignment(attribute, sources, location(start));
    }

    /** {@code list.object} or {@code name in list} or {@code name in list?}. */
    private Binding.Generator generator() {
        if (peek().kind() == Kind.NAME && peekAt(1).kind() == Kind.NAME && peekAt(1).text().equals("in")) {
            final String name = name();
            next();
            final Binding.Path path = path();
            return new Binding.Generator(name, path, accept("?"));
        }
        return new Binding.Generator(null, path(), false);
    }

    /**
     * Whether the next token is a keyword: the name followed by another name or by {@code ^} (not by {@code :} or
     * {@code .}).
     */
    private boolean keyword(final String keyword) {
        return peek().text().equals(keyword) && peek().kind() == Kind.NAME && (peekAt(1).kind() == Kind.NAME
                || peekAt(1).kind() == Kind.SYMBOL && peekAt(1).text().equals("^"));
    }

    private Binding.Path path() {
        final boolean parent = accept("^");
        final List<String> segments = new ArrayList<>();
        segments.add(name());
        while (accept(".")) {
            segments.add(name());
        }
        return new Binding.Path(parent, segments);
    }

    private String qualifiedName() {
        final StringBuilder name = new StringBuilder(name());
        while (accept(".")) {
            name.append('.').append(name());
        }
        return name.toString();
    }

    private String name() {
        final Token token = next();
        if (token.kind() != Kind.NAME) {
            throw new SyntaxError("expected a name, found '" + token.text() + "'", token);
        }
        return token.text();
    }

    private void expectName(final String name) {
        final Token token = next();
        if (!token.text().equals(name) || token.kind() != Kind.NAME) {
            throw new SyntaxError("expected '" + name + "', found '" + token.text() + "'", token);
        }
    }

    private void expect(final String symbol) {
        final Token token = next();
        if (token.kind() != Kind.SYMBOL || !token.text().equals(symbol)) {
            throw new SyntaxError("expected '" + symbol + "', found '" + token.text() + "'", token);
        }
    }

    private boolean accept(final String text) {
        if (peek().text().equals(text) && peek().kind() != Kind.STRING) {
            position++;
            return true;
        }
        return false;
    }

    private Token peek() {
        return peekAt(0);
    }

    private Token peekAt(final int offset) {
        return tokens.get(Math.min(position + offset, tokens.size() - 1));
    }

    private Token next() {
        final Token token = peek();
        if (token.kind() != Kind.END) {
            position++;
        }
        return token;
    }

    private SourceLocation location(final Token token) {
        return new SourceLocation(file, token.line(), token.column());
    }

    /** Splits a code block into tokens; comment lines are attached to the token that follows them. */
    private static List<Token> tokenize(final String content, final int firstLine) {
        final List<Token> result = new ArrayList<>();
        final List<String> comments = new ArrayList<>();
        final List<String> lines = content.lines().toList();
        for (int index = 0; index < lines.size(); index++) {
            final String line = lines.get(index);
            final int number = firstLine + index;
            int i = 0;
            while (i < line.length()) {
                final char c = line.charAt(i);
                final int column = i + 1;
                final String text;
                Kind kind = Kind.SYMBOL;
                if (Character.isWhitespace(c)) {
                    i++;
                    continue;
                } else if (line.startsWith("//", i)) {
                    comments.add(line.substring(i + 2).strip());
                    break;
                } else if (Character.isLetter(c) || c == '_') {
                    final int start = i;
                    while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                        i++;
                    }
                    text = line.substring(start, i);
                    kind = Kind.NAME;
                } else if (c == '"') {
                    final int end = line.indexOf('"', i + 1);
                    text = end < 0 ? "unterminated string" : line.substring(i + 1, end);
                    kind = end < 0 ? Kind.SYMBOL : Kind.STRING;
                    i = end < 0 ? line.length() : end + 1;
                } else if (line.startsWith("->", i)) {
                    text = "->";
                    i += 2;
                } else {
                    text = String.valueOf(c);
                    i++;
                }
                result.add(new Token(text, kind, number, column, List.copyOf(comments)));
                comments.clear();
            }
        }
        result.add(new Token("end of block", Kind.END, firstLine + lines.size(), 1, List.of()));
        return result;
    }
}
