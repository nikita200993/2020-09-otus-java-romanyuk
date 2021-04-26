package ru.otus.vcs.newversion.gitrepo.errors;

public enum MergeError {
    GiverBranchNotExists,
    SameCommitOnGiverBranch,
    NoCommitsOnHeadBranch,
    MergeInProgress,
    MergeConflict,
    UncommittedStagedChanges
}
