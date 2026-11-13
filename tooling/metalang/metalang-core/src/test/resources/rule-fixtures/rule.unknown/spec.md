# Title

## Description

1. `rule:node-choice` A rule.

## API Schema

```
namespace a
```

## Testing

```tests
@@rule("node-choice")
test "a test of a defined rule" {
    assertTrue(condition: true)
}

@@rule("no-such-rule")
test "a test of an undefined rule" {
    assertTrue(condition: true)
}
```

## Questions & Comments
