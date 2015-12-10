package it.com.atlassian.jira.webtest.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

/**
 * Confluence page that shows the current user's OAuth tokens.
 */
public class ConfluenceOAuthTokensPage implements Page
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    protected AtlassianWebDriver driver;

    @ElementBy (name = "approve")
    private PageElement approveButton;

    @ElementBy (name = "deny")
    private PageElement denyButton;

    @Override
    public String getUrl()
    {
        return "/users/revokeoauthtokens.action";
    }

    public ConfluenceOAuthTokensPage revokeAllTokens()
    {
        final List<PageElement> revokeLinks = elementFinder.findAll(By.className("revoke"));

        for (PageElement revokeLink : revokeLinks)
        {
            revokeLink.click();

            // Accept the alert that pops up
            driver.switchTo().alert().accept();
        }

        return this;
    }
}
