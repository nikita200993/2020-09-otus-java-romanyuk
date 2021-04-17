package ru.otus.vcs.utils;

import ru.otus.utils.Contracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public static String sha(final String string) {
        Contracts.requireNonNullArgument(string);

        throw new IllegalStateException();
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
}
