import { describe, it, expect } from 'vitest';
import { proto, com, Reader, Writer } from '../src/base/proto.js';
import Long from 'long';

describe('Proto API', () => {
    it('should export the expected namespaces', () => {
        expect(proto).toBeDefined();
        expect(com).toBeDefined();
        expect(Reader).toBeDefined();
        expect(Writer).toBeDefined();
    });

    it('should correctly instantiate and serialize a basic protobuf message', () => {
        // Instantiate a simple AccountID message
        const accountId = proto.AccountID.create({
            accountNum: Long.fromNumber(12345)
        });

        expect(accountId.accountNum).toBeDefined();

        // Encode the message
        const encoded = proto.AccountID.encode(accountId).finish();
        expect(encoded).toBeInstanceOf(Uint8Array);
        expect(encoded.length).toBeGreaterThan(0);

        // Decode the message
        const decoded = proto.AccountID.decode(encoded);
        
        // Use loose equality because decoded numbers might be typed as Long instances depending on protobufjs configs
        // In our case we forced Long, so we check using Number() cast for safety
        expect(Number(decoded.accountNum)).toBe(12345);
    });

    it('should have properly patched Long.js for 64-bit integer handling', () => {
        // HBAR amounts and timestamps rely on 64-bit integers.
        // If protobufjs/Long.js patching was successful, it should properly decode large numbers.
        
        // 9007199254740991 is Number.MAX_SAFE_INTEGER. 
        // We test a number that requires proper 64-bit support.
        const largeNum = "9007199254740995";
        
        const timestamp = proto.Timestamp.create({
            seconds: Long.fromString(largeNum)
        });

        const encoded = proto.Timestamp.encode(timestamp).finish();
        const decoded = proto.Timestamp.decode(encoded);

        // The decoded `seconds` should have a `toString()` that perfectly matches the large integer.
        expect(decoded.seconds.toString()).toBe(largeNum);
    });
});
