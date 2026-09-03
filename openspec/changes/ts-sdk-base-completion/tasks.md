## 1. Implement Common Primitives

- [x] 1.1 Read `spec/base/common.md` and implement TS structures (e.g., `Page<T>`) in `src/base/Common.ts`. Verify by importing and writing a basic test.

## 2. Implement Native Tokens

- [x] 2.1 Read `spec/base/native-token.md` and implement the `NativeTokenUnit` abstractions in `src/base/NativeToken.ts`. Verify with a test.

## 3. Implement Cryptography Keys

- [x] 3.1 Read `spec/base/keys.md` and implement `PrivateKey` and `PublicKey` hierarchies, mapping `@@sealed` properly in `src/base/Keys.ts`. Verify by compiling `tsc --noEmit`.

## 4. Implement Token Classifications

- [x] 4.1 Read `spec/base/token.md` and implement the enums (e.g., `TokenType`, `TokenSupplyType`) in `src/base/Token.ts`. Verify by asserting enum values in tests.

## 5. Verify Encapsulation

- [x] 5.1 Review exports in `src/base/index.ts` and ensure no private implementations are exposed. Verify package exports in `package.json` lock down module paths securely.
