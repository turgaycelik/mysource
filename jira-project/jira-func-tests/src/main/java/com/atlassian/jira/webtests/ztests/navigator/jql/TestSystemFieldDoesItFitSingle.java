package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Testing for "fitness" in the filter form of singular system field clauses.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSystemFieldDoesItFitSingle extends AbstractJqlFuncTest
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
            administration.restoreData("TestSystemFieldDoesItFitSingle.xml");
        }
    }

    public void testAffectedVersion() throws Exception
    {
        final String fieldName = "affectedVersion";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10000", Result.tooComplex());
        matrix.put("!= 10000", Result.tooComplex());
        addComplexRelational(matrix, "10000");
        matrix.put("is EMPTY", new Result(createFilterFormParam("version", "-1")));
        matrix.put("is not EMPTY", Result.tooComplex());
        matrix.put("in (10000, 10001)", Result.tooComplex());
        matrix.put("not in (10000, 10001)", Result.tooComplex());
        matrix.put("in releasedVersions(HSP)", Result.tooComplex());
        matrix.put("not in releasedVersions(HSP)", Result.tooComplex());
        matrix.put("in unreleasedVersions(HSP)", Result.tooComplex());
        matrix.put("not in unreleasedVersions(HSP)", Result.tooComplex());
        matrix.put("= 10010 and project = HSP", Result.tooComplex());

        matrix.put("= \"New Version 1\"", new Result(createFilterFormParam("version", "New Version 1")));
        matrix.put("!= \"New Version 1\"", Result.tooComplex());
        matrix.put("in (\"New Version 1\", \"New Version 4\")", new Result(createFilterFormParam("version", "New Version 1"), createFilterFormParam("version", "New Version 4")));
        matrix.put("not in (\"New Version 1\", \"New Version 4\")", Result.tooComplex());

        //JRADEV-18728 - a version name should not fit in basic mode if all its associated versions are archived.
        matrix.put("= \"Archived Version\"", Result.tooComplex());
        matrix.put("in (\"New Version 1\", \"Archived Version\")", Result.tooComplex());

        //JRADEV-18728 - a version name should fit in basic mode if only some of its associated versions are archived.
        matrix.put("= \"Archived In HSP\"", new Result(createFilterFormParam("version", "Archived In HSP")));
        matrix.put("in (\"Archived In HSP\", \"New Version 1\")", new Result(createFilterFormParam("version", "Archived In HSP")));

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testAssignee() throws Exception
    {
        final String fieldName = "assignee";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= fred", new Result(createFilterFormParam("assignee", FRED_USERNAME), createFilterFormParam("assigneeSelect", "specificuser")));
        matrix.put("!= fred", Result.tooComplex());
        matrix.put("is EMPTY", new Result(createFilterFormParam("assigneeSelect", "unassigned")));
        matrix.put("is not EMPTY", Result.tooComplex());
        matrix.put("in (fred, admin)", new Result(createFilterFormParam("assignee", "fred", "admin")));
        matrix.put("not in (fred, admin)", Result.tooComplex());
        matrix.put("in membersOf('jira-users')", new Result(createFilterFormParam("assignee", "jira-users"), createFilterFormParam("assigneeSelect", "specificgroup")));
        matrix.put("= currentUser()", new Result(createFilterFormParam("assigneeSelect", "issue_current_user")));

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testCategory() throws Exception
    {
        final String fieldName = "category";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10000", Result.tooComplex());
        matrix.put("!= 10000", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (10000, 10001)", Result.tooComplex());
        matrix.put("not in (10000, 10001)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testComment() throws Exception
    {
        assertFitsFilterForm("comment ~ ccc", createFilterFormParam("comment", "ccc"));
        assertTooComplex("comment !~ ccc");
        // comments with list values wont fit because it is not valid
        // comments with empty wont fit because it is not valid
        // comments with relational operators wont fit because they are not valid
    }

    public void testComponent() throws Exception
    {
        final String fieldName = "component";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10000", Result.tooComplex());
        matrix.put("!= 10000", Result.tooComplex());
        matrix.put("in (10000, 10001)", Result.tooComplex());
        matrix.put("not in (10000, 10001)", Result.tooComplex());
        matrix.put("= \"New Component 1\"", new Result(createFilterFormParam("component", "New Component 1")));
        matrix.put("!= \"New Component 1\"", Result.tooComplex());
        matrix.put("in (\"New Component 1\", \"New Component 2\")", new Result(createFilterFormParam("component", "New Component 1"), createFilterFormParam("component", "New Component 2")));
        matrix.put("not in (\"New Component 1\", \"New Component 1\")", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testCreated() throws Exception
    {
        final String fieldName = "created";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= '2009-05-11'", Result.tooComplex());
        matrix.put("!= '2009-05-11'", Result.tooComplex());
        matrix.put("<= '2009-05-11'", new Result(createFilterFormParam("created:before", "11/May/09")));
        matrix.put(">= '2009-05-11'", new Result(createFilterFormParam("created:after", "11/May/09")));
        matrix.put("< '2009-05-11'", Result.tooComplex());
        matrix.put("> '2009-05-11'", Result.tooComplex());
        matrix.put("<= '1d'", new Result(createFilterFormParam("created:next", "1d")));
        matrix.put(">= '1d'", new Result(createFilterFormParam("created:previous", "1d")));
        matrix.put("< '1d'", Result.tooComplex());
        matrix.put("> '1d'", Result.tooComplex());
        matrix.put("in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("not in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("> now()", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testDescription() throws Exception
    {
        assertFitsFilterForm("description ~ ccc", createFilterFormParam("description", "ccc"));
        assertTooComplex("description !~ ccc");
        assertTooComplex("description is EMPTY");
        assertTooComplex("description is not EMPTY");
        // description with list values wont fit because it is not valid
        // description with relational operators wont fit because they are not valid
    }

    public void testDueDate() throws Exception
    {
        final String fieldName = "due";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= '2009-05-11'", Result.tooComplex());
        matrix.put("!= '2009-05-11'", Result.tooComplex());
        matrix.put("<= '2009-05-11'", new Result(createFilterFormParam("duedate:before", "11/May/09")));
        matrix.put(">= '2009-05-11'", new Result(createFilterFormParam("duedate:after", "11/May/09")));
        matrix.put("<= '1d'", new Result(createFilterFormParam("duedate:next", "1d")));
        matrix.put(">= '1d'", new Result(createFilterFormParam("duedate:previous", "1d")));
        matrix.put("in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("not in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put(">= now()", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testEnvironment() throws Exception
    {
        assertFitsFilterForm("environment ~ ccc", createFilterFormParam("environment", "ccc"));
        assertTooComplex("environment !~ ccc");
        assertTooComplex("environment is EMPTY");
        assertTooComplex("environment is not EMPTY");
        // environment with list values wont fit because it is not valid
        // environment with relational operators wont fit because they are not valid
    }

    public void testFixVersion() throws Exception
    {
        final String fieldName = "fixVersion";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10000", Result.tooComplex());
        matrix.put("!= 10000", Result.tooComplex());
        addComplexRelational(matrix, "10000");
        matrix.put("is EMPTY", new Result(createFilterFormParam("fixfor", "-1")));
        matrix.put("is not EMPTY", Result.tooComplex());
        matrix.put("in (10000, 10001)", Result.tooComplex());
        matrix.put("not in (10000, 10001)", Result.tooComplex());
        matrix.put("in releasedVersions(HSP)", Result.tooComplex());
        matrix.put("not in releasedVersions(HSP)", Result.tooComplex());
        matrix.put("in unreleasedVersions(HSP)", Result.tooComplex());
        matrix.put("not in unreleasedVersions(HSP)", Result.tooComplex());
        matrix.put("= 10010 and project = HSP", Result.tooComplex());

        matrix.put("= \"New Version 1\"", new Result(createFilterFormParam("fixfor", "New Version 1")));
        matrix.put("!= \"New Version 1\"", Result.tooComplex());
        matrix.put("in (\"New Version 1\", \"New Version 4\")", new Result(createFilterFormParam("fixfor", "New Version 1"), createFilterFormParam("fixfor", "New Version 4")));
        matrix.put("not in (\"New Version 1\", \"New Version 4\")", Result.tooComplex());

        //JRADEV-18728 - a version name should not fit in basic mode if all its associated versions are archived.
        matrix.put("= \"Archived Version\"", Result.tooComplex());
        matrix.put("in (\"New Version 1\", \"Archived Version\")", Result.tooComplex());

        //JRADEV-18728 - a version name should fit in basic mode if only some of its associated versions are archived.
        matrix.put("= \"Archived In HSP\"", new Result(createFilterFormParam("version", "Archived In HSP")));
        matrix.put("in (\"Archived In HSP\", \"New Version 1\")", new Result(createFilterFormParam("version", "Archived In HSP")));

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testIssue() throws Exception
    {
        final String fieldName = "issue";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 'HSP-1'", Result.tooComplex());
        matrix.put("!= 'HSP-1'", Result.tooComplex());
        addComplexRelational(matrix, "'HSP-1'");
        addComplexEmpty(matrix);
        matrix.put("in ('HSP-1', 'HSP-2')", Result.tooComplex());
        matrix.put("not in ('HSP-1', 'HSP-2')", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testLevel() throws Exception
    {
        final String fieldName = "level";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10000", Result.tooComplex());
        matrix.put("!= 10000", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (10000, 10001)", Result.tooComplex());
        matrix.put("not in (10000, 10001)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testTimeTracking() throws Exception
    {
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        String fieldName = "originalEstimate";
        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10", Result.tooComplex());
        matrix.put("!= 10", Result.tooComplex());
        addComplexRelational(matrix, "10");
        addComplexEmpty(matrix);
        matrix.put("in (10, 20)", Result.tooComplex());
        matrix.put("not in (10, 20)", Result.tooComplex());
        _testFitnessMatrix(fieldName, matrix);

        fieldName = "remainingEstimate";
        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10", Result.tooComplex());
        matrix.put("!= 10", Result.tooComplex());
        addComplexRelational(matrix, "10");
        addComplexEmpty(matrix);
        matrix.put("in (10, 20)", Result.tooComplex());
        matrix.put("not in (10, 20)", Result.tooComplex());
        _testFitnessMatrix(fieldName, matrix);

        fieldName = "timeSpent";
        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10", Result.tooComplex());
        matrix.put("!= 10", Result.tooComplex());
        addComplexRelational(matrix, "10");
        addComplexEmpty(matrix);
        matrix.put("in (10, 20)", Result.tooComplex());
        matrix.put("not in (10, 20)", Result.tooComplex());
        _testFitnessMatrix(fieldName, matrix);

        fieldName = "workRatio";
        matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10", Result.tooComplex());
        matrix.put("!= 10", Result.tooComplex());
        matrix.put("< 10", Result.tooComplex());
        matrix.put("<= 10", new Result(createFilterFormParam("workratio:max", "10")));
        matrix.put("> 10", Result.tooComplex());
        matrix.put(">= 10", new Result(createFilterFormParam("workratio:min", "10")));
        addComplexEmpty(matrix);
        matrix.put("in (10, 20)", Result.tooComplex());
        matrix.put("not in (10, 20)", Result.tooComplex());
        _testFitnessMatrix(fieldName, matrix);
    }

    public void testParent() throws Exception
    {
        final String fieldName = "parent";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 'HSP-1'", Result.tooComplex());
        matrix.put("!= 'HSP-1'", Result.tooComplex());
        matrix.put("in ('HSP-1', 'HSP-2')", Result.tooComplex());
        matrix.put("not in ('HSP-1', 'HSP-2')", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testPriority() throws Exception
    {
        final String fieldName = "priority";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= Major", new Result(createFilterFormParam(fieldName, "3")));
        matrix.put("!= Major", Result.tooComplex());
        matrix.put("< Major", Result.tooComplex());
        matrix.put("<= Major", Result.tooComplex());
        matrix.put("> Major", Result.tooComplex());
        matrix.put(">= Major", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (Major, Critical)", new Result(createFilterFormParam(fieldName, "2", "3")));
        matrix.put("not in (Major, Critical)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testProject() throws Exception
    {
        final String fieldName = "project";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= HSP", new Result(createFilterFormParam("pid", "10000")));
        matrix.put("!= HSP", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (HSP, MKY)", new Result(createFilterFormParam("pid", "10000", "10001")));
        matrix.put("not in (HSP, MKY)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testReporter() throws Exception
    {
        final String fieldName = "reporter";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= fred", new Result(createFilterFormParam("reporter", FRED_USERNAME), createFilterFormParam("reporterSelect", "specificuser")));
        matrix.put("!= fred", Result.tooComplex());
        matrix.put("is EMPTY", new Result(createFilterFormParam("reporterSelect", "issue_no_reporter")));
        matrix.put("is not EMPTY", Result.tooComplex());
        matrix.put("in (fred, admin)", new Result(createFilterFormParam("reporter", "fred", "admin")));
        matrix.put("not in (fred, admin)", Result.tooComplex());
        matrix.put("in membersOf('jira-users')", new Result(createFilterFormParam("reporter", "jira-users"), createFilterFormParam("reporterSelect", "specificgroup")));
        matrix.put("= currentUser()", new Result(createFilterFormParam("reporterSelect", "issue_current_user")));

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testResolution() throws Exception
    {
        final String fieldName = "resolution";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= Fixed", new Result(createFilterFormParam(fieldName, "1")));
        matrix.put("!= Fixed", Result.tooComplex());
        addComplexRelational(matrix, "Fixed");
        matrix.put("= unresolved", new Result(createFilterFormParam(fieldName, "-1")));
        matrix.put("is EMPTY", Result.tooComplex());
        matrix.put("is not EMPTY", Result.tooComplex());
        matrix.put("in (unresolved, Fixed, Duplicate)", new Result(createFilterFormParam(fieldName, "-1", "1", "3")));
        matrix.put("not in (Fixed, Duplicate)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testResolutionDate() throws Exception
    {
        final String fieldName = "resolved";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= '2009-05-11'", Result.tooComplex());
        matrix.put("!= '2009-05-11'", Result.tooComplex());
        matrix.put("<= '2009-05-11'", new Result(createFilterFormParam("resolutiondate:before", "11/May/09")));
        matrix.put(">= '2009-05-11'", new Result(createFilterFormParam("resolutiondate:after", "11/May/09")));
        matrix.put("< '2009-05-11'", Result.tooComplex());
        matrix.put("> '2009-05-11'", Result.tooComplex());
        matrix.put("<= '1d'", new Result(createFilterFormParam("resolutiondate:next", "1d")));
        matrix.put(">= '1d'", new Result(createFilterFormParam("resolutiondate:previous", "1d")));
        matrix.put("< '1d'", Result.tooComplex());
        matrix.put("> '1d'", Result.tooComplex());
        matrix.put("in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("not in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("= now()", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testSavedFilter() throws Exception
    {
        final String fieldName = "savedFilter";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10000", Result.tooComplex());
        matrix.put("!= 10000", Result.tooComplex());
        matrix.put("in (10000, 10001)", Result.tooComplex());
        matrix.put("not in (10000, 10001)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testStatus() throws Exception
    {
        final String fieldName = "status";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= Open", new Result(createFilterFormParam(fieldName, "1")));
        matrix.put("!= Open", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (Open, 'In Progress')", new Result(createFilterFormParam(fieldName, "1", "3")));
        matrix.put("not in (Open, 'In Progress')", Result.tooComplex());
        matrix.put("= mky_status", new Result(createFilterFormParam(fieldName, "10001")));
        matrix.put("= mky_bug_status", new Result(createFilterFormParam(fieldName, "10002")));

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testSummary() throws Exception
    {
        assertFitsFilterForm("summary ~ ccc", createFilterFormParam("summary", "ccc"));
        assertTooComplex("summary !~ ccc");
        assertTooComplex("summary is EMPTY");
        assertTooComplex("summary is not EMPTY");
        // environment with list values wont fit because it is not valid
        // environment with relational operators wont fit because they are not valid
    }

    public void testText() throws Exception
    {
        assertFitsFilterForm("text ~ ccc", createFilterFormParam("text", "ccc"));
        assertTooComplex("text !~ ccc");
    }

    public void testType() throws Exception
    {
        administration.activateSubTasks();

        final String fieldName = "type";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= Bug", new Result(createFilterFormParam(fieldName, "1")));
        matrix.put("!= Bug", Result.tooComplex());
        addComplexEmpty(matrix);
        matrix.put("in (Bug, Task)", new Result(createFilterFormParam(fieldName, "1", "3")));
        matrix.put("not in (Bug, Task)", Result.tooComplex());
        matrix.put("in standardIssueTypes()", new Result(createFilterFormParam(fieldName, "-2")));
        matrix.put("not in standardIssueTypes()", Result.tooComplex());
        matrix.put("in subTaskIssueTypes()", new Result(createFilterFormParam(fieldName, "-3")));
        matrix.put("not in subTaskIssueTypes()", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testUpdated() throws Exception
    {
        final String fieldName = "updated";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= '2009-05-11'", Result.tooComplex());
        matrix.put("!= '2009-05-11'", Result.tooComplex());
        matrix.put("<= '2009-05-11'", new Result(createFilterFormParam("updated:before", "11/May/09")));
        matrix.put(">= '2009-05-11'", new Result(createFilterFormParam("updated:after", "11/May/09")));
        matrix.put("< '2009-05-11'", Result.tooComplex());
        matrix.put("> '2009-05-11'", Result.tooComplex());
        matrix.put("<= '1d'", new Result(createFilterFormParam("updated:next", "1d")));
        matrix.put(">= '1d'", new Result(createFilterFormParam("updated:previous", "1d")));
        matrix.put("< '1d'", Result.tooComplex());
        matrix.put("> '1d'", Result.tooComplex());
        matrix.put("in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("not in ('2009-05-11', '2009-05-12')", Result.tooComplex());
        matrix.put("> now()", Result.tooComplex());
        addComplexEmpty(matrix);

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testVotes() throws Exception
    {
        administration.generalConfiguration().enableVoting();
        
        final String fieldName = "votes";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("= 10000", Result.tooComplex());
        matrix.put("!= 10000", Result.tooComplex());
        addComplexRelational(matrix, "10000");
        matrix.put("in (10000, 10001)", Result.tooComplex());
        matrix.put("not in (10000, 10001)", Result.tooComplex());

        _testFitnessMatrix(fieldName, matrix);
    }

    public void testVersionsAndComponents() throws Exception
    {
        final String fieldName = "project";
        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();

        matrix.put(" = HSP AND fixVersion in unreleasedVersions()", new Result(createFilterFormParam("pid", "10000"),createFilterFormParam("fixfor", "-2")));
        matrix.put(" = HSP AND affectedVersion in unreleasedVersions()", new Result(createFilterFormParam("pid", "10000"),createFilterFormParam("version", "-2")));
        _testFitnessMatrix(fieldName, matrix);

    }
    

    private void addComplexRelational(final Map<String, Result> matrix, final String lit)
    {
        matrix.put("< " + lit, Result.tooComplex());
        matrix.put("<= " + lit, Result.tooComplex());
        matrix.put("> " + lit, Result.tooComplex());
        matrix.put(">= " + lit, Result.tooComplex());
    }

    private void addComplexEmpty(final Map<String, Result> matrix)
    {
        matrix.put("is EMPTY", Result.tooComplex());
        matrix.put("is not EMPTY", Result.tooComplex());
    }

    private void _testFitnessMatrix(final String fieldName, final Map<String, Result> matrix)
    {
        for (Map.Entry<String, Result> entry : matrix.entrySet())
        {
            final String jqlQuery = String.format("%s %s", fieldName, entry.getKey());
            Result result = entry.getValue();
            if (result.isTooComplex)
            {
                assertTooComplex(jqlQuery);
            }
            else
            {
                assertFitsFilterForm(jqlQuery, result.expectedFormData);
            }
        }
    }

    private static class Result
    {
        private IssueNavigatorAssertions.FilterFormParam[] expectedFormData;
        private boolean isTooComplex;

        private static Result tooComplex()
        {
            final Result result = new Result();
            result.isTooComplex = true;
            return result;
        }

        private Result()
        {
            this.expectedFormData = null;
            this.isTooComplex = true;
        }

        private Result(final IssueNavigatorAssertions.FilterFormParam... expectedFormData)
        {
            this.expectedFormData = expectedFormData;
            this.isTooComplex = false;
        }
    }
}
