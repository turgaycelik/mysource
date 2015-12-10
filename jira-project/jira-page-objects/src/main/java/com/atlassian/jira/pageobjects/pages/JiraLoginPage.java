package com.atlassian.jira.pageobjects.pages;

import com.atlassian.jira.pageobjects.elements.AuiMessage;
import com.atlassian.jira.pageobjects.elements.AuiMessages;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;

import com.google.common.collect.Iterables;

import org.openqa.selenium.By;

import static org.apache.commons.lang.StringUtils.stripToNull;

/**
 * Base class for each flavor of JIRA login page.
 *
 * @since v6.2
 */
public abstract class JiraLoginPage extends AbstractJiraPage implements LoginPage
{

    public final static String USER_ADMIN = "admin";
    public final static String PASSWORD_ADMIN = "admin";


    /**
     * The text field the username is put on login.
     */
    protected abstract PageElement getUsernameField();

    /**
     * The text field the password is put on login.
     */
    protected abstract PageElement getPasswordField();

    /**
     * The tick box for remember me cookie.
     */
    protected abstract PageElement getRememberMeTickBox();

    /**
     * The button to submit the login form.
     */
    protected abstract PageElement getLoginButton();

    /**
     * The element holding the redirect information.
     */
    protected abstract PageElement getRedirect();

    /**
     * The element containing the signup hint
     */
    public abstract PageElement getSignUpHint();

    /**
     * The login form
     */
    public abstract PageElement getLoginForm();

    /**
     * The aui messages in the login form
     */
    public abstract Iterable<PageElement> getMessages();


    public abstract <M extends Page> M loginAsSysAdmin(Class<M> nextPage, Object... args);

    public abstract <M extends Page> M loginAsSystemAdminAndFollowRedirect(Class<M> redirectPage, Object... args);


    public void performLoginSteps(final String username, final String password, final boolean rememberMe)
    {

        final PageElement usernameField = getUsernameField();
        usernameField.clear();
        usernameField.type(username);

        final PageElement passwordField = getPasswordField();
        passwordField.clear();
        passwordField.type(password);

        if (rememberMe)
        {
            getRememberMeTickBox().click();
        }
        getLoginButton().click();
    }

    @Override
    public TimedCondition isAt()
    {
        return getLoginButton().timed().isPresent();
    }

    public <M extends Page> M login(final String username, final String password, final Class<M> nextPage, final Object... args)
    {
        return login(username, password, false, nextPage, args);
    }

    public <M extends Page> M login(final User user, final Class<M> nextPage, final Object... args)
    {
        return login(user.getUserName(), user.getPassword(), nextPage, args);
    }

    public <M extends Page> M loginAndFollowRedirect(final User user, final Class<M> redirectPage, final Object... args)
    {
        return loginAndFollowRedirect(user.getUserName(), user.getPassword(), redirectPage, args);
    }

    public <M extends Page> M loginAndFollowRedirect(final String username, final String password, final Class<M> redirectPage, final Object... args)
    {
        return loginWithRedirect(username, password, false, true, redirectPage, args);
    }

    public DashboardPage loginAndGoToHome(String username, String password)
    {
        return login(username, password, DashboardPage.class);
    }

    public DashboardPage loginAsSysadminAndGoToHome()
    {
        return loginAsSysAdmin(DashboardPage.class);
    }


    public <M extends Page> M login(String username, String password, boolean rememberMe, Class<M> nextPage, Object... args)
    {
        performLoginSteps(username, password, rememberMe);

        if (HomePage.class.isAssignableFrom(nextPage))
        {
            return pageBinder.bind(nextPage, args);
        }
        else
        {
            return pageBinder.navigateToAndBind(nextPage, args);
        }
    }

    /**
     * Logs in a user and sends the browser to the next page
     *
     * @param username The username to login
     * @param password The password to login with
     * @param nextPage The next page to visit, which may involve changing the URL.  Cannot be null.
     * @param <M> The page type
     * @return The next page, fully loaded and initialized.
     */
    public <M extends Page> M login(final String username, final String password, final Class<M> nextPage)
    {
        return login(username, password, nextPage, new Object[] { });
    }

    /**
     * Logs in the default sysadmin user and sends the browser to the next page
     *
     * @param nextPage The next page to visit, which may involve changing the URL.  Cannot be null.
     * @param <M> The page type
     * @return The next page, fully loaded and initialized.
     */
    public <M extends Page> M loginAsSysAdmin(Class<M> nextPage)
    {
        return loginAsSysAdmin(nextPage, new Object[] { });
    }

    public <M extends Page> M loginWithRedirect(String username, String password, boolean rememberMe, boolean followRedirect, Class<M> nextPage, Object... args)
    {
        performLoginSteps(username, password, rememberMe);

        final PageElement redirect = getRedirect();
        followRedirect = followRedirect && redirect.isPresent() && stripToNull(redirect.getValue()) != null;
        if (HomePage.class.isAssignableFrom(nextPage) || followRedirect)
        {
            return pageBinder.bind(nextPage, args);
        }
        else
        {
            final DelayedBinder<M> delayedPage = pageBinder.delayedBind(nextPage, args);
            if (delayedPage.canBind())
            {
                return delayedPage.bind();
            }
            else
            {
                return pageBinder.navigateToAndBind(nextPage, args);
            }
        }
    }

    /**
     * Get first error in the form. If there is no errors, this element's {@link com.atlassian.pageobjects.elements.PageElement#isPresent()}
     * method will return <code>false</code>.
     *
     * @return element representing the first error on the page
     */
    public PageElement getError()
    {
        return getLoginForm().find(By.cssSelector(AuiMessages.AUI_MESSAGE_ERROR_SELECTOR));
    }

    public Iterable<PageElement> getErrors()
    {
        return Iterables.filter(getMessages(), AuiMessages.isAuiMessageOfType(AuiMessage.Type.ERROR));
    }

    public boolean hasErrors()
    {
        return !Iterables.isEmpty(getErrors());
    }
}
