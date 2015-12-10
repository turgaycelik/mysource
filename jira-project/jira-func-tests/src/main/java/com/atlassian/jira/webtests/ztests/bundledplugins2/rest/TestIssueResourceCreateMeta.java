package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.restclient.FieldMetaData;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssueCreateMeta;
import com.atlassian.jira.testkit.client.restclient.IssueCreateMeta.Expand;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.util.StringList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceCreateMeta extends RestFuncTest
{
    private IssueClient issueClient;

    private static final Set<String> defaultRequiredFields = ImmutableSet.<String>builder()
            .add("project")
            .add("versions")
            .add("components")
            .add("description")
            .add("duedate")
            .add("environment")
            .add("fixVersions")
            .add("issuetype")
            .add("labels")
            .add("worklog")
            .add("priority")
            .add("reporter")
            .add("security")
            .add("summary")
            .add("timetracking")
            .build();

    private static final Set<String> hasDefaultValueFields = ImmutableSet.<String>builder()
            .add("assignee")
            .add("priority")
            .build();

    private static final Set<String> subtaskRequiredFields = ImmutableSet.<String>builder()
            .addAll(defaultRequiredFields)
            .add("parent")
            .build();

    private static final Set<String> testBugRequiredFields = ImmutableSet.<String>builder()
            .add("project")
            .add("versions")
            .add("description")
            .add("issuetype")
            .add("priority")
            .add("reporter")
            .add("summary")
            .add("customfield_10000")
            .build();

    private static final Map<String, IssueCreateMeta.JsonType> sharedFieldTypes = ImmutableMap.<String, IssueCreateMeta.JsonType>builder()
            .put("project", IssueCreateMeta.JsonType.system("project", "project"))
            .put("versions", IssueCreateMeta.JsonType.systemArray("version", "versions"))
            .put("assignee", IssueCreateMeta.JsonType.system("user", "assignee"))
            .put("attachment", IssueCreateMeta.JsonType.systemArray("attachment", "attachment"))
//            .put("comment", IssueCreateMeta.JsonType.systemArray("comment", "comment"))
            .put("description", IssueCreateMeta.JsonType.system("string", "description"))
            .put("environment", IssueCreateMeta.JsonType.system("string", "environment"))
            .put("fixVersions", IssueCreateMeta.JsonType.systemArray("version", "fixVersions"))
            .put("issuetype", IssueCreateMeta.JsonType.system("issuetype", "issuetype"))
            .put("issuelinks", IssueCreateMeta.JsonType.systemArray("issuelinks", "issuelinks"))
            .put("worklog", IssueCreateMeta.JsonType.systemArray("worklog", "worklog"))
            .put("priority", IssueCreateMeta.JsonType.system("priority", "priority"))
            .put("reporter", IssueCreateMeta.JsonType.system("user", "reporter"))
            .put("resolution", IssueCreateMeta.JsonType.system("resolution", "resolution"))
            .put("security", IssueCreateMeta.JsonType.system("securitylevel", "security"))
            .put("summary", IssueCreateMeta.JsonType.system("string", "summary"))
            .put("customfield_10000", IssueCreateMeta.JsonType.custom("date", "com.atlassian.jira.plugin.system.customfieldtypes:datepicker", 10000L))
            .put("customfield_10001", IssueCreateMeta.JsonType.custom("datetime", "com.atlassian.jira.plugin.system.customfieldtypes:datetime", 10001L))
            .build();

    private static final Map<String, IssueCreateMeta.JsonType> defaultFieldTypes = ImmutableMap.<String, IssueCreateMeta.JsonType>builder()
            .putAll(sharedFieldTypes)
            .put("duedate", IssueCreateMeta.JsonType.system("date", "duedate"))
            .put("components", IssueCreateMeta.JsonType.systemArray("component", "components"))
            .put("labels", IssueCreateMeta.JsonType.systemArray("string", "labels"))
            .put("timetracking",IssueCreateMeta.JsonType.system("timetracking", "timetracking"))
            .build();

    private static final Map<String, IssueCreateMeta.JsonType> subTaskFieldTypes = ImmutableMap.<String, IssueCreateMeta.JsonType>builder()
            .putAll(defaultFieldTypes)
            .put("parent", IssueCreateMeta.JsonType.system("issuelink", "parent"))
            .build();

    private static final Map<String, IssueCreateMeta.JsonType> testBugFieldTypes = ImmutableMap.<String, IssueCreateMeta.JsonType>builder()
            .putAll(sharedFieldTypes)
            .build();

    private static final Map<String, String> fieldsNamesValues = ImmutableMap.<String, String>builder()
            .put("assignee", "Assignee")
            .put("attachment", "Attachment")
            .put("components", "Component/s")
            .put("customfield_10000", "datePickerCF")
            .put("customfield_10001", "dateTimeCF")
            .put("description", "Description")
            .put("duedate", "Due Date")
            .put("environment", "Environment")
            .put("fixVersions", "Fix Version/s")
            .put("issuelinks", "Linked Issues")
            .put("issuetype", "Issue Type")
            .put("labels", "Labels")
            .put("parent", "Parent")
            .put("priority", "Priority")
            .put("project", "Project")
            .put("reporter", "Reporter")
            .put("resolution", "Resolution")
            .put("security", "Security Level")
            .put("summary", "Summary")
            .put("timetracking", "Time Tracking")
            .put("versions", "Affects Version/s")
            .put("worklog", "Log Work")
            .build();

    public void testWithNullParams() throws Exception
    {
        // Should return all projects and issue types visible to the user
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, null, null, null, Expand.fields);

        assertEquals(2, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        final IssueCreateMeta.Project project2 = meta.projects.get(1);
        assertTestProject(project2);

        assertEquals(3, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2Bug = project2.issuetypes.get(0);
        final IssueCreateMeta.IssueType project2Improvement = project2.issuetypes.get(1);
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(2);
        assertBug(project2Bug);
        assertImprovement(project2Improvement);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project1Bug.fields, defaultRequiredFields);
        assertRequiredFields(project1NewFeature.fields, defaultRequiredFields);
        assertRequiredFields(project1Task.fields, defaultRequiredFields);
        assertRequiredFields(project1Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project1SubTask.fields, subtaskRequiredFields);

        assertRequiredFields(project2Bug.fields, testBugRequiredFields);
        assertRequiredFields(project2Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);

        assertFieldNamesAndTypes(project2Bug.fields, testBugFieldTypes);
        assertFieldNamesAndTypes(project2Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);
    }

    public void testWithProjectIds() throws Exception
    {
        // Should return just the project specified, and all its issue types
        final List<StringList> projectIds = Arrays.asList(new StringList("10000"));
        final IssueCreateMeta meta = issueClient.getCreateMeta(projectIds, null, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project2 = meta.projects.get(0);
        assertTestProject(project2);

        assertEquals(3, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2Bug = project2.issuetypes.get(0);
        final IssueCreateMeta.IssueType project2Improvement = project2.issuetypes.get(1);
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(2);
        assertBug(project2Bug);
        assertImprovement(project2Improvement);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project2Bug.fields, testBugRequiredFields);
        assertRequiredFields(project2Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project2Bug.fields, testBugFieldTypes);
        assertFieldNamesAndTypes(project2Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);
    }

    public void testWithProjectKeys() throws Exception
    {
        // Should return just the project specified, and all its issue types
        final List<StringList> projectKeys = Arrays.asList(new StringList("PEXPRESS"));
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, projectKeys, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        assertRequiredFields(project1Bug.fields, defaultRequiredFields);
        assertRequiredFields(project1NewFeature.fields, defaultRequiredFields);
        assertRequiredFields(project1Task.fields, defaultRequiredFields);
        assertRequiredFields(project1Improvement.fields, defaultRequiredFields);
        assertRequiredFields(project1SubTask.fields, subtaskRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);
    }

    public void testWithIssueTypes() throws Exception
    {
        // Should return all projects visible to the user, and the specified issue types if the project has them
        subtestByIssueTypes(Arrays.asList(new StringList("2,3")), null);
        subtestByIssueTypes(Arrays.asList(new StringList(Arrays.asList("2", "3"))), null);
        subtestByIssueTypes(Arrays.asList(new StringList("2")), Arrays.asList("Task"));
        subtestByIssueTypes(Arrays.asList(new StringList("2,3")), Arrays.asList("Task"));
        subtestByIssueTypes(null, Arrays.asList("New Feature", "Task"));
    }

    private void subtestByIssueTypes(List<StringList> issueTypeIds, List<String> issueTypeNames)
    {
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, null, issueTypeIds, issueTypeNames, Expand.fields);

        assertEquals(2, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(2, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(1);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);

        final IssueCreateMeta.Project project2 = meta.projects.get(1);
        assertTestProject(project2);

        assertEquals(1, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(0);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project1NewFeature.fields, defaultRequiredFields);
        assertRequiredFields(project1Task.fields, defaultRequiredFields);
        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);

        assertDefaultValueFields(project1NewFeature.fields, hasDefaultValueFields);
        assertDefaultValueFields(project1Task.fields, hasDefaultValueFields);
        assertDefaultValueFields(project2NewFeature.fields, hasDefaultValueFields);
    }

    public void testWithAllParams() throws Exception
    {
        // Filter on project (union of ids and keys, avoiding duplicates) and issue type
        final List<StringList> projectIds = Arrays.asList(new StringList("10000"));
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST"));
        final List<StringList> issueTypeIds = Arrays.asList(new StringList("2"));
        final List<String> issueTypeNames = Arrays.asList("Task");
        final IssueCreateMeta meta = issueClient.getCreateMeta(projectIds, projectKeys, issueTypeIds, issueTypeNames, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project2 = meta.projects.get(0);
        assertTestProject(project2);

        assertEquals(1, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(0);
        assertNewFeature(project2NewFeature);

        assertRequiredFields(project2NewFeature.fields, defaultRequiredFields);

        assertFieldNamesAndTypes(project2NewFeature.fields, defaultFieldTypes);
    }

    public void testWithBadParams() throws Exception
    {
        // Invalid projects and issue types
        final List<StringList> projectIds = Arrays.asList(new StringList("-1,10000"));
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST,ABC,XYZ"));
        final List<StringList> issueTypeIds = Arrays.asList(new StringList("1,300"));
        final List<String> issueTypeNames = Arrays.asList("Bug", "ASDASD");
        final IssueCreateMeta meta = issueClient.getCreateMeta(projectIds, projectKeys, issueTypeIds, issueTypeNames, Expand.fields);

        // okay request, just limited results
        assertEquals(1, meta.projects.size());
        final IssueCreateMeta.Project project = meta.projects.get(0);
        assertEquals("10000", project.id);
        assertEquals(1, project.issuetypes.size());
        final IssueCreateMeta.IssueType issueType = project.issuetypes.get(0);
        assertEquals("1", issueType.id);
    }

    public void testWithNoProjectBrowsePermission() throws Exception
    {
        // User fry cannot see the TST project
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST"));
        final IssueCreateMeta meta = issueClient.loginAs("fry").getCreateMeta(null, projectKeys, null, null, Expand.fields);

        assertEquals(0, meta.projects.size()); // no matches
    }

    public void testWithLimitedProjectBrowsePermission() throws Exception
    {
        // User fry cannot browse the TST project, should only get the PEXPRESS project
        final IssueCreateMeta meta = issueClient.loginAs("fry").getCreateMeta(null, null, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        final Set<String> frysFields = ImmutableSet.<String>builder()
                .add("project")
                .add("versions")
                .add("components")
                .add("description")
                .add("environment")
                .add("issuetype")
                .add("labels")
                .add("priority")
                .add("security")
                .add("summary")
                .add("timetracking")
                .build();

        final Set<String> frysSubtaskRequiredFields = ImmutableSet.<String>builder()
                .addAll(frysFields)
                .add("parent")
                .build();

        final Map<String, IssueCreateMeta.JsonType> defaultFieldTypes = ImmutableMap.<String, IssueCreateMeta.JsonType>builder()
                .put("project", IssueCreateMeta.JsonType.system("project", "project"))
                .put("versions", IssueCreateMeta.JsonType.systemArray("version", "versions"))
                .put("attachment", IssueCreateMeta.JsonType.systemArray("attachment", "attachment"))
//                .put("comment", IssueCreateMeta.JsonType.systemArray("comment", "comment"))
                .put("description", IssueCreateMeta.JsonType.system("string", "description"))
                .put("environment", IssueCreateMeta.JsonType.system("string", "environment"))
                .put("issuetype", IssueCreateMeta.JsonType.system("issuetype", "issuetype"))
                .put("priority", IssueCreateMeta.JsonType.system("priority", "priority"))
                .put("resolution", IssueCreateMeta.JsonType.system("resolution", "resolution"))
                .put("security", IssueCreateMeta.JsonType.system("securitylevel", "security"))
                .put("summary", IssueCreateMeta.JsonType.system("string", "summary"))
                .put("customfield_10000", IssueCreateMeta.JsonType.custom("date", "com.atlassian.jira.plugin.system.customfieldtypes:datepicker", 10000L))
                .put("customfield_10001", IssueCreateMeta.JsonType.custom("datetime", "com.atlassian.jira.plugin.system.customfieldtypes:datetime", 10001L))
                .put("components", IssueCreateMeta.JsonType.systemArray("component", "components"))
                .put("labels", IssueCreateMeta.JsonType.systemArray("string", "labels"))
                .put("timetracking", IssueCreateMeta.JsonType.system("timetracking", "timetracking"))
                .put("issuelinks", IssueCreateMeta.JsonType.systemArray("issuelinks", "issuelinks"))
                .build();

        final Map<String, IssueCreateMeta.JsonType> subTaskFieldTypes = ImmutableMap.<String, IssueCreateMeta.JsonType>builder()
                .putAll(defaultFieldTypes)
                .put("parent", IssueCreateMeta.JsonType.system("issuelink", "parent"))
                .build();

        assertRequiredFields(project1Bug.fields, frysFields);
        assertRequiredFields(project1NewFeature.fields, frysFields);
        assertRequiredFields(project1Task.fields, frysFields);
        assertRequiredFields(project1Improvement.fields, frysFields);
        assertRequiredFields(project1SubTask.fields, frysSubtaskRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, defaultFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);
    }

    public void testWithNoProjectCreatePermission() throws Exception
    {
        // User farnsworth cannot create issues in the TST project
        final List<StringList> projectKeys = Arrays.asList(new StringList("TST"));
        final IssueCreateMeta meta = issueClient.loginAs("farnsworth").getCreateMeta(null, projectKeys, null, null, Expand.fields);

        assertEquals(0, meta.projects.size()); // no matches
    }

    public void testWithLimitedProjectCreatePermission() throws Exception
    {
        // User farnsworth cannot create issues in the TST project, should only get the PEXPRESS project
        final IssueCreateMeta meta = issueClient.loginAs("farnsworth").getCreateMeta(null, null, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        Set<String> farnsworthsFields = new HashSet<String>(defaultRequiredFields);
        farnsworthsFields.remove("reporter");

        final Set<String> subtaskRequiredFields = ImmutableSet.<String>builder()
                .addAll(farnsworthsFields)
                .add("parent")
                .build();

        final Map<String, IssueCreateMeta.JsonType> farnsworthFieldTypes = new HashMap<String, IssueCreateMeta.JsonType>(defaultFieldTypes);
        farnsworthFieldTypes.remove("reporter");

        final Map<String, IssueCreateMeta.JsonType> subTaskFieldTypes = ImmutableMap.<String, IssueCreateMeta.JsonType>builder()
                .putAll(farnsworthFieldTypes)
                .put("parent", IssueCreateMeta.JsonType.system("issuelink", "parent"))
                .build();

        assertRequiredFields(project1Bug.fields, farnsworthsFields);
        assertRequiredFields(project1NewFeature.fields, farnsworthsFields);
        assertRequiredFields(project1Task.fields, farnsworthsFields);
        assertRequiredFields(project1Improvement.fields, farnsworthsFields);
        assertRequiredFields(project1SubTask.fields, subtaskRequiredFields);

        assertFieldNamesAndTypes(project1Bug.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1NewFeature.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1Task.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1Improvement.fields, farnsworthFieldTypes);
        assertFieldNamesAndTypes(project1SubTask.fields, subTaskFieldTypes);
    }

    public void testWithoutFields() throws Exception
    {
        // Should not return the fields
        final IssueCreateMeta meta = issueClient.getCreateMeta(null, null, null, null);

        assertEquals(2, meta.projects.size());

        final IssueCreateMeta.Project project1 = meta.projects.get(0);
        assertPlanetExpressProject(project1);

        assertEquals(5, project1.issuetypes.size());
        final IssueCreateMeta.IssueType project1Bug = project1.issuetypes.get(0);
        final IssueCreateMeta.IssueType project1NewFeature = project1.issuetypes.get(1);
        final IssueCreateMeta.IssueType project1Task = project1.issuetypes.get(2);
        final IssueCreateMeta.IssueType project1Improvement = project1.issuetypes.get(3);
        final IssueCreateMeta.IssueType project1SubTask = project1.issuetypes.get(4);
        assertBug(project1Bug);
        assertNewFeature(project1NewFeature);
        assertTask(project1Task);
        assertImprovement(project1Improvement);
        assertSubTask(project1SubTask);

        final IssueCreateMeta.Project project2 = meta.projects.get(1);
        assertTestProject(project2);

        assertEquals(3, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2Bug = project2.issuetypes.get(0);
        final IssueCreateMeta.IssueType project2Improvement = project2.issuetypes.get(1);
        final IssueCreateMeta.IssueType project2NewFeature = project2.issuetypes.get(2);
        assertBug(project2Bug);
        assertImprovement(project2Improvement);
        assertNewFeature(project2NewFeature);

        assertNull(project1Bug.fields);
        assertNull(project1NewFeature.fields);
        assertNull(project1Task.fields);
        assertNull(project1Improvement.fields);
        assertNull(project1SubTask.fields);

        assertNull(project2Bug.fields);
        assertNull(project2Improvement.fields);
        assertNull(project2NewFeature.fields);
    }

    public void testAllowedValuesForFields() throws Exception
    {
        // Add fields which we need to test
        final String cfProjectName = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:project", "Project Custom Field");
        final String cfVersionName = administration.customFields().addCustomField("com.atlassian." + "jira.plugin.system.customfieldtypes:version", "Version Custom Field");

        // Should return just the project specified, and all its issue types
        final List<StringList> projectIds = Arrays.asList(new StringList("10000"));
        final IssueCreateMeta meta = issueClient.getCreateMeta(projectIds, null, null, null, Expand.fields);

        assertEquals(1, meta.projects.size());

        final IssueCreateMeta.Project project2 = meta.projects.get(0);
        assertTestProject(project2);

        assertEquals(3, project2.issuetypes.size());
        final IssueCreateMeta.IssueType project2Bug = project2.issuetypes.get(0);
        assertBug(project2Bug);
        assertRequiredFields(project2Bug.fields, testBugRequiredFields);

        final String[] fieldsNames = new String[]
                {
                        cfProjectName, cfVersionName, "issuetype", "security", "fixVersions", "resolution",
                        "project", "versions", "priority"
                };

        for (String fieldName : fieldsNames)
        {
            final FieldMetaData fieldMetaData = project2Bug.fields.get(fieldName);
            assertNotNull(String.format("Field with name '%s' doesn't exists.", fieldName), fieldMetaData);
            assertFieldHasNonIterableAllowedValuesItems(fieldMetaData);
        }
    }

    private void assertFieldHasNonIterableAllowedValuesItems(FieldMetaData fieldMetaData) {
        final List<Object> allowedValues = fieldMetaData.allowedValues;
        assertNotNull(String.format("Field '%s' has null allowedValues!", fieldMetaData.name), allowedValues);

        assertFalse(String.format("Field '%s' has empty allowedValues!", fieldMetaData.name), allowedValues.isEmpty());

        for (Object value : allowedValues)
        {
            assertFalse(String.format("Field '%s' has value that is an instance of Iterable! Value: %s", fieldMetaData.name, value),
                    value instanceof Iterable);
        }
    }

    private void assertPlanetExpressProject(final IssueCreateMeta.Project project)
    {
        assertEquals("10001", project.id);
        assertEquals("PEXPRESS", project.key);
        assertEquals("Planet Express", project.name);
        assertEquals(getBaseUrl() + "/rest/api/2/project/10001", project.self);
        assertThat(project.avatarUrls, equalTo(createProjectAvatarUrls(10001L, 10011L)));
    }

    private void assertTestProject(final IssueCreateMeta.Project project)
    {
        assertEquals("10000", project.id);
        assertEquals("TST", project.key);
        assertEquals("Test", project.name);
        assertEquals(getBaseUrl() + "/rest/api/2/project/10000", project.self);
        assertThat(project.avatarUrls, equalTo(createProjectAvatarUrls(10000L, 10011L)));
    }

    private void assertBug(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("1", issueType.id);
        assertEquals("Bug", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/issuetypes/bug.png", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/1", issueType.self);
    }

    private void assertNewFeature(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("2", issueType.id);
        assertEquals("New Feature", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/issuetypes/newfeature.png", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/2", issueType.self);
    }

    private void assertTask(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("3", issueType.id);
        assertEquals("Task", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/issuetypes/task.png", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/3", issueType.self);
    }

    private void assertImprovement(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("4", issueType.id);
        assertEquals("Improvement", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/issuetypes/improvement.png", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/4", issueType.self);
    }

    private void assertSubTask(final IssueCreateMeta.IssueType issueType)
    {
        assertEquals("5", issueType.id);
        assertEquals("Sub-task", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/issuetypes/subtask_alternate.png", issueType.iconUrl);
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/5", issueType.self);
    }

    private void assertFieldNamesAndTypes(final Map<String, FieldMetaData> fields, final Map<String, IssueCreateMeta.JsonType> fieldNamesToTypes)
    {
        for (final Map.Entry<String, FieldMetaData> field : fields.entrySet())
        {
            assertTrue("Contains field that should not be visible: " + field.getKey(),
                    fieldNamesToTypes.containsKey(field.getKey()));

            assertEquals("Incorrect field type for field: " + field.getKey(),
                    fieldNamesToTypes.get(field.getKey()), field.getValue().schema);

            assertEquals("Incorrect name for field: " + field.getKey(),
                    fieldsNamesValues.get(field.getKey()), field.getValue().name);
        }
        for (String expectedKey : fieldNamesToTypes.keySet())
        {
            assertTrue("Could not find required key " + expectedKey, fields.containsKey(expectedKey));
        }
    }

    private void assertRequiredFields(final Map<String, FieldMetaData> fields, Set<String> requiredFieldNames)
    {
        for (final Map.Entry<String, FieldMetaData> field : fields.entrySet())
        {
            if (field.getValue().required)
            {
                assertTrue("Field \"" + field.getKey() + "\" should be required",
                        requiredFieldNames.contains(field.getKey()));
            }
            else
            {
                assertFalse("Field \"" + field.getKey() + "\" should not be required",
                        requiredFieldNames.contains(field.getKey()));
            }
        }
        for (String expectedKey : requiredFieldNames)
        {
            assertTrue("Could not find required key " + expectedKey, fields.containsKey(expectedKey));
        }
    }

    private void assertDefaultValueFields(final Map<String, FieldMetaData> fields, Set<String> defaultValueFieldNames)
    {
        for (final Map.Entry<String, FieldMetaData> field : fields.entrySet())
        {
            if (field.getValue().hasDefaultValue)
            {
                assertTrue("Field \"" + field.getKey() + "\" should have default value", defaultValueFieldNames.contains(field.getKey()));
            }
            else
            {
                assertFalse("Field \"" + field.getKey() + "\" should not have default value", defaultValueFieldNames.contains(field.getKey()));
            }
        }
        for (String expectedKey : defaultValueFieldNames)
        {
            assertTrue("Could not find default value key " + expectedKey, fields.containsKey(expectedKey));
        }
    }

    private Map<String, String> createProjectAvatarUrls(final Long projectId, final Long avatarId)
    {
        return ImmutableMap.<String, String>builder()
                .put("24x24", getBaseUrlPlus("secure/projectavatar?size=small&pid="+projectId+"&avatarId="+avatarId))
                .put("16x16", getBaseUrlPlus("secure/projectavatar?size=xsmall&pid="+projectId+"&avatarId="+avatarId))
                .put("32x32", getBaseUrlPlus("secure/projectavatar?size=medium&pid="+projectId+"&avatarId="+avatarId))
                .put("48x48", getBaseUrlPlus("secure/projectavatar?pid="+projectId+"&avatarId="+avatarId))
// TODO JRADEV-20790 - Re-enable the larger avatar sizes.
//                .put("64x64", getBaseUrlPlus("secure/projectavatar?size=xlarge&pid="+projectId+"&avatarId="+avatarId))
//                .put("96x96", getBaseUrlPlus("secure/projectavatar?size=xxlarge&pid="+projectId+"&avatarId="+avatarId))
//                .put("128x128", getBaseUrlPlus("secure/projectavatar?size=xxxlarge&pid="+projectId+"&avatarId="+avatarId))
//                .put("192x192", getBaseUrlPlus("secure/projectavatar?size=xxlarge%402x&pid="+projectId+"&avatarId="+avatarId)) // %40 == "@"
//                .put("256x256", getBaseUrlPlus("secure/projectavatar?size=xxxlarge%402x&pid="+projectId+"&avatarId="+avatarId))
                .build();
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceCreateMeta.xml");
    }
}
