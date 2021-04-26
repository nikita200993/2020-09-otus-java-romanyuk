package ru.otus.vcs.newversion.gitrepo.errors;

public enum CheckoutBranchError {
    TargetBranchNotExists,
    SameCommitOnTarget,
    NoCommitsOnTargetBranch,
    UncommittedStagedChanges,
    MergeInProgress
}
