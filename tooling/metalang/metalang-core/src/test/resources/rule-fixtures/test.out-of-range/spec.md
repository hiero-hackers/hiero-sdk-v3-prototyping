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
test "a number too large" {
    assertEquals(expected: 1, actual: Point{x: 3000000000}.x)
}
```

## Questions & Comments
