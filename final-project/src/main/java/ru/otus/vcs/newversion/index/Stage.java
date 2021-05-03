package ru.otus.vcs.newversion.index;

import ru.otus.utils.Contracts;

public enum Stage {
    normal(0),
    base(1),
    receiver(2),
    giver(3);

    private final int code;

    Stage(int code) {
        this.code = code;
    }

    public static Stage fromCode(final int code) {
        switch (code) {
            case 0:
                return normal;
            case 1:
                return base;
            case 2:
                return receiver;
            case 3:
                return giver;
            default:
                throw Contracts.unreachable("Bad code = " + code);
        }
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "Stage{" +
                "code=" + code +
                '}';
    }
}
