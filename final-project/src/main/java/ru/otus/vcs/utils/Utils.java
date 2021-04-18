package ru.otus.vcs.utils;

import ru.otus.utils.Contracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class Utils {

    private Utils() {
        throw new IllegalStateException();
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
}
