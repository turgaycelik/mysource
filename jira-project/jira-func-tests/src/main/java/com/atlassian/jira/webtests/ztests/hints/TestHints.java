package com.atlassian.jira.webtests.ztests.hints;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests that hints are <b>NOT</b> displayed in AUI dialog screens opened
 * in a non-dialog mode.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestHints extends FuncTestCase
{
    public static final String[] DIALOG_URLS = {
        "secure/EditLabels!default.jspa?id=10011",
        "secure/DeleteIssue!default.jspa?id=10011",
        "secure/AddComment!default.jspa?id=10011",
        "secure/AssignIssue!default.jspa?id=10011",
        "secure/AttachFile!default.jspa?id=10011",
        "secure/CloneIssueDetails!default.jspa?id=10011",
        "secure/CommentAssignIssue!default.jspa?action=2&id=10011",
        "secure/LinkJiraIssue!default.jspa?id=10011",
        "secure/CreateWorklog!default.jspa?id=10011",
        "secure/DeletePortalPage!default.jspa?pageId=10010",
        "secure/DeleteFilter!default.jspa?filterId=10000"
    };

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestHints.xml");
    }


    public void testNoHintsInNonDialogMode()
    {
        for (String url : DIALOG_URLS)
        {
            navigation.gotoPage(url);
            assertNoHints();
        }
    }

    private void assertNoHints()
    {
        assertions.assertNodeDoesNotExist("//p[@class='hint-container']");
    }
}
