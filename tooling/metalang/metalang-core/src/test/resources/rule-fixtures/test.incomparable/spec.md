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
test "a point compared with a string" {
    assertTrue(condition: Point{x: 1} == "a")
}
```

## Questions & Comments
