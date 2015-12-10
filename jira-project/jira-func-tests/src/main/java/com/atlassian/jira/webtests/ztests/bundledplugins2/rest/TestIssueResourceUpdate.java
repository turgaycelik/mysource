package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.rest.api.issue.TimeTracking;
import com.atlassian.jira.testkit.client.restclient.Group;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.type.TypeReference;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.api.issue.ResourceRef.withId;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withName;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withRubbish;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

@WebTest ( { Category.FUNC_TEST, Category.REST })
public class TestIssueResourceUpdate extends RestFuncTest
{
    private IssueClient issueClient;

    public void testEditAllSystemFields() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        Issue original = issueClient.get("TST-1");

        IssueUpdateRequest updateSummaryRequest = new IssueUpdateRequest().fields(new IssueFields()
                .summary("issue that i'm about to edit")
        );

        // first edit only the summary, to make sure the REST api will reuse fields that aren't provided
        issueClient.update(original.id, updateSummaryRequest);
        assertThat(issueClient.get(original.key).fields.summary, equalTo(updateSummaryRequest.fields().summary()));

        // then try to edit everything else.
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .priority(withId("2"))
                .reporter(withName("fry"))
                .assignee(withName("farnsworth"))
                .labels(Arrays.asList("foo", "bar"))
                .timeTracking(new TimeTracking("20m", "20m"))
                .securityLevel(withId("10001"))
                .versions(withId("10001"))
                .environment("edited environment")
                .description("edited description")
                .dueDate("2012-03-01")
                .fixVersions(withId("10001"))
                .components(withId("10001"))
                .resolution(withId("2"))
        );
        issueClient.update(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.priority.id(), not(equalTo(original.fields.priority.id())));
        assertThat(updated.fields.priority.id(), equalTo(((ResourceRef)updateRequest.fields().priority()).id()));

        assertThat(updated.fields.reporter.name, equalTo(updateRequest.fields().reporter().name()));

        assertThat(updated.fields.assignee.name, equalTo(updateRequest.fields().assignee().name()));

        assertThat(newHashSet(updated.fields.labels), not(equalTo(newHashSet(original.fields.labels))));
        assertThat(newHashSet(updated.fields.labels), equalTo(newHashSet(updateRequest.fields().labels())));

        assertThat(updated.fields.timetracking, not(equalTo(original.fields.timetracking)));
        assertThat(updated.fields.timetracking.originalEstimate, equalTo(updateRequest.fields().timeTracking().originalEstimate));
        assertThat(updated.fields.timetracking.remainingEstimate, equalTo(updateRequest.fields().timeTracking().remainingEstimate));

        assertThat(updated.fields.security, not(equalTo(original.fields.security)));
        assertThat(updated.fields.security.name, equalTo("lvl2"));

        assertThat(updated.fields.versions, not(equalTo(original.fields.versions)));
        assertThat(updated.fields.versions.size(), equalTo(1));
        assertThat(updated.fields.versions.get(0).name, equalTo("v2"));

        assertThat(updated.fields.environment, not(equalTo(original.fields.environment)));
        assertThat(updated.fields.environment, equalTo(updateRequest.fields().environment()));

        assertThat(updated.fields.description, not(equalTo(original.fields.description)));
        assertThat(updated.fields.description, equalTo(updateRequest.fields().description()));

        assertThat(updated.fields.duedate, not(equalTo(original.fields.duedate)));
        assertThat(updated.fields.duedate, equalTo(updateRequest.fields().dueDate()));

        assertThat(updated.fields.fixVersions, not(equalTo(original.fields.fixVersions)));
        assertThat(updated.fields.fixVersions.size(), equalTo(1));
        assertThat(updated.fields.fixVersions.get(0).name, equalTo("v2"));

        assertThat(updated.fields.components, not(equalTo(original.fields.components)));
        assertThat(updated.fields.components.size(), equalTo(1));
        assertThat(updated.fields.components.get(0).name, equalTo("comp2"));

        assertThat(updated.fields.resolution.id, not(equalTo(original.fields.resolution.id)));
        assertThat(updated.fields.resolution.id, equalTo(((ResourceRef)updateRequest.fields().resolution()).id()));
    }

    // TODO This needs to be updated as more fields support key/name inplace of IDs
    public void testEditSystemFieldsByName() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        Issue original = issueClient.get("TST-1");

        IssueUpdateRequest updateSummaryRequest = new IssueUpdateRequest().fields(new IssueFields()
                .summary("issue that i'm about to edit")
        );

        // first edit only the summary, to make sure the REST api will reuse fields that aren't provided
        issueClient.update(original.id, updateSummaryRequest);
        assertThat(issueClient.get(original.key).fields.summary, equalTo(updateSummaryRequest.fields().summary()));

        // then try to edit everything else.
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .securityLevel(withName("lvl2"))
                .priority(withName("Critical"))
                .versions(withName("v2"))
                .fixVersions(withName("v2"))
                .components(withName("comp2"))
                .resolution(withName("Duplicate"))
        );
        issueClient.update(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.security, not(equalTo(original.fields.security)));
        assertThat(updated.fields.security.name, equalTo("lvl2"));

        assertThat(updated.fields.priority.id(), not(equalTo(original.fields.priority.id())));
        assertThat(updated.fields.priority.id(), equalTo("2"));

        assertThat(updated.fields.versions, not(equalTo(original.fields.versions)));
        assertThat(updated.fields.versions.size(), equalTo(1));
        assertThat(updated.fields.versions.get(0).name, equalTo("v2"));

        assertThat(updated.fields.fixVersions, not(equalTo(original.fields.fixVersions)));
        assertThat(updated.fields.fixVersions.size(), equalTo(1));
        assertThat(updated.fields.fixVersions.get(0).name, equalTo("v2"));

        assertThat(updated.fields.components, not(equalTo(original.fields.components)));
        assertThat(updated.fields.components.size(), equalTo(1));
        assertThat(updated.fields.components.get(0).name, equalTo("comp2"));

        assertThat(updated.fields.resolution.id, not(equalTo(original.fields.resolution.id)));
        assertThat(updated.fields.resolution.id, equalTo("3"));
    }

    // TODO This needs to be updated as more fields support key/name inplace of IDs
    public void testEditSystemFieldsByNameAndId() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        Issue original = issueClient.get("TST-1");

        IssueUpdateRequest updateSummaryRequest = new IssueUpdateRequest().fields(new IssueFields()
                .summary("issue that i'm about to edit")
        );

        // first edit only the summary, to make sure the REST api will reuse fields that aren't provided
        issueClient.update(original.id, updateSummaryRequest);
        assertThat(issueClient.get(original.key).fields.summary, equalTo(updateSummaryRequest.fields().summary()));

        // then try to edit everything else.
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .priority(withName("Critical"))
                .versions(withName("v1"), withName("v2"))
                .fixVersions(withId("10000"), withName("v2"))
                .components(withId("10000"), withName("comp2"))
        );
        issueClient.update(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.priority.id(), not(equalTo(original.fields.priority.id())));
        assertThat(updated.fields.priority.id(), equalTo("2"));

        assertThat(updated.fields.versions, not(equalTo(original.fields.versions)));
        assertThat(updated.fields.versions.size(), equalTo(2));
        assertThat(updated.fields.versions.get(0).name, equalTo("v1"));
        assertThat(updated.fields.versions.get(1).name, equalTo("v2"));

        assertThat(updated.fields.fixVersions, not(equalTo(original.fields.fixVersions)));
        assertThat(updated.fields.fixVersions.size(), equalTo(2));
        assertThat(updated.fields.fixVersions.get(0).name, equalTo("v1"));
        assertThat(updated.fields.fixVersions.get(1).name, equalTo("v2"));

        assertThat(updated.fields.components, not(equalTo(original.fields.components)));
        assertThat(updated.fields.components.size(), equalTo(2));
        assertThat(updated.fields.components.get(0).name, equalTo("comp1"));
        assertThat(updated.fields.components.get(1).name, equalTo("comp2"));
    }

    // TODO This needs to be updated as more fields support key/name inplace of IDs
    public void testEditSystemFieldsInvalidData() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        Issue original = issueClient.get("TST-1");

        IssueUpdateRequest updateSummaryRequest = new IssueUpdateRequest().fields(new IssueFields()
                .summary("issue that i'm about to edit")
        );

        // first edit only the summary, to make sure the REST api will reuse fields that aren't provided
        issueClient.update(original.id, updateSummaryRequest);
        assertThat(issueClient.get(original.key).fields.summary, equalTo(updateSummaryRequest.fields().summary()));

        // then try to edit everything else.
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .priority(withName("BadPriority"))
                .versions(withName("v1Bad"))
                .fixVersions(withName("v2Bad"))
                .components(withName("comp1Bad"))
                .resolution(withName("BadResolution"))
                .dueDate("2001-01-XV")
        );

        Response response = issueClient.updateResponse(original.id, updateRequest);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertEquals("Priority name 'BadPriority' is not valid", response.entity.errors.get("priority"));
        assertEquals("Resolution name 'BadResolution' is not valid", response.entity.errors.get("resolution"));
        assertEquals("Component name 'comp1Bad' is not valid", response.entity.errors.get("components"));
        assertEquals("Version name 'v1Bad' is not valid", response.entity.errors.get("versions"));
        assertEquals("Version name 'v2Bad' is not valid", response.entity.errors.get("fixVersions"));
        assertEquals("Error parsing date string: 2001-01-XV", response.entity.errors.get("duedate"));

        // Missing name & id.
        updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .priority(withRubbish("BadPriority"))
        );

        response = issueClient.updateResponse(original.id, updateRequest);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertEquals("Could not find valid 'id' or 'name' in priority object.", response.entity.errors.get("priority"));

        // Missing name & id.
        updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .resolution(withRubbish("badResolution"))
        );

        response = issueClient.updateResponse(original.id, updateRequest);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertEquals("Could not find valid 'id' or 'name' in resolution object.", response.entity.errors.get("resolution"));

        // Missing name & id.
        updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .versions(withRubbish("v1Bad"))
                .fixVersions(withRubbish("v2Bad"))
                .components(withRubbish("comp1Bad"))
        );

        response = issueClient.updateResponse(original.id, updateRequest);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertEquals("Component/s is required.", response.entity.errors.get("components"));
        assertEquals("Affects Version/s is required.", response.entity.errors.get("versions"));
        assertEquals("Fix Version/s is required.", response.entity.errors.get("fixVersions"));

        // can't accept Blank ID
        updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .resolution(withId(""))
        );

        response = issueClient.updateResponse(original.id, updateRequest);

        // Expecting Bad Request error
        assertEquals(400, response.statusCode);
        assertEquals("Could not find valid 'id' or 'name' in resolution object.", response.entity.errors.get("resolution"));
     }

    public void testEditSystemFieldsSetNull() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        Issue original = issueClient.get("TST-1");

        IssueUpdateRequest updateSummaryRequest = new IssueUpdateRequest().fields(new IssueFields()
                .summary("issue that i'm about to edit")
        );

        // first edit only the summary, to make sure the REST api will reuse fields that aren't provided
        issueClient.update(original.id, updateSummaryRequest);
        assertThat(issueClient.get(original.key).fields.summary, equalTo(updateSummaryRequest.fields().summary()));

        // Our Client dosen't do nulls, so we just make a little map.
        Map<String, Map<String, String>> updateRequest = new HashMap();
        Map<String, String> fields = new HashMap();
        fields.put("security", null);
        fields.put("duedate", null);
        updateRequest.put("fields", fields);

        Response response = issueClient.update(original.id, updateRequest);
        assertEquals(400, response.statusCode);
        assertEquals("Security Level is required.", response.entity.errors.get("security"));
        assertEquals("Due Date is required.", response.entity.errors.get("duedate"));

        // Make fields optional then test again
        administration.fieldConfigurations().defaultFieldConfiguration().optionalField("Security Level");
        administration.fieldConfigurations().defaultFieldConfiguration().optionalField("Due Date");

        issueClient.update(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.security, not(equalTo(original.fields.security)));
        assertThat(updated.fields.security, equalTo(null));
        assertThat(updated.fields.duedate, not(equalTo(original.fields.duedate)));
        assertThat(updated.fields.duedate, equalTo(null));
    }

    public void testEditGroupCustomFields() throws Exception
    {
        administration.restoreData("TestCreateIssueWithRequiredSystemFields.xml");

        final String TST_1 = "TST-1";
        final Group jira_developers = new Group().name("jira-developers").self(URI.create(getBaseUrlPlus("rest/api/2/group?groupname=jira-developers")));
        final Group jira_users = new Group().name("jira-users").self(URI.create(getBaseUrlPlus("rest/api/2/group?groupname=jira-users")));

        // test SET for group picker
        String picker = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker", "single group");
        {
            issueClient.edit(TST_1, new IssueUpdateRequest().update(picker, new FieldOperation("set", jira_developers)));

            Group group = issueClient.get(TST_1).fields.get(picker, Group.class);
            assertThat(group, equalTo(jira_developers));
        }

        // test SET for multi group picker
        String multiPicker = administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker", "many groups");
        {
            issueClient.edit(TST_1, new IssueUpdateRequest().update(multiPicker, new FieldOperation("set", Arrays.asList(jira_users, jira_developers))));

            List<Group> groups = issueClient.get(TST_1).fields.get(multiPicker, new TypeReference<List<Group>>() {});
            assertThat(groups.size(), equalTo(2));
            assertThat(groups, hasItems(jira_users, jira_developers));
        }

        // test REMOVE for multi group picker
        {
            issueClient.edit(TST_1, new IssueUpdateRequest().update(multiPicker, new FieldOperation("remove", jira_users)));

            List<Group> groups = issueClient.get(TST_1).fields.get(multiPicker, new TypeReference<List<Group>>() {});
            assertThat(groups, equalTo(Arrays.asList(jira_developers)));
        }

        // test ADD for multi group picker
        {
            issueClient.edit(TST_1, new IssueUpdateRequest().update(multiPicker, new FieldOperation("add", jira_users)));

            List<Group> groups = issueClient.get(TST_1).fields.get(multiPicker, new TypeReference<List<Group>>() {});
            assertThat(groups.size(), equalTo(2));
            assertThat(groups, hasItems(jira_users, jira_developers));
        }
    }

    public void testEditWithMetadata() throws Exception
    {
        // having
        administration.restoreBlankInstance();
        backdoor.project().addProject("UPDATE", "UPDATE", ADMIN_USERNAME);
        final IssueCreateResponse issue = backdoor.issues().createIssue("UPDATE", "summary");

        // when
        issueClient.edit(issue.key(), new IssueUpdateRequest()
                .fields(new IssueFields().summary("newmmary"))
                .historyMetadata(
                        HistoryMetadata.builder("updateMetadataTest").build()
                ));
        final JsonNode metadata = backdoor.issueNavControl().getHistoryMetadata(issue.key).get(0);

        // then
        assertThat(metadata.get("type").asText(), equalTo("updateMetadataTest"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
