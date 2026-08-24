# Mirror Node Http API

## Description

TODO

## API Schema

```
namespace mirrornode.http
requires {MirrorNode} from ledger
requires {HttpMethod, HttpClient, HttpResponse} from base.http

MirrorNodeHttpRequest {
    @@immutable method: HttpMethod
    @@immutable path: string
    @@nullable @@immutable body: bytes
    @@nullable @@immutable timeout: duration
    @@immutable headers: map<string, string>
}

MirrorNodeHttpClient {

    @@immutable mirrorNode: MirrorNode

    @@immutable httpClient:HttpClient
    
    @@async
    HttpResponse execute(request: MirrorNodeHttpRequest)
}

```

## Example

```
TODO
```