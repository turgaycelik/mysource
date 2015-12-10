package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.IssuePickerPopup;
import com.atlassian.jira.pageobjects.components.userpicker.LegacyPicker;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents the move subtask parent page.
 *
 * @since v5.2
 */
public class MoveSubtaskParentPage extends AbstractJiraPage
{

    @ElementBy (id = "reparent_submit")
    protected PageElement reparentSubmit;

    @ElementBy (id = "parentIssue")
    protected PageElement parentIssue;

    private static final String URI = "/secure/MoveSubTaskParent!default.jspa";

    private final long issueId;
    @Override
    public TimedCondition isAt()
    {
        return reparentSubmit.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI + "?id=" + issueId;
    }

    public MoveSubtaskParentPage (long issueId)
    {
        this.issueId = issueId;
    }

    public IssuePickerPopup openIssuePickerPopup()
    {
        return (IssuePickerPopup) pageBinder.bind(IssuePickerPopup.class, legacyPicker()).open();
    }

    public LegacyPicker legacyPicker()
    {
        return pageBinder.bind(LegacyPicker.class, "parentIssue");
    }
}
