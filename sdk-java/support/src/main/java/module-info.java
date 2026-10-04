/// Support types of the Hiero SDK that the API generated from the specs uses but the specs do not declare: the
/// `@ThreadSafe` annotation and the streaming types (`HieroStream`, `StreamItem`, `HieroPublisher`,
/// `HieroSubscription`).
///
/// Packages:
/// - `org.hiero.sdk.annotation`
/// - `org.hiero.sdk.common`
module org.hiero.sdk.support {
    exports org.hiero.sdk.annotation;
    exports org.hiero.sdk.common;
}
