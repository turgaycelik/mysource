package com.atlassian.jira.webtests.ztests.attachment;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.rules.CopyAttachmentsRule;

/**
* TODO: Document this class / interface here
*
* @since v6.1
*/
public abstract class AbstractTestAttachmentsBlockSortingOnViewIssue extends FuncTestCase
{
    protected CopyAttachmentsRule copyAttachmentsRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestAttachmentsBlockSortingOnViewIssue.xml");
        copyAttachmentsRule = new CopyAttachmentsRule(this);
        copyAttachmentsRule.before();
        copyAttachmentsRule.copyAttachmentsFrom("TestAttachmentsBlockSortingOnViewIssue/attachments");

        // Attachment Sorting by Name is locale sensitive, so we set this locale before running these tests.
        administration.generalConfiguration().setJiraLocale("English (Australia)");
    }

    @Override
    protected void tearDownTest()
    {
        copyAttachmentsRule.after();
        super.tearDownTest();
    }
}
