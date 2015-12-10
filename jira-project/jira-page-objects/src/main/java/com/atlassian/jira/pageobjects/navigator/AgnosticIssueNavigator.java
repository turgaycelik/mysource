package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.pageobjects.Page;

/**
 * For when you need access to the issue nav, but don't know or care what mode it's in.
 *
 * @since v6.0
 */
public class AgnosticIssueNavigator extends AbstractIssueNavigatorPage implements Page
{
    private final Long filterId;

    public AgnosticIssueNavigator()
    {
        filterId = null;
    }

    public AgnosticIssueNavigator(Long filterId)
    {
        this.filterId = filterId;
    }

    @Override
    public String getUrl()
    {
        if (filterId != null)
        {
            return "/issues/?filter=" + filterId;
        }
        else
        {
            return "/issues/?jql=";
        }
    }
}
