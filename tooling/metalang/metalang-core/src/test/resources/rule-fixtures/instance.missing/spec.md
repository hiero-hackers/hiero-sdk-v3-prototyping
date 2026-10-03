# Title

## Description

Text.

## API Schema

```
namespace a
// signs with a key that is not held in memory
abstraction Signer { bytes sign(data: bytes) }
Client { @@immutable signer: Signer }
```

## Testing

Nothing.

## Questions & Comments
