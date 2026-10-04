package org.hiero.sdk.v3.metalang.tck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Reads the JSON-RPC methods of the TCK test specifications ({@code docs/test-specifications/**.md} of
 * {@code hiero-sdk-tck}): the method name (after {@code ### Method Name}, or a heading {@code ### `name`}) and the
 * names of its input and output parameters (the first column of the tables after {@code Input Parameters} and
 * {@code Output Parameters}). The common transaction parameters are read as method
 * {@value #COMMON} from the table of {@code CommonTransactionParameters.md}.
 */
public final class TckSpecifications {

    /** The name under which the common transaction parameters are read. */
    public static final String COMMON = "commonTransactionParams";

    private static final Pattern METHOD_HEADING = Pattern.compile("^#{2,4}\\s+`(\\w+)`\\s*$");
    private static final Pattern BACKTICK_NAME = Pattern.compile("^`(\\w+)`\\s*$");

    /**
     * A JSON-RPC method of the TCK.
     *
     * @param name    the method
     * @param file    the specification file, relative to the specifications directory
     * @param inputs  the input parameters in table order
     * @param outputs the output parameters in table order
     */
    public record Method(String name, String file, List<String> inputs, List<String> outputs) {

        /**
         * Creates a method.
         *
         * @param name    the method
         * @param file    the file
         * @param inputs  the inputs
         * @param outputs the outputs
         */
        public Method {
            inputs = List.copyOf(inputs);
            outputs = List.copyOf(outputs);
        }
    }

    private TckSpecifications() {
    }

    /**
     * Reads all methods of a specifications directory.
     *
     * @param directory {@code docs/test-specifications} of the TCK
     * @return the methods by name (the first specification of a name wins)
     * @throws IOException if a file cannot be read
     */
    public static Map<String, Method> read(final Path directory) throws IOException {
        final Map<String, Method> methods = new TreeMap<>();
        try (Stream<Path> paths = Files.walk(directory)) {
            for (final Path path : paths.filter(p -> p.toString().endsWith(".md")).sorted().toList()) {
                final String file = directory.relativize(path).toString().replace('\\', '/');
                parse(file, Files.readString(path, StandardCharsets.UTF_8))
                        .forEach(m -> methods.putIfAbsent(m.name(), m));
            }
        }
        return methods;
    }

    /**
     * Reads the methods of one specification file.
     *
     * @param file     the file name
     * @param markdown the content
     * @return the methods in file order
     */
    public static List<Method> parse(final String file, final String markdown) {
        final List<String> lines = markdown.lines().toList();
        final List<Method> methods = new ArrayList<>();
        final boolean common = file.endsWith("CommonTransactionParameters.md");
        String name = common ? COMMON : null;
        Set<String> inputs = new LinkedHashSet<>();
        Set<String> outputs = new LinkedHashSet<>();
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i).strip();
            final Matcher heading = METHOD_HEADING.matcher(line);
            final String found = heading.matches() ? heading.group(1)
                    : line.matches("^#{2,4}\\s+Method Name\\s*$") ? nextName(lines, i) : null;
            if (found != null && !common) {
                add(methods, file, name, inputs, outputs);
                name = found;
                inputs = new LinkedHashSet<>();
                outputs = new LinkedHashSet<>();
            } else if (line.startsWith("#") && (line.contains("Input Parameters")
                    || common && line.contains("Parameter Object Definition"))) {
                inputs.addAll(table(lines, i + 1));
            } else if (line.startsWith("#") && line.contains("Output Parameters")) {
                outputs.addAll(table(lines, i + 1));
            }
        }
        add(methods, file, name, inputs, outputs);
        return methods;
    }

    private static void add(final List<Method> methods, final String file, final String name,
                            final Set<String> inputs, final Set<String> outputs) {
        if (name != null) {
            methods.add(new Method(name, file, List.copyOf(inputs), List.copyOf(outputs)));
        }
    }

    private static String nextName(final List<String> lines, final int index) {
        for (int i = index + 1; i < lines.size(); i++) {
            final String line = lines.get(i).strip();
            if (!line.isEmpty()) {
                final Matcher matcher = BACKTICK_NAME.matcher(line);
                return matcher.matches() ? matcher.group(1) : null;
            }
        }
        return null;
    }

    /** The first column of the table that follows (after blank lines), without header and separator. */
    private static List<String> table(final List<String> lines, final int start) {
        final List<String> names = new ArrayList<>();
        int i = start;
        while (i < lines.size() && lines.get(i).isBlank()) {
            i++;
        }
        int row = 0;
        for (; i < lines.size() && lines.get(i).strip().startsWith("|"); i++, row++) {
            if (row < 2) {
                continue; // header and separator
            }
            final String cell = lines.get(i).strip().substring(1).split("\\|", -1)[0].strip().replace("`", "");
            if (!cell.isEmpty()) {
                names.add(cell);
            }
        }
        return names;
    }
}
