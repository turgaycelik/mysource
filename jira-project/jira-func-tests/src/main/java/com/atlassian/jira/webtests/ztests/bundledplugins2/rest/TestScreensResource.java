package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.MoveField;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.ScreenField;
import com.atlassian.jira.testkit.client.restclient.ScreenTab;
import com.atlassian.jira.testkit.client.restclient.ScreensClient;
import com.atlassian.jira.webtests.LicenseKeys;
import com.google.common.collect.Lists;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestScreensResource extends RestFuncTest
{

    private ScreensClient screensClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        screensClient = new ScreensClient(getEnvironmentData(), 1L);
        administration.restoreDataWithLicense("TestScreensResource.xml", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        backdoor.screens()
                .addTabToScreen("Default Screen", "Scotts Tab")
                .addTabToScreen("Default Screen", "Scotts Tab 2");
    }

    public void testGettingTabsAsAdmin() throws Exception
    {

        final List<ScreenTab> allTabs = screensClient.getAllTabs();
        assertEquals(Lists.newArrayList(new ScreenTab("Field Tab"), new ScreenTab("Scotts Tab"),
                new ScreenTab("Scotts Tab 2")), allTabs);
    }

    public void testGettingTabsAsProjectAdmin() throws Exception
    {
        screensClient.loginAs("bob", "bob");
        final List<ScreenTab> allTabs = screensClient.getAllTabs("BP");
        assertEquals(Lists.newArrayList(new ScreenTab("Field Tab"), new ScreenTab("Scotts Tab"),
                new ScreenTab("Scotts Tab 2")), allTabs);
    }

    public void testGettingTabsAsProjectAdminDifferentProject() throws Exception
    {
        screensClient.loginAs("bob", "bob");
        final Response allTabsResponse = screensClient.getAllTabsResponse("MKY");
        assertEquals(401, allTabsResponse.statusCode);
    }

    public void testGettingTabsNotAdminOrProjectAdminError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        final Response allTabsResponse = screensClient.getAllTabsResponse();
        assertEquals(401, allTabsResponse.statusCode);
    }

    public void testCreatingTab() throws Exception
    {
        final ScreenTab tab = screensClient.createTab("My Tab");
        assertEquals(tab.name, "My Tab");
        final List<ScreenTab> allTabs = screensClient.getAllTabs();
        assertEquals(Lists.newArrayList(new ScreenTab("Field Tab"), new ScreenTab("Scotts Tab"),
                new ScreenTab("Scotts Tab 2"), new ScreenTab("My Tab")), allTabs);
    }

    public void testCreateTabError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        Response res = screensClient.createTabWithResponse("My Tab");
        assertEquals(res.statusCode, 401);
        screensClient.loginAs("admin", "admin");
        res = screensClient.createTabWithResponse("Field Tab");
        assertEquals(res.statusCode, 400);
        assertEquals("Tab Field Tab already exists", res.entity.errors.get("name"));
        res = screensClient.createTabWithResponse("");
        assertEquals(res.statusCode, 400);
        assertEquals("Tab name cannot be empty", res.entity.errors.get("name"));
    }

    public void testRemoveTab() throws Exception
    {
        screensClient.deleteTab(10000L);
        screensClient.deleteTab(10010L);
        final List<ScreenTab> allTabs = screensClient.getAllTabs();
        assertEquals(Lists.newArrayList(new ScreenTab("Scotts Tab 2")), allTabs);
    }

    public void testRemoveTabError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        Response res = screensClient.deleteTabWithResponse(10000L);
        assertEquals(res.statusCode, 401);
    }

    public void testRenameTab() throws Exception
    {
        screensClient.renameTab(10000L, "Renamed Tab");
        assertTabs(Lists.newArrayList("Renamed Tab", "Scotts Tab", "Scotts Tab 2"), screensClient.getAllTabs());
    }

    public void testRenameTabError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        Response res = screensClient.renameTabWithResponse(10000L, "My Tab");
        assertEquals(res.statusCode, 401);
        screensClient.loginAs("admin", "admin");
        res = screensClient.renameTabWithResponse(10000L, "Scotts Tab");
        assertEquals(res.statusCode, 400);
        assertEquals("Tab Scotts Tab already exists", res.entity.errors.get("name"));
        res = screensClient.renameTabWithResponse(10000L, "");
        assertEquals(res.statusCode, 400);
        assertEquals("Tab name cannot be empty", res.entity.errors.get("name"));
    }

    public void testMoveTab() throws Exception
    {
        screensClient.moveTab(10000L, 2);
        assertTabs(Lists.newArrayList("Scotts Tab", "Scotts Tab 2", "Field Tab"), screensClient.getAllTabs());
    }

    public void testMoveTabError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        Response res = screensClient.moveTabWithResponse(10000L, 1);
        assertEquals(res.statusCode, 401);
        screensClient.loginAs("admin", "admin");
        res = screensClient.moveTabWithResponse(10000L, -1);
        assertEquals(res.statusCode, 400);
        assertEquals("Incorrect position, tab position much be between 0 and 2", res.entity.errorMessages.get(0));
        res = screensClient.moveTabWithResponse(10000L, 5);
        assertEquals(res.statusCode, 400);
        assertEquals("Incorrect position, tab position much be between 0 and 2", res.entity.errorMessages.get(0));
    }

    public void testAddingFields() throws Exception
    {
        assertFields(Lists.newArrayList("Linked Issues", "Resolution"), screensClient.getAvailableFields());
        screensClient.addField(10000L, "issuelinks");
        assertFields(Lists.newArrayList("Resolution"), screensClient.getAvailableFields());
        screensClient.addField(10010L, "resolution");
        assertFields(new ArrayList<String>(), screensClient.getAvailableFields());
        final List<ScreenField> fieldsTabFields = screensClient.getFields(10000L);

        assertFields(Lists.newArrayList(
                "Summary",
                "Issue Type",
                "Security Level",
                "Priority",
                "Due Date",
                "Component/s",
                "Affects Version/s",
                "Fix Version/s",
                "Assignee",
                "Reporter",
                "Environment",
                "Description",
                "Time Tracking",
                "Attachment",
                "Labels",
                "Linked Issues"), fieldsTabFields);

        final List<ScreenField> scottsTabFields = screensClient.getFields(10010L);
        assertFields(Lists.newArrayList("Resolution"), scottsTabFields);
    }

    public void testAddingFieldError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        Response res = screensClient.addFieldWithResponse(10000L, "Linked Issues");
        assertEquals(res.statusCode, 401);
        screensClient.loginAs("admin", "admin");
        res = screensClient.addFieldWithResponse(10000L, "summary");
        assertEquals(res.statusCode, 400);
        assertEquals("The field with id summary already exists on the screen.", res.entity.errors.get("fieldId"));
        res = screensClient.addFieldWithResponse(10000L, "fgdfdgfds");
        assertEquals(res.statusCode, 400);
        assertEquals("Invalid Field Id: fgdfdgfds", res.entity.errors.get("fieldId"));
    }

    public void testRemovingFields() throws Exception
    {
        screensClient.removeField(10000L, "summary");
        assertFields(Lists.newArrayList("Linked Issues", "Resolution", "Summary"), screensClient.getAvailableFields());
        screensClient.removeField(10000L, "issuetype");
        assertFields(Lists.newArrayList("Issue Type", "Linked Issues", "Resolution", "Summary"), screensClient.getAvailableFields());
        final List<ScreenField> fieldsTabFields = screensClient.getFields(10000L);

        assertFields(Lists.newArrayList(
                "Security Level",
                "Priority",
                "Due Date",
                "Component/s",
                "Affects Version/s",
                "Fix Version/s",
                "Assignee",
                "Reporter",
                "Environment",
                "Description",
                "Time Tracking",
                "Attachment",
                "Labels"), fieldsTabFields);
    }

    public void testRemovingFieldError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        Response res = screensClient.removeFieldWithResponse(10000L, "Linked Issues");
        assertEquals(res.statusCode, 401);
        screensClient.loginAs("admin", "admin");
        res = screensClient.removeFieldWithResponse(10000L, "issuelinks");
        assertEquals(res.statusCode, 400);
        assertEquals("The field with id issuelinks does not exist on this tab.", res.entity.errorMessages.get(0));
        res = screensClient.removeFieldWithResponse(10000L, "fgdfdgfds");
        assertEquals(res.statusCode, 400);
        assertEquals("The field with id fgdfdgfds does not exist on this tab.", res.entity.errorMessages.get(0));
    }

    public void testMoveField() throws Exception
    {
        // Move assignee after summary
        MoveField moveField = new MoveField();
        moveField.after = new URI("/summary");
        screensClient.moveField(10000L, "assignee", moveField);

        // Move summary last
        moveField = new MoveField();
        moveField.position = MoveField.Position.Last;
        screensClient.moveField(10000L, "summary", moveField);

        // Move labels first
        moveField = new MoveField();
        moveField.position = MoveField.Position.First;
        screensClient.moveField(10000L, "labels", moveField);

        final List<ScreenField> fields = screensClient.getFields(10000L);
        assertFields(Lists.newArrayList(
                "Labels",
                "Assignee",
                "Issue Type",
                "Security Level",
                "Priority",
                "Due Date",
                "Component/s",
                "Affects Version/s",
                "Fix Version/s",
                "Reporter",
                "Environment",
                "Description",
                "Time Tracking",
                "Attachment",
                "Summary"), fields);
    }

    public void testMoveFieldError() throws Exception
    {
        screensClient.loginAs("fred", "fred");
        MoveField moveField = new MoveField();
        moveField.after = new URI("/summary");
        Response res = screensClient.moveFieldWithResponse(10000L, "assignee", moveField);
        assertEquals(res.statusCode, 401);
        screensClient.loginAs("admin", "admin");
        moveField = new MoveField();
        moveField.after = new URI("/reporter");
        res = screensClient.moveFieldWithResponse(10000L, "issuelinks", moveField);
        assertEquals(res.statusCode, 400);
        assertEquals("The field with id issuelinks does not exist on this tab.", res.entity.errorMessages.get(0));
        moveField.after = new URI("/issuelinks");
        res = screensClient.moveFieldWithResponse(10000L, "reporter", moveField);
        assertEquals(res.statusCode, 400);
        assertEquals("The field with id reporter cannot be moved after the issuelinks as it doesn't exist.", res.entity.errorMessages.get(0));

    }

    // JRADEV-18939
    public void testCopyAfterTabRenameHasSameTabNames()
    {
        screensClient.renameTab(10000L, "Renamed Tab");
        assertTabs(Lists.newArrayList("Renamed Tab", "Scotts Tab", "Scotts Tab 2"), screensClient.getAllTabs());

        Long copyId = backdoor.screens().copy("Default Screen", "Default Screen Copy", "-");

        // Copy tab names are the same.
        ScreensClient copyClient = new ScreensClient(getEnvironmentData(), copyId);
        assertTabs(Lists.newArrayList("Renamed Tab", "Scotts Tab", "Scotts Tab 2"), copyClient.getAllTabs());

        // Original screen tab names didn't change.
        assertTabs(Lists.newArrayList("Renamed Tab", "Scotts Tab", "Scotts Tab 2"), screensClient.getAllTabs());
    }

    public void testGetAllFieldsAsAdmin() throws Exception
    {
        final List<ScreenField> allFields = screensClient.getFields(10000L);
        assertFields(Lists.newArrayList(
                "Summary",
                "Issue Type",
                "Security Level",
                "Priority",
                "Due Date",
                "Component/s",
                "Affects Version/s",
                "Fix Version/s",
                "Assignee",
                "Reporter",
                "Environment",
                "Description",
                "Time Tracking",
                "Attachment",
                "Labels"), allFields);
    }

    public void testGetAllFieldsAsProjectAdmin() throws Exception
    {
        screensClient.loginAs("bob", "bob");
        final List<ScreenField> allFields = screensClient.getFields(10000L, "BP");
        assertFields(Lists.newArrayList(
                "Summary",
                "Issue Type",
                "Security Level",
                "Priority",
                "Due Date",
                "Component/s",
                "Affects Version/s",
                "Fix Version/s",
                "Assignee",
                "Reporter",
                "Environment",
                "Description",
                "Time Tracking",
                "Attachment",
                "Labels"), allFields);
    }

    public void testGetAllFieldsAsProjectAdminDifferentProject() throws Exception
    {
        screensClient.loginAs("bob", "bob");
        final Response allFieldsResponse = screensClient.getFieldsResponse(10000L, "MKY");
        assertEquals(401, allFieldsResponse.statusCode);
    }

    private void assertTabs(List<String> expecting, List<ScreenTab> actual)
    {
        assertEquals(expecting, getTabLabels(actual));
    }

    private List<String> getTabLabels(List<ScreenTab> tabs)
    {
        List<String> labels = new ArrayList<String>();

        for (ScreenTab tab : tabs)
        {
            labels.add(tab.name);
        }
        return labels;
    }

    private void assertFields(List<String> expecting, List<ScreenField> actual)
    {
        assertEquals(expecting, getFieldLabels(actual));
    }

    private List<String> getFieldLabels(List<ScreenField> fields)
    {
        List<String> labels = new ArrayList<String>();

        for (ScreenField field : fields)
        {
            labels.add(field.name);
        }
        return labels;
    }


}
