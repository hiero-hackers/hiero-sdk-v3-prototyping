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

## Questions & Comments
