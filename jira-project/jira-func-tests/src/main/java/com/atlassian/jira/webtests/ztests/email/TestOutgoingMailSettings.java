package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

/**
 * Responsible for verifying that enabling or disabling outgoing is applied correctly.
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestOutgoingMailSettings extends EmailFuncTestCase
{
    public static class Backups
    {
        private static String OUTGOING_EMAIL_ENABLED = "TestOutgoingMailSettings/outgoing-mail-enabled.xml";
    }
    public void testDisablingEmailOnDataRestoreIsAppliedEvenWhenItIsEnabledInTheXmlBackupFile()
    {
        administration.restoreData(Backups.OUTGOING_EMAIL_ENABLED, Administration.OutgoingMailSettings.DISABLE);
        assertTrue(administration.mailServers().Smtp().isDisabled());
    }

    @Override
    public void tearDownTest()
    {
        // this shouldn't be necessary as application properties come in the XML backup but there
        // must be a cache that isn't getting cleared after quick restore.
        backdoor.applicationProperties().setOption("jira.mail.send.disabled", false);
        super.tearDownTest();
    }
}
