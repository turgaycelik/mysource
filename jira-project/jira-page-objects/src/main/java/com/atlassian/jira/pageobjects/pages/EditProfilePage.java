package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class EditProfilePage extends AbstractJiraPage
{
    private static final String URI = "/secure/EditProfile.jspa";

    @ElementBy(id = "edit-profile")
    private PageElement editForm;

    @ElementBy(id = "edit-profile-fullname")
    private PageElement fullnameInput;

    @ElementBy(id = "edit-profile-email")
    private PageElement emailInput;

    @ElementBy(id = "edit-profile-password")
    private PageElement passwordInput;

    @ElementBy(id = "edit-profile-submit")
    private PageElement submit;

    @Override
    public String getUrl()
    {
        return URI;
    }

    public EditProfilePage setFullname(final String string)
    {
        fullnameInput.clear().type(string);
        return this;
    }

    public EditProfilePage setPassword(final String string)
    {
        passwordInput.clear().type(string);
        return this;
    }

    public EditProfilePage setEmail(final String string)
    {
        emailInput.clear().type(string);
        return this;
    }

    public ViewProfilePage submit()
    {
        submit.click();
        Poller.waitUntilFalse(editForm.timed().isPresent());
        return pageBinder.bind(ViewProfilePage.class);
    }

    @Override
    public TimedCondition isAt()
    {
        return editForm.timed().isPresent();
    }
}