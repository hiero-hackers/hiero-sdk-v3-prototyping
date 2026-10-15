# Title

## Description

Text.

## API Schema

```
namespace a

Point {
    @@immutable x: int32
    @@immutable @@nullable label: string

    @@throws(illegal-format) @@static Point parse(text: string)
}
```

## Testing

```tests
test "a name twice" {
    let a = 1
    let a = 2
    assertEquals(expected: 2, actual: a)
}
```

## Questions & Comments
