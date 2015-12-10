package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Bulk operation progress report
 *
 * @since v6.3.6
 */
public class BulkOperationProgressPage extends AbstractJiraPage
{
    @ElementBy (id = "acknowledge_submit")
    protected PageElement acknowledge;

    @Override
    public TimedCondition isAt()
    {
        return acknowledge.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public AbstractIssueNavigatorPage submit()
    {
        acknowledge.click();
        return pageBinder.bind(AgnosticIssueNavigator.class);
    }

    public ViewIssuePage submit(String issueKey)
    {
        acknowledge.click();
        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }
}
