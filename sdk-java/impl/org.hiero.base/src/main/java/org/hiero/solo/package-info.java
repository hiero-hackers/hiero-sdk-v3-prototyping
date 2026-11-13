/// Support for local networks created with Solo, the tool to run a Hiero network locally (e.g. for
/// development and testing). Use `SOLO_IDENTIFIER` to look up the network setting of a Solo network.
///
/// `rule:solo-setting` The network setting of `SOLO_IDENTIFIER` is a `SoloNetworkSetting` of the network that
/// `solo one-shot single deploy` starts: the network with the id `0x03` and the name `local`, the mirror node
/// `http://127.0.0.1:38081/api/v1` and one consensus node, `127.0.0.1` with the gRPC port 35211 and the node account
/// `0.0.3`.
package org.hiero.solo;
