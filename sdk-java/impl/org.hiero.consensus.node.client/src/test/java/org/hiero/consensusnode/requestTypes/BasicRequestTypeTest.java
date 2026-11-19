package org.hiero.consensusnode.requestTypes;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BasicRequestType`, generated from the specs.
@SuppressWarnings("deprecation")
@DisplayName("BasicRequestType")
class BasicRequestTypeTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:consensusnode.requestTypes.BasicRequestType/constants")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue(0, BasicRequestType.NONE.code());
        assertValue(1, BasicRequestType.CRYPTO_TRANSFER.code());
        assertValue(2, BasicRequestType.CRYPTO_UPDATE.code());
        assertValue(3, BasicRequestType.CRYPTO_DELETE.code());
        assertValue(4, BasicRequestType.CRYPTO_ADD_LIVE_HASH.code());
        assertValue(5, BasicRequestType.CRYPTO_DELETE_LIVE_HASH.code());
        assertValue(6, BasicRequestType.CONTRACT_CALL.code());
        assertValue(7, BasicRequestType.CONTRACT_CREATE.code());
        assertValue(8, BasicRequestType.CONTRACT_UPDATE.code());
        assertValue(9, BasicRequestType.FILE_CREATE.code());
        assertValue(10, BasicRequestType.FILE_APPEND.code());
        assertValue(11, BasicRequestType.FILE_UPDATE.code());
        assertValue(12, BasicRequestType.FILE_DELETE.code());
        assertValue(13, BasicRequestType.CRYPTO_GET_ACCOUNT_BALANCE.code());
        assertValue(14, BasicRequestType.CRYPTO_GET_ACCOUNT_RECORDS.code());
        assertValue(15, BasicRequestType.CRYPTO_GET_INFO.code());
        assertValue(16, BasicRequestType.CONTRACT_CALL_LOCAL.code());
        assertValue(17, BasicRequestType.CONTRACT_GET_INFO.code());
        assertValue(18, BasicRequestType.CONTRACT_GET_BYTECODE.code());
        assertValue(19, BasicRequestType.GET_BY_SOLIDITY_ID.code());
        assertValue(20, BasicRequestType.GET_BY_KEY.code());
        assertValue(21, BasicRequestType.CRYPTO_GET_LIVE_HASH.code());
        assertValue(22, BasicRequestType.CRYPTO_GET_STAKERS.code());
        assertValue(23, BasicRequestType.FILE_GET_CONTENTS.code());
        assertValue(24, BasicRequestType.FILE_GET_INFO.code());
        assertValue(25, BasicRequestType.TRANSACTION_GET_RECORD.code());
        assertValue(26, BasicRequestType.CONTRACT_GET_RECORDS.code());
        assertValue(27, BasicRequestType.CRYPTO_CREATE.code());
        assertValue(28, BasicRequestType.SYSTEM_DELETE.code());
        assertValue(29, BasicRequestType.SYSTEM_UNDELETE.code());
        assertValue(30, BasicRequestType.CONTRACT_DELETE.code());
        assertValue(31, BasicRequestType.FREEZE.code());
        assertValue(32, BasicRequestType.CREATE_TRANSACTION_RECORD.code());
        assertValue(33, BasicRequestType.CRYPTO_ACCOUNT_AUTO_RENEW.code());
        assertValue(34, BasicRequestType.CONTRACT_AUTO_RENEW.code());
        assertValue(35, BasicRequestType.GET_VERSION_INFO.code());
        assertValue(36, BasicRequestType.TRANSACTION_GET_RECEIPT.code());
        assertValue(50, BasicRequestType.CONSENSUS_CREATE_TOPIC.code());
        assertValue(51, BasicRequestType.CONSENSUS_UPDATE_TOPIC.code());
        assertValue(52, BasicRequestType.CONSENSUS_DELETE_TOPIC.code());
        assertValue(53, BasicRequestType.CONSENSUS_GET_TOPIC_INFO.code());
        assertValue(54, BasicRequestType.CONSENSUS_SUBMIT_MESSAGE.code());
        assertValue(55, BasicRequestType.UNCHECKED_SUBMIT.code());
        assertValue(56, BasicRequestType.TOKEN_CREATE.code());
        assertValue(58, BasicRequestType.TOKEN_GET_INFO.code());
        assertValue(59, BasicRequestType.TOKEN_FREEZE_ACCOUNT.code());
        assertValue(60, BasicRequestType.TOKEN_UNFREEZE_ACCOUNT.code());
        assertValue(61, BasicRequestType.TOKEN_GRANT_KYC_TO_ACCOUNT.code());
        assertValue(62, BasicRequestType.TOKEN_REVOKE_KYC_FROM_ACCOUNT.code());
        assertValue(63, BasicRequestType.TOKEN_DELETE.code());
        assertValue(64, BasicRequestType.TOKEN_UPDATE.code());
        assertValue(65, BasicRequestType.TOKEN_MINT.code());
        assertValue(66, BasicRequestType.TOKEN_BURN.code());
        assertValue(67, BasicRequestType.TOKEN_ACCOUNT_WIPE.code());
        assertValue(68, BasicRequestType.TOKEN_ASSOCIATE_TO_ACCOUNT.code());
        assertValue(69, BasicRequestType.TOKEN_DISSOCIATE_FROM_ACCOUNT.code());
        assertValue(70, BasicRequestType.SCHEDULE_CREATE.code());
        assertValue(71, BasicRequestType.SCHEDULE_DELETE.code());
        assertValue(72, BasicRequestType.SCHEDULE_SIGN.code());
        assertValue(73, BasicRequestType.SCHEDULE_GET_INFO.code());
        assertValue(74, BasicRequestType.TOKEN_GET_ACCOUNT_NFT_INFOS.code());
        assertValue(75, BasicRequestType.TOKEN_GET_NFT_INFO.code());
        assertValue(76, BasicRequestType.TOKEN_GET_NFT_INFOS.code());
        assertValue(77, BasicRequestType.TOKEN_FEE_SCHEDULE_UPDATE.code());
        assertValue(78, BasicRequestType.NETWORK_GET_EXECUTION_TIME.code());
        assertValue(79, BasicRequestType.TOKEN_PAUSE.code());
        assertValue(80, BasicRequestType.TOKEN_UNPAUSE.code());
        assertValue(81, BasicRequestType.CRYPTO_APPROVE_ALLOWANCE.code());
        assertValue(82, BasicRequestType.CRYPTO_DELETE_ALLOWANCE.code());
        assertValue(83, BasicRequestType.GET_ACCOUNT_DETAILS.code());
        assertValue(84, BasicRequestType.ETHEREUM_TRANSACTION.code());
        assertValue(85, BasicRequestType.NODE_STAKE_UPDATE.code());
        assertValue(86, BasicRequestType.UTIL_PRNG.code());
        assertValue(87, BasicRequestType.TRANSACTION_GET_FAST_RECORD.code());
        assertValue(88, BasicRequestType.TOKEN_UPDATE_NFTS.code());
        assertValue(89, BasicRequestType.NODE_CREATE.code());
        assertValue(90, BasicRequestType.NODE_UPDATE.code());
        assertValue(91, BasicRequestType.NODE_DELETE.code());
        assertValue(92, BasicRequestType.TOKEN_REJECT.code());
        assertValue(93, BasicRequestType.TOKEN_AIRDROP.code());
        assertValue(94, BasicRequestType.TOKEN_CANCEL_AIRDROP.code());
        assertValue(95, BasicRequestType.TOKEN_CLAIM_AIRDROP.code());
        assertValue(100, BasicRequestType.STATE_SIGNATURE_TRANSACTION.code());
        assertValue(101, BasicRequestType.HINTS_KEY_PUBLICATION.code());
        assertValue(102, BasicRequestType.HINTS_PREPROCESSING_VOTE.code());
        assertValue(103, BasicRequestType.HINTS_PARTIAL_SIGNATURE.code());
        assertValue(104, BasicRequestType.HISTORY_ASSEMBLY_SIGNATURE.code());
        assertValue(105, BasicRequestType.HISTORY_PROOF_KEY_PUBLICATION.code());
        assertValue(106, BasicRequestType.HISTORY_PROOF_VOTE.code());
        assertValue(107, BasicRequestType.CRS_PUBLICATION.code());
        assertValue(108, BasicRequestType.ATOMIC_BATCH.code());
        assertValue(109, BasicRequestType.LAMBDA_S_STORE.code());
        assertValue(110, BasicRequestType.HOOK_DISPATCH.code());
        assertValue(111, BasicRequestType.HOOK_STORE.code());
        assertValue(112, BasicRequestType.LEDGER_ID_PUBLICATION.code());
        assertValue(113, BasicRequestType.REGISTERED_NODE_CREATE.code());
        assertValue(114, BasicRequestType.REGISTERED_NODE_UPDATE.code());
        assertValue(115, BasicRequestType.REGISTERED_NODE_DELETE.code());
        assertValue(116, BasicRequestType.MIGRATION_ROOT_HASH_VOTE.code());
    }
}
