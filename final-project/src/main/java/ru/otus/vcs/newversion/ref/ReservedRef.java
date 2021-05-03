package ru.otus.vcs.newversion.ref;

import ru.otus.utils.Contracts;

import java.util.Set;

public class ReservedRef extends Ref {

    public static final Set<String> reservedRefStrings = Set.of("HEAD", "MERGE_HEAD");
    public static final ReservedRef head = new ReservedRef("HEAD");
    public static final ReservedRef mergeHead = new ReservedRef("MERGE_HEAD");

    private ReservedRef(final String refString) {
        super(refString);
    }

    public static ReservedRef forRefString(final String refString) {
        Contracts.requireNonNullArgument(refString);
        Contracts.requireThat(isValidReservedRefString(refString));

        if (refString.equals(head.getRefString())) {
            return head;
        } else {
            return mergeHead;
        }
    }

    public static boolean isValidReservedRefString(final String refString) {
        Contracts.requireNonNullArgument(refString);

        return reservedRefStrings.contains(refString);
    }

    @Override
    public String toString() {
        return "ReservedRef{ " + refString + " }";
    }
}
