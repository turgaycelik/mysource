package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * @since v6.1
 */
public class IndexProgressPage extends AbstractJiraPage
{
    @ElementBy (name = "jiraform")
    PageElement form;

    @Override
    public TimedCondition isAt()
    {
        return form.timed().hasText("Re-Indexing");
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Should not navigate to this page");
    }
}
