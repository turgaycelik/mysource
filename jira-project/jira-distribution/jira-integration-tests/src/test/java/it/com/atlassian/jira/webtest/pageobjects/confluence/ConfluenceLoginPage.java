package it.com.atlassian.jira.webtest.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;

import javax.inject.Inject;

/**
 * Page object implementation for the LoginPage in Confluence. We could not use the page object from Confluence
 * as it wanted to navigateAndBind if the nextPage was not the HomePage, and that is no good if we just want to bind.
 */
public class ConfluenceLoginPage implements Page
{
    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy (id = "os_username")
    private PageElement usernameField;

    @ElementBy (id = "os_password")
    private PageElement passwordField;

    @ElementBy (id = "loginButton")
    private PageElement loginButton;

    public String getUrl()
    {
        return "/login.action";
    }

    public <M extends Page> M login(final String username, final String password, final Class<M> nextPage)
    {
        // Only attempt to login if they are presented with login form
        if (usernameField.isPresent())
        {
            usernameField.clear().type(username);
            passwordField.clear().type(password);
            loginButton.click();
        }

        return pageBinder.bind(nextPage);
    }
}