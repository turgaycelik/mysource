package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.rest.api.issue.TimeTracking;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.Worklog;
import com.meterware.httpunit.WebResponse;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.api.issue.ResourceRef.withId;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withKey;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceCreate extends RestFuncTest
{
    final long DATE_CF = 10000L;
    final long DATETIME_CF = 10001L;
    final long CASCADING_SELECT_CF = 10010L;
    final long TEXT_AREA_CF = 10020L;
    final long TEXT_FIELD_CF = 10021L;
    final long GROUP_PICKER = 10030L;
    final long GROUPS_PICKER = 10031L;
    final long SINGLE_VERSION_CF = 10040L;
    final long MULTI_VERSION_CF = 10041L;

    private IssueClient issueClient;

    public void testCreateIssueWithRequiredSystemFields() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final IssueUpdateRequest request = getRequiredSystemFields();
        final IssueFields fields = request.fields();
        final IssueCreateResponse created = issueClient.loginAs("admin").create(request);

        // read back the fields and compare it
        final Issue newIssue = issueClient.get(created.key());
        assertThat(created.self, equalTo(newIssue.self));
        assertThat(newIssue.fields.project.key, equalTo("TST"));
        assertThat(newIssue.fields.issuetype.name, equalTo("Bug"));
        assertThat(newIssue.fields.priority.name(), equalTo("Blocker"));
        assertThat(newIssue.fields.reporter.name, equalTo(fields.reporter().name()));
        assertThat(newIssue.fields.assignee.name, equalTo(fields.assignee().name()));
        assertThat(newIssue.fields.summary, equalTo(fields.summary()));
        assertThat(newIssue.fields.labels, equalTo(fields.labels()));
        assertThat(newIssue.fields.timetracking.originalEstimate, equalTo(fields.timeTracking().originalEstimate));
        assertThat(newIssue.fields.timetracking.remainingEstimate, equalTo("1h 10m"));
        assertThat(newIssue.fields.security.name, equalTo("lvl1"));
        assertThat(newIssue.fields.versions.get(0).name, equalTo("v1"));
        assertThat(newIssue.fields.environment, equalTo(fields.environment()));
        assertThat(newIssue.fields.description, equalTo(fields.description()));
        assertThat(newIssue.fields.duedate, equalTo(fields.dueDate()));
        assertThat(newIssue.fields.fixVersions.get(0).name, equalTo("v2"));
        assertThat(newIssue.fields.components.get(0).name, equalTo("comp1"));

        assertEquals(1, newIssue.fields.worklog.worklogs.size());
        final Worklog worklog = newIssue.fields.worklog.worklogs.get(0);

        assertThat(toDateTime(worklog.started), equalTo(toDateTime("2011-07-05T11:05:00.000+0000")));
        assertThat(worklog.timeSpent, equalTo("50m"));
    }

    public void testCreateIssueWithInvalidIssueType() 
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final IssueUpdateRequest request = getRequiredSystemFields();
        //specify an invalid issuetype id.
        request.fields().issueType = ResourceRef.withId("-99999");
        final Response response = issueClient.loginAs("admin").getResponse(request);
        
        assertThat(response.statusCode, equalTo(400));
        assertNotNull("No error message for invalid issueType", response.entity.errors.get("issuetype"));
    }
    
    public void testCreateIssueWithDefaultedSystemFields() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final IssueUpdateRequest request = getRequiredSystemFields("priority", "assignee", "reporter", "securityLevel");
        final IssueFields fields = request.fields();
        final IssueCreateResponse created = issueClient.loginAs("admin").create(request);

        // read back the fields and compare it
        final Issue newIssue = issueClient.get(created.key());
        assertThat(created.self, equalTo(newIssue.self));
        assertThat(newIssue.fields.project.key, equalTo("TST"));
        assertThat(newIssue.fields.issuetype.name, equalTo("Bug"));
        assertThat(newIssue.fields.priority.name(), equalTo("Major"));  // Default is not specifically set so becomes "Major"
        assertThat(newIssue.fields.reporter.name, equalTo("admin"));  // Default is not set so becomes logged in user
        assertThat(newIssue.fields.assignee.name, equalTo("admin"));  // Default is the project lead = "admin"
        assertThat(newIssue.fields.summary, equalTo(fields.summary()));
        assertThat(newIssue.fields.labels, equalTo(fields.labels()));
        assertThat(newIssue.fields.timetracking.originalEstimate, equalTo(fields.timeTracking().originalEstimate));
        assertThat(newIssue.fields.timetracking.remainingEstimate, equalTo("1h 10m"));
        assertThat(newIssue.fields.security.name, equalTo("lvl1"));  // Default is specifically set to "lvl1"
        assertThat(newIssue.fields.versions.get(0).name, equalTo("v1"));
        assertThat(newIssue.fields.environment, equalTo(fields.environment()));
        assertThat(newIssue.fields.description, equalTo(fields.description()));
        assertThat(newIssue.fields.duedate, equalTo(fields.dueDate()));
        assertThat(newIssue.fields.fixVersions.get(0).name, equalTo("v2"));
        assertThat(newIssue.fields.components.get(0).name, equalTo("comp1"));

        assertEquals(1, newIssue.fields.worklog.worklogs.size());
        final Worklog worklog = newIssue.fields.worklog.worklogs.get(0);

        assertThat(toDateTime(worklog.started), equalTo(toDateTime("2011-07-05T11:05:00.000+0000")));
        assertThat(worklog.timeSpent, equalTo("50m"));
    }

    private Date toDateTime(String time)
    {
        try
        {
            String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
            return new SimpleDateFormat(TIME_FORMAT).parse(time);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void testCreateIssueWithRequiredCustomFields() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredCustomFields.xml");

        final IssueFields fields = getRequiredCustomFields();
        final IssueCreateResponse created = issueClient.loginAs("admin").create(new IssueUpdateRequest().fields(fields));

        // read back the fields and compare it
        Issue newIssue = issueClient.get(created.key());
        assertThat(newIssue.fields.<String>get("customfield_" + DATE_CF), equalTo("1981-06-09"));
        assertThat(newIssue.fields.<String>get("customfield_" + DATETIME_CF), startsWith("2011-07-06T15:25:00.000"));
        Map<String, Object> cascadingSelect = newIssue.fields.get("customfield_" + CASCADING_SELECT_CF);
        assertThat((String) cascadingSelect.get("value"), equalTo("level1_a"));
        Map<String, Object> child = (Map<String, Object>) cascadingSelect.get("child");
        assertThat((String) child.get("value"), equalTo("level2_c"));
        assertThat(newIssue.fields.<String>get("customfield_" + TEXT_FIELD_CF), equalTo("this is a text field"));
        assertThat(newIssue.fields.<String>get("customfield_" + TEXT_AREA_CF), equalTo("this is a text area. big text."));
        Map<String, Object> group = newIssue.fields.get("customfield_" + GROUP_PICKER);
        assertThat((String) group.get("name"), equalTo("jira-developers"));
        List<Map<String, Object>> groups = newIssue.fields.get("customfield_" + GROUPS_PICKER);
        Map<String, Object> group1 = groups.get(0);
        Map<String, Object> group2 = groups.get(1);
        assertThat((String) group1.get("name"), equalTo("jira-administrators"));
        assertThat((String) group2.get("name"), equalTo("jira-users"));

        ObjectMapper MAPPER = new ObjectMapper();
        Version version = MAPPER.convertValue(newIssue.fields.get("customfield_" + SINGLE_VERSION_CF), Version.class);
        assertThat(version.name, equalTo("v1"));

        List versions = newIssue.fields.get("customfield_" + MULTI_VERSION_CF);
        Version version1 = MAPPER.convertValue(versions.get(0), Version.class);
        Version version2 = MAPPER.convertValue(versions.get(1), Version.class);
        assertThat(version1.name, equalTo("v1"));
        assertThat(version2.name, equalTo("v2"));
    }

    public void testCreateIssueWithoutRequiredSystemFields() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final List<String> requiredFields = Arrays.asList("project", "issuetype",
                "summary", "labels", "description", "fixVersions", "versions", "components");

        for (final String requiredField : requiredFields)
        {
            // Neglect one of the required fields
            final IssueUpdateRequest request = getRequiredSystemFields(requiredField);
            final Response response = issueClient.loginAs("admin").getResponse(request);

            // Expecting Bad Request error
            assertEquals("Create without required field: " + requiredField, 400, response.statusCode);
            assertNotNull("No error message for missing field: " + requiredField, response.entity.errors.get(requiredField));
        }
    }

    public void testCreateIssueWithBadSystemFieldValue() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        // Provide invalid project
        final IssueUpdateRequest request = getRequiredSystemFields();
        final IssueFields fields = request.fields();
        fields.project(withId("-1"));
        final Response response = issueClient.loginAs("admin").getResponse(request);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertNotNull("No error message for invalid project", response.entity.errors.get("project"));
    }

    public void testCreateIssueWithoutRequiredCustomFields() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredCustomFields.xml");

        final List<String> requiredFields = Arrays.asList("customfield_" + DATE_CF, "customfield_" + DATETIME_CF,
                "customfield_" + CASCADING_SELECT_CF, "customfield_" + TEXT_FIELD_CF, "customfield_" + TEXT_AREA_CF,
                "customfield_" + GROUP_PICKER, "customfield_" + GROUPS_PICKER, "customfield_" + SINGLE_VERSION_CF, "customfield_" + MULTI_VERSION_CF);

        for (final String requiredField : requiredFields)
        {
            // Neglect one of the required custom fields
            final IssueUpdateRequest request = getRequiredSystemFields(requiredField);
            final Response response = issueClient.loginAs("admin").getResponse(request);

            // Expecting Bad Request error
            assertEquals(400, response.statusCode);
            assertNotNull("No error message for missing field: " + requiredField, response.entity.errors.get(requiredField));
        }
    }

    public void testCreateIssueWithNames() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final IssueUpdateRequest request = getSystemFieldsByName();
        final IssueFields fields = request.fields();
        issueClient.loginAs("admin");
        final IssueCreateResponse created = issueClient.create(request);

        // read back the fields and compare it
        final Issue newIssue = issueClient.get(created.key());
        assertThat(created.self, equalTo(newIssue.self));
        assertThat(newIssue.fields.project.key, equalTo("TST"));
        assertThat(newIssue.fields.issuetype.name, equalTo("Bug"));
        assertThat(newIssue.fields.priority.name(), equalTo("Blocker"));
        assertThat(newIssue.fields.reporter.name, equalTo(fields.reporter().name()));
        assertThat(newIssue.fields.assignee.name, equalTo(fields.assignee().name()));
        assertThat(newIssue.fields.summary, equalTo(fields.summary()));
        assertThat(newIssue.fields.labels, equalTo(fields.labels()));
        assertThat(newIssue.fields.timetracking.originalEstimate, equalTo(fields.timeTracking().originalEstimate));
        assertThat(newIssue.fields.timetracking.remainingEstimate, equalTo("1h 10m"));
        assertThat(newIssue.fields.security.name, equalTo("lvl1"));
        assertThat(newIssue.fields.versions.get(0).name, equalTo("v1"));
        assertThat(newIssue.fields.environment, equalTo(fields.environment()));
        assertThat(newIssue.fields.description, equalTo(fields.description()));
        assertThat(newIssue.fields.duedate, equalTo(fields.dueDate()));
        assertThat(newIssue.fields.fixVersions.get(0).name, equalTo("v2"));
        assertThat(newIssue.fields.components.get(0).name, equalTo("comp1"));

        assertEquals(1, newIssue.fields.worklog.worklogs.size());
        final Worklog worklog = newIssue.fields.worklog.worklogs.get(0);

        assertThat(toDateTime(worklog.started), equalTo(toDateTime("2011-07-05T11:05:00.000+0000")));
        assertThat(worklog.timeSpent, equalTo("50m"));
    }

    public void testCreateIssueWithNamesAndIds() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final IssueUpdateRequest request = getSystemFieldsByNameAndId();
        final IssueFields fields = request.fields();
        issueClient.loginAs("admin");
        final IssueCreateResponse created = issueClient.create(request);

        // read back the fields and compare it
        final Issue newIssue = issueClient.get(created.key());
        assertThat(created.self, equalTo(newIssue.self));
        assertThat(newIssue.fields.project.key, equalTo("TST"));
        assertThat(newIssue.fields.issuetype.name, equalTo("Bug"));
        assertThat(newIssue.fields.priority.name(), equalTo("Blocker"));
        assertThat(newIssue.fields.reporter.name, equalTo(fields.reporter().name()));
        assertThat(newIssue.fields.assignee.name, equalTo(fields.assignee().name()));
        assertThat(newIssue.fields.summary, equalTo(fields.summary()));
        assertThat(newIssue.fields.labels, equalTo(fields.labels()));
        assertThat(newIssue.fields.timetracking.originalEstimate, equalTo(fields.timeTracking().originalEstimate));
        assertThat(newIssue.fields.timetracking.remainingEstimate, equalTo("1h 10m"));
        assertThat(newIssue.fields.security.name, equalTo("lvl1"));
        assertThat(newIssue.fields.versions.get(0).name, equalTo("v1"));
        assertThat(newIssue.fields.versions.get(1).name, equalTo("v2"));
        assertThat(newIssue.fields.environment, equalTo(fields.environment()));
        assertThat(newIssue.fields.description, equalTo(fields.description()));
        assertThat(newIssue.fields.duedate, equalTo(fields.dueDate()));
        assertThat(newIssue.fields.fixVersions.get(0).name, equalTo("v1"));
        assertThat(newIssue.fields.fixVersions.get(1).name, equalTo("v2"));
        assertThat(newIssue.fields.components.get(0).name, equalTo("comp1"));
        assertThat(newIssue.fields.components.get(1).name, equalTo("comp2"));

        assertEquals(1, newIssue.fields.worklog.worklogs.size());
        final Worklog worklog = newIssue.fields.worklog.worklogs.get(0);

        assertThat(toDateTime(worklog.started), equalTo(toDateTime("2011-07-05T11:05:00.000+0000")));
        assertThat(worklog.timeSpent, equalTo("50m"));
    }

    public void testCreateIssueWithBadNames() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final IssueUpdateRequest request = getSystemFieldsWithBadNames();
        final IssueFields fields = request.fields();
        issueClient.loginAs("admin");
        final Response response = issueClient.getResponse(request);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertEquals("Priority name 'BadPriority' is not valid", response.entity.errors.get("priority"));
        assertEquals("Component name 'comp1Bad' is not valid", response.entity.errors.get("components"));
        assertEquals("Version name 'v1Bad' is not valid", response.entity.errors.get("versions"));
        assertEquals("Version name 'v2Bad' is not valid", response.entity.errors.get("fixVersions"));
    }

    public void testCreateSubtask() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        IssueFields fields = new IssueFields()
                .parent(withId("10010"))  // TST-2
                .project(withId("10000")) // TST
                .issueType(withId("5"))   // Sub-task
                .priority(withId("1"))    // Blocker
                .reporter(withName("farnsworth"))
                .assignee(withName("fry"))
                .summary("a sub-task of my first issue")
                .labels(Arrays.asList("abc", "def"))
                .timeTracking(new TimeTracking("120", null))
                .securityLevel(withId("10000"))
                .versions(withId("10000"))
                .environment("environment")
                .description("description")
                .dueDate("2011-03-11")
                .fixVersions(withId("10001"))
                .components(withId("10000"))
                ;
        final Map<String, List<FieldOperation>> updates = new LinkedHashMap<String, List<FieldOperation>>();
        updates.put("worklog", addWorkLog("2011-07-06T15:25:00.000+0000", "50m"));

        final IssueUpdateRequest request = new IssueUpdateRequest().fields(fields).update(updates);
        IssueCreateResponse created = issueClient.loginAs("admin").create(request);

        // read back the issue and make sure it was created as a sub-task
        Issue newIssue = issueClient.get(created.key());
        assertThat(created.self, equalTo(newIssue.self));
        assertThat(newIssue.fields.parent, not(equalTo(null)));
        assertThat(newIssue.fields.parent.id(), equalTo("10010"));
    }

    public void testCreateSubtaskWrongParentProject() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        IssueFields fields = new IssueFields()
                .parent(withId("10010"))  // TST-2
                .project(withId("10001")) // PLANETEXP
                .issueType(withId("5"))   // Sub-task
                .priority(withId("1"))    // Blocker
                .reporter(withName("farnsworth"))
                .assignee(withName("fry"))
                .summary("a sub-task of my first issue")
                .labels(Arrays.asList("abc", "def"))
                .timeTracking(new TimeTracking("120", null))
                .environment("environment")
                .description("description")
                .dueDate("2011-03-11")
                .components(withId("10000"))
                ;

        final IssueUpdateRequest request = new IssueUpdateRequest().fields(fields);
        issueClient.loginAs("admin");
        final Response response = issueClient.getResponse(request);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertEquals("Sub-tasks must be created in the same project as the parent.", response.entity.errors.get("project"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    private void assertNoLongerExistsError(WebResponse resp123) throws JSONException, IOException
    {
        // {"errorMessages":["The issue no longer exists."],"errors":[]}
        JSONObject content = new JSONObject(resp123.getText());
        assertEquals(1, content.getJSONArray("errorMessages").length());
        assertEquals("Issue Does Not Exist", content.getJSONArray("errorMessages").getString(0));
    }

    private IssueUpdateRequest getRequiredSystemFields(final String... fieldsToExclude)
    {
        final List<String> exclusions = Arrays.asList(fieldsToExclude);
        final IssueFields fields = new IssueFields();
        final Map<String, List<FieldOperation>> updates = new LinkedHashMap<String, List<FieldOperation>>();

        if (!exclusions.contains("project")) fields.project(withId("10000")); // TST
        if (!exclusions.contains("issuetype")) fields.issueType(withId("1"));  // Bug
        if (!exclusions.contains("priority")) fields.priority(withId("1"));   // Blocker
        if (!exclusions.contains("reporter")) fields.reporter(withName("farnsworth"));
        if (!exclusions.contains("assignee")) fields.assignee(withName("fry"));
        if (!exclusions.contains("summary")) fields.summary("my first fields");
        if (!exclusions.contains("labels")) fields.labels(Arrays.asList("abc", "def"));
        if (!exclusions.contains("timeTracking")) fields.timeTracking(new TimeTracking("2h", null));
        if (!exclusions.contains("securityLevel")) fields.securityLevel(withId("10000"));
        if (!exclusions.contains("versions")) fields.versions(withId("10000"));
        if (!exclusions.contains("environment")) fields.environment("environment");
        if (!exclusions.contains("description")) fields.description("description");
        if (!exclusions.contains("dueDate")) fields.dueDate("2011-03-01");
        if (!exclusions.contains("fixVersions")) fields.fixVersions(withId("10001"));
        if (!exclusions.contains("components")) fields.components(withId("10000"));
        if (!exclusions.contains("logWork")) updates.put("worklog", addWorkLog("2011-07-05T11:05:00.000+0000", "50m"));

        return new com.atlassian.jira.rest.api.issue.IssueUpdateRequest().fields(fields).update(updates);
    }

    // TODO This needs to be updated as more fields support key/name inplace of IDs
    private IssueUpdateRequest getSystemFieldsByName(final String... fieldsToExclude)
    {
        final List<String> exclusions = Arrays.asList(fieldsToExclude);
        final IssueFields fields = new IssueFields();
        final Map<String, List<FieldOperation>> updates = new LinkedHashMap<String, List<FieldOperation>>();

        if (!exclusions.contains("project")) fields.project(withId("10000")); // TST
        if (!exclusions.contains("issuetype")) fields.issueType(withId("1"));  // Bug
        if (!exclusions.contains("priority")) fields.priority(withName("Blocker"));   // Blocker
        if (!exclusions.contains("reporter")) fields.reporter(withName("farnsworth"));
        if (!exclusions.contains("assignee")) fields.assignee(withName("fry"));
        if (!exclusions.contains("summary")) fields.summary("my first fields");
        if (!exclusions.contains("labels")) fields.labels(Arrays.asList("abc", "def"));
        if (!exclusions.contains("timeTracking")) fields.timeTracking(new TimeTracking("2h", null));
        if (!exclusions.contains("securityLevel")) fields.securityLevel(withId("10000"));
        if (!exclusions.contains("versions")) fields.versions(withName("v1"));
        if (!exclusions.contains("environment")) fields.environment("environment");
        if (!exclusions.contains("description")) fields.description("description");
        if (!exclusions.contains("dueDate")) fields.dueDate("2011-03-01");
        if (!exclusions.contains("fixVersions")) fields.fixVersions(withName("v2"));
        if (!exclusions.contains("components")) fields.components(withName("comp1"));
        if (!exclusions.contains("logWork")) updates.put("worklog", addWorkLog("2011-07-05T11:05:00.000+0000", "50m"));

        return new com.atlassian.jira.rest.api.issue.IssueUpdateRequest().fields(fields).update(updates);
    }

    private IssueUpdateRequest getSystemFieldsByNameAndId(final String... fieldsToExclude)
    {
        final List<String> exclusions = Arrays.asList(fieldsToExclude);
        final IssueFields fields = new IssueFields();
        final Map<String, List<FieldOperation>> updates = new LinkedHashMap<String, List<FieldOperation>>();

        if (!exclusions.contains("project")) fields.project(withKey("TST"));
        if (!exclusions.contains("issuetype")) fields.issueType(withName("Bug"));
        if (!exclusions.contains("priority")) fields.priority(withName("Blocker"));
        if (!exclusions.contains("reporter")) fields.reporter(withName("farnsworth"));
        if (!exclusions.contains("assignee")) fields.assignee(withName("fry"));
        if (!exclusions.contains("summary")) fields.summary("my first fields");
        if (!exclusions.contains("labels")) fields.labels(Arrays.asList("abc", "def"));
        if (!exclusions.contains("timeTracking")) fields.timeTracking(new TimeTracking("2h", null));
        if (!exclusions.contains("securityLevel")) fields.securityLevel(withId("10000"));
        if (!exclusions.contains("versions")) fields.versions(withName("v1"), withName("v2"));
        if (!exclusions.contains("environment")) fields.environment("environment");
        if (!exclusions.contains("description")) fields.description("description");
        if (!exclusions.contains("dueDate")) fields.dueDate("2011-03-01");
        if (!exclusions.contains("fixVersions")) fields.fixVersions(withId("10000"), withName("v2"));
        if (!exclusions.contains("components")) fields.components(withName("comp1"), withName("comp2"));
        if (!exclusions.contains("logWork")) updates.put("worklog", addWorkLog("2011-07-05T11:05:00.000+0000", "50m"));

        return new com.atlassian.jira.rest.api.issue.IssueUpdateRequest().fields(fields).update(updates);
    }

    // TODO This needs to be updated as more fields support key/name inplace of IDs
    private IssueUpdateRequest getSystemFieldsWithBadNames(final String... fieldsToExclude)
    {
        final List<String> exclusions = Arrays.asList(fieldsToExclude);
        final IssueFields fields = new IssueFields();
        final Map<String, List<FieldOperation>> updates = new LinkedHashMap<String, List<FieldOperation>>();

        if (!exclusions.contains("project")) fields.project(withId("10000")); // TST
        if (!exclusions.contains("issuetype")) fields.issueType(withId("1"));  // Bug
        if (!exclusions.contains("priority")) fields.priority(withName("BadPriority"));   // Blocker
        if (!exclusions.contains("reporter")) fields.reporter(withName("farnsworth"));
        if (!exclusions.contains("assignee")) fields.assignee(withName("fry"));
        if (!exclusions.contains("summary")) fields.summary("my first fields");
        if (!exclusions.contains("labels")) fields.labels(Arrays.asList("abc", "def"));
        if (!exclusions.contains("timeTracking")) fields.timeTracking(new TimeTracking("120", null));
        if (!exclusions.contains("securityLevel")) fields.securityLevel(withId("10000"));
        if (!exclusions.contains("versions")) fields.versions(withName("v1Bad"));
        if (!exclusions.contains("environment")) fields.environment("environment");
        if (!exclusions.contains("description")) fields.description("description");
        if (!exclusions.contains("dueDate")) fields.dueDate("2011-03-01");
        if (!exclusions.contains("fixVersions")) fields.fixVersions(withName("v2Bad"));
        if (!exclusions.contains("components")) fields.components(withName("comp1Bad"));
        if (!exclusions.contains("logWork")) updates.put("worklog", addWorkLog("2011-07-05T11:05:00.000+0000", "50m"));

        return new com.atlassian.jira.rest.api.issue.IssueUpdateRequest().fields(fields).update(updates);
    }

    private List<FieldOperation> addWorkLog(String started, String timeSpent)
    {
        final Map<Object, Object> worklog = MapBuilder.newBuilder().add("started", started).add("timeSpent", timeSpent).toMap();
        return Arrays.asList(new FieldOperation().operation("add").value(worklog));
    }

    private IssueFields getRequiredCustomFields(final String ... fieldsToExclude)
    {
        final List<String> exclusions = Arrays.asList(fieldsToExclude);
        final IssueFields fields = new IssueFields();

        if (!exclusions.contains("project")) fields.project(withId("10000")); // TST
        if (!exclusions.contains("issuetype")) fields.issueType(withId("1"));  // Bug
        if (!exclusions.contains("priority")) fields.priority(withId("1"));   // Blocker
        if (!exclusions.contains("reporter")) fields.reporter(withName("farnsworth"));
        if (!exclusions.contains("assignee")) fields.assignee(withName("fry"));
        if (!exclusions.contains("summary")) fields.summary("custom fields test");
        if (!exclusions.contains("customfield_" + DATE_CF)) fields.customField(DATE_CF, "1981-06-09");
        if (!exclusions.contains("customfield_" + DATETIME_CF)) fields.customField(DATETIME_CF, "2011-07-06T15:25:00.000+1000");
        Map<String, Object> parent = new HashMap<String, Object>();
        Map<String, Object> child = new HashMap<String, Object>();
        parent.put("id", "10000");
        parent.put("child", child);
        child.put("id", "10002");
        if (!exclusions.contains("customfield_" + CASCADING_SELECT_CF)) fields.customField(CASCADING_SELECT_CF, parent);
        if (!exclusions.contains("customfield_" + TEXT_FIELD_CF)) fields.customField(TEXT_FIELD_CF, "this is a text field");
        if (!exclusions.contains("customfield_" + TEXT_AREA_CF)) fields.customField(TEXT_AREA_CF, "this is a text area. big text.");
        if (!exclusions.contains("customfield_" + GROUP_PICKER)) fields.customField(GROUP_PICKER, withName("jira-developers") );
        if (!exclusions.contains("customfield_" + GROUPS_PICKER)) fields.customField(GROUPS_PICKER, new ResourceRef[] { withName("jira-administrators"), withName("jira-users") });
        if (!exclusions.contains("customfield_" + SINGLE_VERSION_CF)) fields.customField(SINGLE_VERSION_CF, withName("v1"));
        if (!exclusions.contains("customfield_" + MULTI_VERSION_CF)) fields.customField(MULTI_VERSION_CF, new ResourceRef[]{ withName("v2"), withName("v1")});

        return fields;
    }
}
