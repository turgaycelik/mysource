package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v4.1
 */
public class AttachmentsImpl extends AbstractFuncTestUtil implements Attachments
{
    private final Navigation navigation;

    public AttachmentsImpl(WebTester tester, JIRAEnvironmentData environmentData, final Navigation navigation)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
    }

    public void enable()
    {
        log("Enabling attachments with default path.");
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        tester.clickLinkWithText("Edit Settings");
        tester.checkCheckbox("attachmentPathOption", "DEFAULT");
        tester.submit("Update");
    }

    public void enable(String maxAttachmentSize)
    {
        log("Enabling attachments with max size of : " + maxAttachmentSize);
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        tester.clickLinkWithText("Edit Settings");
        tester.checkCheckbox("attachmentPathOption", "DEFAULT");
        tester.setFormElement("attachmentSize", maxAttachmentSize);
        tester.submit("Update");
    }


    public void disable()
    {
        log("Disabling attachments.");
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        tester.clickLinkWithText("Edit Settings");
        tester.checkCheckbox("attachmentPathOption", "DISABLED");
        tester.checkCheckbox("thumbnailsEnabled", "false");
        tester.checkCheckbox("zipSupport", "false");
        tester.submit("Update");
    }

    public void enableZipSupport()
    {
        log("Enabling ZIP support for downloading attachments.");
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        tester.clickLinkWithText("Edit Settings");
        tester.checkCheckbox("zipSupport", "true");
        tester.submit("Update");
    }

    public void disableZipSupport()
    {
        log("Disabling ZIP support for downloading attachments.");
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        tester.clickLinkWithText("Edit Settings");
        tester.checkCheckbox("zipSupport", "false");
        tester.submit("Update");
    }

    public String getCurrentAttachmentPath()
    {
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        // Get the table 'attachmentSettings'.
        final WebTable attachmentSettings = tester.getDialog().getWebTableBySummaryOrId("attachmentSettings");
        // Check that  'Attachment Path' is in the third row where we expect it:
        if ("Attachment Path".equals(attachmentSettings.getCellAsText(2, 0).trim()))
        {
            String attachmentPath = attachmentSettings.getCellAsText(2, 1).trim();
            // Check if this is the "default" directory. Looks like "Default Directory [/home/mlassau/jira/jira_trunk/data/attachments]"
            if (attachmentPath.startsWith("Default Directory ["))
            {
                // Strip "Default Directory [" from the front, and the "]" from the end
                attachmentPath = attachmentPath.substring("Default Directory [".length(), attachmentPath.length() - 1);
            }
            return attachmentPath;
        }
        else
        {
            throw new RuntimeException("Error occured when trying to screen-scrape the attachment path. 'Attachment Path' not found where expected in the table.");
        }
    }

}
