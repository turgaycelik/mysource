package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.dialogs.AvatarDialog;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class EditIssueTypePage extends AbstractJiraPage
{
    @Inject
    protected PageBinder binder;

    @ElementBy (id = "name")
    private PageElement nameElement;
    @ElementBy (id = "description")
    private PageElement descriptionElement;
    @ElementBy (id = "update_submit")
    private PageElement submitButton;
    @ElementBy (className = "jira-avatar-picker-trigger")
    private PageElement avatarPickerTrigger;

    private final String pageUri;

    public EditIssueTypePage(long issueTypeId)
    {
        pageUri = String.format("/secure/admin/EditIssueType!default.jspa?id=%d", issueTypeId);
    }

    public EditIssueTypePage setAvatar(final String id)
    {
        avatarPickerTrigger.click();
        final AvatarDialog avatarDialog = binder.bind(AvatarDialog.class, "project-avatar-picker");
        avatarDialog.setAvatar(id);

        return this;
    }
    public ViewIssueTypesPage submit()
    {
        submitButton.click();
        return binder.bind(ViewIssueTypesPage.class);
    }

    @Override
    public TimedCondition isAt()
    {
        final Conditions.CombinableCondition allElementsCondition =
                Conditions.and(
                        nameElement.timed().isPresent(),
                        descriptionElement.timed().isPresent(),
                        avatarPickerTrigger.timed().isPresent(),
                        submitButton.timed().isPresent());

        return allElementsCondition;
    }

    @Override
    public String getUrl()
    {
        return pageUri;
    }
}
