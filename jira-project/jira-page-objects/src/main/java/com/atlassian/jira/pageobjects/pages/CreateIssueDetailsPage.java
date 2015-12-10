package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Details of the issue.
 *
 * @since v5.1
 */
public class CreateIssueDetailsPage extends AbstractJiraPage
{
    @ElementBy (id = "issue-create-submit")
    PageElement submit;

    @Override
    public TimedCondition isAt()
    {
        return submit.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
