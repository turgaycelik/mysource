package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.UserJson;
import com.atlassian.jira.testkit.client.restclient.Visibility;
import com.atlassian.jira.testkit.client.restclient.Worklog;
import com.atlassian.jira.testkit.client.restclient.WorklogClient;
import com.atlassian.jira.testkit.client.restclient.WorklogWithPaginationBean;
import com.atlassian.jira.util.collect.MapBuilder;

import org.joda.time.DateTime;

/**
 * Func test for Worklog resource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST, Category.WORKLOGS })
public class TestWorklogResource extends RestFuncTest
{
    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final String ISSUE_KEY = "HSP-1";
    private WorklogClient worklogClient;
    private IssueClient issueClient;

    public void testViewWorklog() throws Exception
    {
        Worklog worklog = getDefaultWorklogFromJira();
        assertTrue(getDefaultWorklog().equals(worklog));
    }

    public void testViewWorklogNotFound() throws Exception
    {
        // {"errorMessages":["Cannot find worklog with id: '123'."],"errors":[]}
        Response response123 = worklogClient.getResponse(ISSUE_KEY, "123");
        assertEquals(404, response123.statusCode);
        assertEquals(1, response123.entity.errorMessages.size());
        assertEquals("Cannot find worklog with id: '123'.", response123.entity.errorMessages.get(0));

        // {"errorMessages":["Cannot find worklog with id: 'abc'."],"errors":[]}
        Response responseAbc = worklogClient.getResponse(ISSUE_KEY, "abc");
        assertEquals(404, responseAbc.statusCode);
        assertEquals(1, responseAbc.entity.errorMessages.size());
        assertEquals("Cannot find worklog with id: 'abc'.", responseAbc.entity.errorMessages.get(0));
    }

    public void testViewAllWorklogs() throws Exception
    {
        WorklogWithPaginationBean response = worklogClient.getAll(ISSUE_KEY);
        assertEquals(new Integer(0), response.startAt);
        assertEquals(new Integer(1), response.maxResults);
        assertEquals(new Integer(1), response.total);

        assertEquals(1, response.worklogs.size());
        Worklog worklog = response.worklogs.get(0);
        assertTrue(getDefaultWorklog().equals(worklog));
    }

    public void testAddWorklog() throws Exception
    {
        // add worklog
        Date now = new Date();
        Worklog toAdd = new Worklog();
        toAdd.timeSpent = "1h";
        toAdd.started = asTimeString(now);
        toAdd.comment = "This is my comment";
        addWorkLog(toAdd, now);
    }

    public void testAddWorklogUsingSeconds() throws Exception
    {
        // add worklog
        Date now = new Date();
        Worklog toAdd = new Worklog();
        toAdd.timeSpentSeconds = 3600L;
        toAdd.started = asTimeString(now);
        toAdd.comment = "This is my comment";
        addWorkLog(toAdd, now);
    }

    private void addWorkLog(Worklog toAdd, Date now)
    {
        //assert fields correct in response
        Response<Worklog> response = worklogClient.post(ISSUE_KEY, toAdd);
        assertEquals(201, response.statusCode);
        assertNotNull(response.body);
        Worklog worklog = response.body;
        String id = worklog.id;
        assertEquals(getBaseUrlPlus("rest/api/2/issue/10000/worklog/" + id), worklog.self);
        assertEquals(getBaseUrlPlus("rest/api/2/user?username=admin"), worklog.author.self);
        assertEquals(ADMIN_USERNAME, worklog.author.name);
        assertEquals(ADMIN_FULLNAME, worklog.author.displayName);
        assertEquals(getBaseUrlPlus("rest/api/2/user?username=admin"), worklog.updateAuthor.self);
        assertEquals(ADMIN_USERNAME, worklog.updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, worklog.updateAuthor.displayName);
        assertNotNull(worklog.created);
        assertNotNull(worklog.updated);
        assertEqualDateStrings(asTimeString(now), worklog.started);
        assertEquals("1h", worklog.timeSpent);

        // reget worklog, confirm correct.
        Worklog response2 = worklogClient.get(ISSUE_KEY, worklog.id);
        assertTrue(worklog.equals(response2));

        //error case - missing field
        toAdd = new Worklog();
        toAdd.started = asTimeString(now);
        response = worklogClient.post(ISSUE_KEY, toAdd);
        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errors.size()); // todo check error message.
    }

    public void testAddWorklogWhenRoleDoesntExist()
    {
        Worklog worklog = new Worklog();
        worklog.timeSpent = "1h";
        worklog.visibility = new Visibility();
        worklog.visibility.type = "role";
        worklog.visibility.value = "not-existing-role";

        Response post = worklogClient.post(ISSUE_KEY, worklog);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testAddWorklogWhenGroupDoesntExist()
    {
        Worklog worklog = new Worklog();
        worklog.timeSpent = "1h";
        worklog.visibility = new Visibility();
        worklog.visibility.type = "group";
        worklog.visibility.value = "not-existing-group";

        Response post = worklogClient.post(ISSUE_KEY, worklog);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testEditWorklog() throws Exception
    {
        DateTime date = new DateTime();
        date.plusDays(10);

        Worklog worklog = getDefaultWorklogFromJira();

        worklog.timeSpent = "10h";
        worklog.timeSpentSeconds = null;
        worklog.started = asTimeString(date.toDate());
        worklog.comment = "This is the new comment";

        Response<Worklog> post = worklogClient.put(ISSUE_KEY, worklog);
        assertEquals(200, post.statusCode);
        assertTrue(worklog.equals(post.body));

        // assert missing fields ignored
        Worklog freshWorklog = new Worklog();
        freshWorklog.id = worklog.id;
        post = worklogClient.put(ISSUE_KEY, freshWorklog);
        assertTrue(worklog.equals(post.body));
    }

    public void testEditWorklogToRoleWhichDoesntExist()
    {
        Worklog worklog = getDefaultWorklogFromJira();
        worklog.visibility = new Visibility();
        worklog.visibility.type = "role";
        worklog.visibility.value = "not-existing-role";

        Response post = worklogClient.post(ISSUE_KEY, worklog);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testEditWorklogToGroupWhichDoesntExist()
    {
        Worklog worklog = getDefaultWorklogFromJira();
        worklog.timeSpent = "1h";
        worklog.visibility = new Visibility();
        worklog.visibility.type = "group";
        worklog.visibility.value = "not-existing-group";

        Response post = worklogClient.post(ISSUE_KEY, worklog);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testAddWorklogIssueFields() throws Exception
    {
        // original estimate should be 6 hours. with a 2 hour work log existing
        Worklog toAdd = new Worklog();
        toAdd.timeSpent = "1h";

        Issue issue = issueClient.get(ISSUE_KEY);
        long timeEstimate = DateUtils.getDuration(issue.fields.timetracking.remainingEstimate);
        long totalTime = DateUtils.getDuration(issue.fields.timetracking.timeSpent);

        // add 1 hour, no adjust
        totalTime = totalTime + 60;
        timeEstimate = timeEstimate - 60;

        worklogClient.post(ISSUE_KEY, toAdd);
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // add 1 hour, auto adjust defined
        totalTime = totalTime + 60;
        timeEstimate = timeEstimate - 60;

        worklogClient.post(ISSUE_KEY, toAdd, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "auto").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // add 1 hour, leave adjust defined
        totalTime = totalTime + 60;

        worklogClient.post(ISSUE_KEY, toAdd, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "leave").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // add 1 hour, new adjust defined
        totalTime = totalTime + 60;
        timeEstimate = TimeUnit.HOURS.toMinutes(6);

        worklogClient.post(ISSUE_KEY, toAdd, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "new").add("newEstimate", "6h").toMap());

        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // add 1 hour, new adjust defined, with spaces
        totalTime = totalTime + 60;
        timeEstimate = TimeUnit.HOURS.toMinutes(5) + 10;

        worklogClient.post(ISSUE_KEY, toAdd, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "new").add("newEstimate", "5h%2010m").toMap());

        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // add 1 hour, new adjust defined, no estimate provided
        Response response = worklogClient.post(ISSUE_KEY, toAdd, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "new").toMap());
        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errors.size());
        assertEquals("Value is required when adjustEstimate is new", response.entity.errors.get("newEstimate"));
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // add 1 hour, manual adjust defined
        totalTime = totalTime + 60;
        timeEstimate = timeEstimate - 120;

        worklogClient.post(ISSUE_KEY, toAdd, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "manual").add("reduceBy", "2h").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // add 1 hour. manual adjust, no adjust value provided
        response = worklogClient.post(ISSUE_KEY, toAdd, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "manual").toMap());
        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errors.size());
        assertEquals("A value is required for manual estimate adjustment", response.entity.errors.get("reduceBy"));
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);
    }

    public void testEditWorklogIssueFields() throws Exception
    {
        // original estimate should be 6 hours. with a 2 hour work log existing
        Issue issue = issueClient.get(ISSUE_KEY);
        long timeEstimate = DateUtils.getDuration(issue.fields.timetracking.remainingEstimate);
        long totalTime = DateUtils.getDuration(issue.fields.timetracking.timeSpent);

        Worklog worklog = getDefaultWorklogFromJira();

        // change to 1 hour, no adjust
        worklog.timeSpent = "1h";
        totalTime = totalTime - 60;
        timeEstimate = timeEstimate + 60;

        worklogClient.put(ISSUE_KEY, worklog);
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // change to 3 hour, no adjust
        worklog.timeSpent = "3h";
        totalTime = totalTime + 120;
        timeEstimate = timeEstimate - 120;

        worklogClient.put(ISSUE_KEY, worklog);
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // change to 1 hour, auto adjust defined
        worklog.timeSpentSeconds = 3600L;
        totalTime = totalTime - 120;
        timeEstimate = timeEstimate + 120;

        worklogClient.put(ISSUE_KEY, worklog, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "auto").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // change to 2 hour, leave adjust defined
        worklog.timeSpentSeconds = 7200L;
        totalTime = totalTime + 60;

        worklogClient.put(ISSUE_KEY, worklog, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "leave").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // change to 1 hour, new adjust defined
        worklog.timeSpent = "1h";
        totalTime = totalTime - 60;
        timeEstimate = TimeUnit.HOURS.toMinutes(8);

        worklogClient.put(ISSUE_KEY, worklog, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "new").add("newEstimate", "8h").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

//        change to 1 hour, manual adjust defined.  Should fail
        Response response = worklogClient.put(ISSUE_KEY, worklog, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "manual").add("reduceBy", "2h").toMap());
        assertEquals(400, response.statusCode);
        assertEquals(1, response.entity.errors.size());
        assertEquals("Manual adjustment of remaining estimate not allowed when editing a worklog", response.entity.errors.get("adjustEstimate"));
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);
    }

    public void testDeleteWorklog() throws Exception
    {
        Date now = new Date();
        Worklog toAdd = new Worklog();
        toAdd.timeSpent = "1h";
        toAdd.started = asTimeString(now);

        Response<Worklog> addReponse = worklogClient.post(ISSUE_KEY, toAdd);

        Response deleteResponse = worklogClient.delete(ISSUE_KEY, addReponse.body);
        assertEquals(204, deleteResponse.statusCode);

        Response response = worklogClient.getResponse(ISSUE_KEY, addReponse.body.id);
        assertEquals(404, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size());
        assertTrue(response.entity.errorMessages.get(0).startsWith("Cannot find worklog with id"));

        deleteResponse = worklogClient.delete(ISSUE_KEY, addReponse.body);
        assertEquals(404, deleteResponse.statusCode);
        assertEquals(1, deleteResponse.entity.errorMessages.size());
        assertTrue(deleteResponse.entity.errorMessages.get(0).startsWith("Cannot find worklog with id"));
    }


    public void testDeleteWorklogAdjustEstimate() throws Exception
    {
        // original estimate should be 6 hours. with a 2 hour work log existing
        Issue issue = issueClient.get(ISSUE_KEY);
        long timeEstimate = DateUtils.getDuration(issue.fields.timetracking.remainingEstimate);
        long totalTime = DateUtils.getDuration(issue.fields.timetracking.timeSpent);

        Worklog worklog = new Worklog();
        worklog.timeSpent = "1h";

        // delete 1 hour, no adjust
        Response<Worklog> response = worklogClient.post(ISSUE_KEY, worklog);
        worklogClient.delete(ISSUE_KEY, response.body);
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime); // values should be the same

        // deleye 1 hour, auto adjust defined
        response = worklogClient.post(ISSUE_KEY, worklog);
        worklogClient.delete(ISSUE_KEY, response.body, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "auto").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime); // values should be the same

        // delete 1 hour, leave adjust defined
        timeEstimate = timeEstimate - 60;

        response = worklogClient.post(ISSUE_KEY, worklog);
        worklogClient.delete(ISSUE_KEY, response.body, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "leave").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // delete 1 hour, new adjust defined, no estimate provided
        response = worklogClient.post(ISSUE_KEY, worklog);
        Response deleteResponse = worklogClient.delete(ISSUE_KEY, response.body, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "new").toMap());
        assertEquals(400, deleteResponse.statusCode);
        assertEquals(1, deleteResponse.entity.errors.size());
        assertEquals("Value is required when adjustEstimate is new", deleteResponse.entity.errors.get("newEstimate"));
        verifyTimeTracking(ISSUE_KEY, timeEstimate - 60, totalTime + 60); // added worklog needs counting

        // delete 1 hour, new adjust defined
        timeEstimate = TimeUnit.HOURS.toMinutes(8);
        worklogClient.delete(ISSUE_KEY, response.body, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "new").add("newEstimate", "8h").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);

        // delete 1 hour. manual adjust, no adjust value provided
        timeEstimate = timeEstimate - 60;

        response = worklogClient.post(ISSUE_KEY, worklog);
        deleteResponse = worklogClient.delete(ISSUE_KEY, response.body, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "manual").toMap());
        assertEquals(400, deleteResponse.statusCode);
        assertEquals(1, deleteResponse.entity.errors.size());
        assertEquals("A value is required for manual estimate adjustment", deleteResponse.entity.errors.get("increaseBy"));
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime + 60);

        // delete 1 hour, manual adjust defined
        timeEstimate = timeEstimate + 120;

        worklogClient.delete(ISSUE_KEY, response.body, MapBuilder.<String, String>newBuilder().add("adjustEstimate", "manual").add("increaseBy", "2h").toMap());
        verifyTimeTracking(ISSUE_KEY, timeEstimate, totalTime);
    }

    public void testWorklogVisibility() throws Exception
    {
        Worklog worklog = new Worklog();
        worklog.timeSpent = "1h";
        worklog.visibility = new Visibility();
        worklog.visibility.type = "group";
        worklog.visibility.value = "jira-administrators";

        Response<Worklog> response = worklogClient.post(ISSUE_KEY, worklog);
        assertEquals(worklog.visibility.type, response.body.visibility.type);
        assertEquals(worklog.visibility.value, response.body.visibility.value);
        worklog = response.body;

        Worklog doubleCheck = worklogClient.get(ISSUE_KEY, worklog.id);
        // We don't send the timeSeconds, but we expect it on return
        doubleCheck.timeSpentSeconds = null;
        assertTrue(worklog.equals(doubleCheck));

        worklog.visibility.value = "jira-users";
        worklog.timeSpentSeconds = null;
        response = worklogClient.put(ISSUE_KEY, worklog);
        doubleCheck.timeSpentSeconds = null;
        assertTrue(worklog.equals(response.body));

        doubleCheck = worklogClient.get(ISSUE_KEY, worklog.id);
        doubleCheck.timeSpentSeconds = null;
        assertTrue(worklog.equals(doubleCheck));

        // check visibility not overwritten if value not provided.
        PUT("rest/api/2/issue/" + ISSUE_KEY + "/worklog/" + worklog.id, "{\"timeSpent\": \"2h\"}");
        doubleCheck = worklogClient.get(ISSUE_KEY, worklog.id);
        worklog.timeSpent = "2h";
        doubleCheck.timeSpentSeconds = 3600L;
        assertTrue(worklog.equals(doubleCheck));

        // check you can clear visibility information
        Worklog freshWorklog = new Worklog();
        freshWorklog.id = worklog.id;
        response = worklogClient.put(ISSUE_KEY, freshWorklog);
        assertNull(response.body.visibility);

        doubleCheck = worklogClient.get(ISSUE_KEY, worklog.id);
        doubleCheck.timeSpentSeconds = null;
        assertNull(doubleCheck.visibility);
    }

    private void verifyTimeTracking(String issueKey, long expectedEstimateHours, long totalTimeHours)
            throws InvalidDurationException
    {
        Issue issue = issueClient.get(issueKey);
        long timeEstimate = DateUtils.getDuration(issue.fields.timetracking.remainingEstimate);
        long timeSpent = DateUtils.getDuration(issue.fields.timetracking.timeSpent);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        worklogClient = new WorklogClient(getEnvironmentData());
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestWorklogAndTimeTracking.xml");
    }

    private static String asTimeString(@Nullable Date date)
    {
        return date != null ? new SimpleDateFormat(TIME_FORMAT).format(date) : null;
    }

    private Worklog getDefaultWorklog()
    {
        Worklog ew = new Worklog();

        ew.started = "2010-05-24T09:52:00.000+1000";
        ew.created = "2010-05-24T09:52:41.092+1000";
        ew.updated = "2010-05-24T09:52:41.092+1000";
        ew.timeSpent = "2h";
        ew.id = "10000";
        ew.comment = "I'm finished finally!";
        ew.self = getBaseUrlPlus("rest/api/2/issue/10000/worklog/10000");

        UserJson user = new UserJson();
        user.self = getBaseUrlPlus("rest/api/2/user?username=admin");
        user.name = ADMIN_USERNAME;
        user.displayName = ADMIN_FULLNAME;

        ew.author = user;
        ew.updateAuthor = user;

        return ew;
    }

    private Worklog getDefaultWorklogFromJira()
    {
        final Worklog worklog = worklogClient.get(ISSUE_KEY, "10000");
        assertNotNull(worklog);
        return worklog;
    }
}
