package com.atlassian.jira.pageobjects.gadgets;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.collect.Iterables;
import it.com.atlassian.gadgets.pages.Gadget;
import org.openqa.selenium.By;

/**
 * Represents login gadget in JIRA.
 *
 * @since v5.1
 */
public class LoginGadget extends Gadget
{

    /**
     * Login gadget is built-in and always has this ID.
     *
     */
    public static final String ID = "gadget-0";

    public LoginGadget()
    {
        super(ID);
    }

    public LoginGadget(String id)
    {
        super(id);
    }

    @WaitUntil
    private void waitUntilFormIsRendered()
    {
        Poller.waitUntilTrue(getLoginButton().withTimeout(TimeoutType.AJAX_ACTION).timed().isPresent());
    }

    public DashboardPage login(User user)
    {
        getUsernameField().type(user.getUserName());
        getPasswordField().type(user.getPassword());
        getLoginButton().click();
        switchBack();
        return pageBinder.bind(DashboardPage.class);
    }


    // TODO set remember me

    // can't inject those guys until 2.1 is released with fix to SELENIUM-184
    public PageElement getUsernameField()
    {
        return find(By.id("login-form-username"));
    }

    public PageElement getPasswordField()
    {
        return find(By.id("login-form-password"));
    }

    public PageElement getLoginButton()
    {
        return find(By.id("login"));
    }

    public PageElement getPublicModeOffMessage()
    {
        return find(By.id("publicmodeoffmsg"));
    }

    /**
     * Get first error. If there is no error, the {@link com.atlassian.pageobjects.elements.PageElement#isPresent()}
     * of the returned element will return <code>false</code>.
     *
     * @return first error on the gadget form.
     */
    public PageElement getError()
    {
        return find(By.cssSelector(".form-message.error"));
    }

    public Iterable<PageElement> getErrors()
    {
        return Iterables.filter(findAll(By.className("form-message")), PageElements.hasClass("error"));
    }

    public boolean hasPublicModeOffMessage()
    {
        return getPublicModeOffMessage().isPresent();
    }

    public boolean hasErrors()
    {
        return !Iterables.isEmpty(getErrors());
    }



}
