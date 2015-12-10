package com.atlassian.jira.bc;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Simple implementation of a validation result.
 *
 * Consider using {@link com.atlassian.jira.bc.ServiceOutcome}, which avoids to have to create a new class.
 * <p>
 * Included in the jira-api module not because it should be used by plugin developers, but because it is extended by
 * inner classes on many Services eg see {@link com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult}
 *
 * @since v4.0
 */
@PublicApi
public class ServiceResultImpl implements ServiceResult
{
    private final ErrorCollection errorCollection;

    public ServiceResultImpl(ErrorCollection errorCollection)
    {
        Assertions.notNull("errorCollection", errorCollection);
        this.errorCollection = errorCollection;
    }

    public boolean isValid()
    {
        return !errorCollection.hasAnyErrors();
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }
}
