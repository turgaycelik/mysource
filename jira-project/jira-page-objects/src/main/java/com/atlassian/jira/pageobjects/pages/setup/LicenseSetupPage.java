package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.hamcrest.Matchers.equalTo;

/**
 * Step 3 in the JIRA setup process - license.
 *
 * @since v5.2
 */
public class LicenseSetupPage extends AbstractJiraPage
{
    @ElementBy (cssSelector = "#jira-setupwizard h2")
    private PageElement formTitle;

    @FindBy (id = "jira-setupwizard-licenseSetupSelectorexistingLicense")
    private WebElement existingLicense;

    private PageElement licenseKeyField;
    private PageElement submitButton;


    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("You can't go to this page by entering URI");
    }

    @Override
    public TimedCondition isAt()
    {
        return formTitlePresent();
    }

    private TimedCondition formTitlePresent()
    {
        return Conditions.forMatcher(formTitle.timed().getText(), equalTo("Adding Your License Key"));
    }

    public LicenseSetupPage selectExistingLicense(String licenseKey)
    {
        existingLicense.click();
        licenseKeyField = elementFinder.find(By.id("licenseKey"));
        submitButton = elementFinder.find(By.cssSelector(".aui-button-primary"));
        licenseKeyField.type("");
        licenseKeyField.type(licenseKey);
        return this;
    }

    public AdminSetupPage submit()
    {
        submitButton.click();
        return pageBinder.bind(AdminSetupPage.class);
    }

    public MailSetupPage submitToMailSetup()
    {
        submitButton.click();
        return pageBinder.bind(MailSetupPage.class);
    }
}
