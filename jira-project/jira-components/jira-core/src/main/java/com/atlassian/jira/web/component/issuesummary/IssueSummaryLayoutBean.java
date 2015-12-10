package com.atlassian.jira.web.component.issuesummary;

/**
 * This bean is used to control the display properties of the IssueSummary, and works with
 * {@link IssueSummaryWebComponent} and issuesummary.vm to achieve this.
 * <p/>
 * It contains sensible defaults.
 */
public class IssueSummaryLayoutBean
{
    private final boolean isLongLayout;

    /**
     * @param isLongLayout  Whether to show all the details, or just a short view of the issue summary
     */
    public IssueSummaryLayoutBean(boolean isLongLayout)
    {
        this.isLongLayout = isLongLayout;
    }

    public boolean isLongLayout()
    {
        return isLongLayout;
    }

    public boolean isShowIssueViews()
    {
        return isLongLayout;
    }

    public boolean isShowIssueDetails()
    {
        return true;
    }

    public boolean isShowIssueActions()
    {
        return false;
    }

    public boolean isShowIssueOperations()
    {
        return false;
    }
}
