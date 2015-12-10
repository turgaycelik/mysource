package com.atlassian.jira.issue;

import com.atlassian.annotations.ExperimentalApi;

import com.google.common.base.Function;

/**
 * Utility methods for {@link com.atlassian.jira.issue.IssueConstant}s.
 *
 * @since v6.2
 */
@ExperimentalApi
public final class IssueConstants
{
    private static Function<IssueConstant, String> GET_ID = new Function<IssueConstant, String>()
    {
        @Override
        public String apply(final IssueConstant input)
        {
            return input.getId();
        }
    };
    private static Function<IssueConstant, String> GET_I18N_NAME = new Function<IssueConstant, String>()
    {
        @Override
        public String apply(final IssueConstant input)
        {
            return input.getNameTranslation();
        }
    };

    private IssueConstants() {}

    /**
     * Return a function that will return the id of the passed {@link com.atlassian.jira.issue.IssueConstant}.
     *
     * The function does not accept null arguments.
     *
     * @return a function that returns the id of the passed {@code IssueConstant}
     */
    public static Function<IssueConstant, String> getIdFunc()
    {
        return GET_ID;
    }

    /**
     * Return a function that will return the translated name of the passed {@link com.atlassian.jira.issue.IssueConstant}.
     *
     * The function does not accept null arguments.
     *
     * @return a function that returns the translated name of the passed {@code IssueConstant}
     */
    public static Function<IssueConstant, String> getTranslatedNameFunc()
    {
        return GET_I18N_NAME;
    }
}
