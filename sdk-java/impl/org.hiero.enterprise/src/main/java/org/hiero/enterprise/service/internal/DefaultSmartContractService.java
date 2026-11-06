package org.hiero.enterprise.service.internal;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import org.hiero.common.Page;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.enterprise.service.contract.Contract;
import org.hiero.enterprise.service.contract.ContractCallResult;
import org.hiero.enterprise.service.contract.ContractCreateParams;
import org.hiero.enterprise.service.contract.ContractExecuteResult;
import org.hiero.enterprise.service.contract.SmartContractService;
import org.hiero.evm.AbiFunction;
import org.hiero.evm.AbiTuple;
import org.hiero.evm.AbiValue;
import org.hiero.evm.EvmAddress;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.mirrornode.contract.ContractCallRequest;
import org.jspecify.annotations.Nullable;

/// The `SmartContractService` of a session. Arguments are encoded and results decoded with the ABI types of
/// `org.hiero.evm`. Calls without a transaction and gas estimates are sent to the Mirror Node. The consensus node
/// client has no transaction to create a contract or to call one yet, so `createContract…` and `executeFunction` fail
/// with a `ServiceException` after the arguments have been encoded. The lookups read the Mirror Node.
public final class DefaultSmartContractService extends SmartContractService {

    private final DefaultSession session;

    /// Creates the service of a session.
    ///
    /// @param session the session
    public DefaultSmartContractService(final DefaultSession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public Contract createContract(final Address fileId, final AbiValue... constructorArguments) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        return createContract(fileId, ContractCreateParams.builder().constructorArguments(arguments(constructorArguments))
                .build());
    }

    @Override
    public Contract createContract(final byte[] contents, final AbiValue... constructorArguments) {
        Objects.requireNonNull(contents, "contents must not be null");
        return createContract(contents, ContractCreateParams.builder()
                .constructorArguments(arguments(constructorArguments)).build());
    }

    @Override
    public Contract createContract(final Address fileId, final ContractCreateParams params) {
        Objects.requireNonNull(fileId, "fileId must not be null");
        return create(params);
    }

    @Override
    public Contract createContract(final byte[] contents, final ContractCreateParams params) {
        Objects.requireNonNull(contents, "contents must not be null");
        return create(params);
    }

    private Contract create(final ContractCreateParams params) {
        Objects.requireNonNull(params, "params must not be null");
        encode(() -> new AbiTuple(params.constructorArguments()).encode());
        throw new ServiceException("The consensus node client offers no transaction to create a smart contract", null);
    }

    @Override
    public ContractExecuteResult executeFunction(final ContractId contractId, final AbiFunction function,
            final AbiValue... arguments) {
        Objects.requireNonNull(contractId, "contractId must not be null");
        callData(function, arguments);
        throw new ServiceException("The consensus node client offers no transaction to call a smart contract", null);
    }

    @Override
    public ContractCallResult callFunction(final ContractId contractId, final AbiFunction function,
            final AbiValue... arguments) {
        final byte[] result = HexFormat.of().parseHex(strip(mirrorCall(contractId, function, arguments, false)));
        final List<AbiValue> values;
        try {
            values = function.outputs().isEmpty() ? List.of() : function.decodeResult(result);
        } catch (final IllegalArgumentException e) {
            throw new ServiceException("The result of " + function.signature() + " cannot be decoded", e);
        }
        return new ContractCallResult(values, result);
    }

    @Override
    public long estimateGas(final ContractId contractId, final AbiFunction function, final AbiValue... arguments) {
        final String gas = strip(mirrorCall(contractId, function, arguments, true));
        try {
            return new BigInteger(gas.isEmpty() ? "0" : gas, 16).longValueExact();
        } catch (final ArithmeticException | NumberFormatException e) {
            throw new ServiceException("The Mirror Node returned no valid gas estimate: " + gas, e);
        }
    }

    @Override
    public @Nullable Contract findById(final ContractId contractId) {
        Objects.requireNonNull(contractId, "contractId must not be null");
        final org.hiero.mirrornode.contract.Contract contract =
                session.read(mirror -> mirror.contracts().findById(contractId));
        return contract == null ? null : new Contract(contract.contractId());
    }

    @Override
    public Page<Contract> findAll() {
        return MappedPage.of(session.read(mirror -> mirror.contracts().findAll()),
                contract -> new Contract(contract.contractId()));
    }

    private String mirrorCall(final ContractId contractId, final AbiFunction function, final AbiValue[] arguments,
            final boolean estimate) {
        Objects.requireNonNull(contractId, "contractId must not be null");
        final String data = "0x" + HexFormat.of().formatHex(callData(function, arguments));
        final ContractCallRequest request =
                new ContractCallRequest(null, data, estimate, null, null, null, evmAddress(contractId), null);
        return session.read(mirror -> mirror.contracts().call(request));
    }

    private static byte[] callData(final AbiFunction function, final AbiValue[] arguments) {
        Objects.requireNonNull(function, "function must not be null");
        final List<AbiValue> values = arguments(arguments);
        return encode(() -> function.encodeCall(values));
    }

    private static byte[] encode(final java.util.function.Supplier<byte[]> encoding) {
        try {
            return encoding.get();
        } catch (final IllegalArgumentException e) {
            throw new ServiceException("The arguments do not match the types of the function: " + e.getMessage(), e);
        }
    }

    /// The EVM address of a contract: its own, or the long-zero form of its number (4 bytes shard, 8 bytes realm,
    /// 8 bytes number).
    private static EvmAddress evmAddress(final ContractId contractId) {
        final EvmAddress address = contractId.evmAddress();
        if (address != null) {
            return address;
        }
        final Long num = Objects.requireNonNull(contractId.num(), "a contract id has a number or an EVM address");
        return EvmAddress.fromBytes(ByteBuffer.allocate(20).putInt((int) contractId.shard())
                .putLong(contractId.realm()).putLong(num).array());
    }

    private static String strip(final String hex) {
        return hex.startsWith("0x") || hex.startsWith("0X") ? hex.substring(2) : hex;
    }

    private static List<AbiValue> arguments(final AbiValue... arguments) {
        Objects.requireNonNull(arguments, "arguments must not be null");
        return List.copyOf(Arrays.asList(arguments));
    }
}
