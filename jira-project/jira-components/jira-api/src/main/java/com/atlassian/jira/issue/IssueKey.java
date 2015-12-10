package com.atlassian.jira.issue;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.project.Project;

import com.google.common.base.Predicate;

/**
 * Represents an Issue Key, allowing you to parse it into its Project key and Issue number components.
 */
@ExperimentalApi
public final class IssueKey
{
    private final String projectKey;
    private final long issueNumber;

    public IssueKey(final String projectKey, final long issueNumber)
    {
        this.projectKey = projectKey;
        this.issueNumber = issueNumber;
    }

    public IssueKey(final Project project, final long issueNumber)
    {
        this.projectKey = project.getKey();
        this.issueNumber = issueNumber;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public long getIssueNumber()
    {
        return issueNumber;
    }

    @Override
    public String toString()
    {
        return projectKey + '-' + issueNumber;
    }

    /**
     * Creates an IssueKey object form the given issue key String.
     * <p>
     * This does some validation on the format of the given String.
     * It must contain a dash ('-'), and the substring following the final dash must be a valid number.
     * It validates that there is at least one character preceding the dash, but it does not validate that this is an
     * existing key on this instance of JIRA.
     *
     * @param key The issue key as a string. eg "ABC_X20-1193"
     * @return the corresponding IssueKey object.
     * @throws IllegalArgumentException if the given String is not a valid JIRA Issue key
     */
    public static IssueKey from(final String key)
    {
        final int dividerIndex = key.lastIndexOf("-");
        if (dividerIndex == -1)
        {
            throw new IllegalArgumentException("Invalid Issue Key '" + key + "' - it must contain a dash.");
        }
        if (dividerIndex == 0)
        {
            throw new IllegalArgumentException("Invalid Issue Key '" + key + "' - it must contain at least one character in the project key part.");
        }
        final long number = Long.parseLong(key.substring(dividerIndex + 1));
        return new IssueKey(key.substring(0, dividerIndex), number);
    }

    public static String format(final Project project, final long issueNumber)
    {
        return format(project.getKey(), issueNumber);
    }

    public static String format(final String projectKey, final long issueNumber)
    {
        return projectKey + '-' + issueNumber;
    }

    /**
     * Returns true if the key is considered "valid"
     *
     * @param issueKey the key
     * @return true if the key is considered "valid"
     */
    public static boolean isValidKey(final String issueKey)
    {
        try
        {
            from(issueKey);
            return true;
        }
        catch (RuntimeException ex)
        {
            return false;
        }
    }

    @ExperimentalApi
    public static class IsValidIssueKeyPredicate implements Predicate<String>
    {
        @Override
        public boolean apply(final String issueKey)
        {
            return IssueKey.isValidKey(issueKey);
        }
    }
}
