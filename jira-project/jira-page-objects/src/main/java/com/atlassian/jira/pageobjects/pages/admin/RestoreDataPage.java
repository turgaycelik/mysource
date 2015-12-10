package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Admin page for restoring data from backup XMLs.
 *
 * @since v4.4
 */
public class RestoreDataPage extends AbstractJiraAdminPage
{
    @FindBy(name = "filename")
    private WebElement fileNameField;

    @FindBy(name = "license")
    private WebElement licenseField;

    @FindBy(name = "quickImport")
    private WebElement quickImport;

    @ElementBy(id = "restore-xml-data-backup-submit", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement submitButton;

    @FindBy(id = "#restore-xml-data-backup-cancel")
    private WebElement cancelButton;

    @ElementBy(id = "default-import-path")
    private PageElement importPath;


    @Override
    public String getUrl()
    {
        return "/secure/admin/XmlRestore!default.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return submitButton.timed().isPresent();
    }

    @Override
    public String linkId()
    {
        return "restore_data";
    }

    public RestoreDataPage setFileName(String fileName)
    {
        fileNameField.sendKeys(fileName);
        return this;
    }

    public RestoreDataPage setLicense(String license)
    {
        licenseField.sendKeys(license);
        return this;
    }

    public RestoreDataPage setQuickImport(boolean doQuickImport)
    {
        if (shouldEnable(doQuickImport) || shouldDisable(doQuickImport))
        {
            showQuickImport(); // WebDriver does not support manipulating hidden components
            if (!quickImport.isSelected())
            {
                quickImport.click();
            }
            hideQuickImport();
        }
        return this;
    }

    public RestoreInProgressPage submitRestore()
    {
        submitButton.click();
        return pageBinder.bind(RestoreInProgressPage.class);
    }

    public RestoreDataPage submitExpectingError()
    {
        submitButton.click();
        return this;
    }

    public JiraAdminHomePage cancel()
    {
        cancelButton.click();
        return pageBinder.bind(JiraAdminHomePage.class);
    }

    public String getDefaultImportPath()
    {
        return importPath.getText().trim();
    }

    private boolean shouldDisable(boolean doQuickImport)
    {
        return quickImport.isSelected() && !doQuickImport;
    }

    private boolean shouldEnable(boolean doQuickImport)
    {
        return !quickImport.isSelected() && doQuickImport;
    }

    private void showQuickImport()
    {
        driver.executeScript("jQuery('#quickImport').show()");
    }
    
    private void hideQuickImport()
    {
        driver.executeScript("jQuery('#quickImport').hide()");
    }
}
