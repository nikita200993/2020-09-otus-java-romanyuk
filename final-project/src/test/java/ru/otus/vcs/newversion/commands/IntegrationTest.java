package ru.otus.vcs.newversion.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.otus.vcs.newversion.gitrepo.GitRepositoryException;
import ru.otus.vcs.newversion.gitrepo.GitRepositoryFactoryImpl;
import ru.otus.vcs.newversion.localrepo.LocalRepositoryException;
import ru.otus.vcs.newversion.utils.Utils;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegrationTest {

    @TempDir
    Path tempDir;
    private CommandProcessor commandProcessor;

    @BeforeEach
    void setup() {
        commandProcessor = new CommandProcessor(new GitRepositoryFactoryImpl(), tempDir);
        commandProcessor.init(tempDir.toString());
    }

    @Test
    void test1() {
        writeContent("1.txt", "1");
        writeContent("2.txt", "2");
        add("1.txt");
        add("2.txt");
        writeContent("2.txt", "22");
        assertThatThrownBy(() -> remove("2.txt"))
                .isInstanceOf(LocalRepositoryException.class);
        commit("1");
        checkoutFile("HEAD", "2.txt");
        assertThat(readContent("2.txt"))
                .isEqualTo("2");
        assertThatThrownBy(() -> commit("2"))
                .isInstanceOf(GitRepositoryException.class)
                .hasMessageContaining("No changes");
        remove("1.txt");
        assertThat(resolve("1.txt"))
                .doesNotExist();
        branch("dev");
        commit("2");
        checkout("dev");
        assertThat(resolve("1.txt"))
                .exists()
                .hasContent("1");
        writeContent("inner/3.txt", "3");
        add("inner/3.txt");
        commit("3");
        writeContent("2.txt", "22");
        assertThatThrownBy(() -> remove("2.txt"))
                .isInstanceOf(LocalRepositoryException.class)
                .hasMessageContaining("local changes");
        removeForce("2.txt");
        commit("4");
        assertThat(resolve("2.txt"))
                .doesNotExist();
        checkout("master");
        assertThat(readContent("2.txt"))
                .isEqualTo("2");
        assertThat(resolve("1.txt"))
                .doesNotExist();
        assertThat(resolve("inner/3.txt"))
                .doesNotExist();
        checkout("dev");
        assertThat(resolve("2.txt"))
                .doesNotExist();
        assertThat(readContent("inner/3.txt"))
                .isEqualTo("3");
        assertThat(readContent("1.txt"))
                .isEqualTo("1");
        checkout("master");
        delete("inner");
        checkout("dev");
        assertThat(readContent("inner/3.txt"))
                .isEqualTo("3");
        branch("feature");
        checkout("feature");
        writeContent("inner/inner2/4.txt", "4");
        add("inner/inner2/4.txt");
        commit("5");
        checkout("dev");
        assertThat(resolve("inner/inner2/4.txt"))
                .doesNotExist();
        checkout("feature");
        assertThat(readContent("inner/inner2/4.txt"))
                .isEqualTo("4");
        checkout("master");
        assertThat(resolve("inner/inner2/4.txt"))
                .doesNotExist();
        delete("inner/inner2");
        delete("inner/");
        checkout("feature");
        assertThat(readContent("inner/inner2/4.txt"))
                .isEqualTo("4");
        writeContent("inner/inner2/4.txt", "44");
        checkout("dev");
        assertThat(readContent("inner/inner2/4.txt"))
                .isEqualTo("44");
    }

    private void writeContent(final String stringPath, final String content) {
        final Path pathToFile = resolve(stringPath);
        Utils.createDirs(pathToFile.getParent());
        Utils.writeUtf8(pathToFile, content);
    }

    private String readContent(final String stringPath) {
        return Utils.readUtf8(resolve(stringPath));
    }

    private void delete(final String path) {
        Utils.delete(resolve(path));
    }

    private Path resolve(final String stringPath) {
        return tempDir.resolve(Path.of(toOsPath(stringPath)));
    }

    private void add(final String stringPath) {
        commandProcessor.add(toOsPath(stringPath));
    }

    private void remove(final String stringPath) {
        commandProcessor.remove(toOsPath(stringPath), RemoveOption.Normal);
    }

    private void removeForce(final String stringPath) {
        commandProcessor.remove(toOsPath(stringPath), RemoveOption.Force);
    }

    private void commit(final String message) {
        commandProcessor.commit(message);
    }

    private void checkout(final String refString) {
        commandProcessor.checkout(refString);
    }

    private void checkoutFile(final String refString, final String filePath) {
        commandProcessor.checkoutFile(refString, toOsPath(filePath));
    }

    private void branch(final String branchName) {
        commandProcessor.branch(branchName);
    }

    private String toOsPath(final String unixPath) {
        return unixPath.replace("/", File.separator);
    }
}