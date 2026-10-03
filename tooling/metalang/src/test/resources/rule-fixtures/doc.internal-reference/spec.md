# Leaks

## Description

Shared types. See `other.md` for details.

## Design Notes

Rationale may mention `other.md`, `@@nullable` and ADR-0006.

## API Schema

```
namespace a

// Remark for spec authors: see ADR-0006 and TODO in other.md.

// A product.
X {
    // the name, see @@minLength
    @@immutable name: string
    // the price; TODO: decide on rounding
    @@immutable price: int32
}
```

## Questions & Comments

None.
