# Data Model: Java Common Code Generation

## Overview

The first code-generation slice transforms one authoritative source spec file
into a normalized intermediate representation and then into generated Java API
artifacts plus verification fixtures.

## Entities

### SourceSpecDocument

| Field | Type | Description |
|-------|------|-------------|
| `path` | string | Repository-relative path to the source spec file |
| `title` | string | Human-readable title from the Markdown file |
| `schemaBlock` | string | Raw fenced `## API Schema` block contents |
| `namespace` | string | Namespace declared inside the schema block |

**Validation rules**:

- `path` MUST point to `spec/base/common.md` for this feature.
- `schemaBlock` MUST exist and contain exactly one `namespace` declaration.

### NamespaceModel

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Meta-language namespace name |
| `imports` | list<ImportModel> | Imported namespace/type references |
| `types` | list<TypeModel> | Top-level types declared in the namespace |

**Validation rules**:

- `name` MUST equal `common` in this slice.
- `types` MUST preserve source declaration order.

### TypeModel

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Type name from the schema |
| `kind` | enum | `abstraction`, `type`, or later extensions |
| `annotations` | list<AnnotationModel> | Type-level annotations |
| `typeParameters` | list<TypeParameterModel> | Generic parameters in source order |
| `fields` | list<FieldModel> | Declared attributes |
| `methods` | list<MethodModel> | Declared methods |

**Validation rules**:

- `name` MUST be unique within the namespace.
- `kind` MUST be preserved exactly from the source schema.

### FieldModel

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Source field name |
| `type` | TypeReferenceModel | Source type expression |
| `annotations` | list<AnnotationModel> | Field annotations such as `@@immutable` |

**Validation rules**:

- Field order MUST match source order.
- Annotations MUST be preserved without normalization loss.

### MethodModel

| Field | Type | Description |
|-------|------|-------------|
| `name` | string | Source method name |
| `returnType` | TypeReferenceModel | Declared return type before async mapping |
| `parameters` | list<ParameterModel> | Method parameters |
| `annotations` | list<AnnotationModel> | Method annotations such as `@@async` |
| `throwsIds` | list<string> | Error identifiers from `@@throws(...)` |

**Validation rules**:

- Boolean predicate names such as `hasNext` and `isFirst` MUST remain unchanged.
- `throwsIds` MUST preserve source order for reproducible output.

### JavaTypeArtifact

| Field | Type | Description |
|-------|------|-------------|
| `qualifiedName` | string | Java fully qualified type name |
| `sourcePath` | string | Output file path for the generated type |
| `sourceText` | string | Full generated Java source |
| `originType` | string | Source `TypeModel.name` used to generate the file |

**Validation rules**:

- `qualifiedName` MUST be unique per generation run.
- `sourceText` MUST be deterministic for unchanged input.

### GoldenBaseline

| Field | Type | Description |
|-------|------|-------------|
| `fixturePath` | string | Stored expected-output file path |
| `qualifiedName` | string | Java type covered by the fixture |
| `approvedSourceText` | string | Expected Java output |

**Validation rules**:

- Every generated public Java type in this slice MUST have a matching baseline.
- Fixture contents MUST be compared byte-for-byte in verification tests.

## Relationships

- `SourceSpecDocument` owns one `NamespaceModel`.
- `NamespaceModel` owns one or more `TypeModel` entries.
- `TypeModel` owns zero or more `FieldModel` and `MethodModel` entries.
- Each `TypeModel` maps to one or more `JavaTypeArtifact` entries.
- Each `JavaTypeArtifact` MUST map to one `GoldenBaseline` entry once the
  output is approved.

## State Flow

1. `SourceSpecDocument` loaded from `spec/base/common.md`
2. `schemaBlock` parsed into `NamespaceModel`
3. `NamespaceModel` normalized into `TypeModel`, `FieldModel`, and `MethodModel`
4. Normalized model rendered into `JavaTypeArtifact`
5. `JavaTypeArtifact` compared against `GoldenBaseline`
