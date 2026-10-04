package org.hiero.sdk.v3.metalang.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.model.LinkedModel;
import org.junit.jupiter.api.Test;

/** The bindings of the repository ({@code tck/bindings}) resolve against the real specs without errors. */
class RepositoryBindingsTest {

    private static final Path SPECS = Path.of(System.getProperty("spec.root", "../../../spec"));

    @Test
    void theBindingsOfTheRepositoryShouldResolve() throws Exception {
        // WHEN
        final TckBindings.Bindings bindings = TckBindings.read(SPECS.resolveSibling("tck/bindings"),
                LinkedModel.of(new MetaLang().validate(SPECS).model()));

        // THEN
        assertThat(bindings.diagnostics()).isEmpty();
        assertThat(bindings.bindings()).extracting(b -> b.binding().name())
                .containsExactly("createAccount", "updateAccount", "deleteAccount", "transferCrypto", "getAccountInfo");
        assertThat(bindings.common()).isPresent();
    }
}
