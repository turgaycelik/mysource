package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.api.model.TestCaseFailure;

public class HallelujahTestFailureException extends RuntimeException {
    private final String message;
    private final com.atlassian.buildeng.hallelujah.api.model.TestCaseFailure failure;

    public HallelujahTestFailureException(final String message, final TestCaseFailure failure) {
        this.message = message;
        this.failure = failure;
    }

    public TestCaseFailure getFailure()
    {
        return failure;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return "HallelujahTestFailureException{" +
                "message='" + message + '\'' +
                ", failure=" + failure +
                '}';
    }
}
