package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BasicTransactionType`, generated from the specs.
@DisplayName("BasicTransactionType")
class BasicTransactionTypeTest {

    @Test
    @DisplayName("the constants have the attribute values of the specs")
    @Tag("testId:mirrornode.transaction.BasicTransactionType/constants")
    void shouldProvideTheAttributesOfTheConstants() {
        assertValue("ATOMICBATCH", BasicTransactionType.ATOMIC_BATCH.protocolName());
        assertValue("CONSENSUSCREATETOPIC", BasicTransactionType.TOPIC_CREATE.protocolName());
        assertValue("CONSENSUSDELETETOPIC", BasicTransactionType.TOPIC_DELETE.protocolName());
        assertValue("CONSENSUSSUBMITMESSAGE", BasicTransactionType.TOPIC_MESSAGE_SUBMIT.protocolName());
        assertValue("CONSENSUSUPDATETOPIC", BasicTransactionType.TOPIC_UPDATE.protocolName());
        assertValue("CONTRACTCALL", BasicTransactionType.CONTRACT_CALL.protocolName());
        assertValue("CONTRACTCREATEINSTANCE", BasicTransactionType.CONTRACT_CREATE.protocolName());
        assertValue("CONTRACTDELETEINSTANCE", BasicTransactionType.CONTRACT_DELETE.protocolName());
        assertValue("CONTRACTUPDATEINSTANCE", BasicTransactionType.CONTRACT_UPDATE.protocolName());
        assertValue("CRSPUBLICATION", BasicTransactionType.CRS_PUBLICATION.protocolName());
        assertValue("CRYPTOADDLIVEHASH", BasicTransactionType.LIVE_HASH_ADD.protocolName());
        assertValue("CRYPTOAPPROVEALLOWANCE", BasicTransactionType.ACCOUNT_ALLOWANCE_APPROVE.protocolName());
        assertValue("CRYPTOCREATEACCOUNT", BasicTransactionType.ACCOUNT_CREATE.protocolName());
        assertValue("CRYPTODELETE", BasicTransactionType.ACCOUNT_DELETE.protocolName());
        assertValue("CRYPTODELETEALLOWANCE", BasicTransactionType.ACCOUNT_ALLOWANCE_DELETE.protocolName());
        assertValue("CRYPTODELETELIVEHASH", BasicTransactionType.LIVE_HASH_DELETE.protocolName());
        assertValue("CRYPTOTRANSFER", BasicTransactionType.CRYPTO_TRANSFER.protocolName());
        assertValue("CRYPTOUPDATEACCOUNT", BasicTransactionType.ACCOUNT_UPDATE.protocolName());
        assertValue("ETHEREUMTRANSACTION", BasicTransactionType.ETHEREUM.protocolName());
        assertValue("FILEAPPEND", BasicTransactionType.FILE_APPEND.protocolName());
        assertValue("FILECREATE", BasicTransactionType.FILE_CREATE.protocolName());
        assertValue("FILEDELETE", BasicTransactionType.FILE_DELETE.protocolName());
        assertValue("FILEUPDATE", BasicTransactionType.FILE_UPDATE.protocolName());
        assertValue("FREEZE", BasicTransactionType.FREEZE.protocolName());
        assertValue("HINTSKEYPUBLICATION", BasicTransactionType.HINTS_KEY_PUBLICATION.protocolName());
        assertValue("HINTSPARTIALSIGNATURE", BasicTransactionType.HINTS_PARTIAL_SIGNATURE.protocolName());
        assertValue("HINTSPREPROCESSINGVOTE", BasicTransactionType.HINTS_PREPROCESSING_VOTE.protocolName());
        assertValue("HISTORYPROOFKEYPUBLICATION", BasicTransactionType.HISTORY_PROOF_KEY_PUBLICATION.protocolName());
        assertValue("HISTORYPROOFSIGNATURE", BasicTransactionType.HISTORY_PROOF_SIGNATURE.protocolName());
        assertValue("HISTORYPROOFVOTE", BasicTransactionType.HISTORY_PROOF_VOTE.protocolName());
        assertValue("HOOKSTORE", BasicTransactionType.HOOK_STORE.protocolName());
        assertValue("LEDGERIDPUBLICATION", BasicTransactionType.LEDGER_ID_PUBLICATION.protocolName());
        assertValue("MIGRATIONROOTHASHVOTE", BasicTransactionType.MIGRATION_ROOT_HASH_VOTE.protocolName());
        assertValue("NODECREATE", BasicTransactionType.NODE_CREATE.protocolName());
        assertValue("NODEDELETE", BasicTransactionType.NODE_DELETE.protocolName());
        assertValue("NODESTAKEUPDATE", BasicTransactionType.NODE_STAKE_UPDATE.protocolName());
        assertValue("NODEUPDATE", BasicTransactionType.NODE_UPDATE.protocolName());
        assertValue("REGISTEREDNODECREATE", BasicTransactionType.REGISTERED_NODE_CREATE.protocolName());
        assertValue("REGISTEREDNODEDELETE", BasicTransactionType.REGISTERED_NODE_DELETE.protocolName());
        assertValue("REGISTEREDNODEUPDATE", BasicTransactionType.REGISTERED_NODE_UPDATE.protocolName());
        assertValue("SCHEDULECREATE", BasicTransactionType.SCHEDULE_CREATE.protocolName());
        assertValue("SCHEDULEDELETE", BasicTransactionType.SCHEDULE_DELETE.protocolName());
        assertValue("SCHEDULESIGN", BasicTransactionType.SCHEDULE_SIGN.protocolName());
        assertValue("STATESIGNATURETRANSACTION", BasicTransactionType.STATE_SIGNATURE.protocolName());
        assertValue("SYSTEMDELETE", BasicTransactionType.SYSTEM_DELETE.protocolName());
        assertValue("SYSTEMUNDELETE", BasicTransactionType.SYSTEM_UNDELETE.protocolName());
        assertValue("TOKENAIRDROP", BasicTransactionType.TOKEN_AIRDROP.protocolName());
        assertValue("TOKENASSOCIATE", BasicTransactionType.TOKEN_ASSOCIATE.protocolName());
        assertValue("TOKENBURN", BasicTransactionType.TOKEN_BURN.protocolName());
        assertValue("TOKENCANCELAIRDROP", BasicTransactionType.TOKEN_CANCEL_AIRDROP.protocolName());
        assertValue("TOKENCLAIMAIRDROP", BasicTransactionType.TOKEN_CLAIM_AIRDROP.protocolName());
        assertValue("TOKENCREATION", BasicTransactionType.TOKEN_CREATE.protocolName());
        assertValue("TOKENDELETION", BasicTransactionType.TOKEN_DELETE.protocolName());
        assertValue("TOKENDISSOCIATE", BasicTransactionType.TOKEN_DISSOCIATE.protocolName());
        assertValue("TOKENFEESCHEDULEUPDATE", BasicTransactionType.TOKEN_FEE_SCHEDULE_UPDATE.protocolName());
        assertValue("TOKENFREEZE", BasicTransactionType.TOKEN_FREEZE.protocolName());
        assertValue("TOKENGRANTKYC", BasicTransactionType.TOKEN_GRANT_KYC.protocolName());
        assertValue("TOKENMINT", BasicTransactionType.TOKEN_MINT.protocolName());
        assertValue("TOKENPAUSE", BasicTransactionType.TOKEN_PAUSE.protocolName());
        assertValue("TOKENREJECT", BasicTransactionType.TOKEN_REJECT.protocolName());
        assertValue("TOKENREVOKEKYC", BasicTransactionType.TOKEN_REVOKE_KYC.protocolName());
        assertValue("TOKENUNFREEZE", BasicTransactionType.TOKEN_UNFREEZE.protocolName());
        assertValue("TOKENUNPAUSE", BasicTransactionType.TOKEN_UNPAUSE.protocolName());
        assertValue("TOKENUPDATE", BasicTransactionType.TOKEN_UPDATE.protocolName());
        assertValue("TOKENUPDATENFTS", BasicTransactionType.TOKEN_UPDATE_NFTS.protocolName());
        assertValue("TOKENWIPE", BasicTransactionType.TOKEN_WIPE.protocolName());
        assertValue("UNCHECKEDSUBMIT", BasicTransactionType.UNCHECKED_SUBMIT.protocolName());
        assertValue("UTILPRNG", BasicTransactionType.PRNG.protocolName());
    }
}
