package ru.otus.vcs.processor;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.UserException;
import ru.otus.vcs.objects.Blob;
import ru.otus.vcs.path.VCSFileName;
import ru.otus.vcs.path.VCSPath;
import ru.otus.vcs.repository.GitRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * Takes verified
 */
public class CommandProcessor {

    public void init(final Path path) {
        Contracts.requireNonNullArgument(path);

        try {
            if (!Files.isDirectory(path) || Files.list(path).count() != 0) {
                throw new UserException("Path '" + path + "' is not valid for init command. Should be empty directory.");
            }
            GitRepository.createNew(path);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't init repository.", ex);
        }
    }

    public void addToIndex(final Path path) {
        Contracts.requireNonNullArgument(path);

        try {
            if (Files.isRegularFile(path)) {
                throw new UserException("Can't add to index. Path '" + path + "' should locate regular file.");
            }
            final var gitRepo = GitRepository.find(path);
            if (gitRepo == null) {
                throw new UserException("Can't add to index file which is not under git repository.");
            }
            final var realPath = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
            final var relativePath = gitRepo.getWorkdir().relativize(realPath);
            if (relativePath.toString().startsWith(GitRepository.GITDIR)) {
                throw new UserException("Can't add to index. Path '" + path +
                        "' shouldn't be under repository dir " + GitRepository.GITDIR
                );
            }
            if (!VCSPath.isValidVCSPath(relativePath)) {
                throw new UserException("Path to repo file should should follow convention - all file names must" +
                        " follow this pattern " + VCSFileName.getPatternString());
            }
            final var vcsPath = VCSPath.create(relativePath);
            final var sha = gitRepo.saveGitObjectIfAbsentAndReturnSha(new Blob(Files.readAllBytes(realPath)));
            final var index = gitRepo.readIndex()
                    .withNewIndexEntry(vcsPath, sha);
            gitRepo.saveIndex(index);
        } catch (final IOException ex) {
            throw new UncheckedIOException("IO error while performing add operation.", ex);
        }
    }

}
