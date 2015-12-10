package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Step 2 in the JIRA setup process - application properties.
 *
 * @since v4.4
 */
public class ApplicationSetupPage extends AbstractJiraPage
{
    @FindBy(name = "title")
    private WebElement applicationTitleField;

    @ElementBy(name = "baseURL")
    private PageElement baseUrlField;

    @FindBy(name = "next")
    private WebElement submitButton;

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("You can't go to this page by entering URI");
    }

    @Override
    public TimedCondition isAt()
    {
        return baseUrlField.timed().isPresent();
    }

    public ApplicationSetupPage setTitle(String appTitle)
    {
        applicationTitleField.sendKeys("");
        applicationTitleField.sendKeys(appTitle);
        return this;
    }

    public ApplicationSetupPage setBaseUrlField(String baseUrlString)
    {
        baseUrlField.type("");
        baseUrlField.type(baseUrlString);
        return this;
    }

    public BundleSetupPage submit()
    {
        submitButton.click();
        return pageBinder.bind(BundleSetupPage.class);
    }

}
