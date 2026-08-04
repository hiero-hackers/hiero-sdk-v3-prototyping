# Dependency Review: Java Base Public API Prototype

**Status**: Pending accountable human approval

**Gate**: No public, build, test, plugin, wrapper, or transitive dependency may be adopted until this
review is complete. AI agents may collect evidence but cannot approve a dependency.

## Review Scope

The review covers the planned JSpecify compile-time annotation dependency, JUnit test dependencies,
Maven build plugins, Maven Wrapper artifacts, and every transitive dependency they introduce.

## Required Evidence

| Dependency or artifact | Exact version and scope | Necessity and alternatives | Maintenance evidence | License and transitive compatibility | Security and provenance evidence | Public API or JPMS impact | Decision |
|---|---|---|---|---|---|---|---|
| JSpecify | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | Pending |
| JUnit Jupiter | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | Pending |
| Maven build plugins | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | Pending |
| Maven Wrapper artifacts | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | To be completed in T001 | Pending |

## Approval Conditions

- Every artifact and transitive dependency has an exact version and intended scope.
- Each dependency is necessary, actively maintained, and has no simpler platform or repository-local
  alternative that satisfies the requirement.
- Licenses are identified and confirmed compatible with the project and intended artifact usage.
- Artifact origin, integrity controls, known vulnerability review, and update policy are recorded.
- Only approved compile-time annotation types may appear in public signatures or JPMS metadata.
- Build and test dependencies cannot leak into the generated API artifact or its runtime graph.
- Any coordinate, version, scope, transitive graph, license, or maintenance-status change requires
  renewed review before adoption.

## Accountable Approval

| Role | Name | Decision | Date | Evidence or conditions |
|---|---|---|---|---|
| Dependency reviewer | Pending | Pending | Pending | Complete during T001 |
| Java API maintainer | Pending | Pending | Pending | Required for public-signature or JPMS dependencies |
| Security reviewer | Pending | Pending | Pending | Required for sensitive-path or supply-chain impact |
