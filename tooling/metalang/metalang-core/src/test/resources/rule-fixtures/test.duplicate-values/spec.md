# Title

## Description

Text.

## API Schema

```
namespace a

Point {
    @@immutable x: int32
}
```

## Default Instances for Testing

```
values NUMBERS: list<int32> = [1, 2]
values NUMBERS: list<int32> = [3]
```

## Testing

```tests
test "uses the numbers" {
    for n in NUMBERS {
        assertTrue(condition: n > 0)
    }
}
```

## Questions & Comments
