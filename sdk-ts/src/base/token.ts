/**
 * Kind of a token: divisible currency (FUNGIBLE_COMMON) or unique-serial collection (NON_FUNGIBLE_UNIQUE).
 * Set once at TokenCreate; cannot be changed by TokenUpdate.
 */
export enum TokenType {
    /** A divisible currency token */
    FUNGIBLE_COMMON = 'FUNGIBLE_COMMON',
    /** A unique-serial collection token (NFT) */
    NON_FUNGIBLE_UNIQUE = 'NON_FUNGIBLE_UNIQUE'
}

/**
 * Supply policy of a token: INFINITE → no protocol-enforced ceiling; FINITE → `totalSupply <= maxSupply` is enforced at every mint.
 * Set once at TokenCreate; cannot be changed by TokenUpdate.
 */
export enum TokenSupplyType {
    /** No protocol-enforced ceiling */
    INFINITE = 'INFINITE',
    /** `totalSupply <= maxSupply` is enforced at every mint */
    FINITE = 'FINITE'
}
