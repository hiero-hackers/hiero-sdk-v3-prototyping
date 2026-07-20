# Documentation Guideline

This document outlines the conventions and best practices for writing documentation in the V3 SDK project. 
Where [`api-guideline.md`](api-guideline.md) defines how we design APIs and [`testing-guideline.md`](testing-guideline.md) defines how we verify them, this document defines how we **explain** them.

## Philosophy

- **Documentation is Code:** Treat documentation with the same rigor as source code. It lives in version control, goes through code review, and must be kept in sync with the implementation.
- **Specifications are the Source of Truth:** The language-agnostic specifications (in the `spec/` and `proposals/` directories) are the definitive source of truth for the SDK's behavior. 
- **Audience-Centric:** Write for the developer using the SDK, not the person who built it. Focus on *what* a method does and *how* to use it, rather than the internal mechanics of the implementation.

## Spec-Level Documentation

Specifications are written in Markdown and define features in a language-agnostic way.

### Spec File Structure

Every specification file MUST follow this exact structure:

1. **`# Title`**: A clear, concise title of the feature or namespace.
2. **`## Description`**: A high-level overview of what this feature does, why it exists, and the primary use cases.
3. **`## API Schema`**: The language-agnostic definition using the syntax defined in the `api-guideline.md`.
4. **`## Examples` (Optional)**: Real-world, language-agnostic examples of how to use the feature. 
5. **`## Testing`**: The behavioural test scenarios (Given/When/Then) as defined in the `testing-guideline.md`.
6. **`## Questions & Comments`**: Open questions, unresolved design choices, or historical context.

### Tone and Grammar

- **Use the Imperative Mood:** Write active, commanding sentences for method descriptions. 
  - *Good:* "Submits the transaction to the network."
  - *Bad:* "This method will submit the transaction to the network."
- **Be Concise:** Avoid filler words. Get straight to the point.
- **Consistent Terminology:** Always use the same terms for the same concepts (e.g., use "Account ID", not interchangeably "Account Identifier" or "User ID").

## SDK-Level Documentation (Language Bindings)

While specs define the agnostic behavior, each concrete language binding (Java, Rust, TS, etc.) must provide native API documentation (JavaDoc, Rustdoc, TSDoc).

### Requirements for Native Docs

1. **Inherit from Spec:** The description of a class or method must carry the same semantic meaning as the `## Description` in the spec.
2. **Document Parameters and Returns:** Every public parameter and return value must be explicitly documented.
3. **Document Errors:** Any stream-level or terminal error (defined by `@@throws` in the spec) MUST be documented in the native language's error format (e.g., `@throws` in JavaDoc).
4. **Link Back:** Complex types and namespaces should include a reference or link back to the original spec file for architectural context.

### Code Examples

Code examples in native SDK documentation are highly encouraged.
- **Must be runnable:** If the language doc tool supports executing examples (like Rustdoc `///`), they must compile and pass.
- **Must be idiomatic:** Use the native language's best practices (e.g., don't write Java-style examples in TypeScript).

## Updating Documentation

- **No Undocumented Features:** A PR that adds a new public API must include both the spec update and the native SDK documentation.
- **Synchronized Updates:** If the implementation changes a behavior, the corresponding `spec/*.md` file and its `## Testing` section must be updated in the same Pull Request.
