/**
 * Minimal placeholder to express SPI dependency.
 * Concrete transport-layer details are language and runtime specific.
 */
export interface MethodDescriptor {
    readonly serviceName: string;
    readonly methodName: string;
}
