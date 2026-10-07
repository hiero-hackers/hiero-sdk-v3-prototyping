package org.hiero.sdk.v3.metalang.generator.go;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GoImportsTest {

    private static final String SELF = "github.com/acme/sdk/ledger";

    @Test
    void shouldNotQualifyAReferenceToItsOwnPackage() {
        final GoImports imports = new GoImports(SELF);

        assertThat(imports.qualify(SELF, "AccountID")).isEqualTo("AccountID");
        assertThat(imports.render()).isEmpty();
    }

    @Test
    void shouldQualifyByTheLastSegmentOfTheImportPath() {
        final GoImports imports = new GoImports(SELF);

        assertThat(imports.qualify("time", "Time")).isEqualTo("time.Time");
        assertThat(imports.qualify("github.com/acme/sdk/nativetoken", "Hbar"))
                .isEqualTo("nativetoken.Hbar");
        assertThat(imports.render()).isEqualTo("""
                import (
                \t"time"

                \t"github.com/acme/sdk/nativetoken"
                )

                """);
    }

    @Test
    void shouldRecordAPathOnlyOnce() {
        final GoImports imports = new GoImports(SELF);

        assertThat(imports.qualify("time", "Time")).isEqualTo("time.Time");
        assertThat(imports.qualify("time", "Duration")).isEqualTo("time.Duration");
        assertThat(imports.use("time")).isEqualTo("time");
        assertThat(imports.render()).isEqualTo("import (\n\t\"time\"\n)\n\n");
    }

    @Test
    void shouldAliasASecondPathThatEndsInTheSameSegment() {
        // GIVEN two packages both called `config`
        final GoImports imports = new GoImports(SELF);

        // WHEN
        final String first = imports.qualify("github.com/acme/sdk/ledger/config", "Setting");
        final String second = imports.qualify("github.com/acme/sdk/token/config", "Setting");

        // THEN the second one is aliased, and the alias is written out in the import block
        assertThat(first).isEqualTo("config.Setting");
        assertThat(second).isEqualTo("tokenconfig.Setting");
        assertThat(imports.render()).contains("\t\"github.com/acme/sdk/ledger/config\"\n")
                .contains("\ttokenconfig \"github.com/acme/sdk/token/config\"\n");
    }

    @Test
    void shouldKeepAliasingWhenEvenTheLongerNameIsTaken() {
        final GoImports imports = new GoImports(SELF);

        assertThat(imports.use("a/x/config")).isEqualTo("config");
        assertThat(imports.use("b/x/config")).isEqualTo("xconfig");
        assertThat(imports.use("c/x/config")).isEqualTo("cxconfig");
        // nothing is left of the path to prepend, so a number settles it
        assertThat(imports.use("x/config")).isEqualTo("config2");
    }

    @Test
    void shouldSeparateTheStandardLibraryFromEverythingElse() {
        final GoImports imports = new GoImports(SELF);
        imports.use("github.com/acme/sdk/keys");
        imports.use("slices");
        imports.use("math/big");

        // the grouping gofmt preserves and goimports produces: standard library first, then the rest
        assertThat(imports.render()).isEqualTo("""
                import (
                \t"math/big"
                \t"slices"

                \t"github.com/acme/sdk/keys"
                )

                """);
    }

    @Test
    void shouldMakeAPathSegmentAUsablePackageName() {
        final GoImports imports = new GoImports(SELF);

        assertThat(imports.use("github.com/acme/sdk/native-token")).isEqualTo("nativetoken");
    }
}
