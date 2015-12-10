package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Test saved filter behaviour with JQL.
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestFilters extends AbstractJqlFuncTest
{
    // JRA-19422
    public void testRunFilterWithCustomFieldOptionWithNoAssociatedConfig() throws Exception
    {
        administration.restoreData("TestCustomFieldOptionsNoConfig.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        // Just load the filter and make sure the screen does not explode, this tests data that used to explode
        navigation.issueNavigator().loadFilter(10000);
        tester.assertElementNotPresent("issuetable");
    }

    /**
     * Loads a filter in view mode, edits it and checks that the UI reflects the changes to the filter.
     *
     * @param filterId the id of the filter to load
     * @param newQuery the JQL query to modify the filter with
     * @param summary the new summary to verify
     * @param visibleOperations the visibleOperations that should be available after modifying the filter
     */
    private void loadModifyFilter(final int filterId, final String newQuery, final String[] summary, final String[] visibleOperations, final String[] hiddenOperations )
    {
        navigation.issueNavigator().loadFilter(filterId, null);

        // modify query with new JQL
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        tester.setFormElement("jqlQuery", newQuery);
        tester.submit();

        // check for 'filter modified' warning
        final Locator editDescriptionLocator = new IdLocator(tester, "filter-description");
        text.assertTextPresent(editDescriptionLocator, "Filter modified since loading");

        // goto view mode and make sure the message appears there as well
        navigation.issueNavigator().gotoViewMode();
        final Locator viwDescriptionLocator = new IdLocator(tester, "filter-description");
        text.assertTextPresent(viwDescriptionLocator, "Filter modified since loading");

        // assert that the summary reflects the new query
        assertions.getTextAssertions().assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), summary);

        // assert visibleOperations
        assertions.getTextAssertions().assertTextSequence(new IdLocator(tester, "filteroperations"),
                visibleOperations);

        // assert hiddenOperations
        for (String operation : hiddenOperations)
        {
            assertions.getTextAssertions().assertTextNotPresent(new IdLocator(tester, "filteroperations"),
                    operation);
        }
    }

    private void checkJqlFilterDiff(final List<DiffCharSequence> expectedOldSearchRequest, final List<DiffCharSequence> expectedUpdatedSearchRequest)
    {
        tester.clickLink("filtersave");
        assertDiffSequence(new XPathLocator(tester, "//*[@id=\"dbJqlQuery\"]/span"), expectedOldSearchRequest);
        assertDiffSequence(new XPathLocator(tester, "//*[@id=\"currentJqlQuery\"]/span"), expectedUpdatedSearchRequest);
    }

    private void assertDiffSequence(final XPathLocator xPathLocator, final List<DiffCharSequence> diffSequenceList)
    {
        final Node[] nodes = xPathLocator.getNodes();
        assertEquals(diffSequenceList.size(), nodes.length);
        for (int i = 0; i < nodes.length; i++)
        {
            final String expectedString = diffSequenceList.get(i).sequence;
            final String actualString = DomKit.getRawText(nodes[i]);
            final String whitespaceNormalized = actualString.replaceAll("[\\s\\xa0]+", " ").trim(); // non-breaking whitespace (0xA0) is apparently not whitespace according to java's regex
            assertEquals(expectedString, whitespaceNormalized);

            final String expectedType = diffSequenceList.get(i).type.toString();
            final String actualType = DomKit.getRawText(nodes[i].getAttributes().getNamedItem("class"));
            assertEquals(expectedType, actualType);
        }
    }

    enum DiffType
    {
        diffremovedchars, diffcontext, diffaddedchars
    }

    class DiffCharSequence
    {
        final DiffType type;
        final String sequence;

        public DiffCharSequence(final DiffType type, final String sequence)
        {
            this.type = type;
            this.sequence = sequence;
        }
    }

    /* Verify that if we load a filter that has become invalid (e.g. refers to a project that has since been deleted) we handle
     * it gracefully
     */
    public void testInvalidatedFilters() throws Exception
    {
        administration.restoreData("TestJqlFilters.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        loadInvalidatedFilter(10000, "project = homosapien", "The value 'homosapien' does not exist for the field 'project'.", true);
        loadInvalidatedFilter(10001, "issuetype = Bug", "The value 'Bug' does not exist for the field 'issuetype'.", true);
        loadInvalidatedFilter(10010, "resolution = Duplicate", "The value 'Duplicate' does not exist for the field 'resolution'.", true);
        loadInvalidatedFilter(10011, "votes > 0", "Field 'votes' does not exist or you do not have permission to view it.", true);
        loadInvalidatedFilter(10014, "workratio = 10", "Field 'workratio' does not exist or you do not have permission to view it.", true);
        loadInvalidatedFilter(10020, "affectedVersion = \"1.0\"", "The value '1.0' does not exist for the field 'affectedVersion'.", true);
        loadInvalidatedFilter(10021, "fixVersion = \"1.0\"", "The value '1.0' does not exist for the field 'fixVersion'.", true);
        loadInvalidatedFilter(10022, "status = \"Not Used\"", "The value 'Not Used' does not exist for the field 'status'.", true);
        loadInvalidatedFilter(10023, "key = \"MKY-1\"", "An issue with key 'MKY-1' does not exist for field 'key'.", true);
        loadInvalidatedFilter(10024, "component = CompA", "The value 'CompA' does not exist for the field 'component'.", true);

        loadInvalidatedFilter(10025, "issue in watchedIssues()", "Function 'watchedIssues' cannot be called as watching issues is currently disabled.", true);
        loadInvalidatedFilter(10026, "issue in votedIssues()", "Function 'votedIssues' cannot be called as voting on issues is currently disabled.", true);
        loadInvalidatedFilter(10027, "issue in linkedIssues(\"MKY-2\")", "Function 'linkedIssues' cannot be called as issue linking is currently disabled.", true);
        loadInvalidatedFilter(10028, "parent = \"MKY-2\"", "Field 'parent' does not exist or you do not have permission to view it.", true);
        // these "fit" so we need a slightly different test
        loadInvalidatedFilter(10030, "type in subtaskIssueTypes()", "Function 'subTaskIssueTypes' is invalid as sub-tasks are currently disabled.",  true);
        // 100031 was deleted
        loadInvalidatedFilter(10032, "filter = 10031", "A value with ID '10031' does not exist for the field 'filter'.", true);

        loadInvalidatedFilter(10034, "originalEstimate >= 5h", "Field 'originalEstimate' does not exist or you do not have permission to view it.", true);
        loadInvalidatedFilter(10035, "remainingEstimate > 5h", "Field 'remainingEstimate' does not exist or you do not have permission to view it.", true);
        loadInvalidatedFilter(10036, "timespent > 5h", "Field 'timespent' does not exist or you do not have permission to view it.", true);

        loadInvalidatedFilter(10038, "affectedVersion in releasedVersions(deleted)", "Could not resolve the project 'deleted' provided to function 'releasedVersions'.", true);
        loadInvalidatedFilter(10039, "affectedVersion in unreleasedVersions(deleted)", "Could not resolve the project 'deleted' provided to function 'unreleasedVersions'.", true);
        loadInvalidatedFilter(10040, "level = SL1", "The value 'SL1' does not exist for the field 'level'.", true);

        loadInvalidatedFilter(10012, "reporter = fred", "The value 'fred' does not exist for the field 'reporter'.", false);
        loadInvalidatedFilter(10013, "assignee = fred", "The value 'fred' does not exist for the field 'assignee'.", false);
        loadInvalidatedFilter(10033, "reporter in membersOf(\"jira-developers\")", "Function 'membersOf' can not generate a list of usernames for group 'jira-developers'; the group does not exist.", true);
    }

    public void testIllegalFilters() throws Exception
    {
        administration.restoreData("TestJqlIllegalFilters.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // Loading a saved filter shouldn't produce errors until we modify it.
        issueTableAssertions.assertSearchWithError(10000L, "The operator '~' is not supported by the 'cs' field.");
        issueTableAssertions.assertSearchWithError(10000L, "cs ~ cs ORDER BY key",
                "The operator '~' is not supported by the 'cs' field.");
    }

    // ORDER BY
    private void loadInvalidatedFilter(final long filterId, final String JQL, final String errorMessage,
            final boolean expectError)
    {
        // But if we modify it, errors and warnings should appear.
        if (expectError)
        {
            // Loading a saved filter shouldn't produce errors.
            issueTableAssertions.assertSearchWithError(filterId, errorMessage);
            issueTableAssertions.assertSearchWithError(filterId,
                    JQL + " ORDER BY key", errorMessage);
        }
        else
        {
            issueTableAssertions.assertSearchWithWarning(filterId, errorMessage);
            issueTableAssertions.assertSearchWithWarning(filterId,
                    JQL + " ORDER BY key", errorMessage);
        }
    }
}