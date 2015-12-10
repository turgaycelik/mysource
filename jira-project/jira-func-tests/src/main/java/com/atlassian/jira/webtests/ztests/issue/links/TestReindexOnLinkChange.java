package com.atlassian.jira.webtests.ztests.issue.links;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests that the update date gets set into Lucene after a link is deleted.
 * see JRA-14877
 *
 * @since v3.13.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestReindexOnLinkChange extends FuncTestCase
{
    public void testDeleteLink() throws Exception
    {
        // restore data
        this.administration.restoreData("TestReindexOnLinkChange.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        // Assert the cells in table 'issuetable'.
        WebTable issuetable = tester.getDialog().getWebTableBySummaryOrId("issuetable");
        // Assert row 0: |T|Key|Summary|Assignee|Reporter|Pr|Status|Res|Created|Updated|Due|
        assertEquals("Cell (0, 1) in table 'issuetable' should be 'Key'.", "Key", issuetable.getCellAsText(0, 1).trim());
        assertEquals("Cell (0, 8) in table 'issuetable' should be 'Created'.", "Created", issuetable.getCellAsText(0, 8).trim());
        assertEquals("Cell (0, 9) in table 'issuetable' should be 'Updated'.", "Updated", issuetable.getCellAsText(0, 9).trim());
        // Assert row 1: ||RAT-3|Where's the cheese?|admin|Fred Flintstone||Open|UNRESOLVED|03/Jan/07|03/Jan/08||
        assertEquals("Cell (1, 1) in table 'issuetable' should be 'RAT-3'.", "RAT-3", issuetable.getCellAsText(1, 1).trim());
        assertEquals("Cell (1, 8) in table 'issuetable' should be '03/Jan/07'.", "03/Jan/07", issuetable.getCellAsText(1, 8).trim());
        assertEquals("Cell (1, 9) in table 'issuetable' should be '03/Jan/08'.", "03/Jan/08", issuetable.getCellAsText(1, 9).trim());
        // Assert row 2: ||RAT-2|Including code snippets in a blog looks terrible|admin|Fred Flintstone||Open|UNRESOLVED|02/Jan/07|02/Jan/08||
        assertEquals("Cell (2, 1) in table 'issuetable' should be 'RAT-2'.", "RAT-2", issuetable.getCellAsText(2, 1).trim());
        assertEquals("Cell (2, 8) in table 'issuetable' should be '02/Jan/07'.", "02/Jan/07", issuetable.getCellAsText(2, 8).trim());
        assertEquals("Cell (2, 9) in table 'issuetable' should be '02/Jan/08'.", "02/Jan/08", issuetable.getCellAsText(2, 9).trim());
        // Assert row 3: ||RAT-1|Most of the contributed Analyzers suffer from invalid recognition of acronyms.|admin|admin||Open|UNRESOLVED|01/Jan/07|01/Jan/08||
        assertEquals("Cell (3, 1) in table 'issuetable' should be 'RAT-1'.", "RAT-1", issuetable.getCellAsText(3, 1).trim());
        assertEquals("Cell (3, 8) in table 'issuetable' should be '01/Jan/07'.", "01/Jan/07", issuetable.getCellAsText(3, 8).trim());
        assertEquals("Cell (3, 9) in table 'issuetable' should be '01/Jan/08'.", "01/Jan/08", issuetable.getCellAsText(3, 9).trim());

        // Now we delete the link between RAT-1 and RAT-3:
        tester.clickLinkWithText("RAT-1");
        tester.clickLink("delete-link_internal-10002_10000");
        tester.submit("Delete");

        // Assert that the Issue's updated date is changed in the DB (on the view issue screen).
        navigation.issue().viewIssue("RAT-1");
        text.assertTextPresent(new IdLocator(tester, "create-date"), "01/Jan/07 12:00 AM");
        text.assertTextPresent(new IdLocator(tester, "updated-date"), "Today");

        // Assert that the updated date is also in Lucene (look at the Issue Navigator):
        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        // Assert the cells in table 'issuetable'.
        issuetable = tester.getDialog().getWebTableBySummaryOrId("issuetable");
        // get today's date for the expected Change date:
        String today = new SimpleDateFormat("dd/MMM/yy").format(new Date());
        // Assert row 0: |T|Key|Summary|Assignee|Reporter|Pr|Status|Res|Created|Updated|Due|
        assertEquals("Cell (0, 1) in table 'issuetable' should be 'Key'.", "Key", issuetable.getCellAsText(0, 1).trim());
        assertEquals("Cell (0, 8) in table 'issuetable' should be 'Created'.", "Created", issuetable.getCellAsText(0, 8).trim());
        assertEquals("Cell (0, 9) in table 'issuetable' should be 'Updated'.", "Updated", issuetable.getCellAsText(0, 9).trim());
        // Assert row 1: ||RAT-3|Where's the cheese?|admin|Fred Flintstone||Open|UNRESOLVED|03/Jan/07|24/Oct/08||
        assertEquals("Cell (1, 1) in table 'issuetable' should be 'RAT-3'.", "RAT-3", issuetable.getCellAsText(1, 1).trim());
        assertEquals("Cell (1, 8) in table 'issuetable' should be '03/Jan/07'.", "03/Jan/07", issuetable.getCellAsText(1, 8).trim());
        assertEquals("Cell (1, 9) in table 'issuetable' should be updated to today.", today, issuetable.getCellAsText(1, 9).trim());
        // Assert row 2: ||RAT-2|Including code snippets in a blog looks terrible|admin|Fred Flintstone||Open|UNRESOLVED|02/Jan/07|02/Jan/08||
        assertEquals("Cell (2, 1) in table 'issuetable' should be 'RAT-2'.", "RAT-2", issuetable.getCellAsText(2, 1).trim());
        assertEquals("Cell (2, 8) in table 'issuetable' should be '02/Jan/07'.", "02/Jan/07", issuetable.getCellAsText(2, 8).trim());
        assertEquals("Cell (2, 9) in table 'issuetable' should be '02/Jan/08'.", "02/Jan/08", issuetable.getCellAsText(2, 9).trim());
        // Assert row 3: ||RAT-1|Most of the contributed Analyzers suffer from invalid recognition of acronyms.|admin|admin||Open|UNRESOLVED|01/Jan/07|24/Oct/08||
        assertEquals("Cell (3, 1) in table 'issuetable' should be 'RAT-1'.", "RAT-1", issuetable.getCellAsText(3, 1).trim());
        assertEquals("Cell (3, 8) in table 'issuetable' should be '01/Jan/07'.", "01/Jan/07", issuetable.getCellAsText(3, 8).trim());
        assertEquals("Cell (3, 9) in table 'issuetable' should be updated to today.", today, issuetable.getCellAsText(3, 9).trim());
    }
}
