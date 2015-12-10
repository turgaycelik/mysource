package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.IssuePickerPopup;
import com.atlassian.jira.pageobjects.components.userpicker.LegacyPicker;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents the convert issue page
 *
 * @since v5.2
 */
public class ConvertIssuePage extends AbstractJiraPage
{
    private static final String URI = "/secure/ConvertIssue.jspa";

    private final long issueId;

    @ElementBy (id = "parentIssueKey")
    protected PageElement parentIssueKey;

    protected IssuePickerPopup parentIssuePicker;

    public ConvertIssuePage(long issueId)
    {
        this.issueId = issueId;
    }

    @Override
    public TimedCondition isAt()
    {
        return parentIssueKey.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI + "?id=" + issueId;
    }

    public IssuePickerPopup openIssuePickerPopup()
    {
        return (IssuePickerPopup) pageBinder.bind(IssuePickerPopup.class, legacyPicker()).open();
    }

    public LegacyPicker legacyPicker()
    {
        return pageBinder.bind(LegacyPicker.class, "parentIssueKey");
    }


}
