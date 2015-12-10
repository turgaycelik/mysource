package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v6.2
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlAttachmentsField extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestJqlAttachments.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testAllIssuesWithAttachments()
    {
        //Make sure we find the issues in the past.
        navigation.issueNavigator().createSearch("attachments is not EMPTY");
        assertIssues("NUMBER-2", "MKY-3", "HSP-3");

        navigation.issueNavigator().createSearch("attachments is not EMPTY AND project = MKY");
        assertIssues("MKY-3");
    }


    public void testIssuesWithoutAttachments()
    {
        //Make sure we find the issues in the past.
        navigation.issueNavigator().createSearch("attachments is EMPTY");
        assertIssues("NUMBER-1", "MKY-2", "MKY-1", "MK-2", "MK-1", "HSP-4", "HSP-1");
    }

    public void testDisableAttachments()
    {
        backdoor.attachments().disable();
        navigation.issueNavigator().createSearch("attachments is not EMPTY");
        assertIssues("NUMBER-2", "MKY-3", "HSP-3");

        navigation.issueNavigator().createSearch("attachments is EMPTY");
        assertIssues("NUMBER-1", "MKY-2", "MKY-1", "MK-2", "MK-1", "HSP-4", "HSP-1");

        backdoor.attachments().enable();
    }

    public void testRemovingAttachments()
    {
        // HSP-2 has an attachment
        navigation.issueNavigator().createSearch("attachments is not EMPTY");
        assertIssues("NUMBER-2", "MKY-3", "HSP-3", "HSP-2");

        navigation.issue().viewIssue("HSP-2");
        tester.clickLink("manage-attachment-link");
        // Click Link 'Delete'
        tester.clickLink("del_10002");
        tester.submit("Delete");

        navigation.issueNavigator().createSearch("attachments is EMPTY AND issuekey = HSP-2");
        assertIssues("HSP-2");
    }

    public void testInvalidJqlQuery()
    {
        assertTooComplex("attachments is \"file.png\"");
        assertTooComplex("attachments is not empt");
        assertTooComplex("attachments is empt");
        assertTooComplex("attachments = empt");
        assertTooComplex("attachments != empt");
    }
}
