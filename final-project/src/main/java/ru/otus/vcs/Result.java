package ru.otus.vcs;

import ru.otus.utils.Contracts;

import javax.annotation.Nullable;

public class Result<ResultType, ErrorCodeType extends Enum<ErrorCodeType>> {
    @Nullable
    private final ResultType result;
    @Nullable
    private final ErrorCodeType errorCode;

    private Result(@Nullable final ResultType result, @Nullable final ErrorCodeType errorCode) {
        this.result = result;
        this.errorCode = errorCode;
    }

    public static <ResultType, ErrorCodeType extends Enum<ErrorCodeType>>
    Result<ResultType, ErrorCodeType> success(final ResultType result) {
        Contracts.requireNonNullArgument(result);
        return new Result<>(result, null);
    }

    public static <ResultType, ErrorCodeType extends Enum<ErrorCodeType>>
    Result<ResultType, ErrorCodeType> fail(final ErrorCodeType errorCode) {
        Contracts.requireNonNullArgument(errorCode);
        return new Result<>(null, errorCode);
    }

    public boolean isSuccess() {
        return result != null;
    }

    public boolean isError() {
        return errorCode != null;
    }

    public ResultType getResult() {
        Contracts.requireNonNull(result);
        return result;
    }

    public ErrorCodeType getErrorCode() {
        Contracts.requireNonNull(errorCode);
        return errorCode;
    }
}
