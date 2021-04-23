package ru.otus.vcs.ref;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import ru.otus.utils.Contracts;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Sha1 extends Ref {

    private static final Pattern pattern = Pattern.compile("^[a-f0-9]{40}$");

    private Sha1(final String hex) {
        super(hex);
    }

    public static Sha1 hash(final String content) {
        Contracts.requireNonNullArgument(content);

        return hash(content.getBytes(StandardCharsets.UTF_8));
    }

    public static Sha1 hash(final byte[] content) {
        Contracts.requireNonNullArgument(content);

        final var hex = DigestUtils.sha1Hex(content)
                .toLowerCase(Locale.ROOT);
        Contracts.requireThat(pattern.asMatchPredicate().test(hex));
        return new Sha1(hex);
    }

    public static Sha1 create(final String hexString) {
        Contracts.requireNonNullArgument(hexString);
        Contracts.requireThat(isValidSha1HexString(hexString));

        return new Sha1(hexString);
    }

    public static boolean isValidSha1HexString(final String hexString) {
        Contracts.requireNonNullArgument(hexString);

        return pattern.asMatchPredicate().test(hexString);
    }

    public String getHexString() {
        return refString;
    }

    public byte[] getBinary() {
        try {
            return Hex.decodeHex(refString);
        } catch (final DecoderException ignored) {
            throw Contracts.unreachable("String is a valid hex string.");
        }
    }

    @Override
    public int hashCode() {
        return refString.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final var other = (Sha1) obj;
        return refString.equals(other.refString);
    }

    @Override
    public String toString() {
        return "Sha1{ " + refString + " }";
    }
}
