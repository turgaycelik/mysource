package com.atlassian.jira.pageobjects.pages.admin.user;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Maps;

import static com.atlassian.pageobjects.elements.query.Conditions.and;

/**
 * Page for adding a new user
 *
 * @since v4.4
 */
public class AddUserPage extends AbstractJiraPage
{

    private static final String URI = "/secure/admin/user/AddUser!default.jspa";

    @ElementBy(id = "user-create")
    private PageElement createForm;
    
    @ElementBy(name = "username")
    private PageElement username;

    @ElementBy(name = "password")
    private PageElement password;

    @ElementBy(name = "confirm")
    private PageElement passwordConfirmation;

    @ElementBy(name = "fullname")
    private PageElement fullName;

    @ElementBy(name = "email")
    private PageElement email;

    @ElementBy(name = "sendEmail")
    private PageElement sendEmail;

    @ElementBy(id = "user-create-submit")
    private PageElement submit;

    @ElementBy (id = "user-create-cancel")
    private PageElement cancelButton;

    @Override
    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return and(username.timed().isPresent(), password.timed().isPresent(), fullName.timed().isPresent());
    }

    public AddUserPage addUser(final String username)
    {
        return addUser(username, username, username, username, username + "@example.com", false);
    }

    public AddUserPage addUser(final String username, final String password, final String fullName, final String email,
            final boolean receiveEmail)
    {
        return addUser(username, password, password, fullName, email, receiveEmail);
    }

    public AddUserPage addUser(final String username, final String password, final String confirmPassword,
            final String fullName, final String email, final boolean receiveEmail)
    {
        this.username.type(username);
        this.password.type(password);
        this.passwordConfirmation.type(confirmPassword);
        this.fullName.type(fullName);
        this.email.type(email);
        return setCheckbox(sendEmail, receiveEmail);
    }

    public UserBrowserPage createUser()
    {
        return createUser(UserBrowserPage.class);
    }

    public AddUserPage createUserExpectingError()
    {
        submit.click();
        Poller.waitUntilTrue(elementFinder.find(By.className("error")).timed().isPresent());
        return pageBinder.bind(AddUserPage.class);
    }

    public <T extends Page> T createUser(final Class<T> nextPage, final Object...args)
    {
        submit.click();
        // TODO https://studio.atlassian.com/browse/JPO-12
        // this is now a dialog so clicking submit results in some JS brain-farting that eventually leads to page re-load
        // in the mean time the page would be bound to the old cached content and.... boooom!
        // as a work-around until this is re-implemented as dialog we re-navigate to the UserBrowser page to make sure
        // the page object gets properly re-bound (-binded?:)
        return pageBinder.navigateToAndBind(nextPage, args);
    }
    
    /**
     * @return keys as id attributes of error containers and
     * values are error messages
     */
    public Map<String, String> getPageErrors()
    {
        final HashMap<String, String> errors = Maps.newHashMap();
        for (final PageElement error : createForm.findAll(By.className("error")))
        {
            errors.put(error.getAttribute("id"), error.getText());
        }
        return errors;
    }

    public UserBrowserPage cancelCreateUser()
    {
        cancelButton.click();
        return pageBinder.bind(UserBrowserPage.class);
    }

    private AddUserPage setCheckbox(final PageElement checkbox, final boolean state)
    {
        if (state)
        {
            checkbox.select();
        }
        else if (checkbox.isSelected())
        {
            checkbox.toggle();
        }
        return this;
    }
}
