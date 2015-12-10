package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Author: Geoffrey Wong
 * JIRA administration page to change the password of a user
 */
public class EditUserPasswordPage extends AbstractJiraPage
{
    private static final String URI_TEMPLATE = "/secure/admin/user/ViewUser.EditUser!default.jspa?name=";

    @ElementBy (name = "password")
    private PageElement password;

    @ElementBy (name = "confirm")
    private PageElement confirm;

    @ElementBy (id = "update_submit")
    private PageElement update;

    @ElementBy (id = "cancelButton")
    private PageElement cancel;

    private final String username;

    public EditUserPasswordPage(String username)
    {
        this.username = username;
    }

    public String getUrl()
    {
        return URI_TEMPLATE + username;
    }

    @Override
    public TimedCondition isAt()
    {
        return update.timed().isPresent();
    }
    
    public EditUserPasswordPage fillPasswordFields(String newPassword)
    {
        password.clear().type(newPassword);
        confirm.clear().type(newPassword);
        return this;
    }

    public ViewUserPage updatePassword()
    {
        update.click();
        return pageBinder.bind(ViewUserPage.class, username);
    }

    public ViewUserPage cancelUpdatePassword()
    {
        cancel.click();
        return pageBinder.bind(ViewUserPage.class, username);
    }
}
