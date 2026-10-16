# File Service API

## Description

`FileService` stores and retrieves files on a Hiero network (the File Service). A file holds an arbitrary byte payload
on the ledger together with an expiration time. The service takes care of splitting payloads that are larger than a
single transaction into chunks. Files are identified by an `Address`.

## API Schema

```
namespace enterprise.service.file
requires {Address} from ledger
requires {Authority} from authority
requires {Session} from enterprise.service

// The parameters of a new file: everything that can be set when it is created. Nothing is required.
@@finalType
FileCreateParams {
    // The initial contents; more can be appended later.
    @@immutable @@default([]) contents: bytes
    // Allows to change and delete the file; an authority that all must sign is an AuthorityList.
    @@immutable @@nullable authority: Authority
    // When the file expires; the network default if absent.
    @@immutable @@nullable expirationTime: zonedDateTime
    @@immutable @@nullable @@maxLength(100) fileMemo: string
}

FileService {

    // Create a new file with the given contents; returns the id of the new file. The same as createFile with
    // FileCreateParams that have only these values.
    @@throws(service-error) Address createFile(contents: bytes)

    @@throws(service-error) Address createFile(contents: bytes, expirationTime: zonedDateTime)

    // Create a new file with the given parameters; returns the id of the new file: the general form of the methods
    // above, for every option of a new file.
    @@throws(service-error) Address createFile(params: FileCreateParams)

    // Read the full contents of a file
    @@throws(service-error) bytes readFile(fileId: Address)

    // Replace the contents of a file
    @@throws(service-error) void updateFile(fileId: Address, contents: bytes)

    // Update only the expiration time of a file
    @@throws(service-error) void updateExpirationTime(fileId: Address, expirationTime: zonedDateTime)

    // Delete a file
    @@throws(service-error) void deleteFile(fileId: Address)

    // Returns true if the file has been deleted
    @@throws(service-error) bool isDeleted(fileId: Address)

    // Size of the file contents in bytes
    @@throws(service-error) int32 getSize(fileId: Address)

    // Expiration time of the file
    @@throws(service-error) zonedDateTime getExpirationTime(fileId: Address)
}

// Creates the service for the given session. With a framework integration the service is usually obtained via
// dependency injection instead.
@@static
FileService createService(session: Session)
```

## Default Instances for Testing

```
// The parameters of a file without values: empty contents and no authority.
instance FileCreateParams = FileCreateParams{}
```

## Testing

Tests of `FileCreateParams`. The methods of `FileService` talk to the network and are not unit-tested here (among them
the splitting of large contents into chunks). A violated `@@maxLength` cannot be expected yet: the test language
expects errors of calls only, and creating a `FileCreateParams` declares none. An expiration time cannot be written
in a test yet, as the test language has no date and time literals.

```tests
// Source: this spec (@@default and @@nullable of FileCreateParams, "Nothing is required"); hiero-enterprise-java
// (ProtocolLayerDataCreationTests, testFileCreateRequestCreation: empty contents without an expiration time and a
// memo are accepted).
test "file parameters without values have empty contents and nothing else" {
    // when
    let params = DEFAULT(FileCreateParams)

    // then
    assertEquals(expected: 0, actual: length(value: params.contents))
    assertNull(actual: params.authority)
    assertNull(actual: params.expirationTime)
    assertNull(actual: params.fileMemo)
}

// Source: the FileCreateTransaction unit tests of the Java (FileCreateTransactionTest), Go
// (file_create_transaction_unit_test.go), C++ (FileCreateTransactionUnitTests) and Python
// (file_create_transaction_test.py, test_constructor_with_parameters) SDKs: every value that is set is read back
// unchanged.
test "file parameters keep the values they are created with" {
    // given
    let contents = utf8(text: "Hello, Hiero!")
    let authority = DEFAULT(Authority)

    // when
    let params = FileCreateParams{contents: contents, authority: authority, fileMemo: "fileMemo"}

    // then
    assertEquals(expected: contents, actual: params.contents)
    assertEquals(expected: authority, actual: params.authority)
    assertEquals(expected: "fileMemo", actual: params.fileMemo)
}

// Source: this spec (@@maxLength(100) of fileMemo); HAPI limits a memo to 100 bytes, which ASCII text of 100
// characters is.
test "a file memo of 100 characters is accepted" {
    // given
    let memo = repeat(value: "a", count: 100)

    // when
    let params = FileCreateParams{fileMemo: memo}

    // then
    assertEquals(expected: memo, actual: params.fileMemo)
}
```

## Questions & Comments
