package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestCustomFieldAliases extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestCustomFieldAliases.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testCustumFielAliasesAndSpaces() throws Exception
    {
        assertSearchWithResults("\"With Spaces\" is not empty", "HSP-3", "HSP-2");
        assertSearchWithResults("cf[10001] is not empty", "HSP-3", "HSP-2");
        assertSearchWithResults("\"With Spaces\" ~ \"Blah\" OR cf[10001] ~ \"Hello there\"", "HSP-3", "HSP-2");
        assertSearchWithResults("\"NoSpaces\" is not empty", "HSP-1");
        assertSearchWithResults("cf[10000] is not empty", "HSP-1");
        assertSearchWithResults("\"With Spaces\" is not empty OR \"NoSpaces\" is not empty", "HSP-3", "HSP-2", "HSP-1");
    }

    public void testCustomFieldDoesItFitWithAliasesAndFullNames() throws Exception
    {
        assertFitsFilterForm("\"With Spaces\" ~ \"Blah\"", createFilterFormParam("customfield_10001", "Blah"));
        assertFitsFilterForm("\"cf[10001]\" ~ \"Blah\"", createFilterFormParam("customfield_10001", "Blah"));
        assertFitsFilterForm("\"NoSpaces\" ~ \"HelloThere\"", createFilterFormParam("customfield_10000", "HelloThere"));
        assertFitsFilterForm("\"cf[10000]\" ~ \"HelloThere\"", createFilterFormParam("customfield_10000", "HelloThere"));
    }

    public void testSameNameSameTypeSameContext() throws Exception
    {
        assertFitsFilterForm("cf[10010] ~ \"Blah\"", createFilterFormParam("customfield_10010", "Blah"));
        assertFitsFilterForm("cf[10010] ~ \"Blah\" AND cf[10011] ~ \"Blah\"", createFilterFormParam("customfield_10010", "Blah"), createFilterFormParam("customfield_10011", "Blah"));
        assertTooComplex("SameSameSame ~ \"Blah\"");

        assertSearchWithResults("SameSameSame ~ \"value1\"", "HSP-5", "HSP-4");
        assertSearchWithResults("SameSameSame ~ \"value2\"", "HSP-5", "HSP-4");
        assertSearchWithResults("cf[10010] ~ \"value1\"", "HSP-4");
        assertSearchWithResults("cf[10011] ~ \"value1\"", "HSP-5");
    }

    public void testSameNameDiffTypeSameContext() throws Exception
    {
        assertSearchWithError("SameDiffSame ~ 1", "The operator '~' is not supported by the 'SameDiffSame' field.");
        assertSearchWithError("SameDiffSame = 1", "The operator '=' is not supported by the 'SameDiffSame' field.");
        assertSearchWithError("SameDiffSame = 2", "The operator '=' is not supported by the 'SameDiffSame' field.");
        assertSearchWithError("SameDiffSame ~ Value1", "The operator '~' is not supported by the 'SameDiffSame' field.");

        assertSearchWithResults("cf[10020] ~ Value1", "HSP-6");
        assertFitsFilterForm("cf[10020] ~ Value1");

        assertSearchWithResults("cf[10020]  ~ 2", "HSP-7");
        assertFitsFilterForm("cf[10020]  ~ 2");

        assertSearchWithResults("cf[10021] = 1", "HSP-7");
        assertFitsFilterForm("cf[10021] = 1");

        assertSearchWithResults("cf[10021] = 2", "HSP-6");
        assertFitsFilterForm("cf[10021] = 2");
    }

    public void testSameNameSameTypeSameContextDifferentValues() throws Exception
    {
        assertSearchWithResults("SelectHidden = \"SameValue\"");
        assertTooComplex("SelectHidden = \"SameValue\"");
        assertSearchWithResults("SelectHidden = \"DiffValue1\"", "HSP-8");
        assertTooComplex("SelectHidden = \"DiffValue1\"");
        assertSearchWithError("SelectHidden = \"DiffValue2\"", "The option 'DiffValue2' for field 'SelectHidden' does not exist.");

        assertSearchWithResults("\"Select\" = \"SameValue\"", "HSP-10", "HSP-9");
        assertTooComplex("\"Select\" = \"SameValue\"");
        assertTooComplex("\"Select\" = \"SameValue\" and project = \"homosapien\"");
        assertTooComplex("\"Select\" = \"SameValue\" and project = \"monkey\"");

        assertFitsFilterForm("cf[10030] = \"SameValue\"");
        assertFitsFilterForm("cf[10031] = \"SameValue\"");

        assertFitsFilterForm("cf[10030] = \"DiffValue1\"");
        assertFitsFilterForm("cf[10031] = \"DiffValue2\"");

        assertSearchWithError("\"Select\" = \"DiffValue1\"", "The option 'DiffValue1' for field 'Select' does not exist.");
        assertSearchWithError("\"Select\" = \"DiffValue2\"", "The option 'DiffValue2' for field 'Select' does not exist.");
    }

    public void testSameNameSameTypeDiffContext() throws Exception
    {
        assertSearchWithResults("SelectContext = 'Option1'", "MKY-1", "HSP-11");
        assertTooComplex("SelectContext = 'Option1'");
        assertSearchWithResults("SelectContext = 'Option1' and project = homosapien", "HSP-11");
        assertTooComplex("SelectContext = 'Option1' and project = homosapien");
        assertSearchWithResults("SelectContext = 'Option1' and project = monkey", "MKY-1");
        assertTooComplex("SelectContext = 'Option1' and project = monkey");

        assertSearchWithError("SelectContext= 'OptionMonkey'", "The option 'OptionMonkey' for field 'SelectContext' does not exist.");
        // todo make work with different user. RestClient auto sets osuername ospassword
//        navigation.login(FRED_USERNAME, FRED_PASSWORD);
//        assertSearchWithResults("SelectContext = 'Option1'", "MKY-1");
//        assertSearchWithResults("SelectContext= 'OptionMonkey'", "MKY-2");
    }

    public void testSameNameDiffTypeSameOperator() throws Exception
    {
        assertSearchWithResults("Picker in (\"TestValue\")","HSP-17","HSP-16","HSP-15","HSP-14","HSP-13");
        assertSearchWithResults("cf[10050] in (\"TestValue\")","HSP-17","HSP-16","HSP-15","HSP-13");
        assertSearchWithResults("cf[10051] in (\"TestValue\")","HSP-17","HSP-14","HSP-13");

        assertSearchWithResults("Picker not in (\"TestValue\")","HSP-18");
        assertSearchWithResults("cf[10050] not in (\"TestValue\")","HSP-18");
        assertSearchWithResults("cf[10051] not in (\"TestValue\")","HSP-18");

        assertSearchWithResults("Picker = \"TestValue\"","HSP-17","HSP-16","HSP-15","HSP-14","HSP-13");
        assertSearchWithResults("cf[10050] = \"TestValue\"","HSP-17","HSP-16","HSP-15","HSP-13");
        assertSearchWithResults("cf[10051] = \"TestValue\"","HSP-17","HSP-14","HSP-13");

        assertSearchWithResults("Picker != \"TestValue\"","HSP-18");
        assertSearchWithResults("cf[10050] != \"TestValue\"","HSP-18");
        assertSearchWithResults("cf[10051] != \"TestValue\"","HSP-18");

        assertSearchWithResults("Picker is empty","MKY-2", "MKY-1", "HSP-16", "HSP-15", "HSP-14", "HSP-12", "HSP-11", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        assertSearchWithResults("cf[10050] is empty","MKY-2", "MKY-1","HSP-14", "HSP-12", "HSP-11", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        assertSearchWithResults("cf[10051] is empty","MKY-2", "MKY-1", "HSP-16", "HSP-15", "HSP-12", "HSP-11", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");

        assertSearchWithResults("Picker is not empty","HSP-18","HSP-17","HSP-16","HSP-15","HSP-14","HSP-13");
        assertSearchWithResults("cf[10050] is not empty","HSP-18","HSP-17","HSP-16","HSP-15","HSP-13");
        assertSearchWithResults("cf[10051] is not empty","HSP-18","HSP-17","HSP-14","HSP-13");

        assertSearchWithError("Picker in (\"New Version 1\")","The value 'New Version 1' does not exist for the field 'Picker'.");
        assertSearchWithError("cf[10051] in (\"New Version 1\")","The value 'New Version 1' does not exist for the field 'cf[10051]'.");
        assertSearchWithResults("cf[10050] in (\"New Version 1\")","HSP-18","HSP-17");

        assertFitsFilterForm("cf[10050] = \"New Version 1\"");
        final IssueNavigatorAssertions.FilterFormParam param1 = createFilterFormParam("pid", "10000");
        final IssueNavigatorAssertions.FilterFormParam param2 = createFilterFormParam("customfield_10050", "10000");
        assertFitsFilterForm("cf[10050] in (\"New Version 1\") AND project = \"Homosapien\"", param1,param2);

        assertTooComplex("cf[10051] in (\"TestValue\")");
        assertFitsFilterForm("cf[10051] = \"TestValue\"");
    }

    //
    // Test to ensure that custom field aliases are ignored when:
    //   1. They have the same name as a system field.
    //   2. Someone tries to name their custom field "cf[\d+]".
    //
    public void testBadCustomFieldName() throws Exception
    {
        /*
            cf:10066 -> summary
            cf:10067 -> cf[10067]
         */

        //Make sure the project custom field does not come into play.
        assertSearchWithResults("summary ~ match");
        assertSearchWithResults("cf[10070] ~ match", "HSP-8");
        assertSearchWithResults("cf[10071] ~ match", "HSP-14");
    }
}
