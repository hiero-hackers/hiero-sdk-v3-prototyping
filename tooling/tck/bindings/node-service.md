# Node service

The TCK methods of the node service
([node-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/node-service)):
creating, updating and deleting consensus nodes (HIP-869), from the admin client. A service endpoint
(ServiceEndpoint.md) has an IPv4 address as hex string (`ipAddress`) or a domain name, and a port.

`getAddressBook` (AddressBookQuery.md) reads the address book from the Mirror Node (`NetworkRepository::nodes`,
`GET /network/nodes`), not with `NodeAddressBookQuery` of the admin client: that query has no input, while the TCK
selects the address-book file and limits the number of nodes.

## Gaps

- **A file that does not exist** (`fileId` `0.0.999999`, test 4): the Mirror Node answers with an empty list, not
  with an error; the V2 SDKs read the address book over the gRPC API of the Mirror Node, which fails. A malformed ID
  (`999.999.999`, test 3) is rejected by the REST API.
- **`ipAddressV4`** is the dotted IPv4 address of the REST API (`127.0.0.1`), not hex as in the test specification.

```bindings
requires {NodeCreateTransaction, NodeUpdateTransaction, NodeDeleteTransaction, ServiceEndpoint} from consensusnode.admin.nodes
requires {NetworkRepository} from mirrornode.network

// Creates a node (NodeCreateTransaction.md, ServiceEndpoint.md).
binding createNode -> NodeCreateTransaction {
    accountId            = accountId : accountId
    description          = description : string
    gossipEndpoints      = each gossipEndpoints -> ServiceEndpoint {
        ipAddress  = ipAddressV4 : ipAddress
        domainName = domainName : string
        port       = port : int32
    }
    serviceEndpoints     = each serviceEndpoints -> ServiceEndpoint {
        ipAddress  = ipAddressV4 : ipAddress
        domainName = domainName : string
        port       = port : int32
    }
    gossipCaCertificate  = gossipCaCertificate : hex
    grpcCertificateHash  = grpcCertificateHash : hex
    grpcWebProxyEndpoint = grpcWebProxyEndpoint -> ServiceEndpoint {
        ipAddress  = ipAddressV4 : ipAddress
        domainName = domainName : string
        port       = port : int32
    }
    adminAuthority       = adminKey : key
    declineReward        = declineReward : bool
    result nodeId = receipt.nodeId : int64
    result status = receipt.status : status
}

// Updates a node (NodeUpdateTransaction.md, ServiceEndpoint.md).
binding updateNode -> NodeUpdateTransaction {
    nodeId               = nodeId : int64
    accountId            = accountId : accountId
    description          = description : string
    gossipEndpoints      = each gossipEndpoints -> ServiceEndpoint {
        ipAddress  = ipAddressV4 : ipAddress
        domainName = domainName : string
        port       = port : int32
    }
    serviceEndpoints     = each serviceEndpoints -> ServiceEndpoint {
        ipAddress  = ipAddressV4 : ipAddress
        domainName = domainName : string
        port       = port : int32
    }
    gossipCaCertificate  = gossipCaCertificate : hex
    grpcCertificateHash  = grpcCertificateHash : hex
    grpcWebProxyEndpoint = grpcWebProxyEndpoint -> ServiceEndpoint {
        ipAddress  = ipAddressV4 : ipAddress
        domainName = domainName : string
        port       = port : int32
    }
    adminAuthority       = adminKey : key
    declineReward        = declineReward : bool
    result status = receipt.status : status
}

// Deletes a node (NodeDeleteTransaction.md).
binding deleteNode -> NodeDeleteTransaction {
    nodeId = nodeId : int64
    result status = receipt.status : status
}

// Reads the address book from the Mirror Node (AddressBookQuery.md): the consensus nodes of the address-book file
// fileId, of all files without it. limit is the number of nodes the TCK wants, not the page size of the REST API:
// the pages are read until there are as many.
binding getAddressBook -> NetworkRepository::nodes {
    fileId = fileId : string
    take limit
    result nodeAddresses = each response {
        result nodeId           = nodeId : number64
        result accountId        = accountId : accountId
        result serviceEndpoints = each serviceEndpoints {
            result ipAddressV4 = ipAddressV4 : string
            result domainName  = domainName : string
            result port        = port : number
        }
        result rsaPublicKey     = publicKey : string
        result nodeCertHash     = nodeCertHash : string
        result description      = description : string
    }
}
```
