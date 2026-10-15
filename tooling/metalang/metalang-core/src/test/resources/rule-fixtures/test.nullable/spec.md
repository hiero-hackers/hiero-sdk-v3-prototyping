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
test "a nullable argument" {
    let p = Point{x: 1}
    assertEquals(expected: 1, actual: length(value: p.label))
}
```

## Questions & Comments
