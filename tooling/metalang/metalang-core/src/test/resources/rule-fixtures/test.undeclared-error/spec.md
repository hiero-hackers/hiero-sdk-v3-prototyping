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
test "an error the method does not declare" {
    @@expectThrows(not-found-error)
    Point.parse(text: "x")
}
```

## Questions & Comments
