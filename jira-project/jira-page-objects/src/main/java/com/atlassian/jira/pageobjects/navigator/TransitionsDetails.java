package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

/**
 * Transitions issues.
 *
 * @since v5.1
 */
public class TransitionsDetails extends AbstractJiraPage
{
    @ElementBy (id = "workflow_0")
    protected PageElement workflowTable;

    @ElementBy (id = "next")
    protected PageElement next;

    @Override
    public TimedCondition isAt()
    {
        return workflowTable.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public TransitionOperationDetails chooseWorkflowAction(final String name)
    {
        final String cssSelector = String.format("input[type=radio][value='%s']", name);
        elementFinder.find(By.cssSelector(cssSelector)).click();
        next.click();
        return pageBinder.bind(TransitionOperationDetails.class);
    }
}
