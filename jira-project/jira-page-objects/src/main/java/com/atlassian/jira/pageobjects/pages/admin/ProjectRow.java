package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.pageobjects.Page;
import com.atlassian.webdriver.utils.Check;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @since v4.2
 */
public class ProjectRow
{

    private final WebElement projectViewRow;
            
    private String name;
    private String key;
    private WebElement url;
    private String urlStr;
    private String projectLead;
    private String defaultAssignee;
    private WebElement viewOperation;
    private WebElement editOperation;
    private WebElement deleteOperation;

    public ProjectRow(WebElement projectViewRow)
    {
        this.projectViewRow = projectViewRow;
        List<WebElement> cols = projectViewRow.findElements(By.tagName("td"));

        name = cols.get(0).getText();
        key = cols.get(1).getText();

        WebElement urlEl = cols.get(2);

        if (Check.elementExists(By.tagName("a"), urlEl))
        {
            url = urlEl.findElement(By.tagName("a"));
            urlStr = url.getText();
        }
        else
        {
            url = null;
            urlStr = "";
        }

        projectLead = cols.get(3).getText();
        defaultAssignee = cols.get(4).getText();

        List<WebElement> operations = cols.get(6).findElements(By.tagName("a"));

        editOperation = operations.get(0);
        deleteOperation = operations.get(1);
    }

    public Page viewProject()
    {
        throw new UnsupportedOperationException("view Project operation on ProjectSummary has not been implemented");
    }

    public Page editProject()
    {
        throw new UnsupportedOperationException("edit project operation on ProjectSummary has not been implemented");
    }

    public Page deleteProject()
    {
        throw new UnsupportedOperationException("delete project operation on ProjectSummary has not been implemented");
    }

    public boolean hasUrl()
    {
        return url != null;
    }

    //
    // GENERATED CODE BELOW
    //

    public String getName()
    {
        return name;
    }

    public String getKey()
    {
        return key;
    }

    public WebElement getUrl()
    {
        return url;
    }

    public String getUrlStr()
    {
        return urlStr;
    }

    public String getProjectLead()
    {
        return projectLead;
    }

    public String getDefaultAssignee()
    {
        return defaultAssignee;
    }
}
