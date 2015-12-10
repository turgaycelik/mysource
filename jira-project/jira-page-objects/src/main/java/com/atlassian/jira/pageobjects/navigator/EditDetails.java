package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Edit details for Bulk Edit.
 *
 * @since v5.1
 */
public class EditDetails extends AbstractJiraPage
{
    @ElementBy (id = "availableActionsTable")
    protected PageElement availableActionTable;

    @Override
    public TimedCondition isAt()
    {
        return availableActionTable.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
