package com.atlassian.jira.webtests.ztests.attachment;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS, Category.ISSUES })
public class TestIssueFileAttachments extends FuncTestCase
{
    private static final String ATTACHMENT_SETTINGS_TABLE_ID = "table-AttachmentSettings";
    private static final int ALLOW_ATTACHMENTS_VALUE_COLUMN_NUMBER = 1;
    private static final int ALLOW_ATTACHMENTS_ROW_NUMBER = 0;
    private static final int ENABLE_THUMBNAILS_ROW_NUMBER = 3;
    private static final int ENABLE_THUMBNAILS_VALUE_COLUMN_NUMBER = 1;

    public void testIssueFileAttachmentEnableThumbnailsAsAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            administration.attachments().enable();

            navigation.logout();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

            navigation.gotoAdminSection("attachments");
            assertThatAttachmentsAreEnabled();

            navigation.clickLinkWithExactText("Edit Settings");
            tester.checkCheckbox("thumbnailsEnabled", "false");
            tester.submit("Update");
            assertThatThumbnailsAreDisabled();
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testCreateAttachmentsWhenAttachmentsAreDisabled()
    {
        administration.restoreData("TestDeleteAttachments.xml");
        tester.gotoPage("/secure/AttachFile!default.jspa?id=10000");
        tester.setWorkingForm("attach-file");
        tester.submit();
        text.assertTextPresent(locator.page(), "Attachments have been disabled for this instance of JIRA.");
    }

    private void assertThatAttachmentsAreEnabled()
    {
        text.assertTextPresent
                (
                        locator.cell
                                (
                                        ATTACHMENT_SETTINGS_TABLE_ID, ALLOW_ATTACHMENTS_ROW_NUMBER,
                                        ALLOW_ATTACHMENTS_VALUE_COLUMN_NUMBER
                                ),
                        "ON"
                );
    }

    private void assertThatThumbnailsAreDisabled()
    {
        text.assertTextSequence
                (
                        locator.cell
                                (
                                        ATTACHMENT_SETTINGS_TABLE_ID,
                                        ENABLE_THUMBNAILS_ROW_NUMBER, ENABLE_THUMBNAILS_VALUE_COLUMN_NUMBER
                                ),
                        "OFF"
                );
    }

}
