package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Queries mentioned in our documentation on
 * http://confluence.atlassian.com/display/JIRA/Advanced+Searching
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestQueriesInDocumentation extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestQueriesInDocumentation.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testJQLLanguageReference() throws Exception
    {
        assertSearchWithResults("project = \"New office\" and status = \"open\"", "NO-1");
        assertSearchWithResults("status = open and priority = urgent and assignee = jsmith", "ABC-3", "ABC-2");
        assertSearchWithResults("project = JRA and assignee != jsmith", "JRA-2");
        assertSearchWithResults("project in (JRA,CONF) and fixVersion = \"3.14\"", "JRA-2", "JRA-1", "CONF-1");
        assertSearchWithResults("reporter = jsmith or reporter = jbrown", "MKY-1", "HSP-1");
        assertSearchWithResults("duedate < now() or duedate is empty", "NO-1", "MKY-1", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("not assignee = jsmith", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4");
        assertSearchWithResults("assignee != jsmith", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4");
        assertSearchWithResults("reporter != jsmith", "NO-1", "MKY-1", "JRA-2", "JRA-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("reporter = currentUser() and assignee != currentUser()", "ABC-3", "ABC-2");
        assertSearchWithResults("assignee != \"John Smith\" or reporter != \"John Smith\"", "NO-1", "MKY-1", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("assignee != empty", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("assignee != empty", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("reporter in (jsmith,jbrown,jjones)", "MKY-1", "HSP-1", "ABC-4");
        assertSearchWithResults("reporter in (Jack,Jill) or assignee in (Jack,Jill)", "MKY-1");
        assertSearchWithResults("affectedVersion in (\"3.14\", \"4.2\")", "JRA-2", "JRA-1");
        assertSearchWithResults("reporter = jsmith", "HSP-1");
        assertSearchWithResults("reporter = \"John Smith\"", "HSP-1");
        assertSearchWithResults("votes > 4", "MKY-1");

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
        final Date date = new Date(System.currentTimeMillis() - 604800000);

        navigation.issue().viewIssue("MKY-1");
        tester.clickLink("edit-issue");
        tester.setFormElement("duedate",dateFormat.format(date));
        tester.submit();

        navigation.issue().viewIssue("NO-1");
        tester.clickLink("edit-issue");
        tester.setFormElement("duedate",dateFormat.format(date));
        tester.submit();

        assertSearchWithResults("due < now()  and resolution is empty", "NO-1", "MKY-1");
        assertSearchWithResults("priority > normal", "MKY-1", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("votes >= 4", "MKY-1", "JRA-2");

        final SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd");
        assertSearchWithResults("due >= \""+dF.format(date)+"\"", "NO-1", "MKY-1");
        assertSearchWithResults("votes < 4", "NO-1", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("votes <= 4", "NO-1", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        //updated <= "-4w 2d"
        assertSearchWithResults("summary ~ fin", "MKY-1");
        assertSearchWithResults("summary ~ Harry\\'s", "NO-1", "JRA-2");
        assertSearchWithResults("summary ~ Sales\\\\Marketing", "ABC-3", "ABC-2");
        assertSearchWithResults("summary ~ \"\\\"Harry's computer\\\"\"", "JRA-2");
        assertSearchWithResults("due = empty", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("due is empty", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("due = null", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("due is null", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");


        //These queries return the results always in the same order, because we store the created date down to the second.
        navigation.issueNavigator().createSearch("due = empty order by created");
        assertIssues("ABC-4", "ABC-3", "HSP-1", "CONF-1", "JRA-2", "JRA-1", "ABC-2");

        navigation.issueNavigator().createSearch("due = empty order by created, priority desc");
        assertIssues("ABC-4", "ABC-3", "HSP-1", "CONF-1", "JRA-2", "JRA-1", "ABC-2");

        navigation.issueNavigator().createSearch("due = empty order by created, priority asc");
        assertIssues("ABC-4", "ABC-3", "HSP-1", "CONF-1", "JRA-2", "JRA-1", "ABC-2");
    }

    public void testFieldsAndFunctionsReference() throws Exception
    {
        assertSearchWithResults("project = \"ABC Project\"", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("project = \"ABC\"", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("project = 10010", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("category = \"Alphabet Projects\"", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("issueKey = \"ABC-2\"", "ABC-2");
        assertSearchWithResults("affectedVersion = \"3.14\"", "JRA-2");
        assertSearchWithResults("affectedVersion = \"Big Ted\"", "NO-1", "ABC-3");
        assertSearchWithResults("affectedVersion = \"10020\"", "NO-1");
        assertSearchWithResults("fixVersion in (\"3.14\", \"4.2\")", "JRA-2", "JRA-1", "CONF-1");
        assertSearchWithResults("fixVersion = \"Little Ted\"", "ABC-3", "ABC-2");
        assertSearchWithResults("fixVersion = \"Little Ted\"", "ABC-3", "ABC-2");
        assertSearchWithResults("fixVersion = 10012", "ABC-3", "ABC-2");
        assertSearchWithResults("component in (Comp1, Comp2)", "ABC-3","ABC-2");
        assertSearchWithResults("component = 10010", "ABC-3","ABC-2");
        assertSearchWithResults("issueType in (Bug,Improvement)", "MKY-1","JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-2");
        assertSearchWithResults("issueType = Bug", "MKY-1","JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-2");
        assertSearchWithResults("issueType = 2", "ABC-4");
        assertSearchWithResults("priority = High", "ABC-4");
        assertSearchWithResults("priority = 8", "ABC-4");
        assertSearchWithResults("status = Open", "NO-1", "MKY-1", "JRA-2", "CONF-1", "ABC-3", "ABC-2");
        assertSearchWithResults("status = 1", "NO-1", "MKY-1", "JRA-2", "CONF-1", "ABC-3", "ABC-2");
        assertSearchWithResults("resolution in (\"Cannot Reproduce\", \"Won't Fix\")", "JRA-1", "HSP-1");
        assertSearchWithResults("resolution = 1", "ABC-4");
        assertSearchWithResults("level in (\"Really High\", level1)", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("level = 10000", "ABC-3", "ABC-2");
        assertSearchWithResults("summary ~ \"Error saving file\"", "ABC-4");
        assertSearchWithResults("description ~ \"Please see screenshot for details.\"", "ABC-4");
        assertSearchWithResults("environment ~ \"Third floor\"", "ABC-4");
        assertSearchWithResults("comment ~ \"My PC is quite old\"", "ABC-4");
        assertSearchWithResults("assignee = \"John Smith\"", "ABC-3", "ABC-2");
        assertSearchWithResults("assignee = jsmith", "ABC-3", "ABC-2");
        assertSearchWithResults("reporter = \"Jill Jones\"", "ABC-4");
        assertSearchWithResults("reporter = jjones", "ABC-4");
        assertSearchWithResults("parent = \"ABC-2\"", "ABC-3");
        assertSearchWithResults("originalEstimate = 1h", "NO-1", "HSP-1");
        assertSearchWithResults("originalEstimate > 2d", "JRA-2", "CONF-1");
        assertSearchWithResults("remainingEstimate > 4h", "JRA-2", "CONF-1");
        assertSearchWithResults("timeSpent > 5d", "CONF-1");
        assertSearchWithResults("workratio > 75", "HSP-1");
        assertSearchWithResults("votes >= 12");
        assertSearchWithResults("created >= \"2009/07/23\" AND created < \"2009/07/24\"", "NO-1", "MKY-1", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-2");
        assertSearchWithResults("updated >= \"2009/07/24\" AND updated < \"2009/07/25\"", "HSP-1", "CONF-1");
        assertSearchWithResults("due = \"2009/07/24\"","NO-1");
        assertSearchWithResults("resolved <= \"2009/07/31\" and resolved >= \"2009/07/01\"", "JRA-1", "HSP-1", "ABC-4");
        assertSearchWithResults("location != empty", "NO-1", "MKY-1", "JRA-2", "ABC-4");
        assertSearchWithResults("location = \"New York\"", "ABC-4");
        assertSearchWithResults("location in (\"London\",\"Milan\",\"Paris\")", "NO-1", "MKY-1", "JRA-2");
        assertSearchWithResults("cf[10000] = \"New York\"", "ABC-4");
        assertSearchWithResults("version in (\"3.12\", \"3.13\", \"3.14\")", "JRA-2", "JRA-1");
        assertSearchWithResults("project = \"JIRA\" and version != \"3.14\"", "JRA-1");
        assertSearchWithResults("assignee = currentUser()", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4");
        assertSearchWithResults("reporter = currentUser() and assignee != currentUser()", "ABC-3", "ABC-2");
        assertSearchWithResults("assignee in membersOf(\"jira-developers\")", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("assignee in membersOf(QA) and assignee not in (\"Jill Jones\")", "ABC-3", "ABC-2");
        assertSearchWithResults("assignee not in membersOf(QA)", "NO-1", "MKY-1", "JRA-2", "HSP-1", "CONF-1", "ABC-4");
        assertSearchWithResults("due < now() and status not in (closed, resolved)", "NO-1", "MKY-1");
        assertSearchWithResults("fixVersion in releasedVersions(ABC)", "ABC-3", "ABC-2");
        assertSearchWithResults("affectedVersion in releasedVersions(ABC)", "ABC-4");
        assertSearchWithResults("fixVersion in unreleasedVersions(ABC)", "ABC-4");
        assertSearchWithResults("affectedVersion in unreleasedVersions(ABC) or fixVersion in unreleasedVersions(ABC)", "ABC-4", "ABC-3");
        assertSearchWithResults("issuetype in standardIssueTypes()", "NO-1", "MKY-1", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-2");
        assertSearchWithResults("issue in linkedIssues(\"ABC-4\")", "ABC-2");
        assertSearchWithResults("issue in linkedIssues(\"ABC-4\",\"is related to\")", "ABC-2");
        assertSearchWithResults("issue in votedIssues()", "MKY-1");
        assertSearchWithResults("issue in watchedIssues()", "JRA-2");
        assertSearchWithResults("filter = \"JIRA, Confluence and ABC Project Issues\"", "JRA-2", "JRA-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");
        assertSearchWithResults("filter = \"JIRA, Confluence and ABC Project Issues\" and assignee = jsmith", "ABC-3", "ABC-2");
        assertSearchWithResults("filter = 10000 and assignee = jsmith", "ABC-3", "ABC-2");
        assertSearchWithResults("myCascadingSelect in cascadeoption(parentOption1,child1OfParentOption1)", "ABC-4");
        navigation.issue().viewIssue("NO-1");
        assertSearchWithResults("issue in issueHistory()", "NO-1", "MKY-1", "JRA-2", "JRA-1", "HSP-1", "CONF-1", "ABC-4", "ABC-3", "ABC-2");

        final String issueKey = navigation.issue().createIssue("JIRA", "Bug", "Just created");
        assertSearchWithResults("created > \"-8h\"", issueKey);
    }

}
