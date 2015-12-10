package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

/**
 * OAuth tokens page
 *
 * @since v5.0
 */
public class OAuthTokensPage implements Page
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private AtlassianWebDriver driver;

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/oauth/users/access-tokens";
    }

    public OAuthTokensPage revokeAllTokens()
    {
        final List<PageElement> revokeLinks = elementFinder.findAll(By.className("revoke"));

        for (PageElement revokeLink : revokeLinks)
        {
            revokeLink.click();

            // Accept the alert that pops up
            driver.switchTo().alert().accept();
        }
        driver.getDriver().navigate().refresh();
        return this;
    }
}
