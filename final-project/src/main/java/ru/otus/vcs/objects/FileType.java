package ru.otus.vcs.objects;

import ru.otus.utils.Contracts;

public enum FileType {

    Regular("reg"),
    Directory("dir");

    private final String fileTypeName;

    static FileType fromFileTypeName(final String fileTypeName) {
        switch (fileTypeName) {
            case "reg":
                return Regular;
            case "dir":
                return Directory;
            default:
                throw Contracts.unreachable();
        }
    }

    static boolean isValidFileTypeName(final String fileTypeName) {
        Contracts.requireNonNullArgument(fileTypeName);

        return Regular.fileTypeName.equals(fileTypeName) || Directory.fileTypeName.equals(fileTypeName);
    }

    FileType(final String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }

    public String getFileTypeName() {
        return fileTypeName;
    }
}
