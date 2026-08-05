package org.hiero.sdk.v3.codegen.model;

import java.util.List;

public sealed interface Declaration
        permits TypeDeclaration, EnumDeclaration, ConstantDeclaration, NamespaceOperationDeclaration {

    String name();

    List<AnnotationUse> annotations();

    String documentation();

    SourceLocation location();
}
