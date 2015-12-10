package com.atlassian.jira.pageobjects.pages.admin.system;

import java.util.List;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.inject.Inject;

import org.openqa.selenium.By;

public class SystemInfo extends AbstractJiraPage
{

    private final static String URI = "/secure/admin/ViewSystemInfo.jspa";

    @Inject
    protected PageElementFinder finder;

    /**
     * Timed condition checking if we're at given page.
     *
     * @return timed condition checking, if the test is at given page
     */
    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("server-info")).timed().isPresent();
    }

    /**
     * @return The URI, including query string, relative to the base url
     */
    @Override
    public String getUrl()
    {
        return URI;
    }

    public String getVersion()
    {
        return getJiraInfoForRowTitle("Version");
    }

    public String getBuildNumber()
    {
        return getJiraInfoForRowTitle("Build Number");
    }

    public String getBuildDate()
    {
        return getJiraInfoForRowTitle("Build Date");
    }

    public String getBuildRevision()
    {
        return getJiraInfoForRowTitle("Build Revision");
    }

    private String getJiraInfoForRowTitle(final String jiraInfoKey)
    {
        for (final PageElement row : jiraInfoElements())
        {
            boolean found = false;
            for (final PageElement cell : row.findAll(By.tagName("td")))
            {
                if (!found)
                {
                    if (cell.getText().contains(jiraInfoKey))
                    {
                        found = true;
                    }
                }
                else
                {
                    return cell.getText();
                }
            }
        }
        return null;
    }

    private List<PageElement> jiraInfoElements()
    {
        return finder.find(By.id("jira-info")).findAll(By.cssSelector("tr"));
    }
}
