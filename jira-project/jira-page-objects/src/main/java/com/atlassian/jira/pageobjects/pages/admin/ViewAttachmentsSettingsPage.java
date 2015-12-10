package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Atrachment configuration in the JIRA admin section.
 *
 * @since 4.4
 */
public class ViewAttachmentsSettingsPage extends AbstractJiraAdminPage
{
    private static final String URI = "/secure/admin/ViewAttachmentSettings.jspa";

    private static final String ENABLED = "ON";

    @FindBy (id = "AttachmentSettings")
    private WebElement attachmentSettingsTable;

    private boolean attachmentsEnabled;
    private boolean thumnailsEnabled;
    private boolean zipSupportEnabled;

    private String attachmentPath;
    private String attachmentSize;


    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("AttachmentSettings")).timed().isPresent();
    }

    @Override
    public String linkId()
    {
        return "attachments";
    }

    @Init
    public void readAttachmentSettings()
    {

        By colLocator = ByJquery.$("td ~ td");
        List<WebElement> attachSettingsRows = attachmentSettingsTable.findElements(By.tagName("tr"));

        attachmentsEnabled = attachSettingsRows.get(0).findElement(colLocator).getText().equals(ENABLED);

        attachmentPath = parseAttachmentPath(attachSettingsRows.get(1).findElement(colLocator).getText());
        attachmentSize = attachSettingsRows.get(2).findElement(colLocator).getText();

        thumnailsEnabled = attachSettingsRows.get(3).findElement(colLocator).getText().equals(ENABLED);
        zipSupportEnabled = attachSettingsRows.get(4).findElement(colLocator).getText().equals(ENABLED);
    }

    /**
     * parses the path to the attachments directory from the attachment path row
     * @param columnText the text to parse to extract the path
     * @return the path as a String otherwise the empty string
     */
    private String parseAttachmentPath(String columnText)
    {
        //Match text inbetween square brackets
        Pattern p = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher m = p.matcher(columnText);

        if (m.find())
        {
            return m.group(1);
        }

        return "";
    }

    public boolean attachmentsAreEnabled()
    {
        return attachmentsEnabled;
    }

    public boolean thumnailsAreEnabled()
    {
        return thumnailsEnabled;
    }

    public boolean zipSupportIsEnabled()
    {
        return zipSupportEnabled;
    }

    public String getAttachmentPath()
    {
        return attachmentPath;
    }

    public String getAttachmentSize()
    {
        return attachmentSize;
    }
}
