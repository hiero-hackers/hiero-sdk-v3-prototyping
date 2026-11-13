# Solo API

## Description

Support for local networks created with Solo, the tool to run a Hiero network locally (e.g. for
development and testing). Use `SOLO_IDENTIFIER` to look up the network setting of a Solo network.

`rule:solo-setting` The network setting of `SOLO_IDENTIFIER` is a `SoloNetworkSetting` of the network that
`solo one-shot single deploy` starts: the network with the id `0x03` and the name `local`, the mirror node
`http://127.0.0.1:38081/api/v1` and one consensus node, `127.0.0.1` with the gRPC port 35211 and the node account
`0.0.3`.

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
@@testId("the-solo-identifier-looks-up-a-solo-network-setting")
test "the Solo identifier looks up a Solo network setting" {
    // when
    let setting = ledger.config.getNetworkSetting(identifier: SOLO_IDENTIFIER)

    // then
    assertEquals(expected: "solo", actual: SOLO_IDENTIFIER)
    assertInstanceOf(expected: SoloNetworkSetting, actual: setting)
}

// Source: this spec (`rule:solo-setting`).
@@testId("the-solo-setting-has-the-local-network-its-mirror-node-and-its-consensus-node")
@@rule("solo-setting")
test "the Solo setting has the local network, its mirror node and its consensus node" {
    // when
    let setting = ledger.config.getNetworkSetting(identifier: SOLO_IDENTIFIER)

    // then
    assertEquals(expected: hex(text: "03"), actual: setting.network.id)
    assertEquals(expected: "local", actual: setting.network.name)
    assertEquals(expected: 1, actual: length(value: setting.getMirrorNodes))
    assertTrue(condition: contains(value: setting.getMirrorNodes,
            element: ledger.MirrorNode{restBaseUrl: "http://127.0.0.1:38081/api/v1"}))
    assertEquals(expected: 1, actual: length(value: setting.getConsensusNodes))
    assertTrue(condition: contains(value: setting.getConsensusNodes, element: ledger.ConsensusNode{
            ip: ledger.IpAddress.fromString(value: "127.0.0.1"), port: 35211,
            account: ledger.AccountId.fromString(value: "0.0.3")}))
}
```

## Questions & Comments
