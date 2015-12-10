
package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.page.IssueSearchPage;
import com.atlassian.jira.functest.framework.page.ViewIssuePage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * These tests essentially mirror those of {@link TestUserRenameOnIssues},
 * but checking the other views instead.
 *
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.ISSUES })
public class TestUserRenameOnIssuePrintableView extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("user_rename.xml");
    }

    //    KEY       USERNAME    NAME
    //    bb	    betty	    Betty Boop
    //    ID10001	bb	        Bob Belcher
    //    cc	    cat	        Crazy Cat
    //    ID10101	cc	        Candy Chaos

    public void testPrintableViewWithRenamedUsers()
    {
        // COW-1
        navigation.issue().viewPrintable("COW-1");
        assertPrintableViewUserLink("word_assignee_betty", "betty", "Betty Boop");
        assertPrintableViewUserLink("word_reporter_cat", "cat", "Crazy Cat");
        assertPrintableViewUser("multiuser_cf_admin", "admin", "Adam Ant");
        assertPrintableViewUser("multiuser_cf_bb", "bb", "Bob Belcher");
        assertPrintableViewUser("user_cf_cc", "cc", "Candy Chaos");

        // COW-3
        navigation.issue().viewPrintable("COW-3");
        assertPrintableViewUserLink("word_assignee_bb", "bb", "Bob Belcher");
        assertPrintableViewUserLink("word_reporter_cc", "cc", "Candy Chaos");
        assertPrintableViewUser("multiuser_cf_admin", "admin", "Adam Ant");
        assertPrintableViewUser("multiuser_cf_betty", "betty", "Betty Boop");
        assertPrintableViewUser("user_cf_cat", "cat", "Crazy Cat");

        // Now test Search
        IssueSearchPage issueSearchPage = navigation.issueNavigator().runPrintableSearch("");
        HtmlTable searchResultsTable = issueSearchPage.getResultsTable();

        // COW-3
        HtmlTable.Row row = searchResultsTable.getRow(2);
        assertTableUsername("bb", row, "Assignee");
        assertTableUsername("cc", row, "Reporter");
        assertTableUsername("cat", row, "Tester");
        assertTableUsernames(asList("admin", "betty"), row, "CC");

        // COW-1
        row = searchResultsTable.getRow(4);
        assertTableUsername("betty", row, "Assignee");
        assertTableUsername("cat", row, "Reporter");
        assertTableUsername("cc", row, "Tester");
        assertTableUsernames(asList("admin", "bb"), row, "CC");
    }

    public void testPrintableViewBeforeAndAfterAnotherRename()
    {
        // COW-1
        navigation.issue().viewPrintable("COW-1");
        assertPrintableViewUser("user_cf_cc", "cc", "Candy Chaos");

        // COW-3
        navigation.issue().viewPrintable("COW-3");
        assertPrintableViewUserLink("word_reporter_cc", "cc", "Candy Chaos");

        // Rename cc to candy
        navigation.gotoPage("secure/admin/user/EditUser!default.jspa?editName=cc");
        tester.setFormElement("username", "candy");
        tester.submit("Update");
        // Now we are on View User
        assertEquals("/secure/admin/user/ViewUser.jspa?name=candy", navigation.getCurrentPage());
        assertEquals("candy", locator.id("username").getText());

        // COW-1 - Issue should still have Candy in the CF, but with the new username
        navigation.issue().viewPrintable("COW-1");
        assertPrintableViewUser("user_cf_candy", "candy", "Candy Chaos");

        // COW-3
        navigation.issue().viewPrintable("COW-3");
        assertPrintableViewUserLink("word_reporter_candy", "candy", "Candy Chaos");

        IssueSearchPage issueSearchPage = navigation.issueNavigator().runPrintableSearch("");
        HtmlTable searchResultsTable = issueSearchPage.getResultsTable();
        assertTableUsername("candy", searchResultsTable.getRow(2), "Reporter");
        assertTableUsername("candy", searchResultsTable.getRow(4), "Tester");
    }



    private void assertPrintableViewText(String id, String expectedValue)
    {
        assertEquals(expectedValue, locator.id(id).getText());
    }

    private void assertPrintableViewUser(String id, String username, String fullName)
    {
        final Element span = (Element)locator.id(id).getNode();
        assertNotNull(span);
        assertEquals(username, span.getAttribute("rel"));
        assertPrintableViewText(id, fullName);
        assertEquals("span", span.getTagName());
        assertFalse(span.hasAttribute("href"));
    }

    private void assertPrintableViewUserLink(String id, String username, String fullName)
    {
        final Element link = (Element)locator.id(id).getNode();
        assertNotNull(link);
        assertEquals(username, link.getAttribute("rel"));
        assertPrintableViewText(id, fullName);
        assertEquals("a", link.getTagName());
        assertTrue(link.getAttribute("href").endsWith("name=" + username));
    }



    private void assertTableUsername(String username, HtmlTable.Row row, String heading)
    {
        final Node cellNode = row.getCellNodeForHeading(heading);
        assertEquals(username, ViewIssuePage.getRelUsername(cellNode));
    }

    private void assertTableUsernames(List<String> usernames, HtmlTable.Row row, String heading)
    {
        final Node cellNode = row.getCellNodeForHeading(heading);
        assertEquals(usernames, ViewIssuePage.getRelUsernames(cellNode));
    }



    private Document getDocument() throws IOException, DocumentException
    {
        SAXReader reader = new SAXReader();
        return reader.read(tester.getDialog().getResponse().getInputStream());
    }
}

