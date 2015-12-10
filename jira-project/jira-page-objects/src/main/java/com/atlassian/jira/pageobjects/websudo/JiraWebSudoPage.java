package com.atlassian.jira.pageobjects.websudo;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.btf.JiraBtfLoginPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.WebSudoPage;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class JiraWebSudoPage extends AbstractJiraPage implements JiraWebSudo, WebSudoPage
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy (id = "login-form-submit")
    private PageElement confirm;

    @ElementBy(id = "login-form-cancel")
    private PageElement loginCancel;

    @ElementBy(id = "login-form-authenticatePassword")
    private PageElement passwordField;

    private String webSudoDestination;

    public JiraWebSudoPage()
    {
    }

    public JiraWebSudoPage(String destination)
    {
        webSudoDestination = destination;
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("login-notyou")).timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        if (webSudoDestination != null)
        {
            return "/secure/admin/WebSudoAuthenticate.jspa?webSudoDestination=" + webSudoDestination;
        }
        else
        {
            return "/secure/admin/WebSudoAuthenticate.jspa";
        }
    }

    @Override
    public <T extends Page> T confirm(Class<T> targetPage)
    {
        return confirm(JiraBtfLoginPage.PASSWORD_ADMIN, targetPage);
    }

    @Override
    public <T extends Page> T confirm(String password, Class<T> targetPage)
    {
        submitPassword(password);
        return pageBinder.navigateToAndBind(targetPage);
    }

    public <T> T authenticate(Class<T> targetPage)
    {
        return authenticate(JiraBtfLoginPage.PASSWORD_ADMIN, targetPage);
    }

    @Override
    public <T> T authenticate(String password, Class<T> targetPage, Object... args)
    {
        submitPassword(password);
        return pageBinder.bind(targetPage, args);
    }

    @Override
    public void authenticate(String password)
    {
        submitPassword(password);
    }

    @Override
    public JiraWebSudoPage authenticateFail(String password)
    {
        submitPassword(password);
        return pageBinder.bind(JiraWebSudoPage.class, webSudoDestination);
    }

    /**
     * This handles cancelling the web sudo authentication and binding to the expected page
     * that cancel should navigate to.
     * @param expectedPage The expected page to navigate to after cancelling.
     * @param args optional arguments to be passed to {@link PageBinder#bind(Class, Object...)}
     * @param <T> The page class type.
     * @return The page object that is expected to navigate to after cancelling.
     */
    @Override
    public <T> T cancel(Class<T> expectedPage, Object... args)
    {
        loginCancel.click();
        return pageBinder.bind(expectedPage, args);
    }

    @Override
    public void cancel()
    {
        loginCancel.click();
    }

    private void submitPassword(String password)
    {
        passwordField.clear();
        if (StringUtils.isNotBlank(password))
        {
            passwordField.type(password);
        }
        confirm.click();
    }
}
