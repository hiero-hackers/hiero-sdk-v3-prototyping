# Http API

## Description

TODO

## API Schema

```
namespace http

enum HttpMethod {
    GET,
    POST
}

HttpConfiguration {
  @@immutable defaultHeaders: map<string, string> 
  @@immutable connectTimeout: duration
  @@immutable defaultRequestTimeout: duration
}

HttpRequest {
    @@immutable method: HttpMethod
    @@immutable url: string
    @@nullable @@immutable body: bytes
    @@nullable @@immutable timeout: duration
    @@immutable headers: map<string, string>
}

HttpResponse {
    @@immutable statusCode: int
    @@immutable body: bytes
    @@immutable headers: map<string, string>
}

abstraction HttpClient {

    @@immutable configuration:HttpConfiguration
    
    @@async
    HttpResponse execute(request: HttpRequest)

    @@async
    close(): void

    @@async
    close(closeTimeout: duration): void
}
```

## Example

```
TODO
```
