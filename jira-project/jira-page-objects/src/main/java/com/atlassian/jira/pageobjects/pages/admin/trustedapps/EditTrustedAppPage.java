package com.atlassian.jira.pageobjects.pages.admin.trustedapps;

import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class EditTrustedAppPage extends AbstractJiraPage
{
    @ElementBy(cssSelector = "form[action=\"EditTrustedApplication.jspa\"]")
    private PageElement form;

    @Override
    public String getUrl()
    {
        return "/secure/admin/trustedapps/EditTrustedApplication!default.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return form.timed().isPresent();
    }

    public String getApplicationName()
    {
        return form.find(By.cssSelector("input[name=\"name\"]")).getValue();
    }

    public String getApplicationId()
    {
        return form.find(By.cssSelector("input[name=\"applicationId\"]")).getValue();
    }

    public String getTimeout()
    {
        return form.find(By.cssSelector("input[name=\"timeout\"]")).getValue();
    }

    public String getIpMatch()
    {
        return form.find(By.cssSelector("textarea[name=\"ipMatch\"]")).getText();
    }
}