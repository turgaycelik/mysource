package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests if JQL searches do not bring back data when fields have been hidden or custom fields have been moved out of
 * configuration.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSearchConstrainedByConfiguration extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSearchConstrainedByConfiguration.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testHideFieldEmptyQueries() throws Exception
    {
        // priority
        assertSearchWithResults(String.format("'%s' is EMPTY", "priority"));
        assertSearchWithResults(String.format("'%s' = EMPTY", "priority"));
        assertSearchWithResults(String.format("'%s' in (EMPTY)", "priority"));
        assertSearchWithResults(String.format("'%s' is not EMPTY", "priority"), "MKY-2", "MKY-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' != EMPTY", "priority"), "MKY-2", "MKY-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' not in (EMPTY)", "priority"), "MKY-2", "MKY-1", "HSP-2", "HSP-1");

        administration.fieldConfigurations().fieldConfiguration("Invisible Configuration").showFields("Priority");
        administration.reIndex();

        assertSearchWithResults(String.format("'%s' is EMPTY", "priority"));
        assertSearchWithResults(String.format("'%s' = EMPTY", "priority"));
        assertSearchWithResults(String.format("'%s' in (EMPTY)", "priority"));
        assertSearchWithResults(String.format("'%s' is not EMPTY", "priority"), "MKY-2", "MKY-1", "INI-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' != EMPTY", "priority"), "MKY-2", "MKY-1", "INI-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' not in (EMPTY)", "priority"), "MKY-2", "MKY-1", "INI-1", "HSP-2", "HSP-1");

        // reporter
        assertSearchWithResults(String.format("'%s' is EMPTY", "reporter"));
        assertSearchWithResults(String.format("'%s' = EMPTY", "reporter"));
        assertSearchWithResults(String.format("'%s' in (EMPTY)", "reporter"));
        assertSearchWithResults(String.format("'%s' is not EMPTY", "reporter"), "MKY-2", "MKY-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' != EMPTY", "reporter"), "MKY-2", "MKY-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' not in (EMPTY)", "reporter"), "MKY-2", "MKY-1", "HSP-2", "HSP-1");

        administration.fieldConfigurations().fieldConfiguration("Invisible Configuration").showFields("Reporter");
        administration.reIndex();

        assertSearchWithResults(String.format("'%s' is EMPTY", "reporter"));
        assertSearchWithResults(String.format("'%s' = EMPTY", "reporter"));
        assertSearchWithResults(String.format("'%s' in (EMPTY)", "reporter"));
        assertSearchWithResults(String.format("'%s' is not EMPTY", "reporter"), "MKY-2", "MKY-1", "INI-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' != EMPTY", "reporter"), "MKY-2", "MKY-1", "INI-1", "HSP-2", "HSP-1");
        assertSearchWithResults(String.format("'%s' not in (EMPTY)", "reporter"), "MKY-2", "MKY-1", "INI-1", "HSP-2", "HSP-1");

        _testHideFieldEmptyQueries("affectedVersion", "Affects Version/s", true, true);
        _testHideFieldEmptyQueries("assignee", "Assignee", false, true);
        _testHideFieldEmptyQueries("Cascading Select CF", true, true);
        _testHideFieldEmptyQueries("component", "Component/s", true, true);
        _testHideFieldEmptyQueries("description", "Description", true, false);
        _testHideFieldEmptyQueries("duedate", "Due Date", true, true);
        _testHideFieldEmptyQueries("environment", "Environment", true, false);
        _testHideFieldEmptyQueries("fixVersion", "Fix Version/s", true, true);
        _testHideFieldEmptyQueries("Free Text Field CF", true, false);
        _testHideFieldEmptyQueries("Group Picker CF", true, true);
        _testHideFieldEmptyQueries("Multi Checkboxes CF", true, true);
        _testHideFieldEmptyQueries("Multi Group Picker CF", true, true);
        _testHideFieldEmptyQueries("Multi Select CF", true, true);
        _testHideFieldEmptyQueries("Multi User Picker CF", true, true);
        _testHideFieldEmptyQueries("Number Field CF", true, true);
        _testHideFieldEmptyQueries("Project Picker CF", true, true);
        _testHideFieldEmptyQueries("Radio Buttons CF", true, true);
        _testHideFieldEmptyQueries("resolution", "Resolution", true, true);
        _testHideFieldEmptyQueries("Select List CF", true, true);
        _testHideFieldEmptyQueries("Single Version Picker CF", true, true);
        _testHideFieldEmptyQueries("Text Field 255", true, false);
        _testHideFieldEmptyQueries("URL Field CF", true, true);
        _testHideFieldEmptyQueries("User Picker CF", true, true);
        _testHideFieldEmptyQueries("Version Picker CF", true, true);

        // note: we do not test Security Levels because their indexer has not been modified to preserve functionality
    }

    private void _testHideFieldEmptyQueries(final String fieldClauseName, final boolean invisibleInEmpty, final boolean equalsAndListsSupported)
    {
        _testHideFieldEmptyQueries(fieldClauseName, fieldClauseName, invisibleInEmpty, equalsAndListsSupported);
    }

    private void _testHideFieldEmptyQueries(final String fieldClauseName, final String fieldConfigName, final boolean invisibleInEmpty, final boolean equalsAndListsSupported)
    {
        assertSearchWithResults(String.format("'%s' is EMPTY", fieldClauseName), "MKY-2", "HSP-2");
        if (equalsAndListsSupported)
        {
            assertSearchWithResults(String.format("'%s' = EMPTY", fieldClauseName), "MKY-2", "HSP-2");
            assertSearchWithResults(String.format("'%s' in (EMPTY)", fieldClauseName), "MKY-2", "HSP-2");
        }

        assertSearchWithResults(String.format("'%s' is not EMPTY", fieldClauseName), "MKY-1", "HSP-1");
        if (equalsAndListsSupported)
        {
            assertSearchWithResults(String.format("'%s' != EMPTY", fieldClauseName), "MKY-1", "HSP-1");
            assertSearchWithResults(String.format("'%s' not in (EMPTY)", fieldClauseName), "MKY-1", "HSP-1");
        }

        administration.fieldConfigurations().fieldConfiguration("Invisible Configuration").showFields(fieldConfigName);
        administration.reIndex();

        final String[] emptyIssues = invisibleInEmpty ? new String []{"MKY-2", "INI-1", "HSP-2"} : new String []{"MKY-2", "HSP-2"};
        final String[] nonEmptyIssues = !invisibleInEmpty ? new String []{"MKY-1", "INI-1", "HSP-1"} : new String []{"MKY-1", "HSP-1"};

        assertSearchWithResults(String.format("'%s' is EMPTY", fieldClauseName), emptyIssues);
        if (equalsAndListsSupported)
        {
            assertSearchWithResults(String.format("'%s' = EMPTY", fieldClauseName), emptyIssues);
            assertSearchWithResults(String.format("'%s' in (EMPTY)", fieldClauseName), emptyIssues);
        }

        assertSearchWithResults(String.format("'%s' is not EMPTY", fieldClauseName), nonEmptyIssues);
        if (equalsAndListsSupported)
        {
            assertSearchWithResults(String.format("'%s' != EMPTY", fieldClauseName), nonEmptyIssues);
            assertSearchWithResults(String.format("'%s' not in (EMPTY)", fieldClauseName), nonEmptyIssues);
        }
    }

    public void testHideFields() throws Exception
    {
        _runFilterAndVerifySearchNoRestrictions(10000);
        _testHideField("affectedVersion", "=", "'New Version 1'", "Affects Version/s");
        _runFilterAndVerifySearchRestricted(10000);

        _runFilterAndVerifySearchNoRestrictions(10001);
        _testHideField("assignee", "=", ADMIN_USERNAME, "Assignee");
        _runFilterAndVerifySearchRestricted(10001);

        _runFilterAndVerifySearchNoRestrictions(10002);
        _testHideField("Cascading Select CF", "in", "cascadeOption(\"Parent Option 1\", \"Child Option 1\")");
        _runFilterAndVerifySearchRestricted(10002);

        _runFilterAndVerifySearchNoRestrictions(10003);
        _testHideField("component", "=", "\"New Component 1\"", "Component/s");
        _runFilterAndVerifySearchRestricted(10003);

        _runFilterAndVerifySearchNoRestrictions(10004);
        _testHideField("description", "~", "'same description'", "Description");
        _runFilterAndVerifySearchRestricted(10004);

        _runFilterAndVerifySearchNoRestrictions(10005);
        _testHideField("duedate", "=", "\"2009/07/06\"", "Due Date");
        _runFilterAndVerifySearchRestricted(10005);

        _runFilterAndVerifySearchNoRestrictions(10006);
        _testHideField("environment", "~", "'same environment'", "Environment");
        _runFilterAndVerifySearchRestricted(10006);

        _runFilterAndVerifySearchNoRestrictions(10007);
        _testHideField("fixVersion", "=", "'New Version 4'", "Fix Version/s");
        _runFilterAndVerifySearchRestricted(10007);

        _runFilterAndVerifySearchNoRestrictions(10008);
        _testHideField("Free Text Field CF", "~", "'free text'");
        _runFilterAndVerifySearchRestricted(10008);

        _runFilterAndVerifySearchNoRestrictions(10009);
        _testHideField("Group Picker CF", "=", "'jira-administrators'");
        _runFilterAndVerifySearchRestricted(10009);

        _runFilterAndVerifySearchNoRestrictions(10010);
        _testHideField("Multi Checkboxes CF", "=", "'check 1'");
        _runFilterAndVerifySearchRestricted(10010);

        _runFilterAndVerifySearchNoRestrictions(10011);
        _testHideField("Multi Group Picker CF", "=", "'jira-administrators'");
        _runFilterAndVerifySearchRestricted(10011);

        _runFilterAndVerifySearchNoRestrictions(10012);
        _testHideField("Multi Select CF", "=", "'opt 1'");
        _runFilterAndVerifySearchRestricted(10012);

        _runFilterAndVerifySearchNoRestrictions(10013);
        _testHideField("Multi User Picker CF", "=", ADMIN_USERNAME);
        _runFilterAndVerifySearchRestricted(10013);

        _runFilterAndVerifySearchNoRestrictions(10014);
        _testHideField("Number Field CF", "=", "1");
        _runFilterAndVerifySearchRestricted(10014);

        _runFilterAndVerifySearchNoRestrictions(10015);
        _testHideField("Project Picker CF", "=", "Invisible");
        _runFilterAndVerifySearchRestricted(10015);

        _runFilterAndVerifySearchNoRestrictions(10016);
        _testHideField("Radio Buttons CF", "=", "'Radio Option 1'");
        _runFilterAndVerifySearchRestricted(10016);

        _runFilterAndVerifySearchNoRestrictions(10017);
        _testHideField("resolution", "=", "Fixed", "Resolution");
        _runFilterAndVerifySearchRestricted(10017);

        _runFilterAndVerifySearchNoRestrictions(10018);
        _testHideField("Select List CF", "=", "\"option 1\"");
        _runFilterAndVerifySearchRestricted(10018);

        _runFilterAndVerifySearchNoRestrictions(10019);
        _testHideField("Single Version Picker CF", "=", "'New Version 1'");
        _runFilterAndVerifySearchRestricted(10019);

        _runFilterAndVerifySearchNoRestrictions(10020);
        _testHideField("Text Field 255", "~", "'text 255'");
        _runFilterAndVerifySearchRestricted(10020);

        _runFilterAndVerifySearchNoRestrictions(10021);
        _testHideField("URL Field CF", "=", "'http://google.com'");
        _runFilterAndVerifySearchRestricted(10021);

        _runFilterAndVerifySearchNoRestrictions(10022);
        _testHideField("User Picker CF", "=", ADMIN_USERNAME);
        _runFilterAndVerifySearchRestricted(10022);

        _runFilterAndVerifySearchNoRestrictions(10023);
        _testHideField("Version Picker CF", "=", "'New Version 4'");
        _runFilterAndVerifySearchRestricted(10023);
    }

    public void testNoBrowseProjectPermission() throws Exception
    {
        _runFilterAndVerifySearchNoRestrictions(10000);
        _runFilterAndVerifySearchNoRestrictions(10001);
        _runFilterAndVerifySearchNoRestrictions(10002);
        _runFilterAndVerifySearchNoRestrictions(10003);
        _runFilterAndVerifySearchNoRestrictions(10004);
        _runFilterAndVerifySearchNoRestrictions(10005);
        _runFilterAndVerifySearchNoRestrictions(10006);
        _runFilterAndVerifySearchNoRestrictions(10007);
        _runFilterAndVerifySearchNoRestrictions(10008);
        _runFilterAndVerifySearchNoRestrictions(10009);
        _runFilterAndVerifySearchNoRestrictions(10010);
        _runFilterAndVerifySearchNoRestrictions(10011);
        _runFilterAndVerifySearchNoRestrictions(10012);
        _runFilterAndVerifySearchNoRestrictions(10013);
        _runFilterAndVerifySearchNoRestrictions(10014);
        _runFilterAndVerifySearchNoRestrictions(10015);
        _runFilterAndVerifySearchNoRestrictions(10016);
        _runFilterAndVerifySearchNoRestrictions(10017);
        _runFilterAndVerifySearchNoRestrictions(10018);
        _runFilterAndVerifySearchNoRestrictions(10019);
        _runFilterAndVerifySearchNoRestrictions(10020);
        _runFilterAndVerifySearchNoRestrictions(10021);
        _runFilterAndVerifySearchNoRestrictions(10022);
        _runFilterAndVerifySearchNoRestrictions(10023);

        _runAndVerifySearchForFieldNoRestrictions("affectedVersion", "=", "'New Version 1'");
        _runAndVerifySearchForFieldNoRestrictions("assignee", "=", ADMIN_USERNAME);
        _runAndVerifySearchForFieldNoRestrictions("Cascading Select CF", "in", "cascadeOption(\"Parent Option 1\", \"Child Option 1\")");
        _runAndVerifySearchForFieldNoRestrictions("component", "=", "\"New Component 1\"");
        _runAndVerifySearchForFieldNoRestrictions("description", "~", "'same description'");
        _runAndVerifySearchForFieldNoRestrictions("duedate", "=", "\"2009/07/06\"");
        _runAndVerifySearchForFieldNoRestrictions("environment", "~", "'same environment'");
        _runAndVerifySearchForFieldNoRestrictions("fixVersion", "=", "'New Version 4'");
        _runAndVerifySearchForFieldNoRestrictions("Free Text Field CF", "~", "'free text'");
        _runAndVerifySearchForFieldNoRestrictions("Group Picker CF", "=", "'jira-administrators'");
        _runAndVerifySearchForFieldNoRestrictions("Multi Checkboxes CF", "=", "'check 1'");
        _runAndVerifySearchForFieldNoRestrictions("Multi Group Picker CF", "=", "'jira-administrators'");
        _runAndVerifySearchForFieldNoRestrictions("Multi Select CF", "=", "'opt 1'");
        _runAndVerifySearchForFieldNoRestrictions("Multi User Picker CF", "=", ADMIN_USERNAME);
        _runAndVerifySearchForFieldNoRestrictions("Number Field CF", "=", "1");
        _runAndVerifySearchForFieldNoRestrictions("Project Picker CF", "=", "Invisible");
        _runAndVerifySearchForFieldNoRestrictions("Radio Buttons CF", "=", "'Radio Option 1'");
        _runAndVerifySearchForFieldNoRestrictions("resolution", "=", "Fixed");
        _runAndVerifySearchForFieldNoRestrictions("Select List CF", "=", "\"option 1\"");
        _runAndVerifySearchForFieldNoRestrictions("Single Version Picker CF", "=", "'New Version 1'");
        _runAndVerifySearchForFieldNoRestrictions("Text Field 255", "~", "'text 255'");
        _runAndVerifySearchForFieldNoRestrictions("URL Field CF", "=", "'http://google.com'");
        _runAndVerifySearchForFieldNoRestrictions("User Picker CF", "=", ADMIN_USERNAME);
        _runAndVerifySearchForFieldNoRestrictions("Version Picker CF", "=", "'New Version 4'");
        assertSearchWithResults("priority = \"Major\"","MKY-2", "MKY-1", "HSP-2", "HSP-1");
        assertSearchWithResults("reporter = \"admin\"","MKY-2", "MKY-1", "HSP-2", "HSP-1");

        // Turn off the browse project permission for Monkey
        administration.permissionSchemes().scheme("Custom Permission Scheme").removePermission(10, "jira-users");

        _runFilterAndVerifySearchRestricted(10000);
        _runFilterAndVerifySearchRestricted(10001);
        _runFilterAndVerifySearchRestricted(10002);
        _runFilterAndVerifySearchRestricted(10003);
        _runFilterAndVerifySearchRestricted(10004);
        _runFilterAndVerifySearchRestricted(10005);
        _runFilterAndVerifySearchRestricted(10006);
        _runFilterAndVerifySearchRestricted(10007);
        _runFilterAndVerifySearchRestricted(10008);
        _runFilterAndVerifySearchRestricted(10009);
        _runFilterAndVerifySearchRestricted(10010);
        _runFilterAndVerifySearchRestricted(10011);
        _runFilterAndVerifySearchRestricted(10012);
        _runFilterAndVerifySearchRestricted(10013);
        _runFilterAndVerifySearchRestricted(10014);
        _runFilterAndVerifySearchRestricted(10015);
        _runFilterAndVerifySearchRestricted(10016);
        _runFilterAndVerifySearchRestricted(10017);
        _runFilterAndVerifySearchRestricted(10018);
        _runFilterAndVerifySearchRestricted(10019);
        _runFilterAndVerifySearchRestricted(10020);
        _runFilterAndVerifySearchRestricted(10021);
        _runFilterAndVerifySearchRestricted(10022);
        _runFilterAndVerifySearchRestricted(10023);

        _runAndVerifySearchForFieldRestricted("affectedVersion", "=", "'New Version 1'");
        _runAndVerifySearchForFieldRestricted("assignee", "=", ADMIN_USERNAME);
        _runAndVerifySearchForFieldRestricted("Cascading Select CF", "in", "cascadeOption(\"Parent Option 1\", \"Child Option 1\")");
        _runAndVerifySearchForFieldRestricted("component", "=", "\"New Component 1\"");
        _runAndVerifySearchForFieldRestricted("description", "~", "'same description'");
        _runAndVerifySearchForFieldRestricted("duedate", "=", "\"2009/07/06\"");
        _runAndVerifySearchForFieldRestricted("environment", "~", "'same environment'");
        _runAndVerifySearchForFieldRestricted("fixVersion", "=", "'New Version 4'");
        _runAndVerifySearchForFieldRestricted("Free Text Field CF", "~", "'free text'");
        _runAndVerifySearchForFieldRestricted("Group Picker CF", "=", "'jira-administrators'");
        _runAndVerifySearchForFieldRestricted("Multi Checkboxes CF", "=", "'check 1'");
        _runAndVerifySearchForFieldRestricted("Multi Group Picker CF", "=", "'jira-administrators'");
        _runAndVerifySearchForFieldRestricted("Multi Select CF", "=", "'opt 1'");
        _runAndVerifySearchForFieldRestricted("Multi User Picker CF", "=", ADMIN_USERNAME);
        _runAndVerifySearchForFieldRestricted("Number Field CF", "=", "1");
        _runAndVerifySearchForFieldRestricted("Project Picker CF", "=", "Invisible");
        _runAndVerifySearchForFieldRestricted("Radio Buttons CF", "=", "'Radio Option 1'");
        _runAndVerifySearchForFieldRestricted("resolution", "=", "Fixed");
        _runAndVerifySearchForFieldRestricted("Select List CF", "=", "\"option 1\"");
        _runAndVerifySearchForFieldRestricted("Single Version Picker CF", "=", "'New Version 1'");
        _runAndVerifySearchForFieldRestricted("Text Field 255", "~", "'text 255'");
        _runAndVerifySearchForFieldRestricted("URL Field CF", "=", "'http://google.com'");
        _runAndVerifySearchForFieldRestricted("User Picker CF", "=", ADMIN_USERNAME);
        _runAndVerifySearchForFieldRestricted("Version Picker CF", "=", "'New Version 4'");

        assertSearchWithResults("priority = \"Major\"", "HSP-2", "HSP-1");
        assertSearchWithResults("reporter = \"admin\"", "HSP-2", "HSP-1");

        administration.permissionSchemes().scheme("Default Permission Scheme").removePermission(10, "jira-users");

        _rundAndVerifyFieldDoesntExist("affectedVersion", "=", "'New Version 1'");
        _rundAndVerifyFieldDoesntExist("assignee", "=", ADMIN_USERNAME);
        _rundAndVerifyFieldDoesntExist("Cascading Select CF", "in", "(\"Parent Option 1\", \"Child Option 1\")");
        _rundAndVerifyFieldDoesntExist("component", "=", "\"New Component 1\"");
        _rundAndVerifyFieldDoesntExist("description", "~", "'same description'");
        _rundAndVerifyFieldDoesntExist("duedate", "=", "\"2009/07/06\"");
        _rundAndVerifyFieldDoesntExist("environment", "~", "'same environment'");
        _rundAndVerifyFieldDoesntExist("fixVersion", "=", "'New Version 4'");
        _rundAndVerifyFieldDoesntExist("Free Text Field CF", "~", "'free text'");
        _rundAndVerifyFieldDoesntExist("Group Picker CF", "=", "'jira-administrators'");
        _rundAndVerifyFieldDoesntExist("Multi Checkboxes CF", "=", "'check 1'");
        _rundAndVerifyFieldDoesntExist("Multi Group Picker CF", "=", "'jira-administrators'");
        _rundAndVerifyFieldDoesntExist("Multi Select CF", "=", "'opt 1'");
        _rundAndVerifyFieldDoesntExist("Multi User Picker CF", "=", ADMIN_USERNAME);
        _rundAndVerifyFieldDoesntExist("Number Field CF", "=", "1");
        _rundAndVerifyFieldDoesntExist("Project Picker CF", "=", "Invisible");
        _rundAndVerifyFieldDoesntExist("Radio Buttons CF", "=", "'Radio Option 1'");
        _rundAndVerifyFieldDoesntExist("resolution", "=", "Fixed");
        _rundAndVerifyFieldDoesntExist("Select List CF", "=", "\"option 1\"");
        _rundAndVerifyFieldDoesntExist("Single Version Picker CF", "=", "'New Version 1'");
        _rundAndVerifyFieldDoesntExist("Text Field 255", "~", "'text 255'");
        _rundAndVerifyFieldDoesntExist("URL Field CF", "=", "'http://google.com'");
        _rundAndVerifyFieldDoesntExist("User Picker CF", "=", ADMIN_USERNAME);
        _rundAndVerifyFieldDoesntExist("Version Picker CF", "=", "'New Version 4'");

        _runFilterAndVerifyFieldDoesntExist("affectedVersion", 10000);
        _runFilterAndVerifyFieldDoesntExist("assignee", 10001);
        _runFilterAndVerifyFieldDoesntExist("component",10003);
        _runFilterAndVerifyFieldDoesntExist("description", 10004);
        _runFilterAndVerifyFieldDoesntExist("duedate", 10005);
        _runFilterAndVerifyFieldDoesntExist("environment", 10006);
        _runFilterAndVerifyFieldDoesntExist("fixVersion", 10007);
        _runFilterAndVerifyFieldDoesntExist("Free Text Field CF", 10008);
        _runFilterAndVerifyFieldDoesntExist("Group Picker CF", 10009);
        _runFilterAndVerifyFieldDoesntExist("Multi Checkboxes CF", 10010);
        _runFilterAndVerifyFieldDoesntExist("Multi Group Picker CF", 10011);
        _runFilterAndVerifyFieldDoesntExist("Multi Select CF", 10012);
        _runFilterAndVerifyFieldDoesntExist("Multi User Picker CF", 10013);
        _runFilterAndVerifyFieldDoesntExist("Number Field CF", 10014);
        _runFilterAndVerifyFieldDoesntExist("Project Picker CF", 10015);
        _runFilterAndVerifyFieldDoesntExist("Radio Buttons CF", 10016);
        _runFilterAndVerifyFieldDoesntExist("resolution", 10017);
        _runFilterAndVerifyFieldDoesntExist("Select List CF", 10018);
        _runFilterAndVerifyFieldDoesntExist("Single Version Picker CF", 10019);
        _runFilterAndVerifyFieldDoesntExist("Text Field 255", 10020);
        _runFilterAndVerifyFieldDoesntExist("URL Field CF", 10021);
        _runFilterAndVerifyFieldDoesntExist("User Picker CF", 10022);
        _runFilterAndVerifyFieldDoesntExist("Version Picker CF", 10023);

//        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + 10002);
//        tester.assertElementNotPresent("issuetable");
        assertSearchWithError(backdoor.filters().getFilterJql(10002), "Field 'Cascading Select CF' does not exist or you do not have permission to view it.");
    }

    public void testMakeFieldsOutOfScope() throws Exception
    {
        _testMakeFieldOutOfScope("Cascading Select CF", "in", "cascadeOption(\"Parent Option 1\", \"Child Option 1\")", "10001", "10011");
        _testMakeFieldOutOfScope("Free Text Field CF", "~", "'free text'", "10010", "10020");
        _testMakeFieldOutOfScope("Group Picker CF", "=", "'jira-administrators'", "10011", "10021");
        _testMakeFieldOutOfScope("Multi Checkboxes CF", "=", "'check 1'", "10012", "10022");
        _testMakeFieldOutOfScope("Multi Group Picker CF", "=", "'jira-administrators'", "10013", "10023");
        _testMakeFieldOutOfScope("Multi Select CF", "=", "'opt 1'", "10014", "10024");
        _testMakeFieldOutOfScope("Multi User Picker CF", "=", ADMIN_USERNAME, "10015", "10025");
        _testMakeFieldOutOfScope("Number Field CF", "=", "1", "10003", "10013");
        _testMakeFieldOutOfScope("Project Picker CF", "=", "Invisible", "10016", "10026");
        _testMakeFieldOutOfScope("Radio Buttons CF", "=", "'Radio Option 1'", "10017", "10027");
        _testMakeFieldOutOfScope("Select List CF", "=", "\"option 1\"", "10000", "10010");
        _testMakeFieldOutOfScope("Single Version Picker CF", "=", "'New Version 1'", "10018", "10028");
        _testMakeFieldOutOfScope("Text Field 255", "~", "'text 255'", "10019", "10029");
        _testMakeFieldOutOfScope("URL Field CF", "=", "'http://google.com'", "10020", "10030");
        _testMakeFieldOutOfScope("User Picker CF", "=", ADMIN_USERNAME, "10002", "10012");
        _testMakeFieldOutOfScope("Version Picker CF", "=", "'New Version 4'", "10021", "10031");
    }

    public void testMakeNumberFieldCFOutOfScopeByIssueType() throws Exception
    {
        administration.restoreData("TestSearchConstrainedByConfiguration.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // Search for field
        assertSearchWithResults("\"Number Field CF\" = 1", "MKY-1", "HSP-1" );

        // Test empty
        assertSearchWithResults("\"Number Field CF\" is empty",  "MKY-2", "HSP-2");

        // Constrain the scope of the field
        administration.customFields().editConfigurationSchemeContextById("10003", "10013", null, new String[] { "4" }, new String[] { "10000" });

        // Lets pickup the change
        administration.reIndex();

        // Search for field should not be found for all since field is not configured for issue type
        navigation.issueNavigator().createSearch("\"Number Field CF\" = 1");
        tester.assertElementNotPresent("issuetable");
        tester.assertTextNotPresent("MKY-1");
        navigation.issueNavigator().createSearch("\"Number Field CF\" is empty");
        tester.assertElementNotPresent("issuetable");
        tester.assertTextNotPresent("MKY-1");
    }

    public void testHidePriority() throws Exception
    {
        // Search for field
        assertSearchWithResults("priority = \"Major\"", "MKY-2", "MKY-1", "HSP-2", "HSP-1");

        // Hide the field
        administration.fieldConfigurations().fieldConfiguration("Hide Stuff Configuration").hideFields("Priority");

        // Lets pickup the change
        administration.reIndex();

        // Search for field should not be found for monkey project
        assertSearchWithResults("priority = \"Major\"", "HSP-2", "HSP-1");
    }

    public void testHideReporter() throws Exception
    {
        // Search for field
        assertSearchWithResults("reporter = \"admin\"", "MKY-2", "MKY-1", "HSP-2", "HSP-1");

        // Hide the field
        administration.fieldConfigurations().fieldConfiguration("Hide Stuff Configuration").hideFields("Reporter");

        // Lets pickup the change
        administration.reIndex();

        // Search for field should not be found for monkey project
        assertSearchWithResults("reporter = \"admin\"", "HSP-2", "HSP-1");
    }

    public void testValidValuesBasedOnBrowseProjectPermission() throws Exception
    {
        final String jqlAffectedVersion = createJqlQuery("affectedVersion", "New Version 1", "New Version 2");
        assertSearchWithResults(jqlAffectedVersion, "MKY-1", "HSP-1");
        _runFilterAndVerifySearchNoRestrictions(10030);

        final String jqlComponents = createJqlQuery("component", "New Component 1", "New Component 4");
        assertSearchWithResults(jqlComponents, "MKY-1", "HSP-1");
        _runFilterAndVerifySearchNoRestrictions(10031);

        final String jqlFixVersion = createJqlQuery("fixVersion", "New Version 4", "New Version 2");
        assertSearchWithResults(jqlFixVersion, "MKY-1", "HSP-1");
        _runFilterAndVerifySearchNoRestrictions(10032);

        final String jqlLevel = createJqlQuery("level", "Example Level 1", "Example Level 3");
        assertSearchWithResults(jqlLevel, "MKY-1", "HSP-1");
        _runFilterAndVerifySearchNoRestrictions(10033);

        final String jqlProject = createJqlQuery("project", "Monkey", "Homosapien");
        assertSearchWithResults(jqlProject, "MKY-2", "MKY-1", "HSP-2", "HSP-1");
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10034");
        assertIssues("HSP-1","HSP-2", "MKY-1","MKY-2");

        final String jqlProjectPicker = createJqlQuery("Project Picker CF", "Monkey", "Invisible");
        assertSearchWithResults(jqlProjectPicker, "MKY-1", "HSP-1");
        _runFilterAndVerifySearchNoRestrictions(10035);

        final String jqlVersionPicker = createJqlQuery("Version Picker CF", "New Version 1", "New Version 2");
        assertSearchWithResults(jqlVersionPicker, "MKY-1", "HSP-1");
        _runFilterAndVerifySearchNoRestrictions(10036);

        administration.permissionSchemes().scheme("Custom Permission Scheme").removePermission(10, "jira-users");

        assertInvalidValue("affectedVersion", "New Version 1", "New Version 2");
        _runFilterAndVerifySearchRestricted(10030, "affectedVersion", "New Version 2");

        assertInvalidValue("component", "New Component 1", "New Component 4");
        _runFilterAndVerifySearchRestricted(10031, "component", "New Component 4");

        assertInvalidValue("fixVersion", "New Version 4", "New Version 2");
        _runFilterAndVerifySearchRestricted(10032, "fixVersion", "New Version 2");

        assertInvalidValue("level", "Example Level 1", "Example Level 3");
        _runFilterAndVerifySearchRestricted(10033, "level", "Example Level 3");

        assertInvalidValue("project", "Homosapien", "Monkey");
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10034");
        assertSearchWithError(backdoor.filters().getFilterJql(10034), "A value with ID '10001' does not exist for the field 'project'.");
        tester.assertElementNotPresent("issuetable");

        assertInvalidValue("Project Picker CF", "Invisible", "Monkey");
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10035");
        assertSearchWithError(backdoor.filters().getFilterJql(10035), "A value with ID '10001' does not exist for the field 'Project Picker CF'.");
        tester.assertElementNotPresent("issuetable");

        assertInvalidValue("Version Picker CF", "New Version 1", "New Version 2");
        _runFilterAndVerifySearchRestricted(10036, "Version Picker CF", "New Version 2");
    }

    public void testValidValuesForParentClause() throws Exception
    {
        final String subTaskMKY = navigation.issue().createSubTask("MKY-1", "Sub-task", "blub", "blah");
        final String subTaskHSP = navigation.issue().createSubTask("HSP-1", "Sub-task", "blub", "blah");

        final String jqlParent = createJqlQuery("parent", "MKY-1", "HSP-1");
        assertSearchWithResults(jqlParent, subTaskMKY, subTaskHSP);
        _runFilterAndVerifySearchNoRestrictions(10037);

        administration.permissionSchemes().scheme("Custom Permission Scheme").removePermission(10, "jira-users");

        assertSearchWithResults(createJqlQuery("parent", "HSP-1", "MKY-1"), subTaskHSP);
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10037");
        _runFilterAndVerifySearchRestricted(10037);
    }

    private String createJqlQuery(final String clauseName, final String value1, final String value2)
    {
        return "\"" + clauseName + "\" = \"" + value1 + "\" OR \"" + clauseName + "\" = \"" + value2 + "\"";
    }

    private void assertInvalidValue(final String fieldName, final String value1, final String invalidValue)
    {
        assertSearchWithError(createJqlQuery(fieldName, value1, invalidValue), "The value '" + invalidValue + "' does not exist for the field '" + fieldName + "'.");
    }

    private void _testHideField(String fieldName, String operator, String fieldValue)
    {
        _testHideField(fieldName, operator, fieldValue, fieldName);
    }

    private void _testHideField(String fieldClauseName, String operator, String fieldValue, String fieldConfigName)
    {
        _runAndVerifySearchForFieldNoRestrictions(fieldClauseName, operator, fieldValue);

        // Hide the field
        administration.fieldConfigurations().fieldConfiguration("Hide Stuff Configuration").hideFields(fieldConfigName);

        // Lets pickup the change
        administration.reIndex();
        _runAndVerifySearchForFieldRestricted(fieldClauseName, operator, fieldValue);

    }

    private void _testMakeFieldOutOfScope(String fieldName, String operator, String fieldValue, String customFieldId, String fieldConfigSchemeId)
    {
        // Search for field
        _runAndVerifySearchForFieldNoRestrictions(fieldName, operator, fieldValue);

        // Constrain the scope of the field
        administration.customFields().editConfigurationSchemeContextById(customFieldId, fieldConfigSchemeId, null, null, new String[] { "10000" });

        // Lets pickup the change
        administration.reIndex();

        // Search for field should not be found for monkey project
        _runAndVerifySearchForFieldRestricted(fieldName, operator, fieldValue);
    }

    private void _runAndVerifySearchForFieldRestricted(final String fieldName, final String operator, final String fieldValue)
    {// Search for field should not be found for monkey project

        assertSearchWithResults("\"" + fieldName + "\" " + operator + " " + fieldValue, "HSP-1");
        assertSearchWithResults("\"" + fieldName + "\" is empty", "HSP-2");
    }

    private void _runAndVerifySearchForFieldNoRestrictions(final String fieldName, final String operator, final String fieldValue)
    {
        // Search for field
        assertSearchWithResults("\"" + fieldName + "\" " + operator + " " + fieldValue, "MKY-1", "HSP-1");

        // Test empty
        assertSearchWithResults("\"" + fieldName + "\" is empty", "MKY-2", "HSP-2");
    }

    private void _rundAndVerifyFieldDoesntExist(final String fieldName, String operator, String fieldValue)
    {
        assertSearchWithError("\"" + fieldName + "\" " + operator + " " + fieldValue, "Field '" + fieldName + "' does not exist or you do not have permission to view it.");
    }

    private void _runFilterAndVerifySearchNoRestrictions(final int filterId)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId);
        assertIssues("HSP-1","MKY-1");
    }

    private void _runFilterAndVerifySearchRestricted(final int filterId)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId);
        assertIssues("HSP-1");
    }
    
    private void _runFilterAndVerifySearchRestricted(final int filterId, final String fieldName, final String invalidValue)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId);
        assertSearchWithError(backdoor.filters().getFilterJql(filterId), "The value '"+invalidValue+"' does not exist for the field '"+fieldName+"'.");
        tester.assertElementNotPresent("issuetable");
    }

    private void _runFilterAndVerifyFieldDoesntExist(final String field, final int filterId)
    {
        tester.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=" + filterId);
        assertSearchWithError(backdoor.filters().getFilterJql(filterId), "Field '"+field+"' does not exist or you do not have permission to view it.");
        tester.assertElementNotPresent("issuetable");
    }
}
