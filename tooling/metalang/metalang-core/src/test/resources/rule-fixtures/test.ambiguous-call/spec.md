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

@@static int64 length(value: string)
```

## Testing

```tests
test "length is ambiguous" {
    assertEquals(expected: 1, actual: length(value: "a"))
}
```

## Questions & Comments
