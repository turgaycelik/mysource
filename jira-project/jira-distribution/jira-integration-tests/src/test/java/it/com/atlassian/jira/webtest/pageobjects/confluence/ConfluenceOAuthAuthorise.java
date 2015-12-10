package it.com.atlassian.jira.webtest.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

/**
 * OAuth authorisation page.
 */
public class ConfluenceOAuthAuthorise implements Page
{
    @ElementBy (name = "approve")
    private PageElement approveButton;

    @ElementBy (name = "deny")
    private PageElement denyButton;

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/oauth/authorize";
    }

    public void allow()
    {
        approveButton.click();
    }
}
