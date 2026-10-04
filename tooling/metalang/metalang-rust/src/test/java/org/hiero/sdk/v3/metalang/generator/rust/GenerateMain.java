package org.hiero.sdk.v3.metalang.generator.rust;

import java.nio.file.Path;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.generator.GeneratedOutput;
import org.hiero.sdk.v3.metalang.model.LinkedModel;

/** Generates the Rust API of specs into a directory; started in another JVM by the determinism test. */
final class GenerateMain {

    private GenerateMain() {
    }

    /**
     * Generates.
     *
     * @param args the spec directory and the output directory
     * @throws Exception if the output cannot be written
     */
    public static void main(final String[] args) throws Exception {
        GeneratedOutput.write(Path.of(args[1]), new RustGenerator().generate(LinkedModel.of(new MetaLang()
                .validate(Path.of(args[0])).model())), RustGenerator.MARKER);
    }
}
