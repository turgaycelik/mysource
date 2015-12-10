package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.tpm.ldap.UserDirectoryTable;
import com.meterware.httpunit.WebLink;
import org.xml.sax.SAXException;

import java.io.IOException;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * This test class test for User queries being case insensitive.
 * There are situations with multiple directories (particularly when using LDAP) where customers
 * can have the same user in multiple directories but with the user name varying by case. In this case (and always)
 * queries should find issues where the user is stored with either an upper, lower or mixed case name.
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.SLOW_IMPORT })
public class TestUserQueriesCaseInsensitive extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreDataSlowOldWay("TestUserQueriesCaseInsensitive.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testUserQueries() throws Exception
    {
        assertEquals("Administrator from LDAP", parse.header().getFullUserName());
        log("Running queries with (pseudo) LDAP directory first");
        runUserQueries();
        swapDirectories();
        assertEquals("Administrator in the Shadows", parse.header().getFullUserName());
        log("Running queries with internal directory first");
        runUserQueries();
    }

    private void runUserQueries()
    {
        log("Running assignee queries");
        assertSearchWithResults("assignee = admin", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4");
        assertSearchWithResults("assignee = Admin", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4");
        assertSearchWithResults("assignee = fred");
        assertSearchWithResults("assignee = Fred");
        log("Running reporter queries");
        assertSearchWithResults("reporter = admin", "NO-1", "JRA-2", "JRA-1", "CONF-1", "ABC-3", "ABC-2");
        assertSearchWithResults("reporter = Admin", "NO-1", "JRA-2", "JRA-1", "CONF-1", "ABC-3", "ABC-2");
        assertSearchWithResults("reporter = currentUser()", "NO-1", "JRA-2", "JRA-1", "CONF-1", "ABC-3", "ABC-2");
        log("Running voter queries");
        assertSearchWithResults("voter = admin", "MKY-1");
        assertSearchWithResults("voter = Admin", "MKY-1");
        assertSearchWithResults("voter = fred", "MKY-1", "JRA-2");
        assertSearchWithResults("voter = Fred", "MKY-1", "JRA-2");
        log("Running watcher queries");
        assertSearchWithResults("watcher = admin", "JRA-2");
        assertSearchWithResults("watcher = Admin", "JRA-2");
        log("Running custom field queries");
        assertSearchWithResults("\"QA done by\" = John", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("\"QA done by\" = john", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("\"Interested parties\" = admin", "JRA-1", "HSP-1");
        assertSearchWithResults("\"Interested parties\" = Admin", "JRA-1", "HSP-1");
        assertSearchWithResults("\"Interested parties\" = John", "JRA-2", "HSP-1", "CONF-1");
        assertSearchWithResults("\"Interested parties\" = john", "JRA-2", "HSP-1", "CONF-1");

        log("Running change history queries");
        assertSearchWithResults("reporter was admin before \"2009-07-24\"", "NO-1", "JRA-2", "JRA-1", "CONF-1", "ABC-2");
        assertSearchWithResults("reporter was Admin before \"2009-07-24\"", "NO-1", "JRA-2", "JRA-1", "CONF-1", "ABC-2");
        assertSearchWithResults("reporter was not admin after \"2009-07-24\"", "MKY-1", "HSP-1", "ABC-4");
        assertSearchWithResults("reporter was not Admin after \"2009-07-24\"", "MKY-1", "HSP-1", "ABC-4");
    }

    public void swapDirectories() throws IOException, SAXException
    {
        log("Swap directory order");
        navigation.gotoPage("/plugins/servlet/embedded-crowd/directories/list");
        tester.setWorkingForm("");

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(2, 3).getLinkWith("up");
        navigation.clickLink(link);
    }
}
