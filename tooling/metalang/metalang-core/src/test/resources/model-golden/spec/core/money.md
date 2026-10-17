# Money

## Description

## API Schema

```
namespace shop.money

// An amount of money.
@@hip("17")
Money {
    @@immutable @@min(-1_000_000_000_000_000_000) cents: int256   // the amount in cents
    @@immutable @@nullable @@hip("1046") timeout: duration
}

enum Plan(timeout: seconds, limit: int256, tags: list<string>) {
    BASIC(30, 1_000, ["a", "b"])
    @@hip("540") PREMIUM(3_600, 100_000_000_000_000_000_000, [])
}
```

## Testing

## Questions & Comments
