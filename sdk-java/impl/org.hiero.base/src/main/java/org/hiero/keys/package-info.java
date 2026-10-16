/// The keys API provides functionality to create and manage cryptographic keys.
/// A cryptographic key is defined by a byte sequence and a cryptographic algorithm.
/// Here it is independent if the key is a public or private key.
/// A private key can be used to sign messages and a public key can be used to verify signatures.
/// A private key is normally generated as a random key for a specific algorithm.
/// A public key can be derived from a private key.
///
/// To read or write keys different formats are supported.
/// The easiest way is to read or write the raw bytes of the key.
/// To do so, the algorithm must be known.
///
/// To make import and export of keys more convenient, so-called key containers exist.
/// Like algorithms those containers are well defined and standardized.
/// A container normally contains the raw bytes of the key and the algorithm.
/// To import and export a key in a container an encoding must be specified.
/// Not all container formats support all encodings according to their specification, and the encoding can end in a byte array result or a
/// string result.
///
/// ### KeyContainer rules
///
/// Each container holds exactly one type of key: a PKCS#8 container holds a private key, an SPKI container holds a public
/// key. Both containers can be written in DER (binary) and in PEM (text) encoding.
///
/// #### The standards of the containers
///
/// The containers follow their standards, and only them:
///
/// - **PKCS#8** is the `PrivateKeyInfo` of RFC 5958. An Ed25519 key has the algorithm `id-Ed25519` (RFC 8410). An ECDSA
///   key has the algorithm `id-ecPublicKey` with the named curve `secp256k1` as parameter (RFC 5480), and its private key
///   is an `ECPrivateKey` (RFC 5915).
/// - **SPKI** is the `SubjectPublicKeyInfo` of RFC 5280. An Ed25519 key has the algorithm `id-Ed25519` (RFC 8410). An
///   ECDSA key has the algorithm `id-ecPublicKey` with the named curve `secp256k1` (RFC 5480); the key is written with the
///   compressed point (33 bytes) and read with the compressed or the uncompressed point.
///
/// Earlier Hiero SDKs wrote an ECDSA private key as `3030020100300706052b8104000a04220420` followed by the 32-byte scalar:
/// the curve as algorithm and the bare scalar as key. That is no PKCS#8 structure, and it is not read.
///
/// #### Key examples
///
/// The following examples show the different key formats as string representations.
///
/// ##### PKCS#8 + DER (Private Key)
///
/// The string is a hex dump of the DER bytes.
///
/// ```
/// 30 2E 02 01 00 30 05 06 03 2B 65 70 04 22 04 20 D3 67 1A 1E 98 BB 22 F0 11 C0 E4 BC F5 12 55 90
/// E1 5D 8F 21 A7 01 73 09 BB 55 88 52 03 9B C7 5C
/// ```
///
/// ##### PKCS#8 + PEM (Private Key)
///
/// ```
/// -----BEGIN PRIVATE KEY-----
/// MC4CAQAwBQYDK2VwBCIEINNnGh6YuyLwEcDkvPUSVZDhXY8hpwFzCbtViFIDm8dc
/// -----END PRIVATE KEY-----
/// ```
///
/// ##### SPKI + DER (Public Key)
///
/// The string is a hex dump of the DER bytes.
///
/// ```
/// 30 2A 30 05 06 03 2B 65 70 03 21 00
/// 1D 0F 36 11 67 7D 11 EC 59 85 55 9B
/// 0A 7C 22 7A D9 88 CB 0B E6 C8 27 67
/// E8 96 F6 9E E3
/// ```
///
/// ##### SPKI + PEM (Public Key)
///
/// ```
/// -----BEGIN PUBLIC KEY-----
/// MCowBQYDK2VwAyEAHQ82EWd9EexZhVWbCnwietmIywvmyCdn6Jb2nuM=
/// -----END PUBLIC KEY-----
/// ```
package org.hiero.keys;
