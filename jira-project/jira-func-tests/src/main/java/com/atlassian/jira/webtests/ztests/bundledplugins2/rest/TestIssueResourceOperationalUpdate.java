package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.OperationalUpdateRequest;
import com.atlassian.jira.testkit.client.restclient.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceOperationalUpdate extends RestFuncTest
{
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    private void importData()
    {
        administration.restoreData("TestIssueResourceOperationalUpdate.xml");
    }

    public void testSimpleImplicitUpdateAndOperationalWorks() throws Exception
    {
        importData();

        Issue original = issueClient.get("TST-1");

        Map<String, List<Map<String, Object>>> update = new HashMap<String, List<Map<String, Object>>>();
        update.put("description", Arrays.asList(MapBuilder.<String, Object>newBuilder().add("set", "newd1").toMap()));

        OperationalUpdateRequest updateRequest = new OperationalUpdateRequest(update);
        IssueFields fields = new IssueFields();
        fields.summary("news1");
        updateRequest.setFields(fields);

        Response response = issueClient.operationalUpdateResponse(original.id, updateRequest);
        assertEquals(204, response.statusCode);

        Issue updated = issueClient.get("TST-1");
        assertEquals("news1", updated.fields.summary);
        assertEquals("newd1", updated.fields.description);
    }

    public void testImplicitUpdateAndOperationaForSameFieldFails() throws Exception
    {
        importData();

        Issue original = issueClient.get("TST-1");

        Map<String, List<Map<String, Object>>> update = new HashMap<String, List<Map<String, Object>>>();
        update.put("summary", Arrays.asList(MapBuilder.<String, Object>newBuilder().add("set", "implicit").toMap()));

        OperationalUpdateRequest updateRequest = new OperationalUpdateRequest(update);
        IssueFields fields = new IssueFields();
        fields.summary("explicit");
        updateRequest.setFields(fields);

        Response response = issueClient.operationalUpdateResponse(original.id, updateRequest);
        assertResponseCodeAndErrorMessage(400, "Field 'summary' cannot appear in both 'fields' and 'update'", response);

        Issue updated = issueClient.get("TST-1");
        assertEquals("A critical bug", updated.fields.summary);
    }

    private void assertResponseCodeAndErrorMessage(int statusCode, String errorMessage, Response response)
    {
        assertEquals(statusCode, response.statusCode);
        String error = response.entity.errorMessages.get(0);
        assertEquals(errorMessage, error);
    }

    private void assertResponseCodeAndErrors(int statusCode, String field, String errorMessage, Response response)
    {
        assertEquals(statusCode, response.statusCode);
        Map<String, String> errors = response.entity.errors;
        assertEquals(errorMessage, errors.get(field));
    }

    public void testInvalidFieldId() throws Exception
    {
        importData();

        Issue original = issueClient.get("TST-1");
        OperationalUpdateRequest updateRequest = createRequest("sumary", "set", "test");
        Response response = issueClient.operationalUpdateResponse(original.id, updateRequest);
        assertResponseCodeAndErrors(400, "sumary", "Field 'sumary' cannot be set. It is not on the appropriate screen, or unknown.", response);
    }

    public void testDescriptionField() throws Exception
    {
        importData();

        Issue original = issueClient.get("TST-1");

        OperationalUpdateRequest updateRequest = createRequest("description", "set", "blah");
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.description, equalTo("blah"));
    }


    OperationalUpdateRequest createRequest(String fieldName, String operation, Object newvalue)
    {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.addOperation(fieldName, operation, newvalue);
        return requestBuilder.build();
    }
    OperationalUpdateRequest createArrayRequest(String fieldName, String operation, Object... values)
    {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.addOperation(fieldName, operation, values);
        return requestBuilder.build();
    }

    class RequestBuilder
    {
        private Map<String, List<Map<String, Object>>> fieldUpdates = new HashMap<String, List<Map<String, Object>>>();

        RequestBuilder addOperation(String fieldName, String operation, Object newvalue)
        {
            List<Map<String, Object>> fieldOper = fieldUpdates.get(fieldName);
            if (fieldOper == null)
            {
                fieldOper = new ArrayList<Map<String, Object>>();
            }
            Map<String, Object> operations = new HashMap<String, Object>();
            operations.put(operation, newvalue);
            fieldOper.add(operations);

            fieldUpdates.put(fieldName, fieldOper);
            return this;
        }


        OperationalUpdateRequest build()
        {
            return new OperationalUpdateRequest(fieldUpdates);
        }
    }


    public void testSummaryField() throws Exception
    {
        importData();

        Issue original = issueClient.get("TST-1");

        OperationalUpdateRequest updateRequest = createRequest("summary", "set", "A critical bug");
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.summary, equalTo("A critical bug"));
    }

    public void testComponentField() throws Exception
    {
        importData();
        Issue original = issueClient.get("PH-1");
        assertThat(original.fields.components.size(), equalTo(0));
        //ADD
        OperationalUpdateRequest updateRequest = createRequest("components", "add", MapBuilder.newBuilder().add("id", "10010").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.components.size(), equalTo(1));
        assertThat(updated.fields.components.get(0).id, equalTo(10010L));
        assertThat(updated.fields.components.get(0).name, equalTo("Database"));

        //SET
        updateRequest = createArrayRequest("components", "set", MapBuilder.newBuilder().add("id", "10010").toMap(), MapBuilder.newBuilder().add("id", "10013").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.components.size(), equalTo(2));
        assertThat(updated.fields.components.get(0).name, equalTo("3rd Party Integration"));
        assertThat(updated.fields.components.get(1).name, equalTo("Database"));

        //REMOVE
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.addOperation("components", "remove", MapBuilder.newBuilder().add("id", "10010").toMap());
        requestBuilder.addOperation("components", "remove", MapBuilder.newBuilder().add("id", "10040").toMap());
        updateRequest = requestBuilder.build();
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.fields.components.size(), equalTo(1));
        assertThat(updated.fields.components.get(0).name, equalTo("3rd Party Integration"));

        //CLEAR
        updateRequest = createArrayRequest("components", "set");
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.fields.components.size(), equalTo(0));
    }

    public void testPriorityField() throws Exception
    {
        importData();
        Issue original = issueClient.get("PH-1");
        assertThat(original.fields.priority.name(), equalTo("Major"));
        //SET
        OperationalUpdateRequest updateRequest = createRequest("priority", "set", MapBuilder.newBuilder().add("id", "4").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.priority.name(), equalTo("Minor"));
    }

    public void testAssigneeField() throws Exception
    {
        importData();
        Issue original = issueClient.get("PH-1");
        assertThat(original.fields.assignee.name, equalTo("admin"));
        //SET
        OperationalUpdateRequest updateRequest = createRequest("assignee", "set", MapBuilder.newBuilder().add("name", "fry").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.assignee.name, equalTo("fry"));

        //CLEAR (should not work, issues cannot be unassigned)
        updateRequest = createRequest("assignee", "set", null);
        Response response = issueClient.operationalUpdateResponse(original.id, updateRequest);
        assertResponseCodeAndErrors(400, "assignee", "Issues must be assigned.", response);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));
        assertThat(updated.fields.assignee.name, equalTo("fry"));

        //CLEAR (should now work, allow issues to be unassigned)
        administration.generalConfiguration().setAllowUnassignedIssues(true);
        updateRequest = createRequest("assignee", "set", null);
        response = issueClient.operationalUpdateResponse(original.id, updateRequest);
        assertEquals(204, response.statusCode);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));
        assertThat(updated.fields.assignee, equalTo(null));
    }

    public void testFixForField() throws Exception
    {
        importData();
        //ADD
        Issue original = issueClient.get("TST-1");
        assertThat(original.fields.fixVersions.size(), equalTo(1));
        assertThat(original.fields.fixVersions.get(0).name, equalTo("v2"));

        OperationalUpdateRequest updateRequest = createRequest("fixVersions", "add", MapBuilder.newBuilder().add("id", "10000").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.fixVersions.size(), equalTo(2));
        assertThat(updated.fields.fixVersions.get(0).name, equalTo("v1"));
        assertThat(updated.fields.fixVersions.get(1).name, equalTo("v2"));

        //CLEAR
        original = issueClient.get("TST-1");
        assertThat(original.fields.fixVersions.size(), equalTo(2));

        updateRequest = createArrayRequest("fixVersions", "set");
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.fixVersions.size(), equalTo(0));

        //SET
        original = issueClient.get("TST-1");
        assertThat(original.fields.fixVersions.size(), equalTo(0));

        updateRequest = createArrayRequest("fixVersions", "set", MapBuilder.newBuilder().add("id", "10000").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.fixVersions.size(), equalTo(1));
        assertThat(updated.fields.fixVersions.get(0).name, equalTo("v1"));

        //REMOVE
        original = issueClient.get("TST-1");
        assertThat(original.fields.fixVersions.size(), equalTo(1));

        updateRequest = createRequest("fixVersions", "remove", MapBuilder.newBuilder().add("id", "10000").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.fixVersions.size(), equalTo(0));
    }

    public void testAffectedVersionsField() throws Exception
    {
        importData();
        //ADD
        Issue original = issueClient.get("TST-1");
        assertThat(original.fields.versions.size(), equalTo(1));
        assertThat(original.fields.versions.get(0).name, equalTo("v2"));

        OperationalUpdateRequest updateRequest = createRequest("versions", "add", MapBuilder.newBuilder().add("id", "10000").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.versions.size(), equalTo(2));
        assertThat(updated.fields.versions.get(0).name, equalTo("v1"));
        assertThat(updated.fields.versions.get(1).name, equalTo("v2"));

        //CLEAR
        original = issueClient.get("TST-1");
        assertThat(original.fields.versions.size(), equalTo(2));

        updateRequest = createArrayRequest("versions", "set");
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.versions.size(), equalTo(0));

        //SET
        original = issueClient.get("TST-1");
        assertThat(original.fields.versions.size(), equalTo(0));

        updateRequest = createArrayRequest("versions", "set", MapBuilder.newBuilder().add("id", "10000").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.versions.size(), equalTo(1));
        assertThat(updated.fields.versions.get(0).name, equalTo("v1"));

        //REMOVE
        original = issueClient.get("TST-1");
        assertThat(original.fields.versions.size(), equalTo(1));

        updateRequest = createRequest("versions", "remove", MapBuilder.newBuilder().add("id", "10000").toMap());
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.versions.size(), equalTo(0));
    }

    public void testLabelsField() throws Exception
    {
        importData();
        //ADD
        Issue original = issueClient.get("TST-1");
        assertThat(original.fields.labels.size(), equalTo(2));
        assertThat(original.fields.labels.get(0), equalTo("bar"));
        assertThat(original.fields.labels.get(1), equalTo("foo"));

        OperationalUpdateRequest updateRequest = createRequest("labels", "add", "baz");
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.labels.size(), equalTo(3));
        assertThat(updated.fields.labels.get(0), equalTo("bar"));
        assertThat(updated.fields.labels.get(1), equalTo("baz"));
        assertThat(updated.fields.labels.get(2), equalTo("foo"));

        //CLEAR
        original = issueClient.get("TST-1");
        assertThat(original.fields.labels.size(), equalTo(3));

        updateRequest = createArrayRequest("labels", "set", new String[0]);
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.labels.size(), equalTo(0));

        //SET
        original = issueClient.get("TST-1");
        assertThat(original.fields.labels.size(), equalTo(0));

        updateRequest = createArrayRequest("labels", "set", "quux", "bob");
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.labels.size(), equalTo(2));
        assertThat(updated.fields.labels.get(0), equalTo("bob"));
        assertThat(updated.fields.labels.get(1), equalTo("quux"));

        //REMOVE
        original = issueClient.get("TST-1");
        assertThat(original.fields.labels.size(), equalTo(2));

        updateRequest = createRequest("labels", "remove", "bob");
        issueClient.operationalUpdate(original.id, updateRequest);

        updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));

        assertThat(updated.fields.labels.size(), equalTo(1));
    }

    public void testMultipleFieldsAndMultipleOperations() throws Exception
    {
        importData();
        Issue original = issueClient.get("TST-1");
        assertThat(original.fields.versions.size(), equalTo(1));

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.addOperation("versions", "remove", MapBuilder.newBuilder().add("id", "10000").toMap());
        requestBuilder.addOperation("versions", "add", MapBuilder.newBuilder().add("id", "10000").toMap());
        requestBuilder.addOperation("versions", "add", MapBuilder.newBuilder().add("id", "10001").toMap());
        requestBuilder.addOperation("assignee", "set", MapBuilder.newBuilder().add("name", "admin").toMap());

        OperationalUpdateRequest updateRequest = requestBuilder.build();
        issueClient.operationalUpdate(original.id, updateRequest);

        Issue updated = issueClient.get(original.key);
        assertThat(updated.self, equalTo(original.self));
        assertThat(updated.fields.versions.size(), equalTo(2));
    }

    public void testErrorForUnconfiguredField() throws Exception
    {
        importData();

        // We should be able to update the 2 custom fields on TST-1 but not on PH-1 where they are hidden
        Issue original = issueClient.get("TST-1");

        Map<String, List<Map<String, Object>>> update = new HashMap<String, List<Map<String, Object>>>();
        update.put("customfield_10000", Arrays.asList(MapBuilder.<String, Object>newBuilder().add("set", "2011-11-17").toMap()));

        OperationalUpdateRequest updateRequest = new OperationalUpdateRequest(update);
        IssueFields fields = new IssueFields();
        fields.customField(10001L, "2011-11-17T08:45:00.000+1100");
        updateRequest.setFields(fields);

        Response response = issueClient.operationalUpdateResponse(original.id, updateRequest);
        assertEquals(204, response.statusCode);

        Issue updated = issueClient.get("TST-1");
        assertEquals("2011-11-17", updated.fields.get("customfield_10000"));
        assertEquals("2011-11-17T08:45:00.000+1100", updated.fields.get("customfield_10001"));

        // Now for PH-1
        original = issueClient.get("PH-1");

        update = new HashMap<String, List<Map<String, Object>>>();
        update.put("customfield_10000", Arrays.asList(MapBuilder.<String, Object>newBuilder().add("set", "2011-11-17").toMap()));

        updateRequest = new OperationalUpdateRequest(update);
        fields = new IssueFields();
        fields.customField(10001L, "2011-11-17T08:45:00.000+1100");
        updateRequest.setFields(fields);

        response = issueClient.operationalUpdateResponse(original.id, updateRequest);
        assertResponseCodeAndErrors(400, "customfield_10000","Field 'customfield_10000' cannot be set. It is not on the appropriate screen, or unknown.", response);
        assertResponseCodeAndErrors(400, "customfield_10001","Field 'customfield_10001' cannot be set. It is not on the appropriate screen, or unknown.", response);
    }

}
