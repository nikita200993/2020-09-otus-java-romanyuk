package ru.otus.vcs.newversion.ref;

import ru.otus.utils.Contracts;

import java.util.regex.Pattern;

public final class BranchName extends Ref {

    public static final String MASTER = "master";

    /**
     * Should be compatible with {@link ru.otus.vcs.newversion.path.VCSFileName}.
     */
    private static final Pattern namePattern = Pattern.compile("^[a-zA-Z0-9-_]{1,200}$");

    public static String getNamePattern() {
        return namePattern.pattern().substring(1, namePattern.pattern().length() - 1);
    }

    private BranchName(String refString) {
        super(refString);
    }

    public String getBranchName() {
        return refString;
    }

    public static boolean isValidBranchName(final String branchName) {
        Contracts.requireNonNullArgument(branchName);

        return !ReservedRef.reservedRefStrings.contains(branchName)
                && !Sha1.isValidSha1HexString(branchName)
                && namePattern.asMatchPredicate().test(branchName);
    }

    public static BranchName create(final String refString) {
        Contracts.requireNonNullArgument(refString);
        Contracts.requireThat(isValidBranchName(refString));

        return new BranchName(refString);
    }

    public boolean isMaster() {
        return refString.equals(MASTER);
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
        final var otherRef = (BranchName) obj;
        return refString.equals(otherRef.refString);
    }

    @Override
    public String toString() {
        return "BranchName{ " + refString + " }";
    }
}
