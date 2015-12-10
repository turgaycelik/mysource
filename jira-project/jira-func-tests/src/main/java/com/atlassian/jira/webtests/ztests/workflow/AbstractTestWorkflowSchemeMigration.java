package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.testkit.client.restclient.ChangeLog;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueTransitionsMeta;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;

/**
 *
 * @since v5.2
 */
public abstract class AbstractTestWorkflowSchemeMigration extends JIRAWebTest
{
    protected static final String TEST_PROJECT_NAME = "Test Project";
    protected static final String TEST_PROJECT_KEY = "TST";
    protected static final String HOMOSAPIEN_PROJECT_NAME = "homosapien";

    protected static final String SOURCE_WORKFLOW_SCHEME = "Source Workflow Scheme";
    protected static final String DESTINATION_WORKFLOW_SCHEME = "Destination Workflow Scheme";

    protected static final String REOPENED_STATUS_NAME = "Reopened";
    protected static final String WORKFLOW_HOMOSAPIEN_SOURCE_1 = "Homosapien Source 1";
    protected static final String WORKFLOW_HOMOSPIEN_DESTINATION = "Homospien Destination";
    protected static final String WORKFLOW_HOMOSAPIEN_SOURCE_2 = "Homosapien Source 2";
    protected static final String WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE = "Homosapien Custom Issue Type Source";

    protected static final String WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION = "Homosapien Custom Issue Type Destination";

    protected static final String TRANSITION_NAME_GO_CUSTOM = "Go Custom";
    protected static final String ACKNOWLEDGE = "Acknowledge";

    protected static final String DONE = "Done";
    protected static final String WORKFLOW_FIELD_ID = "Workflow";
    protected static final String SUMMARY_FIELD_ID = "summary";
    protected static final String RESOLUTION_FIELD_ID = "resolution";
    protected static final String STATUS_FIELD_ID = "status";

    protected static final String FIX_VERSIONS_FIELD_ID = "Fix Version";
    protected static final String CUSTOM_STATUS_1 = "Custom Status 1";
    protected static final String CUSTOM_STATUS_2 = "Custom Status 2";
    protected static final String CUSTOM_STATUS_3 = "Custom Status 3";
    protected static final String CUSTOM_STATUS_4 = "Custom Status 4";
    protected static final String RESOLVED_STATUS_NAME = "Resolved";
    protected static final String CLOSED_STATUS_NAME = "Closed";

    protected static final String IN_PROGRESS_STATUS_NAME = "In Progress";
    protected static final String DESTINATION_WORKFLOW = "Destinatiom Workflow";
    protected static final String JIRA_DEFAULT_WORKFLOW = "jira";
    protected static final String SOURCE_WORKFLOW_1 = "Source Workflow 1";
    protected static final String SOURCE_WORKFLOW_2 = "Source Workflow 2";

    public AbstractTestWorkflowSchemeMigration(String name)
    {
        super(name);
    }

    protected Map<String, String> createTestWorkflowMigrationMapping()
    {
        Map<String, String> statusMapping = new HashMap<String, String>();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For Custom Status 2 - select Custom Status 4
        statusMapping.put("mapping_1_10001", CUSTOM_STATUS_4);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);

