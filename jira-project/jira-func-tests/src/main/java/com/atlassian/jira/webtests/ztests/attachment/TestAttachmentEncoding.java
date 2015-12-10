package com.atlassian.jira.webtests.ztests.attachment;

import java.util.List;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock;
import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.CopyAttachmentsRule;

// Over the years JIRA has attempted several things to deal with filesystem encoding issues of attachments. In the old, old
// days we didn't know and didn't care. This lead to attachments being broken in some cases. Then we tried Workaround #1, which was
// insufficient. Now we are on Workaround #2 (http://jira.atlassian.com/browse/JRA-23311). This test verifies that all
// three methods still work.
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS })
public class TestAttachmentEncoding extends FuncTestCase
{
    protected CopyAttachmentsRule copyAttachmentsRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestAttachmentEncoding.xml");
        copyAttachmentsRule = new CopyAttachmentsRule(this);
        copyAttachmentsRule.before();
        copyAttachmentsRule.copyAttachmentsFrom("TestAttachmentEncoding/attachments");
    }

    @Override
    protected void tearDownTest()
    {
        copyAttachmentsRule.after();
        super.tearDownTest();
    }

    public void testCheckVariousAttachmentFilenames()
    {
        final AttachmentsBlock attachments = navigation.issue().attachments("HSP-1");
        final List<FileAttachmentsList.FileAttachmentItem> attachmentsList = attachments.list().get();
        assertEquals(3, attachmentsList.size());
        tester.gotoPage("/secure/attachment/10000/clover.license");
        tester.gotoPage("/secure/attachment/10001/sqltool.rc");
        tester.gotoPage("/secure/attachment/10002/svn");
    }

    // JRA-23830 Make sure that when you do a Move Issue all the various encoding of attachments get moved correctly.
    public void testMoveIssue() throws Exception
    {
        navigation.issue().viewIssue("HSP-1");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Bovine' from select box 'pid'.
        tester.selectOption("pid", "monkey");
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Move");

        for (FileAttachmentsList.FileAttachmentItem fileAttachmentItem : navigation.issue().attachments("MKY-1").list().get())
        {
            tester.gotoPage("/secure/attachment/" + fileAttachmentItem.getId() + "/" + fileAttachmentItem.getName());
        }
    }
}