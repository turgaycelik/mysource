package com.atlassian.jira.pageobjects.pages.viewissue.people;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedQuery;

/**
 * Represents the people block on the view issue page.
 *
 * @since v5.0
 */
public class PeopleSection
{
    @ElementBy (id="assignee-val")
    private PageElement assignee;

    @ElementBy (id="reporter-val")
    private PageElement reporter;

    public String getAssignee()
    {
        return assignee.getText();
    }

    public TimedQuery<String> getTimedAssignee()
    {
        return assignee.timed().getText();
    }

    public String getReporter()
    {
        return reporter.getText();
    }
}
