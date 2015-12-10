package com.atlassian.jira.pageobjects.pages.btf;

import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

/**
 * Page object implementation for the LoginPage in JIRA.
 *
 * @since 4.4
 */
public class JiraBtfLoginPage extends JiraLoginPage
{
    public static final String URI = "/login.jsp";

    public static final String USER_ADMIN = "admin";
    public static final String PASSWORD_ADMIN = "admin";

    @ElementBy (id = "login-form")
    protected PageElement loginForm;

    @ElementBy (className = "aui-message", within = "loginForm")
    protected Iterable<PageElement> messages;

    @ElementBy (name = "os_username")
    protected PageElement usernameField;

    @ElementBy (name = "os_password")
    protected PageElement passwordField;

    @ElementBy (name = "os_cookie")
    protected PageElement rememberMeTickBox;

    @ElementBy (id = "login-form-submit")
    protected PageElement loginButton;

    @ElementBy (name = "os_destination")
    protected PageElement redirect;

    @ElementBy (id = "sign-up-hint")
    protected PageElement signUpHint;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public <M extends Page> M loginAsSysAdmin(Class<M> nextPage, Object... args)
    {
        return login(USER_ADMIN, PASSWORD_ADMIN, nextPage, args);
    }

    @Override
    public <M extends Page> M loginAsSystemAdminAndFollowRedirect(Class<M> redirectPage, Object... args)
    {
        return loginAndFollowRedirect(USER_ADMIN, PASSWORD_ADMIN, redirectPage, args);
    }

    @Override
    protected final PageElement getRememberMeTickBox()
    {
        return rememberMeTickBox;
    }

    @Override
    protected final PageElement getLoginButton()
    {
        return loginButton;
    }

    @Override
    protected final PageElement getPasswordField()
    {
        return passwordField;
    }

    @Override
    protected final PageElement getUsernameField()
    {
        return usernameField;
    }

    @Override
    protected final PageElement getRedirect()
    {
        return redirect;
    }

    @Override
    public final PageElement getSignUpHint()
    {
        return signUpHint;
    }

    @Override
    public final PageElement getLoginForm()
    {
        return loginForm;
    }

    public final Iterable<PageElement> getMessages()
    {
        return messages;
    }

}