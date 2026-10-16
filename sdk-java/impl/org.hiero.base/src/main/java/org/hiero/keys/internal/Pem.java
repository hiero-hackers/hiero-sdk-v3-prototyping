package org.hiero.keys.internal;

import java.util.Base64;
import java.util.List;
import org.hiero.keys.KeyType;

/// Reads and writes the PEM envelope (RFC 7468) of a DER-encoded key.
public final class Pem {

    private static final int LINE_LENGTH = 64;

    private Pem() {
    }

    /// Wraps DER bytes in a PEM envelope.
    ///
    /// @param keyType the key type, which selects the label
    /// @param der     the DER-encoded key
    /// @return the PEM text, ending with a line break
    public static String toPem(final KeyType keyType, final byte[] der) {
        final String label = label(keyType);
        final String base64 = Base64.getEncoder().encodeToString(der);
        final StringBuilder pem = new StringBuilder("-----BEGIN ").append(label).append("-----\n");
        for (int start = 0; start < base64.length(); start += LINE_LENGTH) {
            pem.append(base64, start, Math.min(start + LINE_LENGTH, base64.length())).append('\n');
        }
        return pem.append("-----END ").append(label).append("-----\n").toString();
    }

    /// Reads the DER bytes out of a PEM envelope.
    ///
    /// @param keyType the expected key type, which selects the label
    /// @param pem     the PEM text
    /// @return the DER-encoded key
    /// @throws IllegalArgumentException if the text is no PEM envelope with the label of that type
    public static byte[] fromPem(final KeyType keyType, final String pem) {
        final String label = label(keyType);
        final List<String> lines = pem.replace("\r", "\n").lines().map(String::strip).filter(l -> !l.isEmpty())
                .toList();
        if (lines.size() < 2 || !lines.getFirst().equals("-----BEGIN " + label + "-----")
                || !lines.getLast().equals("-----END " + label + "-----")) {
            throw new IllegalArgumentException("Not a PEM envelope with the label '" + label + "'");
        }
        try {
            return Base64.getDecoder().decode(String.join("", lines.subList(1, lines.size() - 1)));
        } catch (final IllegalArgumentException invalid) {
            throw new IllegalArgumentException("Invalid Base64 payload in the PEM envelope", invalid);
        }
    }

    private static String label(final KeyType keyType) {
        return switch (keyType) {
            case PUBLIC -> "PUBLIC KEY";
            case PRIVATE -> "PRIVATE KEY";
        };
    }
}
