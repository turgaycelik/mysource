package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Page Object for UPM web login
 */
public class WebLoginPage<T>
{
    @Inject
    private PageElementFinder elementFinder;

    @ElementBy (name = "os_username")
    private PageElement usernameField;

    @ElementBy(name = "os_password")
    private PageElement passwordField;

    @ElementBy (name = "login")
    private PageElement submitButton;

    private final T nextPage;

    public WebLoginPage(T nextPage)
    {
        this.nextPage = nextPage;
    }

    @WaitUntil
    public void waitUntilReady()
    {
        Poller.waitUntilTrue(getWaitCondition(elementFinder));
    }
    
    /**
     * Returns the TimedCondition to wait (until true) for, to ensure the page is fully loaded.
     *
     * @param pageElementFinder page element finder
     * @return TimedCondition
     */
    public static TimedCondition getWaitCondition(PageElementFinder pageElementFinder)
    {
        return pageElementFinder.find(By.name("os_username")).timed().isVisible();
    }

    public T handleWebLoginIfRequired(String username, String password)
    {
        if (usernameField.isPresent())
        {
            usernameField.type(username);
            passwordField.type(password);
            submitButton.click();
        }
        return nextPage;
    }
}
