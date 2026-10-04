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
 * file       := (requires | binding)*
 * requires   := 'requires' '{' NAME (',' NAME)* '}' 'from' QNAME
 * binding    := ('binding' | 'common') NAME '-&gt;' QNAME '{' member* '}'
 * member     := 'result' path '=' path ':' NAME
 *             | 'unsupported' 'result'? NAME STRING
 *             | NAME '=' 'each' path '-&gt;' QNAME '{' member* '}'
 *             | NAME '=' source ('|' source)*
 * source     := path ':' NAME
 * path       := '^'? NAME ('.' NAME)*
 * </pre>
 *
 * <p>Comments ({@code //}) directly above a binding become its documentation. A syntax error is reported as
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
                    bindings.add(binding());
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

    private Binding binding() {
        final Token start = next();
        final String documentation = String.join("\n", start.comments());
        final String name = name();
        expect("->");
        final String type = qualifiedName();
        final List<Binding.Member> members = members();
        return new Binding(start.text().equals("common"), name, type, members, documentation, location(start));
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
        if (start.text().equals("result") && peekAt(1).kind() == Kind.NAME) {
            next();
            final Binding.Path name = path();
            expect("=");
            final Binding.Path value = path();
            expect(":");
            return new Binding.Result(name, value, name(), location(start));
        }
        if (start.text().equals("unsupported") && peekAt(1).kind() == Kind.NAME) {
            next();
            final boolean result = peek().text().equals("result") && peekAt(1).kind() == Kind.NAME && accept("result");
            final String name = name();
            final Token reason = next();
            if (reason.kind() != Kind.STRING) {
                throw new SyntaxError("expected the reason as string, found '" + reason.text() + "'", reason);
            }
            return new Binding.Unsupported(result, name, reason.text(), location(start));
        }
        final String attribute = name();
        expect("=");
        if (peek().text().equals("each") && peekAt(1).kind() == Kind.NAME) {
            next();
            final Binding.Path source = path();
            expect("->");
            final String type = qualifiedName();
            return new Binding.Each(attribute, source, type, members(), location(start));
        }
        final List<Binding.Source> sources = new ArrayList<>();
        do {
            final Binding.Path path = path();
            expect(":");
            sources.add(new Binding.Source(path, name()));
        } while (accept("|"));
        return new Binding.Assignment(attribute, sources, location(start));
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
