package ru.otus.vcs.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigTest {

    private static final String CONTENT_ONE = String.format(
            "%s = %s%n"
                    + "%s = %s%n"
                    + "%s = %s%n",
            GitConfig.BARE_KEY.getName(), "true",
            GitConfig.FILEMODE_KEY.getName(), "false",
            GitConfig.REPO_VER_KEY.getName(), "2"
    );

    private static final String CONTENT_WITH_NO_NEWLINE = GitConfig.REPO_VER_KEY.getName() + " = " + 3;

    private static final String CONTENT_WITH_BLANK_LINE = String.format(
            "%n%s = %s%n",
            GitConfig.REPO_VER_KEY.getName(),
            3
    );

    @Test
    void testConfigParsing() {
        final var config = GitConfig.createFromString(CONTENT_ONE);
        Assertions.assertThat(config.get(GitConfig.BARE_KEY))
                .isTrue();
        Assertions.assertThat(config.get(GitConfig.FILEMODE_KEY))
                .isFalse();
        Assertions.assertThat(config.get(GitConfig.REPO_VER_KEY))
                .isEqualTo(2);
    }

    @Test
    void testConfigParsingWithNoNewLine() {
        final var config = GitConfig.createFromString(CONTENT_WITH_NO_NEWLINE);
        Assertions.assertThat(config.get(GitConfig.REPO_VER_KEY))
                .isEqualTo(3);
    }

    @Test
    void testConfigParsingWithBlankLine() {
        final var config = GitConfig.createFromString(CONTENT_WITH_BLANK_LINE);
        Assertions.assertThat(config.get(GitConfig.REPO_VER_KEY))
                .isEqualTo(3);
    }
}
