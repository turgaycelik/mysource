package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * Author: Geoffrey Wong
 * Page to configure the assignees for the Purchase Order plugin
 * THIS IS AN EACJ SPECIFIC PAGE
 */
public class IPOAssigneeActionPage extends AbstractJiraAdminPage
{
    private String URI = "/secure/admin/IPOAssigneeAction.jspa";

    @ElementBy (cssSelector = "input[type=\"submit\"]")
    PageElement submit;

    @ElementBy (id = "amsterdam")
    PageElement amsterdamUser;

    @ElementBy (id = "sydney")
    PageElement sydneyUser;

    @ElementBy (id = "sanfran")
    PageElement sanfranUser;

    @ElementBy (id = "traveller")
    PageElement travellerUser;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return submit.timed().isPresent();
    }

    @Override
    public String linkId()
    {
        return "IPOAssigneeActionLink_id";
    }

    public String getAmsterdamUser()
    {
        return amsterdamUser.getValue();
    }

    public String getAmsterdamFullNiceName()
    {
        return getHiddenFullName(driver.findElement(By.id("amsterdam-nice")));
    }
    
    public String getSydneyUser()
    {
        return sydneyUser.getValue();
    }
    
    public String getSydneyFullNiceName()
    {
        return getHiddenFullName(driver.findElement(By.id("sydney-nice")));
    }
    
    public String getSanfranUser()
    {
        return sanfranUser.getValue(); 
    }
    
    public String getSanfranFullNiceName()
    {
        return getHiddenFullName(driver.findElement(By.id("sanfran-nice")));
    }
    
    public String getTravellerUser()
    {
        return travellerUser.getValue();
    }
    
    public String getTravellerFullNiceName()
    {
        return getHiddenFullName(driver.findElement(By.id("traveller-nice")));
    }
    
    public IPOAssigneeActionPage setNewAmsterdamAssignee(String newAssignee)
    {
        amsterdamUser.clear().type(newAssignee);
        return this;
    }
    
    public IPOAssigneeActionPage setNewSydneyAssignee(String newAssignee)
    {
        sydneyUser.clear().type(newAssignee);
        return this;
    }
    
    public IPOAssigneeActionPage setNewSanfranAssignee(String newAssignee)
    {
        sanfranUser.clear().type(newAssignee);
        return this;
    }

    public IPOAssigneeActionPage setNewTravellerAssignee(String newAssignee)
    {
        travellerUser.clear().type(newAssignee);
        return this;
    }

    public void submit()
    {
        submit.click();
    }
    
    private String getHiddenFullName(WebElement fullNameElement)
    {
        String hiddenFullName = ((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", fullNameElement).toString();
        return hiddenFullName;
    }
}
