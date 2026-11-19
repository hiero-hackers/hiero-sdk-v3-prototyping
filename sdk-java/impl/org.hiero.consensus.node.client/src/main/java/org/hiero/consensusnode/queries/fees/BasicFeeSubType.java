package org.hiero.consensusnode.queries.fees;


/// The fee sub types of the protocol the SDK is built for, with their codes.
public enum BasicFeeSubType implements FeeSubType {

    /// The resource cost for the transaction type has no additional attributes
    DEFAULT(0),
    /// The resource cost for the transaction type includes an operation on a fungible/common token
    TOKEN_FUNGIBLE_COMMON(1),
    /// The resource cost for the transaction type includes an operation on a non-fungible/unique token
    TOKEN_NON_FUNGIBLE_UNIQUE(2),
    /// The resource cost for the transaction type includes an operation on a fungible/common token with a custom fee
    /// schedule
    TOKEN_FUNGIBLE_COMMON_WITH_CUSTOM_FEES(3),
    /// The resource cost for the transaction type includes an operation on a non-fungible/unique token with a custom
    /// fee schedule
    TOKEN_NON_FUNGIBLE_UNIQUE_WITH_CUSTOM_FEES(4),
    /// The resource cost for the transaction type includes a ScheduleCreate containing a ContractCall.
    SCHEDULE_CREATE_CONTRACT_CALL(5),
    /// The resource cost for the transaction type includes a TopicCreate with custom fees.
    TOPIC_CREATE_WITH_CUSTOM_FEES(6),
    /// The resource cost for the transaction type includes a ConsensusSubmitMessage for a topic with custom fees.
    SUBMIT_MESSAGE_WITH_CUSTOM_FEES(7),
    /// The resource cost for the transaction type that includes a CryptoTransfer with hook invocations
    CRYPTO_TRANSFER_WITH_HOOKS(8);

    private final int code;

    BasicFeeSubType(final int code) {
        this.code = code;
    }

    /// Returns the `code` of this value.
    @Override
    public int code() {
        return code;
    }
}
