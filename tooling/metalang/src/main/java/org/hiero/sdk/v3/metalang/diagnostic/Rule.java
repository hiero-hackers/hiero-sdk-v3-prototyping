package org.hiero.sdk.v3.metalang.diagnostic;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Catalog of all rules the tooling checks. Every {@link Diagnostic} refers to exactly one rule by its
 * stable {@link #id()}. Rule ids are part of the tool's contract (they are used in reports and,
 * later, in per-language exception files) and must not be renamed.
 *
 * <p>The {@link #reference()} points to the section of {@code guidelines/api-guideline.md} (or another
 * document) the rule is derived from.
 */
public enum Rule {

    // --- Markdown document structure -------------------------------------------------------------
    DOC_NO_SCHEMA("doc.no-schema", Severity.INFO,
            "File contains no API schema and is not treated as a spec", "CLAUDE.md#how-to-make-changes"),
    DOC_SCHEMA_OUTSIDE_SECTION("doc.schema-outside-section", Severity.WARNING,
            "Schema code block is not placed in the '## API Schema' section", "CLAUDE.md#how-to-make-changes"),
    DOC_EMPTY_SCHEMA_SECTION("doc.empty-schema-section", Severity.ERROR,
            "'## API Schema' section contains no code block", "CLAUDE.md#how-to-make-changes"),
    DOC_MULTIPLE_SCHEMA_BLOCKS("doc.multiple-schema-blocks", Severity.WARNING,
            "'## API Schema' section contains more than one code block", "CLAUDE.md#how-to-make-changes"),
    DOC_SECTION_ORDER("doc.section-order", Severity.WARNING,
            "Known sections are not in canonical order", "testing-guideline.md#where-tests-live-the-testing-section"),
    DOC_SECTION_NAME("doc.section-name", Severity.WARNING,
            "Known section uses a non-canonical heading", "testing-guideline.md#where-tests-live-the-testing-section"),
    DOC_MISSING_SECTION("doc.missing-section", Severity.INFO,
            "A section of the spec skeleton is missing", "testing-guideline.md#where-tests-live-the-testing-section"),
    DOC_UNCLOSED_FENCE("doc.unclosed-fence", Severity.ERROR,
            "Fenced code block is never closed", "CLAUDE.md#how-to-make-changes"),

    // --- syntax ----------------------------------------------------------------------------------
    SYNTAX_ERROR("syntax.error", Severity.ERROR,
            "Text does not match the meta-language grammar", "api-guideline.md#syntax"),
    SYNTAX_TYPE_KEYWORD("syntax.type-keyword", Severity.WARNING,
            "Complex type declared with the non-standard 'type' keyword", "api-guideline.md#complex-types"),
    SYNTAX_USE_SITE_BOUND("syntax.use-site-bound", Severity.ERROR,
            "Bound on a generic argument at the use site; bounds belong to the declaration",
            "api-guideline.md#generic-methods"),
    SYNTAX_TRAILING_RETURN_TYPE("syntax.trailing-return-type", Severity.WARNING,
            "Method declared as 'name(...): Type' instead of 'Type name(...)'", "api-guideline.md#methods"),
    SYNTAX_MISSING_RETURN_TYPE("syntax.missing-return-type", Severity.WARNING,
            "Method declared without return type; use 'void'", "api-guideline.md#methods"),
    SYNTAX_ANNOTATION_IN_COMMENT("syntax.annotation-in-comment", Severity.WARNING,
            "Annotation written inside a comment is ignored by tooling", "api-guideline.md#method-annotations"),

    // --- namespace-level functions ---------------------------------------------------------------
    FUNCTION_NOT_STATIC("function.not-static", Severity.ERROR,
            "Namespace-level functions must be annotated with @@static", "api-guideline.md#namespace-level-functions"),

    // --- naming ----------------------------------------------------------------------------------
    NAMING_NAMESPACE("naming.namespace", Severity.ERROR,
            "Namespace segments must be lowerCamelCase", "api-guideline.md#naming-conventions"),
    NAMING_TYPE("naming.type", Severity.ERROR,
            "Type names must be PascalCase", "api-guideline.md#naming-conventions"),
    NAMING_MEMBER("naming.member", Severity.ERROR,
            "Field, method and parameter names must be lowerCamelCase", "api-guideline.md#naming-conventions"),
    NAMING_ENUM_VALUE("naming.enum-value", Severity.ERROR,
            "Enum values must be UPPER_SNAKE_CASE", "api-guideline.md#naming-conventions"),
    NAMING_CONSTANT("naming.constant", Severity.ERROR,
            "Constant names must be UPPER_SNAKE_CASE", "api-guideline.md#naming-conventions"),
    NAMING_ERROR_ID("naming.error-id", Severity.ERROR,
            "Error identifiers in @@throws must be lowercase-kebab-case", "api-guideline.md#naming-conventions"),
    NAMING_GENERIC("naming.generic", Severity.WARNING,
            "Generic parameter names should be PascalCase after the '$$' prefix",
            "api-guideline.md#generic-type-parameters"),

