export declare enum HttpMethod {
    GET = "GET",
    HEAD = "HEAD",
    POST = "POST",
    PUT = "PUT",
    PATCH = "PATCH",
    DELETE = "DELETE",
    OPTIONS = "OPTIONS",
    TRACE = "TRACE",
    CONNECT = "CONNECT"
}
export interface HttpConfiguration {
    readonly defaultHeaders: ReadonlyMap<string, string>;
    readonly connectTimeout: number;
    readonly defaultRequestTimeout: number;
}
export interface HttpRequest {
    readonly method: HttpMethod;
    readonly url: string;
    readonly body: Uint8Array | null;
    readonly timeout: number | null;
    readonly headers: ReadonlyMap<string, string>;
}
export interface HttpResponse {
    readonly statusCode: number;
    readonly body: Uint8Array;
    readonly headers: ReadonlyMap<string, string>;
}
export interface HttpClient {
    readonly configuration: HttpConfiguration;
    execute(request: HttpRequest): Promise<HttpResponse>;
    close(closeTimeout?: number): Promise<void>;
}
export declare function createHttpClient(configuration: HttpConfiguration): HttpClient;
