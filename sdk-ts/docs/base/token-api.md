# Token API

The Token API provides foundational enumerations that describe HTS (Hedera Token Service) tokens. Unlike the native network token (HBAR), these refer to first-class, protocol-level custom tokens.

## Enumerations

### `TokenType`
Defines the divisibility and serial-uniqueness of the token. 
This is strictly set once during token creation (`TokenCreate`) and cannot be altered afterward.
- `FUNGIBLE_COMMON`: Represents a divisible, fungible currency token (e.g., standard ERC-20 style tokens).
- `NON_FUNGIBLE_UNIQUE`: Represents a non-divisible, unique-serial collection token (e.g., standard NFTs).

### `TokenSupplyType`
Defines the supply policy mechanics of the token. 
This is strictly set once during token creation and cannot be altered afterward.
- `INFINITE`: Indicates there is no protocol-enforced ceiling limit on how many tokens can be minted.
- `FINITE`: Indicates a firm protocol-enforced ceiling (`totalSupply <= maxSupply`), preventing minting beyond the maximum limit.

## Usage Note

This namespace intentionally serves as a narrow registry to keep write-side transaction APIs and read-side mirror node APIs strictly in sync. In the future, this module is expected to house full identifier classes such as `TokenId` and `NftId`.