    // --- namespaces & requires -------------------------------------------------------------------
    NAMESPACE_DUPLICATE_DECLARATION("namespace.duplicate-declaration", Severity.ERROR,
            "Two declarations with the same name in one namespace", "api-guideline.md#namespace"),
    NAMESPACE_SPLIT("namespace.split", Severity.INFO,
            "Namespace is declared in more than one spec file", "api-guideline.md#namespace"),
    REQUIRES_UNKNOWN_NAMESPACE("requires.unknown-namespace", Severity.ERROR,
            "Required namespace does not exist", "api-guideline.md#namespace"),
    REQUIRES_UNKNOWN_TYPE("requires.unknown-type", Severity.ERROR,
            "Required type does not exist in the given namespace", "api-guideline.md#namespace"),
    REQUIRES_UNUSED("requires.unused", Severity.WARNING,
            "Imported type is never used", "api-guideline.md#namespace"),
    REQUIRES_DUPLICATE_NAMESPACE("requires.duplicate-namespace", Severity.WARNING,
            "More than one requires statement for the same namespace", "api-guideline.md#namespace"),
    REQUIRES_SELF("requires.self", Severity.WARNING,
            "Namespace requires types from itself", "api-guideline.md#namespace"),

    // --- type references -------------------------------------------------------------------------
    TYPE_UNKNOWN("type.unknown", Severity.ERROR,
            "Referenced type does not exist", "api-guideline.md#basic-data-types"),
    TYPE_NOT_IMPORTED("type.not-imported", Severity.ERROR,
            "Referenced type exists in another namespace but is not imported", "api-guideline.md#namespace"),
    TYPE_AMBIGUOUS("type.ambiguous", Severity.ERROR,
            "Simple type name is ambiguous; qualify it with its namespace",
            "api-guideline.md#referencing-imported-types"),
    TYPE_UNNECESSARY_QUALIFICATION("type.unnecessary-qualification", Severity.INFO,
            "Qualified type reference where the simple name is unambiguous",
            "api-guideline.md#referencing-imported-types"),
    TYPE_ARITY("type.arity", Severity.ERROR,
            "Wrong number of type arguments", "api-guideline.md#generic-type-parameters"),
    TYPE_INT_WIDTH("type.int-width", Severity.ERROR,
            "Integer width must satisfy 8 <= X <= 256", "api-guideline.md#basic-data-types"),
    TYPE_ANY_STANDALONE("type.any-standalone", Severity.WARNING,
            "ANY used as a standalone type", "api-guideline.md#avoid-any-as-a-standalone-type"),
    TYPE_STREAM_RESULT_OUTSIDE_STREAMING("type.stream-result-outside-streaming", Severity.WARNING,
            "streamResult<T> used outside the return type of a @@streaming method", "api-guideline.md#streaming"),
    GENERIC_UNDECLARED("generic.undeclared", Severity.ERROR,
            "Generic parameter is declared neither by the enclosing type nor by the method",
            "api-guideline.md#generic-methods"),
    GENERIC_DUPLICATE("generic.duplicate", Severity.ERROR,
            "Generic parameter declared twice or shadowing a parameter of the enclosing type",
            "api-guideline.md#generic-methods"),
    GENERIC_METHOD_NOT_FINAL("generic.method-not-final", Severity.ERROR,
            "Generic instance method must be @@finalMethod", "api-guideline.md#generic-methods"),
    FINAL_METHOD_OVERRIDDEN("method.final-overridden", Severity.ERROR,
            "Subtype re-declares an inherited @@finalMethod method", "api-guideline.md#method-annotations"),
    FINAL_METHOD_MULTIPLE_INHERITANCE("method.final-multiple-inheritance", Severity.ERROR,
            "@@finalMethod methods inherited from two unrelated abstractions (needs multiple class inheritance)",
            "api-guideline.md#method-annotations"),
    FINAL_METHOD_REDUNDANT("method.final-redundant", Severity.WARNING,
            "@@finalMethod on a @@static method or in a @@finalType has no effect", "api-guideline.md#method-annotations"),

