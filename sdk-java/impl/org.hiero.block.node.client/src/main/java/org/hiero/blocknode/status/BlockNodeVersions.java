package org.hiero.blocknode.status;

import java.util.List;
import java.util.Objects;

/// The versions of a block node.
///
/// @param streamProtoVersion the version of the block stream the block node supports
/// @param blockNodeVersion the version of the block node software
/// @param installedPluginVersions the plugins of the block node; may be empty
public record BlockNodeVersions(
        SemanticVersion streamProtoVersion,
        SemanticVersion blockNodeVersion,
        List<PluginVersion> installedPluginVersions) {

    /// Creates a new `BlockNodeVersions`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public BlockNodeVersions {
        Objects.requireNonNull(streamProtoVersion, "streamProtoVersion must not be null");
        Objects.requireNonNull(blockNodeVersion, "blockNodeVersion must not be null");
        Objects.requireNonNull(installedPluginVersions, "installedPluginVersions must not be null");
        installedPluginVersions = List.copyOf(installedPluginVersions);
    }
}
