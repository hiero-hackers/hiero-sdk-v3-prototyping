package org.hiero.enterprise.service.internal;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import org.hiero.enterprise.service.contract.ParamSupplier;
import org.jspecify.annotations.Nullable;

/// The supplier of a parameter created by `ContractFactory`: its native contract value is the ABI encoding of the
/// parameter on its own, i.e. one 32-byte word for a static type and the length word followed by the padded data for a
/// dynamic type (`string`, `bytes`).
public final class AbiParamSupplier implements ParamSupplier<byte[]> {

    private final byte[] encoding;

    /// Creates the supplier of an encoded value.
    ///
    /// @param encoding the ABI encoding of the value
    public AbiParamSupplier(final byte[] encoding) {
        this.encoding = Objects.requireNonNull(encoding, "encoding must not be null").clone();
    }

    /// Returns the ABI encoding of the value.
    ///
    /// @return a copy of the encoding
    @Override
    public byte[] getNativeContractValue() {
        return encoding.clone();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof AbiParamSupplier other && Arrays.equals(encoding, other.encoding);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encoding);
    }

    @Override
    public String toString() {
        return "AbiParamSupplier[" + HexFormat.of().formatHex(encoding) + "]";
    }
}
