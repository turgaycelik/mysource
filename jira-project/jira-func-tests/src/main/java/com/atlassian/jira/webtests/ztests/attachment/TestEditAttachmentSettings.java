package com.atlassian.jira.webtests.ztests.attachment;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.xml.sax.SAXException;

/**
 * @since v3.12
 */
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS, Category.BROWSING })
public class TestEditAttachmentSettings extends JIRAWebTest
{
    public TestEditAttachmentSettings(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestEditAttachmentSettings.xml");
    }

    public void tearDown()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        restoreBlankInstance();
        super.tearDown();
    }

    public void testEditAttachmentSettingsValidation()
    {
        gotoAdmin();
        clickLink("attachments");
        clickLinkWithText("Edit Settings");

        selectMultiOptionByValue("attachmentPathOption", "DEFAULT");
        setFormElement("attachmentSize", "");
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        //
        assertTextPresent("Please specify the attachment size.");

        selectMultiOptionByValue("attachmentPathOption", "DEFAULT");
        setFormElement("attachmentSize", "-1");
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        assertTextPresent("Attachment size must be a positive number.");

        selectMultiOptionByValue("attachmentPathOption", "DEFAULT");
        setFormElement("attachmentSize", "0");
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        assertTextPresent("Attachment size must be a positive number.");

        selectMultiOptionByValue("attachmentPathOption", "DEFAULT");
        setFormElement("attachmentSize", "2147483648"); //Integer.MAX_VALUE + 1
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        assertTextPresent("Attachment size must be a number between 1 to 2147483647");

        selectMultiOptionByValue("attachmentPathOption", "DEFAULT");
        setFormElement("attachmentSize", "9999999999");
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        assertTextPresent("Attachment size must be a number between 1 to 2147483647");

        selectMultiOptionByValue("attachmentPathOption", "DISABLED");
        setFormElement("attachmentSize", "1");
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        assertTextPresent("Attachments must be enabled to enable thumbnails.");

        selectMultiOptionByValue("attachmentPathOption", "DISABLED");
        setFormElement("attachmentSize", "1");
        setFormElement("thumbnailsEnabled", "false");
        setFormElement("zipSupport", "true");
        submit("Update");
        assertTextPresent("Attachments must be enabled to enable ZIP support.");
    }


    public void testSizeIsNotUpdatedIfAttachmentsDisabled() throws SAXException
    {
        gotoAdmin();
        clickLink("attachments");
        clickLinkWithText("Edit Settings");

        //set the attachment size to a known size
        selectMultiOptionByValue("attachmentPathOption", "DEFAULT");
        setFormElement("attachmentSize", "12345");
        submit("Update");
        assertTextPresent("12 kB");

        clickLinkWithText("Edit Settings");

        //disable attachments and change the attachment size
        selectMultiOptionByValue("attachmentPathOption", "DISABLED");
        setFormElement("attachmentSize", "54321");
        setFormElement("thumbnailsEnabled", "false");
        setFormElement("zipSupport", "false");
        submit("Update");
        assertTextPresent("12 kB");//the attachment size should not have been updated

        //go back to the edit page and assert the size value has not changed
        clickLinkWithText("Edit Settings");
        assertTextPresent("12345");
        assertTextNotPresent("54321");
    }

    public void testEditAttachmentSettingsJiraHome()
    {
        logout();
        login("sysadmin", "sysadmin");
        // this uses the default path (i.e. jira home)
        administration.attachments().enable("2940");
        gotoAdmin();
        clickLink("attachments");
        clickLinkWithText("Edit Settings");
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        assertTextSequence(new String[] {"Allow Attachments", "ON", "Attachment Path", "Default Directory", "Attachment Size", "3 kB", "Enable Thumbnails", "ON"});
    }

    public void testEditAttachmentSettingsWithGlobalAdmin()
    {
        String attachmentPath = getEnvironmentData().getWorkingDirectory().getAbsolutePath() + FS + "attachments";

        //disable attachments so that the global admin does not have a link to the edit page
        logout();
        login("sysadmin", "sysadmin");
        gotoAdmin();
        administration.attachments().enable("8765");//enable to set the attachment size (so size that value of size is known)
        clickLink("attachments");//disable to restrict global admins from editing
        clickLinkWithText("Edit Settings");
        selectMultiOptionByValue("attachmentPathOption", "DISABLED");
        setFormElement("thumbnailsEnabled", "false");
        setFormElement("zipSupport", "false");
        submit("Update");
        assertTextSequence(new String[] {"Allow Attachments", "OFF", "Attachment Path", "Attachment Size", "9 kB", "Enable Thumbnails", "OFF"});

        //check global admin cannot see the edit config link
        logout();
        login("globaladmin", "globaladmin");
        gotoAdmin();
        clickLink("attachments");
        assertTextPresent("You can not enable thumbnails unless attachments are enabled.");
        assertLinkNotPresentWithText("Edit Settings");
        
        //goto the edit page directly as the global admin and verify cannot change system admin options
        gotoPage("/secure/admin/jira/EditAttachmentSettings!default.jspa");
        assertFormElementNotPresent("attachmentPathOption"); //should be hidden as only system admin can update this field
        assertFormElementNotPresent("attachmentPath"); //should be hidden as only system admin can update this field
        setFormElement("attachmentSize", "5678");
        setFormElement("thumbnailsEnabled", "true");//try to enable thumbnails
        submit("Update");
        assertTextPresent("Attachments must be enabled to enable thumbnails.");//verify thumbnails cannot be enabled
        setFormElement("thumbnailsEnabled", "false");//set to false to check attachment size is not updated
        submit("Update");
        //check its still off and that size hasnt changed
        assertTextSequence(new String[] {"Allow Attachments", "OFF", "Attachment Path", "Attachment Size", "9 kB", "Enable Thumbnails", "OFF"});

        //enable attachments so that global admins can modify the size and enable thumbnails
        logout();
        login("sysadmin", "sysadmin");
        gotoAdmin();
        clickLink("attachments");
        clickLinkWithText("Edit Settings");
        selectMultiOptionByValue("attachmentPathOption", "DEFAULT");
        setFormElement("attachmentSize", "1234");
        setFormElement("thumbnailsEnabled", "false");
        submit("Update");
        assertTextSequence(new String[] {"Allow Attachments", "ON", "Attachment Size", "1 kB", "Enable Thumbnails", "OFF"});

        //check global admins can now edit part of the attachment settings
        logout();
        login("globaladmin", "globaladmin");
        gotoAdmin();
        clickLink("attachments");
        assertLinkPresentWithText("Edit Settings");
        clickLinkWithText("Edit Settings");
        assertFormElementNotPresent("attachmentPathOption"); //should be hidden as only system admin can update this field
        assertFormElementNotPresent("attachmentPath"); //should be hidden as only system admin can update this field
        setFormElement("attachmentSize", "3456");
        setFormElement("thumbnailsEnabled", "true");
        submit("Update");
        assertTextSequence(new String[] {"Allow Attachments", "ON", "Attachment Size", "3 kB", "Enable Thumbnails", "ON"});
    }

    public void testEditAttachmentSize()
    {
        assertAttachmentSize(1, "0.0 kB");
        assertAttachmentSize(1024, "1.0 kB");
        assertAttachmentSize(1024 * 2, "2 kB");
        assertAttachmentSize(1024 * 1024, "1024 kB");
        assertAttachmentSize(1024 * 1024 + 1, "1.00 MB");
        assertAttachmentSize(1024 * 1024 * 155 / 100, "1.55 MB");
        assertAttachmentSize(1024 * 1024 * 10, "10.00 MB");
        assertAttachmentSize(Integer.MAX_VALUE, "2,048.00 MB");
    }

    private void assertAttachmentSize(int maxSizeInBytes, String maxSizePrettyFormat)
    {
        administration.attachments().enable(String.valueOf(maxSizeInBytes));
        assertTextSequence(new String[] {"Allow Attachments", "ON", "Attachment Path", "Attachment Size", maxSizePrettyFormat, "Enable Thumbnails"});

        navigation.issue().goToCreateIssueForm(null,null);
        assertTextPresent("The maximum file upload size is " + maxSizePrettyFormat + ".");

        gotoIssue("HSP-1");
        clickLink("attach-file");
        assertTextPresent("The maximum file upload size is " + maxSizePrettyFormat + ".");
    }
}