    // --- annotations -----------------------------------------------------------------------------
    ANNOTATION_UNKNOWN("annotation.unknown", Severity.ERROR,
            "Annotation is not defined by the meta-language", "api-guideline.md#attribute-annotations"),
    ANNOTATION_TARGET("annotation.target", Severity.ERROR,
            "Annotation is not allowed on this kind of element", "api-guideline.md#attribute-annotations"),
    ANNOTATION_ARGUMENTS("annotation.arguments", Severity.ERROR,
            "Annotation has invalid arguments", "api-guideline.md#attribute-annotations"),
    ANNOTATION_EMPTY_PARENTHESES("annotation.empty-parentheses", Severity.WARNING,
            "Annotation without arguments written with empty parentheses; write '@@name' instead of '@@name()'",
            "api-guideline.md#annotation-syntax"),
    ANNOTATION_DUPLICATE("annotation.duplicate", Severity.WARNING,
            "Annotation repeated on the same element", "api-guideline.md#attribute-annotations"),
    ANNOTATION_VALUE_TYPE("annotation.value-type", Severity.ERROR,
            "Validation annotation does not fit the element's type", "api-guideline.md#attribute-annotations"),
    ANNOTATION_REDUNDANT_THREAD_SAFE("annotation.redundant-thread-safe", Severity.WARNING,
            "Method of a @@threadSafe type carries its own @@threadSafe", "api-guideline.md#thread-safety-on-types"),
    DEFAULT_VALUE_TYPE("default.value-type", Severity.WARNING,
            "@@default value does not match the field type", "api-guideline.md#attribute-annotations"),
    CONSTANT_VALUE_TYPE("constant.value-type", Severity.WARNING,
            "Constant value does not match the declared type", "api-guideline.md#constants"),

    // --- @@oneOf / @@oneOrNoneOf ---------------------------------------------------------------------
    ONEOF_UNKNOWN_FIELD("oneof.unknown-field", Severity.ERROR,
            "@@oneOf/@@oneOrNoneOf references a field that the type does not have",
            "api-guideline.md#complex-type-annotations"),
    ONEOF_NOT_NULLABLE("oneof.not-nullable", Severity.ERROR,
            "All fields listed in @@oneOf/@@oneOrNoneOf must be @@nullable", "api-guideline.md#complex-type-annotations"),
    ONEOF_MIXED_IMMUTABILITY("oneof.mixed-immutability", Severity.ERROR,
            "None or all fields listed in @@oneOf/@@oneOrNoneOf must be @@immutable",
            "api-guideline.md#complex-type-annotations"),
    ONEOF_MULTIPLE_DEFAULTS("oneof.multiple-defaults", Severity.ERROR,
            "At most one field listed in @@oneOf/@@oneOrNoneOf may have a @@default",
            "api-guideline.md#complex-type-annotations"),

    // --- inheritance -----------------------------------------------------------------------------
    EXTENDS_INVALID("extends.invalid", Severity.ERROR,
            "Only complex types (and abstractions) can be extended", "api-guideline.md#abstraction--inheritance"),
    EXTENDS_CYCLE("extends.cycle", Severity.ERROR,
            "Inheritance cycle", "api-guideline.md#abstraction--inheritance"),
    EXTENDS_MULTIPLE("extends.multiple", Severity.INFO,
            "Multiple inheritance is supported but should be avoided", "api-guideline.md#abstraction--inheritance"),
    SEALED_NOT_ABSTRACTION("sealed.not-abstraction", Severity.ERROR,
            "@@sealed is only allowed on an abstraction", "api-guideline.md#abstraction--inheritance"),
    SEALED_UNKNOWN_SUBTYPE("sealed.unknown-subtype", Severity.ERROR,
            "Type listed in @@sealed cannot be resolved", "api-guideline.md#abstraction--inheritance"),
    SEALED_SUBTYPE_NOT_EXTENDING("sealed.subtype-not-extending", Severity.ERROR,
            "Type listed in @@sealed does not extend the sealed abstraction",
            "api-guideline.md#abstraction--inheritance"),
    SEALED_UNLISTED_SUBTYPE("sealed.unlisted-subtype", Severity.ERROR,
            "Type extends a sealed abstraction without being listed in @@sealed",
            "api-guideline.md#abstraction--inheritance"),
    FINAL_TYPE_EXTENDED("final.extended", Severity.ERROR,
            "Type extends a @@finalType", "api-guideline.md#complex-type-annotations"),
    FINAL_TYPE_ON_ABSTRACTION("final.on-abstraction", Severity.ERROR,
            "@@finalType on an abstraction", "api-guideline.md#complex-type-annotations"),
    OVERRIDE_NO_PARENT_FIELD("override.no-parent-field", Severity.ERROR,
            "@@override on a field that no parent type declares", "api-guideline.md#narrowing-inherited-nullability"),
    OVERRIDE_TYPE_MISMATCH("override.type-mismatch", Severity.ERROR,
            "@@override field must keep the parent's type", "api-guideline.md#narrowing-inherited-nullability"),
    OVERRIDE_IMMUTABILITY_MISMATCH("override.immutability-mismatch", Severity.ERROR,
            "@@override field must keep the parent's @@immutable discipline",
            "api-guideline.md#narrowing-inherited-nullability"),
    OVERRIDE_NOT_NARROWING("override.not-narrowing", Severity.ERROR,
            "@@override must narrow a @@nullable parent field to non-nullable",
            "api-guideline.md#narrowing-inherited-nullability"),
    OVERRIDE_MISSING("override.missing", Severity.ERROR,
            "Field re-declares an inherited field without @@override",
            "api-guideline.md#narrowing-inherited-nullability"),

