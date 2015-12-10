package com.atlassian.jira.pageobjects.pages.ondemand;

import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import org.openqa.selenium.By;

/**
 * Page object implementation for the LoginPage in JIRA.
 *
 * @since 4.4
 */
public class JiraOdLoginPage extends JiraLoginPage
{
    private static final String URI = "/login";

    public static final String USER_SYSADMIN = "sysadmin";
    public static final String PASSWORD_SYSADMIN = "sysadmin";

    @ElementBy (id = "form-crowd-login")
    protected PageElement loginForm;

    @ElementBy (name = "username")
    protected PageElement usernameField;

    @ElementBy (name = "password")
    protected PageElement passwordField;

    @ElementBy (name = "remember-me")
    protected PageElement rememberMeTickBox;

    @ElementBy (id = "login")
    protected PageElement loginButton;

    @ElementBy (name = "dest-url")
    protected PageElement redirect;

    @ElementBy (id = "signup-enabled")
    protected PageElement signUpHint;

    @ElementBy (className = "aui-message")
    protected Iterable<PageElement> messages;


    @Override
    public String getUrl()
    {
        return URI;
    }

    @Override
    public PageElement getSignUpHint()
    {
        return signUpHint;
    }

    @Override
    public PageElement getLoginForm()
    {
        return loginForm;
    }

    @Override
    public Iterable<PageElement> getMessages()
    {
        return messages;
    }

    @Override
    protected PageElement getLoginButton()
    {
        return loginButton;
    }

    @Override
    protected PageElement getRedirect()
    {
        return redirect;
    }

    @Override
    protected PageElement getRememberMeTickBox()
    {
        return rememberMeTickBox;
    }

    @Override
    protected PageElement getUsernameField()
    {
        return usernameField;
    }

    @Override
    protected PageElement getPasswordField()
    {
        return passwordField;
    }

    public boolean checkIfPrivateSignUpIsVisible()
    {
        final PageElement disabledSignUp = loginForm.find(By.id("signup-disabled"), TimeoutType.AJAX_ACTION);
        Poller.waitUntilTrue(disabledSignUp.timed().isVisible());
        return (disabledSignUp.isVisible() && !signUpHint.isVisible());
    }

    @Override
    public <M extends Page> M loginAsSysAdmin(Class<M> nextPage, Object... args)
    {
        return login(USER_SYSADMIN, PASSWORD_SYSADMIN, nextPage, args);
    }

    public <M extends Page> M loginAsAdmin(Class<M> nextPage, Object... args)
    {
        return login(USER_ADMIN, PASSWORD_ADMIN, nextPage, args);
    }

    @Override
    public <M extends Page> M loginAsSystemAdminAndFollowRedirect(Class<M> redirectPage, Object... args)
    {
        return loginAndFollowRedirect(USER_SYSADMIN, PASSWORD_SYSADMIN, redirectPage, args);
    }

    @Override
    public DashboardPage loginAndGoToHome(String username, String password)
    {
        return login(username, password, DashboardPage.class);
    }

    @Override
    public DashboardPage loginAsSysadminAndGoToHome()
    {
        return loginAsSysAdmin(DashboardPage.class);
    }

    public String getRedirectUrl()
    {
        return getRedirect().getAttribute("value");
    }


}