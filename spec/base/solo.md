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

## Questions & Comments
