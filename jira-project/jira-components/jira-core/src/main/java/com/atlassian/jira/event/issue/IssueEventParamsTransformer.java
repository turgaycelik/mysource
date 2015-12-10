package com.atlassian.jira.event.issue;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for classes that want to modify the parameters of {@link IssueEvent} objects.
 */
public interface IssueEventParamsTransformer
{
    /**
     * Transforms the given issue event parameters, returning the ones that should be used instead.
     * @param issueEventParams The parameters of the issue event.
     * @return The modifed map of parameters.
     */
    @Nonnull
    Map<String, Object> transformParams(@Nullable Map<String, Object> issueEventParams);
}
