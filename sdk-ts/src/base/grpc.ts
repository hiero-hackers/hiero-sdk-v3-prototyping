/**
 * Minimal placeholder to express SPI dependency.
 * Concrete transport-layer details are language and runtime specific.
 * @see {@link file://../../spec/base/grpc.md}
 */
export interface MethodDescriptor {
    readonly serviceName: string;
    readonly methodName: string;
}
