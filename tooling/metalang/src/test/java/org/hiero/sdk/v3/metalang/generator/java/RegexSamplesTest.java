package org.hiero.sdk.v3.metalang.generator.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegexSamplesTest {

    @ParameterizedTest
    @ValueSource(strings = {"^/[^\\s]*$", "^[a-z]{3,5}$", "^\\d+(\\.\\d+)?$", "^0x[0-9a-fA-F]{40}$",
            "^(foo|bar)-\\w+$", "^(?:ab)+c?$", "^\\S+@\\S+\\.\\S+$", "^[^a-zA-Z0-9]$", "^a.b$", "^\\$\\{x\\}$",
            "^[\\w-]{2}$", "^\\s\\t\\n\\r\\W\\D$", "^[\\d\\s]+$", "\\bword\\b", "^x{2}?$", "^[-a]$", "^[]a]$",
            "^\\A\\z$", "^[\\t\\n\\r]$", "^[\\.\\-]$"})
    void shouldDeriveAnAcceptedString(final String regex) {
        assertThat(RegexSamples.accepted(regex)).hasValueSatisfying(s -> assertThat(Pattern.compile(regex)
                .matcher(s).find()).isTrue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"^(?=a)a$", "^(a)\\1$", "^\\p{Lu}$", "^[[a]]$", "^[\\D]$", "^\\cA$", "^[\\pL]$",
            "^[a-\\d]$", "^(a$", "^a{2$"})
    void shouldFallBackOrGiveUpOnUnsupportedConstructs(final String regex) {
        // never a wrong value: either verified or empty
        RegexSamples.accepted(regex).ifPresent(s -> assertThat(Pattern.compile(regex).matcher(s).find()).isTrue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[", "a{2,1}"})
    void shouldNotSampleInvalidPatterns(final String regex) {
        assertThat(RegexSamples.accepted(regex)).isEmpty();
        assertThat(RegexSamples.rejected(regex)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"^/[^\\s]*$", "^[a-z]+$", "\\d", "^$", "a"})
    void shouldDeriveARejectedString(final String regex) {
        assertThat(RegexSamples.rejected(regex)).hasValueSatisfying(s -> assertThat(Pattern.compile(regex)
                .matcher(s).find()).isFalse());
    }

    @ParameterizedTest
    @ValueSource(strings = {".*", "", "^.*$"})
    void shouldNotRejectWhatEverythingMatches(final String regex) {
        assertThat(RegexSamples.rejected(regex)).isEmpty();
    }
}
