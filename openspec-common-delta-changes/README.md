# Common Delta Changes

This directory holds language-neutral changes shared by all SDKs. It is not an OpenSpec project.

Each change contains:

```text
changes/<change-name>/
├── proposal.md   # Why and scope
└── spec.md       # Required cross-SDK behavior
```

For each common change, the Java and TypeScript owners independently:

1. Create an OpenSpec change in their SDK.
2. Copy both `proposal.md` and `spec.md` into it.
3. Adapt those copies to their language without changing the common behavior.
4. Add `design.md` and `tasks.md`.
5. Validate, implement, and test the public API.
6. Archive the completed change into the SDK's accepted specs.

Keep language-specific types and business implementation out of the common files.

See [the pagination example](sdk-implementation-example.md) for the exact Java and TypeScript commands.
