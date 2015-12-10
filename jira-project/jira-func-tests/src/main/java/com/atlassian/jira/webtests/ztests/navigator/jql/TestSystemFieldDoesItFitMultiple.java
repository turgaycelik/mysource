package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Testing for "fitness" in the filter form of multiple system field clauses.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSystemFieldDoesItFitMultiple extends AbstractJqlFuncTest
{
    private static final ThreadLocal<AtomicBoolean> dataSetUp = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue()
        {
            return new AtomicBoolean(false);
        }
    };

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        if (!dataSetUp.get().getAndSet(true))
        {
            administration.restoreData("TestSystemFieldDoesItFitMultiple.xml");
            backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        }
    }

    public void testAffectedVersion() throws Exception
    {
        final String fieldName = "affectedVersion";

        assertOrTooComplex(fieldName, "\"New Version 1\"", "\"New Version 4\"");
        assertAndTooComplex(fieldName, "\"New Version 1\"", "\"New Version 4\"");

        // ID-s are not allowed in basic mode.
        assertTooComplex("affectedVersion IN (10000, 100001)");

        assertAndWithHspFitsFilterForm("affectedVersion = \"New Version 1\"", createFilterFormParam("version", "New Version 1"));
        assertOrWithHspTooComplex("affectedVersion = \"New Version 1\"");
        assertAndWithHspTooComplex("affectedVersion != \"New Version 1\"");
        assertAndWithHspTooComplex("affectedVersion >= 10000");
        assertAndWithHspTooComplex("affectedVersion > 10000");
        assertAndWithHspTooComplex("affectedVersion <= 10000");
        assertAndWithHspTooComplex("affectedVersion < 10000");
        assertAndWithHspFitsFilterForm("affectedVersion is EMPTY", createFilterFormParam("version", "-1"));
        assertAndWithHspTooComplex("affectedVersion is not EMPTY");
        assertAndWithHspFitsFilterForm("affectedVersion in (\"New Version 1\", \"New Version 4\")", createFilterFormParam("version", "New Version 1", "New Version 4"));
        assertAndWithHspTooComplex("affectedVersion not in (\"New Version 1\", \"New Version 4\")");
        assertAndWithHspTooComplex("affectedVersion in releasedVersions(HSP)");
        assertAndWithHspFitsFilterForm("affectedVersion in releasedVersions()", createFilterFormParam("version", "-3"));
        assertAndWithHspTooComplex("affectedVersion not in releasedVersions()");
        assertAndWithHspTooComplex("affectedVersion in unreleasedVersions(HSP)");
        assertAndWithHspFitsFilterForm("affectedVersion in unreleasedVersions()", createFilterFormParam("version", "-2"));
        assertAndWithHspTooComplex("affectedVersion not in unreleasedVersions()");

        assertFitsFilterForm("affectedVersion = \"New Version 1\"");
        assertFitsFilterForm("affectedVersion IN (\"New Version 1\", MonkeyVersion)");

        // if there are no released versions, it will still fit, but nothing will be selected
        assertFitsFilterForm("project = MKY AND affectedVersion in releasedVersions()", createFilterFormParam("version", (String[]) null));

        // TODO: test that component is invalid
        assertFitsFilterForm("project = MKY AND affectedVersion = MonkeyVersion", createFilterFormParam("project", "MKY"), createFilterFormParam("version", "MonkeyVersion"));

        assertFitsFilterForm("project in (HSP, MKY) AND affectedVersion = \"New Version 1\"");

        // TODO: test that MonkeyVersion is invalid
        assertFitsFilterForm("project = HSP AND affectedVersion IN (\"New Version 1\", MonkeyVersion)", createFilterFormParam("project", "MKY"),
                createFilterFormParam("version", "10000", "MonkeyVersion"));

        // can be on different levels as fitness is calculated on context
        assertFitsFilterForm("project = HSP AND (status = Open AND affectedVersion = MonkeyVersion)", createFilterFormParam("pid", "10000"),
                createFilterFormParam("version", "MonkeyVersion"), createFilterFormParam("status", "1"));
    }

    public void testAssignee() throws Exception
    {
        final String fieldName = "assignee";

        assertOrTooComplex(fieldName, ADMIN_USERNAME, FRED_USERNAME);
        assertAndTooComplex(fieldName, ADMIN_USERNAME, FRED_USERNAME);

        assertAndWithHspFitsFilterForm("assignee = fred", createFilterFormParam("assignee", FRED_USERNAME), createFilterFormParam("assigneeSelect", "specificuser"));
        assertOrWithHspTooComplex("assignee = fred");
    }

    public void testQuery() throws Exception
    {
        assertOrTooComplex("text", "~", "ccc", "ccc");
        assertAndTooComplex("text", "~", "ccc", "ccc");
        assertOrTooComplex("text", "~", "ccc", "different");

        final IssueNavigatorAssertions.FilterFormParam queryGood = createFilterFormParam("text", "ccc");

        // all the good fit combinations
        assertFitsFilterForm("text ~ ccc", queryGood);

        assertTooComplex("comment ~ ccc OR environment ~ ccc");

        assertTooComplex("comment ~ ccc OR summary ~ ccc");

        assertTooComplex("comment ~ ccc OR description ~ ccc OR environment ~ ccc");

        assertTooComplex("comment ~ ccc OR description ~ ccc OR summary ~ ccc");

        assertTooComplex("comment ~ ccc OR environment ~ ccc OR summary ~ ccc");

        assertTooComplex("comment ~ ccc OR description ~ ccc OR environment ~ ccc OR summary ~ ccc");

        assertTooComplex("description ~ ccc OR environment ~ ccc");

        assertTooComplex("description ~ ccc OR summary ~ ccc");

        assertTooComplex("description ~ ccc OR environment ~ ccc OR summary ~ ccc");

        assertTooComplex("environment ~ ccc OR summary ~ ccc");

        // doesnt fit if one uses !~
        assertTooComplex("comment ~ ccc OR description !~ ccc");

        // doesnt fit if one is different from the others
        assertTooComplex("comment ~ ccc OR description ~ different");

        // doesnt fit if one is empty
        assertTooComplex("comment ~ ccc OR description is EMPTY");

        // doesnt fit if on multiple levels
        assertTooComplex("(comment ~ ccc OR description ~ ccc) OR (environment ~ ccc OR summary ~ ccc)");
    }

    public void testComponent() throws Exception
    {
        final String fieldName = "component";

        assertOrTooComplex(fieldName, "\"New Component 1\"", "\"New Component 2\"");
        assertAndTooComplex(fieldName, "\"New Component 1\"", "\"New Component 2\"");

        // ID-s are not allowed in basic mode.
        assertTooComplex("component IN (10000, 100001)");

        assertAndWithHspFitsFilterForm("component = \"New Component 1\"", createFilterFormParam("component", "New Component 1"));
        assertOrWithHspTooComplex("component = \"New Component 1\"");
        assertAndWithHspTooComplex("component != \"New Component 1\"");
        assertAndWithHspFitsFilterForm("component is EMPTY", createFilterFormParam("component", "-1"));
        assertAndWithHspTooComplex("component is not EMPTY");
        assertAndWithHspFitsFilterForm("component in (\"New Component 1\", \"New Component 2\")", createFilterFormParam("component", "New Component 1", "New Component 2"));
        assertAndWithHspTooComplex("component not in (\"New Component 1\", \"New Component 2\")");

        assertFitsFilterForm("component = \"New Component 1\"");
        assertFitsFilterForm("component IN (\"New Component 1\", MonkeyComponent)");

        // TODO: test that component is invalid
        assertFitsFilterForm("project = MKY AND component = MonkeyComponent", createFilterFormParam("project", "MKY"), createFilterFormParam("component", "MonkeyComponent"));

        assertFitsFilterForm("project in (HSP, MKY) AND component = \"New Component 1\"");

        // TODO: test that MonkeyVersion is invalid
        assertFitsFilterForm("project = HSP AND component IN (\"New Component 1\", MonkeyComponent)", createFilterFormParam("project", "MKY"),
                createFilterFormParam("version", "New Component 1", "MonkeyComponent"));

        // can be on different levels as fitness is calculated on context
        assertFitsFilterForm("project = HSP AND (status = Open AND component = MonkeyComponent)", createFilterFormParam("pid", "10000"),
                createFilterFormParam("component", "MonkeyComponent"), createFilterFormParam("status", "1"));
    }

    public void testCreated() throws Exception
    {
        final IssueNavigatorAssertions.FilterFormParam afterAbs = createFilterFormParam("created:after", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeAbs = createFilterFormParam("created:before", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeRel = createFilterFormParam("created:next", "1d");
        final IssueNavigatorAssertions.FilterFormParam afterRel = createFilterFormParam("created:previous", "1d");

        // all good combinations
        assertFitsFilterForm("created >= '2009-05-11' AND created <= '2009-05-11'", afterAbs, beforeAbs);
        assertFitsFilterForm("created >= '2009-05-11' AND created <= '1d'", afterAbs, beforeRel);
        assertFitsFilterForm("created >= '1d' AND created <= '2009-05-11'", afterRel, beforeAbs);
        assertFitsFilterForm("created >= '1d' AND created <= '1d'", afterRel, beforeRel);
        assertFitsFilterForm("created >= '2009-05-11' AND created <= '2009-05-11' AND created >= '1d'", afterAbs, beforeAbs, afterRel);
        assertFitsFilterForm("created >= '2009-05-11' AND created <= '2009-05-11' AND created <= '1d'", afterAbs, beforeAbs, beforeRel);
        assertFitsFilterForm("created >= '1d' AND created <= '2009-05-11' AND created >= '2009-05-11'", afterRel, beforeAbs, afterAbs);
        assertFitsFilterForm("created >= '1d' AND created <= '1d' AND created <= '2009-05-11'", afterRel, beforeRel, beforeAbs);
        assertFitsFilterForm("created >= '1d' AND created <= '1d' AND created >= '2009-05-11' AND created <= '2009-05-11'", afterRel, beforeRel, afterAbs, beforeAbs);

        // can't combine with or
        assertTooComplex("created >= '1d' OR created <= '1d'");

        // can't combine same relational operator
        assertTooComplex("created >= '1d' AND created >= '2d'");
        assertTooComplex("created <= '1d' AND created <= '2d'");

        // cant use < or >
        assertTooComplex("created > '1d' AND created <= '1d' AND created >= '2009-05-11' AND created <= '2009-05-11'");

        // if one of the operands is wrong it wont fit
        assertTooComplex("created >= 1234567890 AND created <= '1d' AND created >= '2009-05-11' AND created <= '2009-05-11'");

        // fits even if split over multiple levels
        assertFitsFilterForm("(project = HSP AND created <= '1d') AND (status = Open AND created <= '2009-05-11')", createFilterFormParam("pid", "10000"), createFilterFormParam("status", "1"), beforeRel, beforeAbs);

        // with other clauses
        assertAndWithHspFitsFilterForm("created >= '1d'", afterRel);
        assertOrWithHspTooComplex("created >= '1d'");
    }

    public void testDueDate() throws Exception
    {
        final IssueNavigatorAssertions.FilterFormParam afterAbs = createFilterFormParam("duedate:after", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeAbs = createFilterFormParam("duedate:before", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeRel = createFilterFormParam("duedate:next", "1d");
        final IssueNavigatorAssertions.FilterFormParam afterRel = createFilterFormParam("duedate:previous", "1d");

        // all good combinations
        assertFitsFilterForm("due >= '2009-05-11' AND due <= '2009-05-11'", afterAbs, beforeAbs);
        assertFitsFilterForm("due >= '2009-05-11' AND due <= '1d'", afterAbs, beforeRel);
        assertFitsFilterForm("due >= '1d' AND due <= '2009-05-11'", afterRel, beforeAbs);
        assertFitsFilterForm("due >= '1d' AND due <= '1d'", afterRel, beforeRel);
        assertFitsFilterForm("due >= '2009-05-11' AND due <= '2009-05-11' AND due >= '1d'", afterAbs, beforeAbs, afterRel);
        assertFitsFilterForm("due >= '2009-05-11' AND due <= '2009-05-11' AND due <= '1d'", afterAbs, beforeAbs, beforeRel);
        assertFitsFilterForm("due >= '1d' AND due <= '2009-05-11' AND due >= '2009-05-11'", afterRel, beforeAbs, afterAbs);
        assertFitsFilterForm("due >= '1d' AND due <= '1d' AND due <= '2009-05-11'", afterRel, beforeRel, beforeAbs);
        assertFitsFilterForm("due >= '1d' AND due <= '1d' AND due >= '2009-05-11' AND due <= '2009-05-11'", afterRel, beforeRel, afterAbs, beforeAbs);

        // can't combine with or
        assertTooComplex("due >= '1d' OR due <= '1d'");

        // can't combine same relational operator
        assertTooComplex("due >= '1d' AND due >= '2d'");
        assertTooComplex("due <= '1d' AND due <= '2d'");

        // cant use < or >
        assertTooComplex("due > '1d' AND due <= '1d' AND due >= '2009-05-11' AND due <= '2009-05-11'");

        // if one of the operands is wrong it wont fit
        assertTooComplex("due >= 1234567890 AND due <= '1d' AND due >= '2009-05-11' AND due <= '2009-05-11'");

        // fits even if split over multiple levels
        assertFitsFilterForm("(project = HSP AND due <= '1d') AND (status = Open AND due <= '2009-05-11')", createFilterFormParam("pid", "10000"), createFilterFormParam("status", "1"), beforeRel, beforeAbs);

        // with other clauses
        assertAndWithHspFitsFilterForm("due >= '1d'", afterRel);
        assertOrWithHspTooComplex("due >= '1d'");
    }

    public void testFixVersion() throws Exception
    {
        final String fieldName = "fixVersion";

        assertOrTooComplex(fieldName, "\"New Version 1\"", "\"New Version 4\"");
        assertAndTooComplex(fieldName, "\"New Version 1\"", "\"New Version 4\"");

        // ID-s are not allowed in basic mode.
        assertTooComplex("fixVersion IN (10000, 100001)");

        assertAndWithHspFitsFilterForm("fixVersion = \"New Version 1\"", createFilterFormParam("fixfor", "New Version 1"));
        assertOrWithHspTooComplex("fixVersion = \"New Version 1\"");
        assertAndWithHspTooComplex("fixVersion != \"New Version 1\"");
        assertAndWithHspTooComplex("fixVersion >= 10000");
        assertAndWithHspTooComplex("fixVersion > 10000");
        assertAndWithHspTooComplex("fixVersion <= 10000");
        assertAndWithHspTooComplex("fixVersion < 10000");
        assertAndWithHspFitsFilterForm("fixVersion is EMPTY", createFilterFormParam("fixfor", "-1"));
        assertAndWithHspTooComplex("fixVersion is not EMPTY");
        assertAndWithHspFitsFilterForm("fixVersion in (\"New Version 1\", \"New Version 4\")", createFilterFormParam("fixfor", "New Version 1", "New Version 4"));
        assertAndWithHspTooComplex("fixVersion not in (\"New Version 1\", \"New Version 4\")");
        assertAndWithHspTooComplex("fixVersion in releasedVersions(HSP)");
        assertAndWithHspFitsFilterForm("fixVersion in releasedVersions()", createFilterFormParam("fixfor", "-3"));
        assertAndWithHspTooComplex("fixVersion not in releasedVersions()");
        assertAndWithHspTooComplex("fixVersion in unreleasedVersions(HSP)");
        assertAndWithHspFitsFilterForm("fixVersion in unreleasedVersions()", createFilterFormParam("fixfor", "-2"));
        assertAndWithHspTooComplex("fixVersion not in unreleasedVersions()");

        assertFitsFilterForm("fixVersion = \"New Version 1\"");
        assertFitsFilterForm("fixVersion IN (\"New Version 1\", MonkeyVersion)");

        // if there are no released versions, it will still fit, but nothing will be selected
        assertFitsFilterForm("project = MKY AND fixVersion in releasedVersions()", createFilterFormParam("fixfor", (String[]) null));

        // TODO: test that component is invalid
        assertFitsFilterForm("project = MKY AND fixVersion = MonkeyVersion", createFilterFormParam("project", "MKY"), createFilterFormParam("fixfor", "MonkeyVersion"));

        assertFitsFilterForm("project in (HSP, MKY) AND fixVersion = \"New Version 1\"");

        // TODO: test that MonkeyVersion is invalid
        assertFitsFilterForm("project = HSP AND fixVersion IN (\"New Version 1\", MonkeyVersion)", createFilterFormParam("project", "MKY"),
                createFilterFormParam("fixfor", "10000", "MonkeyVersion"));

        // can be on different levels as fitness is calculated on context
        assertFitsFilterForm("project = HSP AND (status = Open AND affectedVersion = MonkeyVersion)", createFilterFormParam("pid", "10000"),
                createFilterFormParam("fixfor", "MonkeyVersion"), createFilterFormParam("status", "1"));
    }

    public void testPriority() throws Exception
    {
        final String fieldName = "priority";

        assertOrTooComplex(fieldName, "Blocker", "Critical");
        assertAndTooComplex(fieldName, "Blocker", "Critical");

        assertAndWithHspFitsFilterForm("priority = Blocker", createFilterFormParam("priority", "1"));
        assertAndWithHspFitsFilterForm("priority IN (Blocker, Critical)", createFilterFormParam("priority", "1", "2"));
        assertOrWithHspTooComplex("priority = Blocker");
    }

    public void testProject() throws Exception
    {
        final String fieldName = "project";

        assertOrTooComplex(fieldName, "HSP", "MKY");
        assertAndTooComplex(fieldName, "HSP", "MKY");

        // the other regular tests are extensively checked in other test methods
    }

    public void testReporter() throws Exception
    {
        final String fieldName = "reporter";

        assertOrTooComplex(fieldName, ADMIN_USERNAME, FRED_USERNAME);
        assertAndTooComplex(fieldName, ADMIN_USERNAME, FRED_USERNAME);

        assertAndWithHspFitsFilterForm("reporter = fred", createFilterFormParam("reporter", FRED_USERNAME), createFilterFormParam("reporterSelect", "specificuser"));
        assertOrWithHspTooComplex("reporter = fred");
    }

    public void testResolution() throws Exception
    {
        final String fieldName = "resolution";

        assertOrTooComplex(fieldName, "Fixed", "Duplicate");
        assertAndTooComplex(fieldName, "Fixed", "Duplicate");

        assertAndWithHspFitsFilterForm("resolution = Fixed", createFilterFormParam("resolution", "1"));
        assertAndWithHspFitsFilterForm("resolution IN (Fixed, Duplicate)", createFilterFormParam("resolution", "1", "3"));
        assertOrWithHspTooComplex("resolution = Fixed");
    }

    public void testResolutionDate() throws Exception
    {
        final IssueNavigatorAssertions.FilterFormParam afterAbs = createFilterFormParam("resolutiondate:after", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeAbs = createFilterFormParam("resolutiondate:before", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeRel = createFilterFormParam("resolutiondate:next", "1d");
        final IssueNavigatorAssertions.FilterFormParam afterRel = createFilterFormParam("resolutiondate:previous", "1d");

        // all good combinations
        assertFitsFilterForm("resolved >= '2009-05-11' AND resolved <= '2009-05-11'", afterAbs, beforeAbs);
        assertFitsFilterForm("resolved >= '2009-05-11' AND resolved <= '1d'", afterAbs, beforeRel);
        assertFitsFilterForm("resolved >= '1d' AND resolved <= '2009-05-11'", afterRel, beforeAbs);
        assertFitsFilterForm("resolved >= '1d' AND resolved <= '1d'", afterRel, beforeRel);
        assertFitsFilterForm("resolved >= '2009-05-11' AND resolved <= '2009-05-11' AND resolved >= '1d'", afterAbs, beforeAbs, afterRel);
        assertFitsFilterForm("resolved >= '2009-05-11' AND resolved <= '2009-05-11' AND resolved <= '1d'", afterAbs, beforeAbs, beforeRel);
        assertFitsFilterForm("resolved >= '1d' AND resolved <= '2009-05-11' AND resolved >= '2009-05-11'", afterRel, beforeAbs, afterAbs);
        assertFitsFilterForm("resolved >= '1d' AND resolved <= '1d' AND resolved <= '2009-05-11'", afterRel, beforeRel, beforeAbs);
        assertFitsFilterForm("resolved >= '1d' AND resolved <= '1d' AND resolved >= '2009-05-11' AND resolved <= '2009-05-11'", afterRel, beforeRel, afterAbs, beforeAbs);

        // can't combine with or
        assertTooComplex("resolved >= '1d' OR resolved <= '1d'");

        // can't combine same relational operator
        assertTooComplex("resolved >= '1d' AND resolved >= '2d'");
        assertTooComplex("resolved <= '1d' AND resolved <= '2d'");

        // cant use < or >
        assertTooComplex("resolved > '1d' AND resolved <= '1d' AND resolved >= '2009-05-11' AND resolved <= '2009-05-11'");

        // if one of the operands is wrong it wont fit
        assertTooComplex("resolved >= 1234567890 AND resolved <= '1d' AND resolved >= '2009-05-11' AND resolved <= '2009-05-11'");

        // fits even if split over multiple levels
        assertFitsFilterForm("(project = HSP AND resolved <= '1d') AND (status = Open AND resolved <= '2009-05-11')", createFilterFormParam("pid", "10000"), createFilterFormParam("status", "1"), beforeRel, beforeAbs);

        // with other clauses
        assertAndWithHspFitsFilterForm("resolved >= '1d'", afterRel);
        assertOrWithHspTooComplex("resolved >= '1d'");
    }

    public void testStatus() throws Exception
    {
        final String fieldName = "status";

        assertOrTooComplex(fieldName, "Open", "Resolved");
        assertAndTooComplex(fieldName, "Open", "Resolved");

        assertAndWithHspFitsFilterForm("status = Open", createFilterFormParam("status", "1"));
        assertAndWithHspFitsFilterForm("status IN (Open, Resolved)", createFilterFormParam("status", "1", "5"));
        assertOrWithHspTooComplex("status = Open");
        
        assertAndWithHspFitsFilterForm("status = hsp_status", createFilterFormParam("status", "10001"));
        // TODO: test invalid
        assertFitsFilterForm("project = HSP and status = mky_status", createFilterFormParam("project", "HSP"), createFilterFormParam("status", "mky_status"));
        // TODO: test invalid
        assertFitsFilterForm("project = HSP and status = mky_bug_status", createFilterFormParam("project", "HSP"), createFilterFormParam("status", "mky_status"));
        // TODO: test invalid
        assertFitsFilterForm("project = HSP and type = bug and status = mky_bug_status", createFilterFormParam("project", "HSP"),
                createFilterFormParam("type", "bug"), createFilterFormParam("status", "mky_bug_status"));
        // TODO: test invalid
        assertFitsFilterForm("project = MKY and type = improvement and status = mky_bug_status", createFilterFormParam("project", "MKY"),
                createFilterFormParam("type", "improvement"), createFilterFormParam("status", "mky_bug_status"));

        assertFitsFilterForm("project = MKY and status = mky_bug_status", createFilterFormParam("status", "10002"));
        assertFitsFilterForm("project = MKY and type = bug and status = mky_bug_status", createFilterFormParam("status", "10002"));
    }

    public void testType() throws Exception
    {
        administration.activateSubTasks();

        final String fieldName = "type";

        assertOrTooComplex(fieldName, "Bug", "Task");
        assertAndTooComplex(fieldName, "Bug", "Task");

        assertAndWithHspFitsFilterForm("type = Bug", createFilterFormParam(fieldName, "1"));
        assertOrWithHspTooComplex("type = Bug");
        assertAndWithHspFitsFilterForm("type in (Bug, Task)", createFilterFormParam(fieldName, "1", "3"));
        assertAndWithHspFitsFilterForm("type in standardIssueTypes()", createFilterFormParam(fieldName, "-2"));
        assertAndWithHspFitsFilterForm("type in subTaskIssueTypes()", createFilterFormParam(fieldName, "-3"));

        assertAndWithHspFitsFilterForm("type = hsp_type", createFilterFormParam(fieldName, "6"));
        // TODO: test that mky_type is invalid
        assertAndWithHspFitsFilterForm("type = mky_type", createFilterFormParam(fieldName, "mky_type"));
        // TODO: test that mky_type is invalid
        assertAndWithHspFitsFilterForm("type in (mky_type, hsp_type)", createFilterFormParam(fieldName, "mky_type", "hsp_type"));
        assertFitsFilterForm("project in (mky, hsp) and type in (mky_type, hsp_type)", createFilterFormParam(fieldName, "6", "7"));
        assertFitsFilterForm("type in (mky_type, hsp_type)", createFilterFormParam(fieldName, "6", "7"));
    }

    public void testUpdated() throws Exception
    {
        final IssueNavigatorAssertions.FilterFormParam afterAbs = createFilterFormParam("updated:after", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeAbs = createFilterFormParam("updated:before", "11/May/09");
        final IssueNavigatorAssertions.FilterFormParam beforeRel = createFilterFormParam("updated:next", "1d");
        final IssueNavigatorAssertions.FilterFormParam afterRel = createFilterFormParam("updated:previous", "1d");

        // all good combinations
        assertFitsFilterForm("updated >= '2009-05-11' AND updated <= '2009-05-11'", afterAbs, beforeAbs);
        assertFitsFilterForm("updated >= '2009-05-11' AND updated <= '1d'", afterAbs, beforeRel);
        assertFitsFilterForm("updated >= '1d' AND updated <= '2009-05-11'", afterRel, beforeAbs);
        assertFitsFilterForm("updated >= '1d' AND updated <= '1d'", afterRel, beforeRel);
        assertFitsFilterForm("updated >= '2009-05-11' AND updated <= '2009-05-11' AND updated >= '1d'", afterAbs, beforeAbs, afterRel);
        assertFitsFilterForm("updated >= '2009-05-11' AND updated <= '2009-05-11' AND updated <= '1d'", afterAbs, beforeAbs, beforeRel);
        assertFitsFilterForm("updated >= '1d' AND updated <= '2009-05-11' AND updated >= '2009-05-11'", afterRel, beforeAbs, afterAbs);
        assertFitsFilterForm("updated >= '1d' AND updated <= '1d' AND updated <= '2009-05-11'", afterRel, beforeRel, beforeAbs);
        assertFitsFilterForm("updated >= '1d' AND updated <= '1d' AND updated >= '2009-05-11' AND updated <= '2009-05-11'", afterRel, beforeRel, afterAbs, beforeAbs);

        // can't combine with or
        assertTooComplex("updated >= '1d' OR updated <= '1d'");

        // can't combine same relational operator
        assertTooComplex("updated >= '1d' AND updated >= '2d'");
        assertTooComplex("updated <= '1d' AND updated <= '2d'");

        // cant use < or >
        assertTooComplex("updated > '1d' AND updated <= '1d' AND updated >= '2009-05-11' AND updated <= '2009-05-11'");

        // if one of the operands is wrong it wont fit
        assertTooComplex("updated >= 1234567890 AND updated <= '1d' AND updated >= '2009-05-11' AND updated <= '2009-05-11'");

        // fits even if split over multiple levels
        assertFitsFilterForm("(project = HSP AND updated <= '1d') AND (status = Open AND updated <= '2009-05-11')", createFilterFormParam("pid", "10000"), createFilterFormParam("status", "1"), beforeRel, beforeAbs);

        // with other clauses
        assertAndWithHspFitsFilterForm("updated >= '1d'", afterRel);
        assertOrWithHspTooComplex("updated >= '1d'");
    }

    public void testWorkRatio() throws Exception
    {
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        assertFitsFilterForm("workratio >= '10' AND workratio <= '20'", createFilterFormParam("workratio:min", "10"), createFilterFormParam("workratio:max", "20"));

        // fits when split over multiple levels
        assertFitsFilterForm("(project = HSP AND workratio >= '10') AND workratio <= '20'", createFilterFormParam("pid", "10000"), createFilterFormParam("workratio:min", "10"), createFilterFormParam("workratio:max", "20"));

        // can't combine with or
        assertTooComplex("workratio >= '10' OR workratio <= '10'");

        // can't combine same relational operator
        assertTooComplex("workratio >= '10' AND workratio >= '20'");
        assertTooComplex("workratio <= '10' AND workratio <= '20'");

        // cant use < or >
        assertTooComplex("workratio > '10' AND workratio <= '20'");

        // with other clauses
        assertAndWithHspFitsFilterForm("workratio >= '10'", createFilterFormParam("workratio:min", "10"));
        assertOrWithHspTooComplex("workratio >= '10'");
    }

    private void assertAndWithHspFitsFilterForm(final String jqlQuery, final IssueNavigatorAssertions.FilterFormParam... formParams)
    {
        IssueNavigatorAssertions.FilterFormParam[] params = new IssueNavigatorAssertions.FilterFormParam[formParams.length + 1];
        params[0] = createFilterFormParam("pid", "10000");
        System.arraycopy(formParams, 0, params, 1, formParams.length);
        assertFitsFilterForm(String.format("project = HSP AND %s", jqlQuery), params);
    }

    private void assertOrTooComplex(final String fieldName, final String operand1, final String operand2)
    {
        assertOrTooComplex(fieldName, "=", operand1, operand2);
    }

    private void assertOrTooComplex(final String fieldName, final String operator, final String operand1, final String operand2)
    {
        assertTooComplex(String.format("%1$s %2$s %3$s OR %1$s %2$s %4$s", fieldName, operator, operand1, operand2));
    }

    private void assertAndTooComplex(final String fieldName, final String operand1, final String operand2)
    {
        assertTooComplex(String.format("%1$s = %2$s AND %1$s = %3$s", fieldName, operand1, operand2));
    }

    private void assertAndTooComplex(final String fieldName, final String operator, final String operand1, final String operand2)
    {
        assertTooComplex(String.format("%1$s %2$s %3$s AND %1$s %2$s %4$s", fieldName, operator, operand1, operand2));
    }

    private void assertAndWithHspTooComplex(final String jqlQuery)
    {
        assertAndWithProjectTooComplex("HSP", jqlQuery);
    }

    private void assertOrWithHspTooComplex(final String jqlQuery)
    {
        assertOrWithProjectTooComplex("HSP", jqlQuery);
    }

    private void assertAndWithProjectTooComplex(final String project, final String jqlQuery)
    {
        assertTooComplex(String.format("project = %s AND %s", project, jqlQuery));
    }

    private void assertOrWithProjectTooComplex(final String project, final String jqlQuery)
    {
        assertTooComplex(String.format("project = %s OR %s", project, jqlQuery));
    }
}
