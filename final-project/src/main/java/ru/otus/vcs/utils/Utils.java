package ru.otus.vcs.utils;

import ru.otus.utils.Contracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class Utils {

    private static final Predicate<String> SHA1 = Pattern.compile("^[a-f0-9]{40}$").asMatchPredicate();

    private Utils() {
        throw new IllegalStateException();
    }

    public static boolean isSha1(final String sha1) {
        Contracts.requireNonNullArgument(sha1);

        return SHA1.test(sha1);
    }

    public static byte[] compress(final byte[] original) {
        try {
            final var output = new ByteArrayOutputStream();
            try (var deflater = new DeflaterOutputStream(output)) {
                deflater.write(original);
            }
            return output.toByteArray();
        } catch (final IOException ex) {
            throw new IllegalStateException("Here can't be io error, because writing to byte array output stream", ex);
        }
    }

    public static byte[] decompress(final byte[] compressed) {
        try {
            final var output = new ByteArrayOutputStream();
            try (var inflater = new InflaterOutputStream(output)) {
                inflater.write(compressed);
            }
            return output.toByteArray();
        } catch (final IOException ex) {
            throw new IllegalStateException("Here can't be io error. Writing toy byte array output stream", ex);
        }
    }

    public static byte[] concat(final byte[] first, final byte[] second) {
        Contracts.requireNonNullArgument(first);
        Contracts.requireNonNullArgument(second);

        final byte[] concatResult = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, concatResult, first.length, second.length);
        return concatResult;
    }

    public static byte[] concat(final List<byte[]> byteArrays) {
        Contracts.requireNonNullArgument(byteArrays);
        final Optional<Integer> totalBytesOpt = byteArrays.stream()
                .map(byteArr -> byteArr.length)
                .reduce(Integer::sum);
        if (totalBytesOpt.isEmpty() || totalBytesOpt.get() == 0) {
            return new byte[0];
        }
        final var result = Arrays.copyOf(byteArrays.get(0), totalBytesOpt.get());
        if (byteArrays.size() == 1) {
            return result;
        }
        var arrayToConcat = byteArrays.get(1);
        int start = byteArrays.get(0).length;
        for (int i = 1; i < byteArrays.size(); i++) {
            System.arraycopy(arrayToConcat, 0, result, start, arrayToConcat.length);
        }
        return result;
    }

    public static int indexOf(final byte[] bytes, final byte searchValue, final int start, final int end) {
        Contracts.requireNonNullArgument(bytes);
        Contracts.requireThat(start < bytes.length);
        Contracts.requireThat(end <= bytes.length);

        for (int pos = start; pos < end; pos++) {
            if (bytes[pos] == searchValue) {
                return pos;
            }
        }
        return -1;
    }

    public static String utf8(final byte[] bytes) {
        Contracts.requireNonNullArgument(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static boolean isEmptyDir(final Path path) {
        Contracts.requireNonNullArgument(path);

        try {
            return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && Files.list(path).count() == 0;
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Path toReal(final Path path) {
        try {
            return path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] readBytes(final Path path) {
        Contracts.requireNonNullArgument(path);

        try {
            return Files.readAllBytes(path);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't read " + path + ".", ex);
        }
    }

    public static void writeBytes(final Path path, final byte[] bytes) {
        Contracts.requireNonNullArgument(path);
        Contracts.requireNonNullArgument(bytes);

        try {
            Files.write(path, bytes);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't read " + path + ".", ex);
        }
    }

    public static void delete(final Path path) {
        Contracts.requireNonNullArgument(path);

        try {
            Files.delete(path);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Can't delete path " + path + ".", ex);
        }
    }

    public static boolean isRegularFileNoFollow(final Path path) {
        Contracts.requireNonNullArgument(path);

        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    public static boolean isDirectoryNoFollow(final Path path) {
        Contracts.requireNonNullArgument(path);

        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }
}
