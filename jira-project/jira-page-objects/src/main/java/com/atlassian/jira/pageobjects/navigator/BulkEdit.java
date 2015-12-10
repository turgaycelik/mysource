package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Bulk Edit page.
 *
 * @since v5.1
 */
public class BulkEdit extends AbstractJiraPage implements Page
{
    @ElementBy (id = "issuetable")
    protected PageElement issueTable;

    @ElementBy (name = "all")
    protected CheckboxElement all;

    @ElementBy (id = "next")
    protected PageElement next;

    @Override
    public TimedCondition isAt()
    {
        return issueTable.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public BulkEdit selectAllIssues() {
        all.check();
        return this;
    }

    public ChooseOperation chooseOperation() {
        next.click();
        return pageBinder.bind(ChooseOperation.class);
    }
}
