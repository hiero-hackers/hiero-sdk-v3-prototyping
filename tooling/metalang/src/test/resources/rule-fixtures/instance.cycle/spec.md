# Title

## Description

Text.

## API Schema

```
namespace a
A { @@immutable b: B }
B { @@immutable a: A }
```

## Default Instances

```
instance A = A{b: DEFAULT}
instance B = B{a: DEFAULT}
```

## Testing

Nothing.

## Questions & Comments
