package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Author: Geoffrey Wong
 * JIRA administration page to edit the user details (i.e. Full name and email) of a JIRA user
 */
public class EditUserDetailsPage extends AbstractJiraPage
{
    private static final String URI_TEMPLATE = "/secure/admin/user/EditUser!default.jspa?editName=";

    @ElementBy(id = "user-edit-username")
    private PageElement currentUserName;

    @ElementBy(id = "user-edit-fullName")
    private PageElement currentUserFullName;

    @ElementBy(id = "user-edit-email")
    private PageElement currentUserEmail;

    @ElementBy(id = "user-edit-submit")
    private PageElement update;

    @ElementBy(id = "user-edit-cancel")
    private PageElement cancel;

    @ElementBy(id = "user-edit-active")
    private PageElement activeUser;

    @ElementBy(cssSelector = "#user-edit h2")
    private PageElement formCaption;

    @ElementBy(cssSelector = "label[for=\"user-edit-fullName\"]")
    private PageElement fullNameLabel;

    @ElementBy(cssSelector = "label[for=\"user-edit-email\"]")
    private PageElement emailLabel;

    private final String username;

    public EditUserDetailsPage(final String username)
    {
        this.username = username;
    }

    @Override
    public String getUrl()
    {
        return URI_TEMPLATE + username;
    }

    @Override
    public TimedCondition isAt()
    {
        return update.timed().isPresent();
    }

    public String getCurrentUserFullName()
    {
        return currentUserFullName.getValue();
    }

    public String getCurrentUserEmail()
    {
        return currentUserEmail.getValue();
    }

    public boolean getIsActiveUser()
    {
        return activeUser.isSelected();
    }

    public EditUserDetailsPage setUserAsActive()
    {
        if (!getIsActiveUser())
        {
            activeUser.select();
        }

        return this;
    }

    public EditUserDetailsPage setUserAsInactive()
    {
        if (getIsActiveUser())
        {
            activeUser.click();
        }

        return this;
    }

    public EditUserDetailsPage fillUserName(final String newUserName)
    {
        currentUserName.clear().type(newUserName);
        return this;
    }

    public EditUserDetailsPage fillUserFullName(final String newUserFullName)
    {
        currentUserFullName.clear().type(newUserFullName);
        return this;
    }

    public EditUserDetailsPage fillUserEmail(final String newUserEmail)
    {
        currentUserEmail.clear().type(newUserEmail);
        return this;
    }

    public ViewUserPage cancelEdit()
    {
        cancel.click();
        return pageBinder.bind(ViewUserPage.class, username);
    }

    public ViewUserPage submit()
    {
        update.click();
        return pageBinder.bind(ViewUserPage.class, username);
    }

    public String getFormCaption()
    {
        return formCaption.getText().trim();
    }

    public String getFullNameLabel()
    {
        return fullNameLabel.getText().trim();
    }

    public String getEmailLabel()
    {
        return emailLabel.getText().trim();
    }
}
