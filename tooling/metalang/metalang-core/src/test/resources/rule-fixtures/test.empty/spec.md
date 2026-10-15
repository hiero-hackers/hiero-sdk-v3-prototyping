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
test "checks nothing" {
    let p = Point{x: 1}
}
```

## Questions & Comments
