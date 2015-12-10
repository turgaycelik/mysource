package com.atlassian.jira.webtests.ztests.subtask.move;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryItem;
import com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryRecord;
import com.google.common.collect.Sets;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestMoveSubTaskEnterprise extends EmailFuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestMoveSubTaskEnterprise.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        backdoor.generalConfiguration().setContactAdminFormOn();
    }

    public void testMoveSubtaskToParentWithNoSecurityLevel() throws SAXException
    {
        navigation.issueNavigator().displayAllIssues();
        //lets change COW-36' parent to be COW-39, which has no security level set.
        navigation.issue().viewIssue("COW-36");
        //check that COW-36 still has the MyFriendsOnly Security level.
        tester.assertTextPresent("MyFriendsOnly");

        //now lets move it
        tester.clickLink("move-issue");
        tester.assertTextPresent("Move Sub-Task: Choose Operation");
        tester.checkCheckbox("operation", "move.subtask.parent.operation.name");
        tester.submit("Next >>");
        tester.assertTextPresent("Move Sub-Task: COW-36");
        tester.setFormElement("parentIssue", "COW-39");
        tester.submit("Change Parent");
        tester.assertTextNotPresent("MyFriendsOnly");
        navigation.issue().returnToSearch();

        //check that the index has been updated as well.
        tester.assertTextPresent("Issue Navigator");
        WebTable issueTable = getIssueTable();
        assertEquals("COW-36", issueTable.getCellAsText(7, 1).trim());
        assertEquals("", issueTable.getCellAsText(7, 11).trim());

        //check that the change history shows the parent key and security level have been updated
        assertions.assertLastChangeHistoryRecords("COW-36", new ExpectedChangeHistoryRecord(
                new ExpectedChangeHistoryItem("Security", "MyFriendsOnly", ""),
                new ExpectedChangeHistoryItem("Parent Issue", "COW-35", "COW-39")
                ));
    }

    public void testMoveSubtaskToParentWithSecurityLevel() throws SAXException
    {
        navigation.issueNavigator().displayAllIssues();
        //lets change COW-36' parent to be COW-39, which has no security level set.
        navigation.issue().viewIssue("COW-36");
        //check that COW-36 still has the MyFriendsOnly Security level.
        tester.assertTextPresent("MyFriendsOnly");

        //now lets move it
        tester.clickLink("move-issue");
        tester.assertTextPresent("Move Sub-Task: Choose Operation");
        tester.checkCheckbox("operation", "move.subtask.parent.operation.name");
        tester.submit("Next >>");
        tester.assertTextPresent("Move Sub-Task: COW-36");
        tester.setFormElement("parentIssue", "COW-38");
        tester.submit("Change Parent");
        tester.assertTextNotPresent("MyFriendsOnly");
        //note: security level is the 5th row of the issue header table
        Locator locator = new IdLocator(tester, "security-val");
        assertions.getTextAssertions().assertTextPresent(locator, "A");
        navigation.issue().returnToSearch();

        //check that the index has been updated as well.
        tester.assertTextPresent("Issue Navigator");
        WebTable issueTable = tester.getDialog().getWebTableBySummaryOrId("issuetable");
        assertions.getTableAssertions().assertTableCellHasText(issueTable, 7, 1, "COW-36");
        assertions.getTableAssertions().assertTableCellHasText(issueTable, 7, 11, "A");

        //check that the change history shows the parent key and security level have been updated
        assertions.assertLastChangeHistoryRecords("COW-36", new ExpectedChangeHistoryRecord(
                new ExpectedChangeHistoryItem("Security", "MyFriendsOnly", "A"),
                new ExpectedChangeHistoryItem("Parent Issue", "COW-35", "COW-38")
                ));
    }

    public void testNotifications() throws InterruptedException, MessagingException, IOException
    {
        configureAndStartSmtpServerWithNotify();
        backdoor.userProfile().changeUserNotificationType("admin", "text");
        backdoor.userProfile().changeUserNotificationType("henry.ford", "text");

        administration.generalConfiguration().fixBaseUrl();

        //setup JIRA mail server
        navigation.issueNavigator().displayAllIssues();
        //lets change COW-36' parent to be COW-39, which has no security level set.
        navigation.issue().viewIssue("COW-36");

        //now lets move it
        tester.clickLink("move-issue");
        tester.checkCheckbox("operation", "move.subtask.parent.operation.name");
        tester.submit("Next >>");
        tester.setFormElement("parentIssue", "COW-38");
        tester.submit("Change Parent");

        flushMailQueueAndWait(2);

        //there should be a notification for the assignee and reporter.
        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);

        Set<String> expectedList = Sets.newHashSet("admin@example.com", "Henry.Ford@example.com");
        Set<String> receivedList = Sets.newHashSet(mimeMessages[0].getHeader("To")[0], mimeMessages[1].getHeader("To")[0]);
        assertEquals(expectedList, receivedList);

        assertMailProperties(mimeMessages[0]);
        assertMailProperties(mimeMessages[1]);
    }

    private void assertMailProperties(MimeMessage mimeMessage) throws MessagingException, IOException
    {
        assertEquals("[JIRATEST] (COW-36) Get another milk bucket", mimeMessage.getHeader("Subject")[0]);

        assertEquals("\"Mark (JIRA)\" <jiratest@atlassian.com>", mimeMessage.getHeader("From")[0]);
        String body = GreenMailUtil.getBody(mimeMessage);

        String baseUrl = getEnvironmentData().getBaseUrl().toString();
        if (!baseUrl.endsWith("/"))
        {
            baseUrl += "/";
        }

        List<String> testStrings = new LinkedList<String>();
        testStrings.add("[ " + baseUrl + "browse/COW-36?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel ]");
        testStrings.add("Mark updated COW-36:");
        testStrings.add("--------------------");
        testStrings.add("> Get another milk bucket");
        testStrings.add("> -----------------------");
        testStrings.add("Key: COW-36");
        testStrings.add("URL: " + baseUrl + "browse/COW-36");
        testStrings.add("Project: Bovine");
        testStrings.add("Issue Type: Sub-task");
        testStrings.add("Security Level: A(A)");
        testStrings.add("Reporter: Mark");
        testStrings.add("Assignee: Henry Ford");
        testStrings.add("This message was sent by Atlassian JIRA");

        assertTextSequence(testStrings, body);

        // Asserting that the change history items are present.
        // Unfortunatley the order is random, so we just assert they are somewhere in the message.
        assertTrue(body.contains("Parent Issue: COW-38  (was: COW-35)"));
        assertTrue(body.contains("Security: A  (was: MyFriendsOnly)"));
    }

    private String assertTextSequence(final List<String> testStrings, String body)
    {
        for (String test : testStrings)
        {
            int index = body.indexOf(test);
            if (index < 0)
            {
                fail("Unable to find string '" + test + "' in the body of the e-mail message.");
            }
            body = body.substring(index + test.length());
        }
        return body;
    }

    private WebTable getIssueTable() throws SAXException
    {
        return tester.getDialog().getResponse().getTableWithID("issuetable");
    }

}
