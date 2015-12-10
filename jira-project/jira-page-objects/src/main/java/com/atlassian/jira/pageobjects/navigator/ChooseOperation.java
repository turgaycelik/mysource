package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Choose operation page from Bulk Edit wizard.
 *
 * @since v5.1
 */
public class ChooseOperation extends AbstractJiraPage implements Page
{
    @ElementBy (id = "bulk.edit.operation.name_id")
    protected PageElement editIssues;

    @ElementBy (id = "bulk.workflowtransition.operation.name_id")
    protected PageElement transitionIssues;

    @ElementBy (id = "next")
    protected PageElement next;

    @Override
    public TimedCondition isAt()
    {
        return editIssues.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public EditDetails editIssues() {
        editIssues.click();
        next.click();
        return pageBinder.bind(EditDetails.class);
    }

    public TransitionsDetails transitionIssues()
    {
        transitionIssues.click();
        next.click();
        return pageBinder.bind(TransitionsDetails.class);
    }
}
