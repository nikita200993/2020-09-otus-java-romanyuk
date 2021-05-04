package ru.otus.vcs.newversion.path;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

public class VCSPathTest {

    @Test
    void testEmpty() {
        Assertions.assertThat(VCSPath.isValidVCSPathString(""))
                .isFalse();
    }

    @Test
    void testCurDir1() {
        Assertions.assertThat(VCSPath.isValidVCSPathString("."))
                .isFalse();
    }

    @Test
    void testCurDir2() {
        Assertions.assertThat(VCSPath.isValidVCSPathString("abc/./dd"))
                .isFalse();
    }

    @Test
    void testParentDir1() {
        Assertions.assertThat(VCSPath.isValidVCSPathString(".."))
                .isFalse();
    }

    @Test
    void testParentDir2() {
        Assertions.assertThat(VCSPath.isValidVCSPathString("ddd/.."))
                .isFalse();
    }

    @Test
    void testNormalDir() {
        Assertions.assertThat(VCSPath.isValidVCSPathString("ddd/abc.jar"))
                .isTrue();
    }

    @Test
    void testName() {
        Assertions.assertThat(VCSPath.isValidVCSPathString("ddd"))
                .isTrue();
    }

    @Test
    void testToOsPath() {
        final var osPath = Path.of("a", "b");
        final var vcsPath= VCSPath.create(osPath.toString().replace(File.separator, VCSPath.getSeparator()));
        Assertions.assertThat(vcsPath.toOsPath())
                .isEqualTo(osPath);
    }

    @Test
    void testToUnixPath() {
        final var unixPath = "a/b";
        final var vcsPath= VCSPath.create(unixPath.replace("/", VCSPath.getSeparator()));
        Assertions.assertThat(vcsPath.toUnixPathString())
                .isEqualTo(unixPath);
    }

    @Test
    void testAbsolutePath() {
        final var absPath = "/f";
        Assertions.assertThat(VCSPath.isValidVCSPathString(absPath))
                .isFalse();
    }
}
