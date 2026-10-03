# Money

## Description

## API Schema

```
namespace shop.money

Money {
    @@immutable cents: int64
}

enum Plan(timeout: seconds, limit: int256, tags: list<string>) {
    BASIC(30, 1_000, ["a", "b"])
    PREMIUM(3_600, 100_000_000_000_000_000_000, [])
}
```

## Testing

## Questions & Comments
