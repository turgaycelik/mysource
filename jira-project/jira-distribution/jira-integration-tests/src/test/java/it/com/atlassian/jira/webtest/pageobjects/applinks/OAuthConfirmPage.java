package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Page Object for OAuth confirmation page
 *
 * @since v5.0
 */
public class OAuthConfirmPage
{
    private static final String OAUTH_WINDOW_NAME = "com_atlassian_applinks_authentication";

    @Inject
    private AtlassianWebDriver webDriver;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    @ElementBy (name = "approve")
    private PageElement allowField;

    @ElementBy (name = "deny")
    private PageElement denyField;

    public void denyHandlingWebLoginIfRequired(String username, String password)
    {
        handleWebLoginIfRequiredThenClick(username, password, denyField);
    }

    public void confirmHandlingWebLoginIfRequired(String username, String password)
    {
        handleWebLoginIfRequiredThenClick(username, password, allowField);
    }

    private void handleWebLoginIfRequiredThenClick(String username, String password, PageElement fieldToClick)
    {
        String initialWindowName = webDriver.getWindowHandle();
        webDriver.switchTo().window(OAUTH_WINDOW_NAME);
        Poller.waitUntilTrue(Conditions.or(fieldToClick.timed().isPresent(), WebLoginPage.getWaitCondition(elementFinder)));
        if (!fieldToClick.isPresent())
        {
            WebLoginPage<OAuthConfirmPage> webLoginPage = pageBinder.bind(WebLoginPage.class, this);
            webLoginPage.handleWebLoginIfRequired(username, password);
        }
        fieldToClick.click();

        if (fieldToClick == denyField)
        {
            elementFinder.find(By.id("continue-link")).click();
        }
        webDriver.switchTo().window(initialWindowName);
    }
}
