package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.UserJson;
import com.atlassian.jira.testkit.client.restclient.Visibility;
import com.atlassian.jira.testkit.client.restclient.Worklog;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.api.issue.ResourceRef.withId;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withName;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceWorklog extends RestFuncTest
{
    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestWorklogAndTimeTracking.xml");
    }

    public void testView() throws Exception
    {
        final Issue json = issueClient.get("HSP-1");

        final List<Worklog> worklogs = json.fields.worklog.worklogs;
        assertEquals(1, worklogs.size());

        final Worklog log = worklogs.get(0);
        assertNotNull(log.self);
        assertEquals("I'm finished finally!", log.comment);
        assertEquals("2h", log.timeSpent);
        assertEqualDateStrings("2010-05-24T09:52:41.092+1000", log.created);
        assertEqualDateStrings("2010-05-24T09:52:41.092+1000", log.updated);
        assertEqualDateStrings("2010-05-24T09:52:00.000+1000", log.started);

        final UserJson author = log.author;
        assertNotNull(author.self);
        assertEquals(ADMIN_USERNAME, author.name);
        assertEquals(ADMIN_FULLNAME, author.displayName);

        final UserJson updateAuthor = log.updateAuthor;
        assertNotNull(updateAuthor.self);
        assertEquals(ADMIN_USERNAME, updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, updateAuthor.displayName);
    }

    public void testRenderedView()
    {
        Issue expandedHsp = issueClient.get("HSP-1", Issue.Expand.renderedFields);

        final List<Worklog> worklogs = expandedHsp.renderedFields.worklog.worklogs;
        assertEquals(1, worklogs.size());

        final Worklog log = worklogs.get(0);
        assertNotNull(log.self);
        assertEquals("I&#39;m finished finally!", log.comment);
        assertEquals("2 hours", log.timeSpent);
        assertEqualDateStrings("24/May/10 9:52 AM", log.created);
        assertEqualDateStrings("24/May/10 9:52 AM", log.updated);
        assertEqualDateStrings("24/May/10 9:52 AM", log.started);
    }

    /**
     * JRADEV-2313.
     *
     * @throws Exception junit makes me
     */
    public void testViewLoggedByDeletedUser() throws Exception
    {
        final Issue json = issueClient.get("HSP-3");

        final List<Worklog> worklogs = json.fields.worklog.worklogs;
        assertEquals(1, worklogs.size());

        final Worklog log = worklogs.get(0);
        assertNotNull(log.self);
        assertEquals("spent a whole minute on this", log.comment);
        assertEquals("1m", log.timeSpent);
        assertEqualDateStrings("2010-07-12T12:47:39.198+1000", log.created);
        assertEqualDateStrings("2010-07-12T12:47:39.198+1000", log.updated);
        assertEqualDateStrings("2010-07-12T12:47:00.000+1000", log.started);

        UserJson author = log.author;
        assertEquals("deleted", author.name);

        UserJson updateAuthor = log.updateAuthor;
        assertEquals("deleted", updateAuthor.name);
    }

    public void testAddLogDuringCreate()
    {
        Date date = new Date();
        final IssueUpdateRequest request = getSystemFieldsByNameAndId(date);
        issueClient.loginAs("admin");
        final IssueCreateResponse created = issueClient.create(request);

        // read back the fields and compare it
        final Issue newIssue = issueClient.get(created.key());
        assertEquals(1, newIssue.fields.worklog.worklogs.size());
        final Worklog worklog = newIssue.fields.worklog.worklogs.get(0);
        assertEquals("hello", worklog.comment);
        assertEquals("50m", worklog.timeSpent);
        assertEquals("group", worklog.visibility.type);
        assertEquals("jira-administrators", worklog.visibility.value);
//        assertEqualDateStrings(asTimeString(date), worklog.started); times are rounded for some reason...
        assertEquals("4h", newIssue.fields.timetracking.remainingEstimate);
    }

    public void testAddDuringEdit()
    {
        Date date = new Date();
        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();
        operations.put("worklog", addWorkLog(asTimeString(date), "50m"));
        issueUpdateRequest.update(operations);

        issueClient.edit("HSP-1", issueUpdateRequest);

        final Issue newIssue = issueClient.get("HSP-1");
        assertEquals(2, newIssue.fields.worklog.worklogs.size());
        final Worklog worklog = newIssue.fields.worklog.worklogs.get(1);
        assertEquals("hello", worklog.comment);
        assertEquals("50m", worklog.timeSpent);
        assertEquals("group", worklog.visibility.type);
        assertEquals("jira-administrators", worklog.visibility.value);
//        assertEqualDateStrings(asTimeString(date), worklog.started); times are rounded for some reason..
        assertEquals("4h", newIssue.fields.timetracking.remainingEstimate);
    }

    private IssueUpdateRequest getSystemFieldsByNameAndId(Date date)
    {
        final IssueFields fields = new IssueFields();
        final Map<String, List<FieldOperation>> updates = new LinkedHashMap<String, List<FieldOperation>>();
        fields.project(withId("10000")); // TST
        fields.issueType(withId("1"));  // Bug
        fields.reporter(withName("admin"));
        fields.summary("my first fields");
        updates.put("worklog", addWorkLog(asTimeString(date), "50m"));
        return new com.atlassian.jira.rest.api.issue.IssueUpdateRequest().fields(fields).update(updates);
    }

    private List<FieldOperation> addWorkLog(String started, String timeSpent)
    {
        Visibility visibility = new Visibility();
        visibility.type="group";
        visibility.value="jira-administrators";

        final Map<Object, Object> worklog = MapBuilder.newBuilder()
                .add("started", started)
                .add("timeSpent", timeSpent)
                .add("adjustEstimate", "new")
                .add("newEstimate", "4h")
                .add("comment", "hello")
                .add("visibility", visibility).toMap();

        return Arrays.asList(new FieldOperation().operation("add").value(worklog));
    }

    private static String asTimeString(@Nullable Date date)
    {
        return date != null ? new SimpleDateFormat(TIME_FORMAT).format(date) : null;
    }
}
