package org.hiero.blocknode.status;

import java.util.List;
import java.util.Objects;

/// The version of a plugin of a block node.
///
/// @param pluginId the id of the plugin, by default the class name of the plugin
/// @param softwareVersion the version of the plugin
/// @param featureNames the features the plugin provides; may be empty
public record PluginVersion(String pluginId, SemanticVersion softwareVersion, List<String> featureNames) {

    /// Creates a new `PluginVersion`.
    ///
    /// @throws NullPointerException if a required value is `null`
    public PluginVersion {
        Objects.requireNonNull(pluginId, "pluginId must not be null");
        Objects.requireNonNull(softwareVersion, "softwareVersion must not be null");
        Objects.requireNonNull(featureNames, "featureNames must not be null");
        featureNames = List.copyOf(featureNames);
    }
}
