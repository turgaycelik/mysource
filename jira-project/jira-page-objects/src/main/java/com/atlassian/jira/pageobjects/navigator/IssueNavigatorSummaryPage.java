package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import javax.annotation.Nullable;

/**
 * Represents the Issue Navigator page in the summary mode.
 *
 * @since 5.2
 * @deprecated since 6.2. Use {@link com.atlassian.jira.pageobjects.navigator.BasicSearch} instead.
 */
public class IssueNavigatorSummaryPage extends AbstractIssueNavigatorPage
{

    @ElementBy(id = "viewfilter")
    /** @deprecated this element will never exist. */
    protected PageElement summaryLink;


    @Nullable
    protected Long filterId;

    public IssueNavigatorSummaryPage()
    {
    }

    public IssueNavigatorSummaryPage(Long filterId)
    {
        this.filterId = filterId;
    }

    @Override
    public String getUrl()
    {
        if (filterId != null)
        {
            return "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId;
        } else
        {
            return "/secure/IssueNavigator!switchView.jspa?mode=hide&createNew=true";
        }
    }
}
