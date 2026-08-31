// Source: spec/base/keys.md (keys#KeyFormat).

package org.hiero.sdk.v3.keys;

/** Combined key container and encoding. */
public enum KeyFormat {
    /** PKCS #8 encoded with DER. */
    PKCS8_WITH_DER(KeyContainer.PKCS8, KeyEncoding.DER),
    /** SPKI encoded with DER. */
    SPKI_WITH_DER(KeyContainer.SPKI, KeyEncoding.DER),
    /** PKCS #8 encoded with PEM. */
    PKCS8_WITH_PEM(KeyContainer.PKCS8, KeyEncoding.PEM),
    /** SPKI encoded with PEM. */
    SPKI_WITH_PEM(KeyContainer.SPKI, KeyEncoding.PEM);
    private final KeyContainer container;
    private final KeyEncoding encoding;
    KeyFormat(KeyContainer container, KeyEncoding encoding) { this.container = container; this.encoding = encoding; }
    /** Returns the container. */ public KeyContainer container() { return container; }
    /** Returns the encoding. */ public KeyEncoding encoding() { return encoding; }
}