        return statusMapping;
    }

    protected void assertStandardIssues(String projectKey)
    {
        // Ensure the issues have been migrated to the workflow properly

        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.
        // TST-1, Task, should be migrated from Source Workflow 1 to default workflow and stay in Open status

        // *NOTE*: This issue is not re-indexed during migration as not changes are made to it (well we do add a change
        // item and change its workflowId, but these ain't indexed). We have optimised the migration
        // code to only index the issue when indexable changes are made to it. At the time of writing, this issue has
        // no changes when migration occurs. A failing assertion here indicates that our optimisation are now probably
        // incorrect.
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-1"), true);

        // TST-2, Task, should be migrated from Source Workflow 1 to default workflow and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-2"), false);

        // TST-3, Improvement, should be migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue(projectKey + "-3"), false);

        // TST-4, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue(projectKey + "-4"), false);

        // TST-5, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3))
                .assertIssue(getIssue(projectKey + "-5"), true);

        // TST-6, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue(projectKey + "-6"), false);

        // TST-7, New Feature, should be migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, CLOSED_STATUS_NAME, CUSTOM_STATUS_4))
                .assertIssue(getIssue(projectKey + "-7"), false);

        // TST-8, Improvement, should be migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue(projectKey + "-8"), false);

        // TST-9, Task, should be migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addTransitions("Reopen Issue")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-9"), false);

        // TST-10, Bug, should be migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3))
                .assertIssue(getIssue(projectKey + "-10"), true);

        // TST-11, Bug, should be migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3))
                .assertIssue(getIssue(projectKey + "-11"), true);

        //JRADEV-11118: Lets do some searches to make sure that the issues were indexed correctly.
        //Issues TST-1, TST-2 and TST-9 were not changed and as such should no be in these results.
        assertJqlResults("updated >-1d and project = " + projectKey, projectKey + "-3", projectKey + "-4",
                projectKey + "-5", projectKey + "-6", projectKey + "-7", projectKey + "-8", projectKey + "-10", projectKey + "-11");

        //These old issues have not been updated, but we should still be able to find them.
        assertJqlResults("status = OPEN and project = " + projectKey, projectKey + "-1", projectKey + "-2");

        //These old issues have not been updated, but we should still be able to find them.
        assertJqlResults("status in (OPEN, CLOSED) and project = " + projectKey, projectKey + "-1", projectKey + "-2", projectKey + "-9");

        //These issues have not been updated and their statuses changed.
        assertJqlResults("status in ('Custom Status 4') and project = " + projectKey, projectKey + "-7", projectKey + "-6", projectKey + "-4");
    }

    protected static ChangeLog.HistoryItem item(String field, String from, String to)
    {
        return new ChangeLog.HistoryItem().setField(field).setFromString(from).setToString(to);
    }

    protected Issue getIssue(String issueKey)
    {
        return getBackdoor().issues().getIssue(issueKey, Issue.Expand.changelog, Issue.Expand.transitions);
    }

    protected void assertJqlResults(String jql, String...expectedKeys)
    {
        final SearchResult searchResult = getBackdoor().search()
                .getSearch(new SearchRequest().jql(jql).fields("key"));
        Set<String> actualKeys = newHashSet(transform(searchResult.issues, new Function<Issue, String>()
        {
            @Override
            public String apply(Issue input)
            {
                return input.key;
            }
        }));
        assertEquals(Sets.newHashSet(expectedKeys), actualKeys);
    }

    protected void assertIssuesAfterMigrationWithUnupdatedWorkflowScheme()
    {
        // Ensure the issues have been migrated to the workflow properly

        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.

        // TST-1, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("TST-1"), false);

        // TST-2, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("TST-2"), false);

        // TST-3, Improvement, has already been migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        // Should be migrated again and stay in Resolved status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-3"), false);

        // TST-4, Bug, has alreaddy been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-4"), false);

        // TST-5, Bug, has already been migrated from from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-5"), false);

        // TST-6, Bug, has already been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-6"), false);

        // TST-7, New Feature, has already been migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-7"), false);

        // TST-8, Improvement, has already been migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        // This issue should be migrated again and stay in Resolved status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-8"), false);

        // TST-9, Task, has already been migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        // This issue should be migrated again and stay in Closed status
        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addTransitions("Reopen Issue")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("TST-9"), false);

        // TST-10, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-10"), false);

        // TST-11, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-11"), false);
    }

    protected void assertIssuesMigratedAndChangeHistory(String projectKey)
    {
        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.

        // TST-1, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-1"), false);

        // TST-2, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-2"), false);

        // TST-3, Improvement, should be migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue(projectKey + "-3"), false);

        // TST-4, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue(projectKey + "-4"), false);

        // TST-5, Bug, has already been migrated from from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        // The issue should be migrated again and stay in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-5"), false);

        // TST-6, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue(projectKey + "-6"), false);

        // TST-7, New Feature, should be migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, CLOSED_STATUS_NAME, CUSTOM_STATUS_4))
                .assertIssue(getIssue(projectKey + "-7"), false);

        // TST-8, Improvement, should be migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue(projectKey + "-8"), false);

        // TST-9, Task, should be migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addTransitions("Reopen Issue")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-9"), false);

        // TST-10, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW))
                .assertIssue(getIssue(projectKey + "-10"), false);

        // TST-11, Bug, should be migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_2, DESTINATION_WORKFLOW),
                        item(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3))
                .assertIssue(getIssue(projectKey + "-11"), false);
    }

    protected void assertMappingAndSelectOptionsForHalfMigratedDataNewDestination() throws SAXException
    {
        Map<String, IssueTypeMapping> typeMappings = Maps.uniqueIndex(mappings(), new Function<IssueTypeMapping, String>()
        {
            @Override
            public String apply(IssueTypeMapping input)
            {
                return input.issueType;
            }
        });

        // Bug Issue Type
        // Ensure the correct mappings are present
        IssueTypeMapping mapping = typeMappings.get(ISSUE_TYPE_BUG);
        assertEquals(WORKFLOW_HOMOSAPIEN_SOURCE_1, mapping.oldWorkflow);
        assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, mapping.newWorkflow);

        // Now go through the table and ensure that we have the correct statuses showing on the mapping screen
        // and that statuses appear in correct order. The statuses should appear sorted by sequence. Remember that
        // each issue type might need a different mapping
        assertStatusMappings(ImmutableList.of(IN_PROGRESS_STATUS_NAME, CLOSED_STATUS_NAME, CUSTOM_STATUS_1, CUSTOM_STATUS_2),
                ImmutableList.of(STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                mapping);

        tester.selectOption("mapping_1_3", STATUS_OPEN);
        tester.selectOption("mapping_1_6", RESOLVED_STATUS_NAME);
        tester.selectOption("mapping_1_10000", CUSTOM_STATUS_3);
        tester.selectOption("mapping_1_10001", CUSTOM_STATUS_4);

        // We have issues that have failed migration to Homospien Destination Failure workflow and have been migrated to
        // Custom Status 4. Custom Status 4 is present in Homospien Destination workflow, so the user should not get asked for the mapping
        assertFormElementNotPresent("mapping_1_10003");

        // Improvement Issue Type
        // This issue type is using Homosapien Source 2 and is being migrated to the default JIRA workflow
        mapping = typeMappings.get(ISSUE_TYPE_IMPROVEMENT);
        assertEquals(WORKFLOW_HOMOSAPIEN_SOURCE_2, mapping.oldWorkflow);
        assertEquals("JIRA Workflow (jira)", mapping.newWorkflow);

        assertStatusMappings(ImmutableList.of(CUSTOM_STATUS_1, CUSTOM_STATUS_3),
                ImmutableList.of(STATUS_OPEN, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME, REOPENED_STATUS_NAME, CLOSED_STATUS_NAME),
                mapping);

        tester.selectOption("mapping_4_10000", IN_PROGRESS_STATUS_NAME);
        tester.selectOption("mapping_4_10002", RESOLVED_STATUS_NAME);

        // New Feature Issue Type
        // The issue type is already using the destination workflow so we should not get any mappings for it.
        assertTextNotPresent("New Feature");

        // Task Issue Type
        // The source workflow for the task is 'Homospien Destination' so normally we would not need to
        // do any migration. However, the previous migration has moved some of the Tasks to
        // the 'Homospien Destination Failure' workflow. This issues need to be moved (back) to
        // the 'Homospien Destination' workflow. Hence we will need to get mapping for
        // 'In Progress' and 'Closed' stauses which exist in 'Homospien Destination Failure' workflow, but
        // do not exist in 'Homospien Destination' workflow.
        mapping = typeMappings.get(ISSUE_TYPE_TASK);
        assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, mapping.oldWorkflow);
        assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, mapping.newWorkflow);

        assertStatusMappings(ImmutableList.of(IN_PROGRESS_STATUS_NAME, CLOSED_STATUS_NAME),
                ImmutableList.of(STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME ),
                mapping);

        tester.selectOption("mapping_3_3", CUSTOM_STATUS_3);
        tester.selectOption("mapping_3_6", RESOLVED_STATUS_NAME);

        // Sub-Tasks
        // Sub Tasks (like Bugs) are using Hamosapien Source 1 and are being migrated to Homosapien Destination workflow

        mapping = typeMappings.get(ISSUE_TYPE_SUB_TASK);
                assertEquals(WORKFLOW_HOMOSAPIEN_SOURCE_1, mapping.oldWorkflow);
                assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, mapping.newWorkflow);

        // Now go through the table and ensure that we have the correct statuses showing on the mapping screen
        // and that statuses appear in correct order. There should be no corrupt sub tasks (i.e. all sub tasks should be on the correct
        // workflow). So we should only be asked for mappings for statuses that exist in Homospien Source 1 workflow and do *not* exist in
        // Homosapien Destination workflow.
        assertStatusMappings(ImmutableList.of(CUSTOM_STATUS_1, CUSTOM_STATUS_2),
                ImmutableList.of(STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                mapping);

        tester.selectOption("mapping_5_10000", CUSTOM_STATUS_3);
        tester.selectOption("mapping_5_10001", CUSTOM_STATUS_4);

        // Custom Issue Type
        // Issues of Custom Issue Type are moving between Homosapien Custom Issue Type Source workflow and Homosapien Custom Issue Type Destination
        // All the statuses in Custom Issue Type Source workflow (Open, Resolved, Custom Status 3) are in Homosapien Custom Issue Type Destination as well (Open,
        // Custom Status 1, Resolved, Custom Status 3, Closed), so no status mapping should appear on the page for Custom Issue Type issues.
        assertTextNotPresent("Custom Issue Type");
    }

    private void assertStatusMappings(List<String> oldStatuses, List<String> targetStatuses, IssueTypeMapping mapping)
    {
        List<String> actualOldStatuses = Lists.newArrayList();
        for (StatusMapping statusMapping : mapping.statusMappings)
        {
            assertEquals(targetStatuses, statusMapping.newStatuses);
            actualOldStatuses.add(statusMapping.oldStatus);
        }
        assertEquals(oldStatuses, actualOldStatuses);
    }

    protected void assertIssuesInHomosapienProjectAfterHalfMigratedDataNewDestination()
    {
        /*******************************/
        /* Ensure migration has worked */
        /*******************************/

        // Go to each issue and ensure that:
        // 1. It is on the correct status
        // 2. It has the correct Workflow Transitions available for execution (which we hope means it is actually using the correct workflow)
        // 3. It has the expected change history records

        // HSP-1, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow and left in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-1"), false);

        // HSP-2, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow and left in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-2"), false);

        // HSP-3, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from In Progress status to Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, STATUS_OPEN))
                .assertIssue(getIssue("HSP-3"), false);

        // HSP-4, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from In Progress status to Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, STATUS_OPEN))
                .assertIssue(getIssue("HSP-4"), false);

        // HSP-5, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and stay in Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-5"), false);

        // HSP-6, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and stay in Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-6"), false);

        // HSP-7, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from Closed status to Resolved Status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Reopen")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CLOSED_STATUS_NAME, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue("HSP-7"), false);

        // HSP-8, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from Closed status to Resolved Status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Reopen")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CLOSED_STATUS_NAME, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue("HSP-8"), false);

        // HSP-9, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-9"), false);

        // HSP-10, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-10"), false);

        // HSP-11, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3))
                .assertIssue(getIssue("HSP-11"), false);

        // HSP-12, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3))
                .assertIssue(getIssue("HSP-12"), false);

        // HSP-13, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue("HSP-13"), false);

        // HSP-14, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue("HSP-14"), false);

        // HSP-15, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-15"), false);

        // HSP-16, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-16"), false);

        // Tasks - the issues are moving to the same workflow as their source workflow. However, there are issues here that
        // are on the wrong workflow. We need to ensure that these issues have been fixed up.

        // HSP-17, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and left in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-17"), false);

        // HSP-18, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and from In Progress status to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, CUSTOM_STATUS_3))
                .assertIssue(getIssue("HSP-18"), false);

        // HSP-19, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and left in Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-19"), false);

        // HSP-20, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and from Closed status to Resolved status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Reopen")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CLOSED_STATUS_NAME, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue("HSP-20"), false);

        // HSP-20, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .assertIssue(getIssue("HSP-21"), false);

        // HSP-21, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3))
                .assertIssue(getIssue("HSP-22"), false);

        // HSP-23, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(STATUS_FIELD_ID, CUSTOM_STATUS_3, CUSTOM_STATUS_4))
                .assertIssue(getIssue("HSP-23"), false);

        // HSP-24, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Reopen")
                .addHistoryItems(item(STATUS_FIELD_ID, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                        item(RESOLUTION_FIELD_ID.toLowerCase(Locale.ENGLISH), null, "Fixed"))
                .assertIssue(getIssue("HSP-24"), false);

        // Improvements
        // These should all be migrated to the default JIRA workflow

        // HSP-25, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("HSP-25"), false);

        // HSP-26, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("HSP-26"), false);

        // HSP-27, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 1 to In Progres status
        new IssueAssertions().status(IN_PROGRESS_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_STOP_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, IN_PROGRESS_STATUS_NAME))
                .assertIssue(getIssue("HSP-27"), false);

        // HSP-28, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 1 to In Progres status
        new IssueAssertions().status(IN_PROGRESS_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_STOP_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, IN_PROGRESS_STATUS_NAME))
                .assertIssue(getIssue("HSP-28"), false);

        // HSP-29, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 3 to Resolved status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_REOPEN, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_3, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue("HSP-29"), false);

        // HSP-30, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 3 to Resolved status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_REOPEN, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_3, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue("HSP-30"), false);

        // HSP-31, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Closed status
        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_REOPEN)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("HSP-31"), false);

        // HSP-32, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Closed status
        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_REOPEN)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("HSP-32"), false);

        // New Features
        // These issues should all be untouched - left on the Homospien Destination workflow
        // HSP-33, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .assertIssue(getIssue("HSP-33"), false);

        // HSP-34, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .assertIssue(getIssue("HSP-34"), false);

        // HSP-35, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(SUMMARY_FIELD_ID, "In Progress New Feature 1", "Custom Status 3 New Feature 1"))
                .assertIssue(getIssue("HSP-35"), false);

        // HSP-36, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(SUMMARY_FIELD_ID, "In Progress New Feature 2", "Custom Status 3 New Feature 2"))
                .assertIssue(getIssue("HSP-36"), false);

        // HSP-37, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(STATUS_FIELD_ID, CUSTOM_STATUS_3, CUSTOM_STATUS_4))
                .assertIssue(getIssue("HSP-37"), false);

        // HSP-38, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(STATUS_FIELD_ID, CUSTOM_STATUS_3, CUSTOM_STATUS_4))
                .assertIssue(getIssue("HSP-38"), false);

        // HSP-39, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Reopen")
                .addHistoryItems(item(STATUS_FIELD_ID, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                        item(RESOLUTION_FIELD_ID, null, "Fixed"))
                .assertIssue(getIssue("HSP-39"), false);

        // HSP-40, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Reopen")
                .addHistoryItems(item(STATUS_FIELD_ID, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                        item(RESOLUTION_FIELD_ID, null, "Fixed"))
                .assertIssue(getIssue("HSP-40"), false);

        // Sub-Tasks
        // Sub-Tasks should be migrated to the Homosapien Destination Workflow

        // HSP-41, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-41"), false);

        // HSP-42, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue("HSP-42"), false);

        // HSP-43, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3))
                .assertIssue(getIssue("HSP-43"), false);

        // HSP-44, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and should stay on Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-44"), false);

        // HSP-45, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and should stay on Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-45"), false);

        // HSP-46, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3))
                .assertIssue(getIssue("HSP-46"), false);

        // HSP-47, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and should stay on Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION))
                .assertIssue(getIssue("HSP-47"), false);

        // HSP-48, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue("HSP-48"), false);

        // Custom Issue Type
        // Issues of Custom Issue Type should be migrated from Homosapien Custom Issue Type Source workflow to Homosapien Custom Issue Type Destination workflow

        // HSP-49, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go custom")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION))
                .assertIssue(getIssue("HSP-49"), false);

        // HSP-50, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Close")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION))
                .assertIssue(getIssue("HSP-50"), false);

        // HSP-51, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Open status
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions("Go custom")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION))
                .assertIssue(getIssue("HSP-51"), false);

        // HSP-52, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Resolved status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION))
                .assertIssue(getIssue("HSP-52"), false);

        // HSP-53, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Resolved status
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go Custom 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION))
                .assertIssue(getIssue("HSP-53"), false);

        // HSP-54, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Custom Status 3
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Close")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION))
                .assertIssue(getIssue("HSP-54"), false);
    }

    protected void checkIssuesInTestProjectAfterHalfMigratedDataNewDestination()
    {
        // Check issues in another project and ensure they have not been touched by the migration

        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS)
                .assertIssue(getIssue("TST-1"), false);

        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS)
                .addHistoryItems(item("assignee", "Developer User", "Admin"))
                .assertIssue(getIssue("TST-2"), false);

        new IssueAssertions().status(IN_PROGRESS_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_STOP_PROGRESS, "Close")
                .addHistoryItems(item(STATUS_FIELD_ID, STATUS_OPEN, IN_PROGRESS_STATUS_NAME))
                .assertIssue(getIssue("TST-3"), false);

        new IssueAssertions().status(CUSTOM_STATUS_2)
                .addTransitions("Reopen")
                .addHistoryItems(item(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_2),
                        item(RESOLUTION_FIELD_ID, null, "Incomplete"))
                .assertIssue(getIssue("TST-4"), false);

        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSITION_NAME_GO_CUSTOM)
                .assertIssue(getIssue("TST-5"), false);

        new IssueAssertions().status(CUSTOM_STATUS_2)
                .addTransitions("Reopen")
                .addHistoryItems(item(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_2),
                        item(FIX_VERSIONS_FIELD_ID, null, "Version 1"),
                        item(RESOLUTION_FIELD_ID, null, "Fixed"))
                .assertIssue(getIssue("TST-6"), false);

        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addHistoryItems(item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, CLOSED_STATUS_NAME),
                        item(FIX_VERSIONS_FIELD_ID, null, "Version 1"),
                        item(RESOLUTION_FIELD_ID, null, "Cannot Reproduce"))
                .assertIssue(getIssue("TST-7"), false);


        new IssueAssertions().status(IN_PROGRESS_STATUS_NAME)
                .addTransitions(TRANSIION_NAME_STOP_PROGRESS, "Close")
                .addHistoryItems(item(STATUS_FIELD_ID, STATUS_OPEN, IN_PROGRESS_STATUS_NAME))
                .assertIssue(getIssue("TST-8"), false);

        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addHistoryItems(item(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, CLOSED_STATUS_NAME),
                        item(FIX_VERSIONS_FIELD_ID, null, "Version 3"),
                        item(RESOLUTION_FIELD_ID, null, "Won't Fix"))
                .assertIssue(getIssue("TST-9"), false);

        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSITION_NAME_GO_CUSTOM)
                .assertIssue(getIssue("TST-10"), false);

        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSITION_NAME_GO_CUSTOM)
                .assertIssue(getIssue("TST-11"), false);
    }

    protected void waitForFailedMigration()
    {
        final int MAX_ITERATIONS = 100;
        int its = 0;
        while (true)
        {
            its++;
            if (its > MAX_ITERATIONS)
            {
                fail("The Workflow Migration took longer than " + MAX_ITERATIONS + " attempts!  Why?");
            }
            // are we on the "still working" page or the "error" page
            // if its neither then fail
            if (getResponseText().contains("type=\"submit\" name=\"Refresh\""))
            {
                // we are on the "still working page"
                // click on the Refresh Button
                submit("Refresh");
            }
            else if (getDialog().getResponsePageTitle().contains("WORKFLOW ASSOCIATION ERROR"))
            {
                // we are on the "error" page"
                return;
            }
            else
            {
                // we are on a page we dont expect
                fail("Page encountered during migration that was not expected");
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                fail("Test interupted");
            }
        }
    }

    protected void assertIssueVerifierErrorMessages(String projectKey)
    {
        // Ensure that the Issue Verifier found problems.
        // TODO unfortunately XPath does not understand this pseudo-HTML page
        // if you have idea hot to make it XPath-searchable - try
        // for now, fe have to fall back to asserting against raw HTML
//        getAssertions().getJiraFormAssertions().assertFormNotificationMsg("There are errors associated with issues that are to"
//                + " be migrated to the new workflow association");
        assertTextPresent("There are errors associated with issues that are to"
                + " be migrated to the new workflow association");

        // Assert the correct error messages are found
        assertTextPresent("Unable to determine the current workflow entry for issue &#39;" + projectKey + "-2&#39;");
        assertTextPresent("Unable to determine the current status for issue &#39;" + projectKey + "-3&#39;");
        assertTextPresent("Unable to determine the current workflow entry for issue &#39;" + projectKey + "-6&#39;");
        assertTextPresent("Unable to determine the current issue type for issue &#39;" + projectKey + "-7&#39;");
        assertTextPresent("Unable to determine the current issue type for issue &#39;" + projectKey + "-9&#39;");
        assertTextPresent("Unable to determine the current status for issue &#39;" + projectKey + "-11&#39;");

        // NOTE: the issue verifier produces extra messages:
        // 1. Encountered an error processing the issue 'TST-3' - please refer to the logs.
        // 2. Encountered an error processing the issue 'TST-11' - please refer to the logs.
        // These messages do not really add any value, but the way the code is written it is not easy to take them out.
        // Therefore, we do not assert for their presentce in this test. But do not be scared if you see the messages.
    }

    public static class IssueAssertions
    {
        private String status;
        private List<ChangeLog.HistoryItem> items = Lists.newArrayList();
        private Set<String> transitions = Sets.newHashSet();
        public IssueAssertions status(String status)
        {
            this.status = status;
            return this;
        }

        public IssueAssertions addHistoryItems(ChangeLog.HistoryItem...items)
        {
            this.items.addAll(Arrays.asList(items));
            return this;
        }

        public IssueAssertions addTransitions(String...transtions)
        {
            this.transitions.addAll(Arrays.asList(transtions));
            return this;
        }

        public void assertIssue(Issue issue, boolean exactHistory)
        {
            assertStatus(issue);
            assertTransitions(issue);
            if (exactHistory)
            {
                assertExactChangeHistory(issue);
            }
            else
            {
                assertLastChangeHistory(issue);
            }

        }

        private void assertStatus(Issue issue)
        {
            assertEquals(this.status, issue.fields.status.name());
        }

        private void assertTransitions(Issue issue)
        {
            Set<String> actualTransitions = newHashSet(transform(issue.transitions, new Function<IssueTransitionsMeta.Transition, String>()
            {
                @Override
                public String apply(IssueTransitionsMeta.Transition input)
                {
                    return input.name;
                }
            }));
            assertEquals(this.transitions, actualTransitions);
        }

        private void assertLastChangeHistory(Issue issue)
        {
            List<ChangeLog.History> histories = issue.changelog.getHistories();
            if (items.isEmpty())
            {
                assertTrue(histories.isEmpty());
            }
            else
            {
                assertFalse("Expected at least one change group.", histories.isEmpty());
                assertHistory(histories.get(histories.size() - 1).items, items);
            }
        }

        private void assertExactChangeHistory(Issue issue)
        {
            List<ChangeLog.History> histories = issue.changelog.getHistories();
            if (items.isEmpty())
            {
                assertTrue(histories.isEmpty());
            }
            else
            {
                for (Iterator<ChangeLog.History> i = histories.iterator(); i.hasNext();)
                {
                    ChangeLog.History history = i.next();

                    for (ChangeLog.HistoryItem item : history.getItems())
                    {
                        if ("ProjectImport".equals(item.getField()))
                        {
                            i.remove();
                            break;
                        }
                    }
                }

                assertEquals("Expected only 1 history but got " + histories.size(), 1, histories.size());
                assertHistory(histories.get(0).items, items);
            }
        }

        private void assertHistory(Collection<ChangeLog.HistoryItem> actualItems, Collection<ChangeLog.HistoryItem> expectedItems)
        {
            actualItems = simplifyHistory(actualItems);
            expectedItems = simplifyHistory(expectedItems);

            if (!actualItems.equals(expectedItems))
            {
                fail(String.format("%s != %s.", expectedItems, actualItems));
            }
        }

        private Set<ChangeLog.HistoryItem> simplifyHistory(Iterable<? extends ChangeLog.HistoryItem> simplify)
        {
            return newHashSet(transform(simplify, new Function<ChangeLog.HistoryItem, ChangeLog.HistoryItem>()
            {
                @Override
                public ChangeLog.HistoryItem apply(ChangeLog.HistoryItem input)
                {
                    return new ChangeLog.HistoryItem().setField(input.field)
                            .setToString(input.toString).setFromString(input.fromString);
                }
            }));
        }
    }

    public List<IssueTypeMapping> mappings()
    {
        Pattern issueTypePattern = Pattern.compile("(.*)\\s(\\d+)");
        Pattern existngWorkflowPattern = Pattern.compile("(.*)");
        Pattern targetWorkflowPattern = Pattern.compile("(.*)");
        Pattern affectedIssuesPattern = Pattern.compile("Affected issues:\\s(\\d+)\\sof\\s(\\d+)");

        IssueTypeMapping mapping = null;
        List<IssueTypeMapping> mappings = Lists.newArrayList();

        Node[] tableRows = locator.css("#workflow-mapping-table tbody tr").getNodes();
        for (Node tableRow : tableRows)
        {
            Node[] cells = new CssLocator(tableRow, "td").getNodes();
            String text = getText(cells[0]);
            if (text != null)
            {
                if (mapping != null)
                {
                    mappings.add(mapping);
                }
                mapping = new IssueTypeMapping();

                Matcher matcher = issueTypePattern.matcher(text);
                if (!matcher.matches())
                {
                    throw new RuntimeException("Issue Type column '" + text + "' did not match.");
                }

                mapping.issueType =  matcher.group(1);
                long affectedIssues = Long.parseLong(matcher.group(2));

                text = getText(cells[1]);
                matcher = existngWorkflowPattern.matcher(text);
                if (!matcher.matches())
                {
                    throw new RuntimeException("Existing Workflow column '" + text + "' did not match.");
                }
                mapping.oldWorkflow = matcher.group(1);

                text = getText(cells[3]);
                matcher = targetWorkflowPattern.matcher(text);
                if (!matcher.matches())
                {
                    throw new RuntimeException("Affected Workflow column '" + text + "' did not match.");
                }
                mapping.newWorkflow = matcher.group(1);

                String title = getAttribute(new CssLocator(cells[0], ".status-issue-count").getNode(), "title");
                matcher = affectedIssuesPattern.matcher(title);
                if (!matcher.matches())
                {
                    throw new RuntimeException("Affected issues title '" + title + "' not as expected.");
                }

                long actualAffectedIssues = Long.parseLong(matcher.group(1));
                if (affectedIssues != actualAffectedIssues)
                {
                    throw new RuntimeException("Expected affected issues of '" + affectedIssues + "' but got value of '" + actualAffectedIssues + "'.");
                }
                mapping.affectedIssues = actualAffectedIssues;
                mapping.totalIssue = Long.parseLong(matcher.group(2));
            }
            else
            {
                StatusMapping statusMapping =  new StatusMapping();
                statusMapping.oldStatus = getText(cells[1]);
                for (Node option : new CssLocator(cells[3], "option").getNodes())
                {
                    statusMapping.newStatuses.add(getText(option));
                }

                assert mapping != null;

                mapping.statusMappings.add(statusMapping);
            }
        }

        if (mapping != null)
        {
            mappings.add(mapping);
        }

        return mappings;
    }

    private String getAttribute(Node cell, String attributeName)
    {
        Node title = cell.getAttributes().getNamedItem(attributeName);
        return title != null ? StringUtils.stripToNull(title.getNodeValue()) : null;
    }

    private static String getText(Node option)
    {
        return StringUtils.stripToNull(DomKit.getCollapsedText(option));
    }

    private static class IssueTypeMapping
    {
        private String issueType;
        private String oldWorkflow;
        private String newWorkflow;
        private long totalIssue;
        private long affectedIssues;
        private List<StatusMapping> statusMappings = Lists.newArrayList();

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    private static class StatusMapping
    {
        private String oldStatus;
        private List<String> newStatuses = Lists.newArrayList();

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
