package org.hiero.sdk.v3.metalang.generator.python;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PyImportsTest {

    @Test
    void annotationOnlyTypesShouldBeImportedForTheTypeCheckerOnly() {
        // GIVEN
        final PyImports imports = new PyImports("sdk.a._x", Set.of("X"));
        assertThat(imports.sdk("sdk.b._y", "Y", false)).isEqualTo("Y");
        assertThat(imports.sdk("sdk.c._base", "Base", true)).isEqualTo("Base");
        assertThat(imports.sdk("sdk.a._x", "X", false)).isEqualTo("X");
        assertThat(imports.standard("typing", "Protocol")).isEqualTo("Protocol");
        assertThat(imports.module("datetime")).isEqualTo("_dt");

        // THEN
        assertThat(imports.render()).isEqualTo("""
                from __future__ import annotations

                import datetime as _dt
                from typing import TYPE_CHECKING, Protocol

                from sdk.c._base import Base

                if TYPE_CHECKING:
                    from sdk.b._y import Y


                """);
    }

    @Test
    void aRuntimeImportShouldServeTheAnnotationsAsWell() {
        final PyImports imports = new PyImports("sdk.a._x", Set.of("X"));
        imports.sdk("sdk.b._y", "Y", false);
        imports.sdk("sdk.b._y", "Y", true);
        imports.sdk("sdk.b._y", "Y", false);
        assertThat(imports.render()).isEqualTo("""
                from __future__ import annotations

                from sdk.b._y import Y


                """);
        // a long statement is wrapped, one name per line
        assertThat(PyImports.statement("", "sdk." + "x".repeat(100), java.util.List.of("A", "B")))
                .isEqualTo("from sdk." + "x".repeat(100) + " import (\n    A,\n    B,\n)\n");
    }

    @Test
    void clashingNamesShouldGetAnAlias() {
        final PyImports imports = new PyImports("sdk.a._x", Set.of("Item"));
        assertThat(imports.sdk("sdk.b._item", "Item", false)).isEqualTo("sdk_b__item_Item");
        assertThat(imports.render()).contains("    from sdk.b._item import Item as sdk_b__item_Item\n");
    }
}
