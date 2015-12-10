package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Func tests for custom fields.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceCustomFields extends RestFuncTest
{
    private IssueClient issueClient;

    public void testTextField() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema, Issue.Expand.renderedFields);

        String textField = issue.fields.get("customfield_10021");
        assertNotNull(textField);
        assertEquals("this is *text*", textField);

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:textfield", issue.schema.get("customfield_10021").getCustom());

        String renderedField = issue.renderedFields.getCustomField("customfield_10021");
        assertEquals("<p>this is <b>text</b></p>", renderedField);

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10021", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        textField = issue.fields.get("customfield_10021");
        assertNull(textField);
    }

    public void testTextArea() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema, Issue.Expand.renderedFields);
        String textArea = issue.fields.get("customfield_10013");

        assertNotNull(textArea);
        assertEquals("lots of text here, *brother*!", textArea);

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:textarea", issue.schema.get("customfield_10013").getCustom());

        String renderedField = issue.renderedFields.getCustomField("customfield_10013");
        assertEquals("<p>lots of text here, <b>brother</b>!</p>", renderedField);

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10013", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        textArea = issue.fields.get("customfield_10013");
        assertNull(textArea);
    }

    public void testDatePicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema, Issue.Expand.renderedFields);
        String datePickerCF = issue.fields.get("customfield_10012");
        assertNotNull(datePickerCF);
        assertEquals("2010-06-23", datePickerCF);

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:datepicker", issue.schema.get("customfield_10012").getCustom());

        String renderedField = issue.renderedFields.getCustomField("customfield_10012");
        assertEquals("23/Jun/10", renderedField);

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10012", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        datePickerCF = issue.fields.get("customfield_10012");
        assertNull(datePickerCF);

        // Check invalid dates return an error
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().update("customfield_10012", new FieldOperation("set", "1981-00-0"));
        Response response = issueClient.updateResponse("HSP-1", updateRequest);
        assertEquals(400, response.statusCode);
        assertEquals("Error parsing date string: 1981-00-0", response.entity.errors.get("customfield_10012"));
        updateRequest = new IssueUpdateRequest().update("customfield_10012", new FieldOperation("set", "1981-13-1"));
        response = issueClient.updateResponse("HSP-1", updateRequest);
        assertEquals(400, response.statusCode);
        assertEquals("Error parsing date string: 1981-13-1", response.entity.errors.get("customfield_10012"));
    }

    public void testDateTime() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema, Issue.Expand.renderedFields);
        String dateTimeCF = issue.fields.get("customfield_10001");
        assertNotNull(dateTimeCF);
        assertEqualDateStrings("2010-06-16T15:26:00.000+1000", dateTimeCF);

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:datetime", issue.schema.get("customfield_10001").getCustom());

        String renderedField = issue.renderedFields.getCustomField("customfield_10001");
        assertEquals("16/Jun/10 3:26 PM", renderedField);

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10001", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        dateTimeCF = issue.fields.get("customfield_10001");
        assertNull(dateTimeCF);

        // Check invalid dates return an error
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().update("customfield_10001", new FieldOperation("set", "1981-00-00T15:12:00.000+1000"));
        Response response = issueClient.updateResponse("HSP-1", updateRequest);
        assertEquals(400, response.statusCode);
        assertEquals("Error parsing time: 1981-00-00T15:12:00.000+1000", response.entity.errors.get("customfield_10001"));
        updateRequest = new IssueUpdateRequest().update("customfield_10001", new FieldOperation("set", "1981-13-01T15:12:00.000+1000"));
        response = issueClient.updateResponse("HSP-1", updateRequest);
        assertEquals(400, response.statusCode);
        assertEquals("Error parsing time: 1981-13-01T15:12:00.000+1000", response.entity.errors.get("customfield_10001"));
    }

    public void testFloat() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        Double floatCF = issue.fields.get("customfield_10018");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:float", issue.schema.get("customfield_10018").getCustom());
        assertEquals(42.0, floatCF);

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10018", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        floatCF = issue.fields.get("customfield_10018");
        assertNull(floatCF);
    }

    public void testImportId() throws Exception
    {
        administration.restoreData("TestIssueLinkCheck.xml");

        Issue issue = issueClient.get("ANOT-1", Issue.Expand.schema);
        Double bugzillaId = issue.fields.get("customfield_10000");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:importid", issue.schema.get("customfield_10000").getCustom());
        assertEquals(2.0, bugzillaId);

        // Check clearing the field - ImportId is a set-once, readonly field, so should we should NOT be able to null it out
        issueClient.edit("ANOT-1", new IssueUpdateRequest().update("customfield_10000", new FieldOperation("set", null)));
        issue = issueClient.get("ANOT-1", Issue.Expand.schema);
        bugzillaId = issue.fields.get("customfield_10000");
        assertEquals(2.0, bugzillaId);
    }

    public void testSelect() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        Map<String, String> selectList = issue.fields.get("customfield_10020");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:select", issue.schema.get("customfield_10020").getCustom());
        Map<String, String> options = selectList;
        assertEquals(getBaseUrl() + "/rest/api/2/customFieldOption/10011", options.get("self"));
        assertEquals("Select!", options.get("value"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10020", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        selectList = issue.fields.get("customfield_10020");
        assertNull(selectList);
    }

    public void testRadioButtons() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);

        assertThat(issue.schema.get("customfield_10019").getCustom(), equalTo("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons"));

        Map<String, Object> radioButtons = issue.fields.get("customfield_10019");
        assertThat(radioButtons.get("self"), Matchers.<Object>equalTo(getBaseUrl() + "/rest/api/2/customFieldOption/10010"));
        assertThat(radioButtons.get("value"), Matchers.<Object>equalTo("Radio Ga Ga"));
        assertThat("id must be a string=10010", radioButtons.get("id"), Matchers.<Object>equalTo("10010"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10019", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        radioButtons = issue.fields.get("customfield_10019");
        assertNull(radioButtons);
    }

    public void testProject() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        Map<String, Object> projectPicker = issue.fields.get("customfield_10007");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:project", issue.schema.get("customfield_10007").getCustom());
        assertEquals(getBaseUrl() + "/rest/api/2/project/10001", projectPicker.get("self"));
        assertEquals("MKY", projectPicker.get("key"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10007", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        projectPicker = issue.fields.get("customfield_10007");
        assertNull(projectPicker);
    }

    public void testMultiVersion() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        List<Map<String, String>> multiVersion = issue.fields.get("customfield_10011");
        List<Map<String, String>> versions = multiVersion;

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", issue.schema.get("customfield_10011").getCustom());
        assertEquals(2, versions.size());

        // The versions may appear in any order
        final Set<Map<String, String>> expected = new HashSet<Map<String, String>>(2);
        expected.add(MapBuilder.<String, String>newBuilder().add("self", getBaseUrl() + "/rest/api/2/version/10000").add("name", "New Version 1").toMap());
        expected.add(MapBuilder.<String, String>newBuilder().add("self", getBaseUrl() + "/rest/api/2/version/10002").add("name", "New Version 5").toMap());

        final Set<Map<String, String>> found = new HashSet<Map<String, String>>(2);
        found.add(MapBuilder.<String, String>newBuilder().add("self", versions.get(0).get("self")).add("name", versions.get(0).get("name")).toMap());
        found.add(MapBuilder.<String, String>newBuilder().add("self", versions.get(1).get("self")).add("name", versions.get(1).get("name")).toMap());

        assertEquals(found, expected);

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10011", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        multiVersion = issue.fields.get("customfield_10011");
        assertNull(multiVersion);

        // Check update to version in wrong project returns errror
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .customField(10011L, Collections.singletonList(EasyMap.build("id", "10005"))));
        Response response = issueClient.updateResponse("HSP-1", updateRequest);
        assertEquals(400, response.statusCode);
        assertEquals("Version id '10005' is not valid", response.entity.errors.get("customfield_10011"));
    }

    public void testVersion() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        Map<String, String> version = issue.fields.get("customfield_10009");
        Map<String, String> v4 = version;

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:version", issue.schema.get("customfield_10009").getCustom());
        assertEquals(getBaseUrl() + "/rest/api/2/version/10001", v4.get("self"));
        assertEquals("New Version 4", v4.get("name"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10009", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        version = issue.fields.get("customfield_10009");
        assertNull(version);

        // Check update to version in wrong project returns errror
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().fields(new IssueFields()
                .customField(10009L, EasyMap.build("id", "10005")));
        Response response = issueClient.updateResponse("HSP-1", updateRequest);
        assertEquals(400, response.statusCode);
        assertEquals("Version id '10005' is not valid", response.entity.errors.get("customfield_10009"));
    }

    public void testUserPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        Map<String, String> userPicker = issue.fields.get("customfield_10022");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", issue.schema.get("customfield_10022").getCustom());
        Map<String, String> fred = userPicker;
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=fred", fred.get("self"));
        assertEquals(FRED_USERNAME, fred.get("name"));
        assertEquals(FRED_FULLNAME, fred.get("displayName"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10022", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        userPicker = issue.fields.get("customfield_10022");
        assertNull(userPicker);
    }

    public void testUrl() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema, Issue.Expand.renderedFields);
        String url = issue.fields.get("customfield_10010");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:url", issue.schema.get("customfield_10010").getCustom());
        assertEquals("http://www.atlassian.com", url);

        String renderedField = issue.renderedFields.getCustomField("customfield_10010");
        assertNull(renderedField); // url extends GenericTest but technically is not renderable so should not be included

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10010", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        url = issue.fields.get("customfield_10010");
        assertNull(url);
    }

    public void testMultiSelect() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        List<Map<String, String>> multiSelect = issue.fields.get("customfield_10017");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multiselect", issue.schema.get("customfield_10017").getCustom());
        assertEquals(2, multiSelect.size());

        boolean option2Present = false;
        boolean option3Present = false;
        List<String> expectedValues = EasyList.build("Option 2", "Option 3");
        for (Map<String, String> option : multiSelect)
        {
            assertTrue(expectedValues.contains(option.get("value")));
            if (option.get("value").equals("Option 2"))
            {
                option2Present = true;
                assertEquals("10007", option.get("id"));
                assertEquals(getBaseUrl() + "/rest/api/2/customFieldOption/10007", option.get("self"));
            }
            if (option.get("value").equals("Option 3"))
            {
                option3Present = true;
                assertEquals("10008", option.get("id"));
                assertEquals(getBaseUrl() + "/rest/api/2/customFieldOption/10008", option.get("self"));
            }
        }
        assertTrue((option2Present && option3Present));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10017", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        multiSelect = issue.fields.get("customfield_10017");
        assertNull(multiSelect);
    }

    public void testMultiCheckboxes() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        List<Map<String, String>> multiCheckbox = issue.fields.get("customfield_10016");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes", issue.schema.get("customfield_10016").getCustom());
        assertEquals(1, multiCheckbox.size());
        Map<String, String> option1 = multiCheckbox.get(0);
        assertEquals("10014", option1.get("id"));
        assertEquals(getBaseUrl() + "/rest/api/2/customFieldOption/10014", option1.get("self"));
        assertEquals("check out my stats", option1.get("value"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10016", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        multiCheckbox = issue.fields.get("customfield_10016");
        assertNull(multiCheckbox);
    }

    public void testMultiUserPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        List<Map<String, String>> multiUser = issue.fields.get("customfield_10006");
        List<Map<String, String>> users = multiUser;

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker", issue.schema.get("customfield_10006").getCustom());
        assertEquals(2, users.size());

        Map<String, String> admin = users.get(0);
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", admin.get("self"));
        assertEquals(ADMIN_USERNAME, admin.get("name"));
        assertEquals(ADMIN_FULLNAME, admin.get("displayName"));

        Map<String, String> fred = users.get(1);
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=fred", fred.get("self"));
        assertEquals(FRED_USERNAME, fred.get("name"));
        assertEquals(FRED_FULLNAME, fred.get("displayName"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10006", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        multiUser = issue.fields.get("customfield_10006");
        assertNull(multiUser);
    }

    public void testMultiGroupPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        List<Map<String, String>> groups = issue.fields.get("customfield_10005");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker", issue.schema.get("customfield_10005").getCustom());
        assertEquals(2, groups.size());

        Map<String, String> developers = groups.get(0);
        assertEquals("jira-developers", developers.get("name"));
        Map<String, String> users = groups.get(1);
        assertEquals("jira-users", users.get("name"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10005", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        groups = issue.fields.get("customfield_10005");
        assertNull(groups);
    }

    public void testGroupPicker() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        Map<String, String> group = issue.fields.get("customfield_10002");
        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker", issue.schema.get("customfield_10002").getCustom());
        assertEquals("jira-developers", group.get("name"));

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10002", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        group = issue.fields.get("customfield_10002");
        assertNull(group);
    }

    public void testCascadingSelect() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        Map<String, Object> option = issue.fields.get("customfield_10000");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect", issue.schema.get("customfield_10000").getCustom());

        if (option.get("value").equals("Option 2"))
        {
            assertEquals("Option 2", option.get("value"));
            assertEquals(getBaseUrl() + "/rest/api/2/customFieldOption/10001", option.get("self"));
            assertNull(option.get("children"));

            Map<String, Object> child = (Map<String, Object>) option.get("child");
            assertEquals("Sub-option I", child.get("value"));
            assertEquals(getBaseUrl() + "/rest/api/2/customFieldOption/10004", child.get("self"));
            assertNull(child.get("child"));
            assertNull(child.get("children"));
        }

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10000", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        option = issue.fields.get("customfield_10000");
        assertNull(option);
    }

    public void testLabels() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        Issue issue = issueClient.get("HSP-1", Issue.Expand.schema);
        List<String> labels = issue.fields.get("customfield_10004");

        assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:labels", issue.schema.get("customfield_10004").getCustom());
        assertEquals(Arrays.asList("wack", "whoa"), labels);

        // Check clearing the field
        issueClient.edit("HSP-1", new IssueUpdateRequest().update("customfield_10004", new FieldOperation("set", null)));
        issue = issueClient.get("HSP-1", Issue.Expand.schema);
        labels = issue.fields.get("customfield_10004");
        assertNull(labels);
    }

    public void testEmptyFieldsReturned() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");
        navigation.issue().createIssue("homosapien", "Bug", "some issue");

        Issue issue = issueClient.get("HSP-2");
        Issue.Fields fields = issue.fields;
        assertTrue(fields.has("customfield_10000"));
        assertNull(fields.get("customfield_10000"));
        assertTrue(fields.has("customfield_10012"));
        assertNull(fields.get("customfield_10012"));
        assertTrue(fields.has("customfield_10001"));
        assertNull(fields.get("customfield_10001"));
        assertTrue(fields.has("customfield_10013"));
        assertNull(fields.get("customfield_10013"));
        assertTrue(fields.has("customfield_10002"));
        assertNull(fields.get("customfield_10002"));
        assertTrue(fields.has("customfield_10003"));
        assertNull(fields.get("customfield_10003"));
        assertTrue(fields.has("customfield_10016"));
        assertNull(fields.get("customfield_10016"));
        assertTrue(fields.has("customfield_10017"));
        assertNull(fields.get("customfield_10017"));
        assertTrue(fields.has("customfield_10005"));
        assertNull(fields.get("customfield_10005"));
        assertTrue(fields.has("customfield_10006"));
        assertNull(fields.get("customfield_10006"));
        assertTrue(fields.has("customfield_10018"));
        assertNull(fields.get("customfield_10018"));
        assertTrue(fields.has("customfield_10007"));
        assertNull(fields.get("customfield_10007"));
        assertTrue(fields.has("customfield_10019"));
        assertNull(fields.get("customfield_10019"));
        assertTrue(fields.has("customfield_10008"));
        assertNull(fields.get("customfield_10008"));
        assertTrue(fields.has("customfield_10020"));
        assertNull(fields.get("customfield_10020"));
        assertTrue(fields.has("customfield_10009"));
        assertNull(fields.get("customfield_10009"));
        assertTrue(fields.has("customfield_10021"));
        assertNull(fields.get("customfield_10021"));
        assertTrue(fields.has("customfield_10010"));
        assertNull(fields.get("customfield_10010"));
        assertTrue(fields.has("customfield_10022"));
        assertNull(fields.get("customfield_10022"));
        assertTrue(fields.has("customfield_10011"));
        assertNull(fields.get("customfield_10011"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
