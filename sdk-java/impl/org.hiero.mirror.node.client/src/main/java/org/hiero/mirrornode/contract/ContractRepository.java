package org.hiero.mirrornode.contract;

import java.util.concurrent.CompletionStage;
import org.hiero.common.Page;
import org.hiero.mirrornode.internal.MirrorNodeRest;
import org.hiero.ledger.ContractId;
import org.jspecify.annotations.Nullable;

public class ContractRepository {

    /// Creates a new `ContractRepository`.
    ///
    /// A repository created with this constructor is bound to no mirror node: its queries fail with
    /// `MirrorNodeException`. `ContractFactory.createRepository` creates a repository for a mirror node.
    public ContractRepository() {
    }

    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<Page<Contract>> findAll() {
        return MirrorNodeRest.unbound("ContractRepository");
    }

    /// The returned stage completes exceptionally with `MirrorNodeException` if the operation fails.
    public CompletionStage<@Nullable Contract> findById(final ContractId contractId) {
        return MirrorNodeRest.unbound("ContractRepository");
    }
}
