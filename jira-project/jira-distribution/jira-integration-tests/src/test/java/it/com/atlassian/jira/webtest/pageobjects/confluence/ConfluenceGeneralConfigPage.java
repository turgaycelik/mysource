package it.com.atlassian.jira.webtest.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import javax.inject.Inject;

/**
 * Confluence General Configuration Page.
 */
public class ConfluenceGeneralConfigPage implements Page
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    protected AtlassianWebDriver driver;

    @ElementBy (id = "editbaseurl")
    private PageElement serverBaseUrl;

    @Override
    public String getUrl()
    {
        return "/admin/viewgeneralconfig.action";
    }

    public String getServerBaseUrl()
    {
        return serverBaseUrl.getText();
    }
}