    // --- members ---------------------------------------------------------------------------------
    MEMBER_DUPLICATE_FIELD("member.duplicate-field", Severity.ERROR,
            "Field declared twice", "api-guideline.md#attributes"),
    MEMBER_DUPLICATE_METHOD("member.duplicate-method", Severity.ERROR,
            "Method with the same signature declared twice", "api-guideline.md#methods"),
    MEMBER_DUPLICATE_PARAMETER("member.duplicate-parameter", Severity.ERROR,
            "Parameter declared twice", "api-guideline.md#methods"),
    VARARGS_NOT_LAST("varargs.not-last", Severity.ERROR,
            "Varargs parameter must be the last parameter", "api-guideline.md#variable-arguments-varargs"),
    VARARGS_MULTIPLE("varargs.multiple", Severity.ERROR,
            "A method can have at most one varargs parameter", "api-guideline.md#variable-arguments-varargs"),
    VARARGS_NULLABLE("varargs.nullable", Severity.ERROR,
            "A varargs parameter must not be @@nullable", "api-guideline.md#variable-arguments-varargs"),
    METHOD_ASYNC_AND_STREAMING("method.async-and-streaming", Severity.ERROR,
            "@@async and @@streaming are mutually exclusive", "api-guideline.md#streaming-annotation-combinations"),
    METHOD_STREAMING_STATIC("method.streaming-static", Severity.ERROR,
            "@@streaming must not be combined with @@static", "api-guideline.md#streaming-annotation-combinations"),
    COLLECTION_NULLABLE("collection.nullable", Severity.ERROR,
            "list/set/map must never be @@nullable", "api-guideline.md#never-define-nullable-collections"),
    FIELD_MUTABLE("field.mutable", Severity.INFO,
            "Field is mutable; prefer @@immutable unless mutability is required",
            "api-guideline.md#prefer-immutable-fields-and-objects"),

    // --- enums -----------------------------------------------------------------------------------
    ENUM_FIELD_NOT_IMMUTABLE("enum.field-not-immutable", Severity.ERROR,
            "Enum attributes must be @@immutable", "api-guideline.md#enumerations"),
    ENUM_DUPLICATE_VALUE("enum.duplicate-value", Severity.ERROR,
            "Enum value declared twice", "api-guideline.md#enumerations"),
    ENUM_EMPTY("enum.empty", Severity.WARNING,
            "Enum declares no values", "api-guideline.md#enumerations"),
    ENUM_EXPLICIT_VALUES_METHOD("enum.explicit-values-method", Severity.ERROR,
            "Enums provide values() implicitly; it must not be declared", "api-guideline.md#enumerations"),
    ENUM_UNASSIGNABLE_FIELDS("enum.unassignable-fields", Severity.INFO,
            "Enum has attributes, but the meta-language has no syntax to assign per-value attribute values",
            "api-guideline.md#enumerations");

    private final String id;
    private final Severity severity;
    private final String description;
    private final String reference;

    Rule(final String id, final Severity severity, final String description, final String reference) {
        this.id = id;
        this.severity = severity;
        this.description = description;
        this.reference = reference;
    }

    /**
     * Returns the stable rule identifier.
     *
     * @return the id, e.g. {@code naming.type}
     */
    public String id() {
        return id;
    }

    /**
     * Returns the severity of findings of this rule.
     *
     * @return the severity
     */
    public Severity severity() {
        return severity;
    }

    /**
     * Returns a short description of the rule.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the document section the rule is derived from.
     *
     * @return reference such as {@code api-guideline.md#streaming}
     */
    public String reference() {
        return reference;
    }

    /**
     * Looks up a rule by its id.
     *
     * @param id the rule id
     * @return the rule, if known
     */
    public static Optional<Rule> byId(final String id) {
        Objects.requireNonNull(id, "id must not be null");
        return Arrays.stream(values()).filter(r -> r.id.equals(id)).findFirst();
    }
}
