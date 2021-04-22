package ru.otus.vcs.ref;

import ru.otus.utils.Contracts;

public abstract class Ref {

    protected final String refString;

    protected Ref(final String refString) {
        Contracts.requireNonNullArgument(refString);

        this.refString = refString;
    }

    protected String getRefString() {
        return refString;
    }

    public static Ref create(final String refString) {
        Contracts.requireNonNullArgument(refString);
        Contracts.requireThat(isValidRefString(refString));

        if (Sha1.isValidRefString(refString)) {
            return Sha1.create(refString);
        } else if (BranchName.isValidBranchName(refString)) {
            return BranchName.create(refString);
        } else {
            return ReservedRef.forRefString(refString);
        }
    }

    public static boolean isValidRefString(final String refString) {
        Contracts.requireNonNullArgument(refString);

        return Sha1.isValidSha1HexString(refString)
                || ReservedRef.isValidReservedRefString(refString)
                || BranchName.isValidBranchName(refString);
    }

    public static String hint() {
        return "reserved ref " + ReservedRef.reservedRefStrings + " or valid sha1 or valid branch name";
    }

}
