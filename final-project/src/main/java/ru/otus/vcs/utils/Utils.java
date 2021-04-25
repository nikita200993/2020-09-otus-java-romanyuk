package ru.otus.vcs.utils;

import ru.otus.utils.Contracts;
import ru.otus.vcs.exception.InnerException;
import ru.otus.vcs.exception.UserException;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import static java.util.stream.Collectors.toMap;

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

    @Nullable
    public static Path toRealPath(final Path path) {
        Contracts.requireNonNullArgument(path);

        try {
            if (Files.exists(path)) {
                return path.toRealPath(LinkOption.NOFOLLOW_LINKS);
            } else {
                return null;
            }
        } catch (final IOException ex) {
            throw new InnerException("Can't transform to real path = " + path, ex);
        }
    }

    public static Path convertUserProvidedStringToPath(final String stringPath) {
        Contracts.requireNonNullArgument(stringPath);

        try {
            return Path.of(stringPath);
        } catch (final InvalidPathException ex) {
            throw new UserException("Provided by user path = " + stringPath + " has bad syntax.", ex);
        }
    }

    public static <K, V, U> Map<K, U> mapValuesToImmutableMap(
            final Map<K, V> mapToTransform,
            final Function<? super V, ? extends U> mapper) {
        Contracts.requireNonNullArgument(mapToTransform);
        Contracts.requireNonNullArgument(mapper);

        return Map.copyOf(
                mapToTransform.entrySet()
                        .stream()
                        .collect(toMap(Map.Entry::getKey, entry -> mapper.apply(entry.getValue())))
        );
    }
}
