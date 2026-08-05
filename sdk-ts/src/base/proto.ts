import * as $protobuf from "protobufjs/minimal.js";
import Long from "long";
import * as $proto from "./internal/generated-proto.js";

/**
 * Patch protobuf race condition between loading protobuf and Long.js libraries.
 * Inherited from V2 stability patch.
 */
(() => {
    const $util = $protobuf.util;
    if ($util.Long == null) {
        $util.Long = Long;
        // @ts-ignore - Patching an internal method that exists at runtime but isn't typed
        if ($protobuf.Reader._configure != null) {
            // @ts-ignore - Patching an internal method
            $protobuf.Reader._configure($protobuf.BufferReader);
        }
    }
})();

export const Reader = $protobuf.Reader;
export const Writer = $protobuf.Writer;

/**
 * Core protobuf namespaces exposing strict definitions compiled from the `.proto` files.
 * @see {@link file://../../spec/base/proto.md}
 */
export const proto = $proto.proto;
export const com = $proto.com;
export const google = $proto.google;
