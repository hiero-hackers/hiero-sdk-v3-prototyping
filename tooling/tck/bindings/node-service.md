# Node service

The TCK methods of the node service
([node-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/node-service)):
creating, updating and deleting consensus nodes (HIP-869), from the admin client. A service endpoint
(ServiceEndpoint.md) has an IPv4 address as hex string (`ipAddress`) or a domain name, and a port.

## Methods without binding

- `getAddressBook`: `NodeAddressBookQuery` has no input (the TCK sends a file ID and a limit), and its result is a
  list of objects, which the binding format cannot write.

```bindings
requires {NodeCreateTransaction, NodeUpdateTransaction, NodeDeleteTransaction, ServiceEndpoint} from consensusnode.admin.nodes

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
```
