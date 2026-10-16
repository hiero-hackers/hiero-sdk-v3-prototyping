# Solo API

## Description

Support for local networks created with Solo, the tool to run a Hiero network locally (e.g. for
development and testing). Use `SOLO_IDENTIFIER` to look up the network setting of a Solo network.

## API Schema — Abstraction

```
namespace solo
requires {NetworkSetting} from ledger.config

constant SOLO_IDENTIFIER:string = "solo" // identifier for a solo based network created by the one-shot command

SoloNetworkSetting extends NetworkSetting {
}
```

## Testing

```tests
// Source: this spec (SOLO_IDENTIFIER is "solo" and looks up the network setting of a Solo network); the Go SDK (the
// "localhost" name of ClientForName targets the local Solo network).
test "the Solo identifier looks up a Solo network setting" {
    // when
    let setting = ledger.config.getNetworkSetting(identifier: SOLO_IDENTIFIER)

    // then
    assertEquals(expected: "solo", actual: SOLO_IDENTIFIER)
    assertInstanceOf(expected: SoloNetworkSetting, actual: setting)
}
```

## Questions & Comments
