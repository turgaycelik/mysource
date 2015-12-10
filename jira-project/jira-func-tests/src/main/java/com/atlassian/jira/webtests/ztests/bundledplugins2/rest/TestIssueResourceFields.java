package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssueType;
import com.atlassian.jira.testkit.client.restclient.Priority;
import com.atlassian.jira.testkit.client.restclient.Progress;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.Resolution;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.testkit.client.restclient.Status;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.Vote;
import com.atlassian.jira.testkit.client.restclient.Watches;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.rest.api.util.StringList.fromList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceFields extends RestFuncTest
{
    private IssueClient issueClient;
    private SearchClient searchClient;

    public void testExpandos() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");
        final Issue minimal = issueClient.get("HSP-1");
        assertThat(minimal.expand, containsString(Issue.Expand.renderedFields.name()));
        assertThat(minimal.renderedFields, equalTo(null));

        final Issue expanded = issueClient.get("HSP-1", Issue.Expand.renderedFields);
        assertNotNull(expanded.renderedFields.environment);
        assertNotNull(expanded.renderedFields.description);
        assertEquals(1, expanded.renderedFields.comment.getComments().size());
        assertNotNull(expanded.renderedFields.timetracking);
        assertEquals(1, expanded.renderedFields.worklog.worklogs.size());
    }

    public void testRenderedTextFields() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");
        final Issue expanded = issueClient.get("HSP-1", Issue.Expand.renderedFields);

        assertEquals("Curabitur bibendum molestie eros vel pretium.\n", expanded.fields.environment);
        assertEquals("Curabitur bibendum molestie eros vel pretium.<br/>\n", expanded.renderedFields.environment);

        assertEquals("Suspendisse a mi augue. Donec quis.\n", expanded.fields.description);
        assertEquals("Suspendisse a mi augue. Donec quis.<br/>\n", expanded.renderedFields.description);
    }

    public void testRenderedDateFields() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");
        final Issue expanded = issueClient.get("HSP-1", Issue.Expand.renderedFields);

        assertEquals("2010-06-11T12:17:45.383+1000", expanded.fields.created);
        assertEquals("11/Jun/10 12:17 PM", expanded.renderedFields.created);

        assertEquals("2010-06-11T12:25:16.265+1000", expanded.fields.updated);
        assertEquals("11/Jun/10 12:25 PM", expanded.renderedFields.updated);

        assertEquals("2010-06-11T12:19:10.488+1000", expanded.fields.resolutiondate);
        assertEquals("11/Jun/10 12:19 PM", expanded.renderedFields.resolutiondate);

        assertEquals("2010-06-23", expanded.fields.duedate);
        assertEquals("23/Jun/10", expanded.renderedFields.duedate);
    }

    public void testSystemFields() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");
        final Issue json = issueClient.get("HSP-1", Issue.Expand.renderedFields, Issue.Expand.transitions);

        assertEquals("10000", json.id);
        assertEquals("HSP-1", json.key);
        assertEquals(getBaseUrl() + "/rest/api/2/issue/10000", json.self);

        assertNotNull(json.fields);
        assertNotNull(json.transitions);
        assertNotNull(json.renderedFields);

        assertEquals(2, json.transitions.size());

        Issue.Fields fields = json.fields;

        // first the "simple" fields...the ones that aren't JSONObject or JSONArray

        assertNotNull(fields.summary);
        assertEquals("Donec posuere tellus nulla; vitae pellentesque.", fields.summary);

        Vote votes = fields.votes;
        assertEquals(0, votes.votes);
        assertEquals(false, votes.hasVoted);
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-1/votes", votes.self);

        Watches watchers = fields.watches;
        assertEquals(1, watchers.watchCount);
        assertEquals(true, watchers.isWatching);
        assertEquals(getBaseUrl() + "/rest/api/2/issue/HSP-1/watchers", watchers.self);

        assertNotNull(fields.security);
        assertEquals("Insecure", fields.security.name);

        assertNotNull(fields.resolutiondate);
        assertEqualDateStrings("2010-06-11T12:19:10.488+1000", fields.resolutiondate);

        assertNotNull(fields.environment);
        assertEquals("Curabitur bibendum molestie eros vel pretium.<br/>\n", json.renderedFields.environment);

        assertNotNull(fields.updated);
        assertEqualDateStrings("2010-06-11T12:25:16.265+1000", fields.updated);

        assertNotNull(fields.created);
        assertEqualDateStrings("2010-06-11T12:17:45.383+1000", fields.created);

        assertNotNull(fields.description);
        assertEquals("Suspendisse a mi augue. Donec quis.<br/>\n", json.renderedFields.description);

        assertNotNull(fields.duedate);
        assertEquals("2010-06-23", fields.duedate);

        // things are that just arrays of strings
        checkLabels(fields);

        // there are already separate tests for timetracking so we'll leave this as a simple assertion
        assertNotNull(fields.timetracking);

        checkIssueType(fields);
        checkStatus(fields);
        checkAssignee(fields);
        checkReporter(fields);
        checkResolution(fields);
        checkProject(fields);
        checkPriority(fields);
        checkProgress(fields);

        // these things are the most complicated...arrays of JSONObjects
        checkComponents(fields);
        checkFixVersions(fields);
        checkVersions(fields);
    }

    public void testFieldsParam() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");

        // Restrict the list of issue fields we want to see in the result
        final Set<String> fieldsToInclude = Sets.newHashSet("summary", "status", "assignee");
        final Issue json = issueClient.getPartially("HSP-2", fromList("summary"), fromList("status", "assignee"));
        final Issue.Fields fields = json.fields;
        assertNotNull(fields);

        // Fields we are expecting
        for (String field : fieldsToInclude)
        {
            assertNotNull("field was not returned: " + field, fields.get(field));
        }

        // Fields we are not expecting
        Set<String> notReturnedFields = Sets.difference(fields.idSet(), fieldsToInclude);
        for (String field : notReturnedFields)
        {
            assertNull("field was returned: " + field, fields.get(field));
        }
    }
    
    public void testNoCommentsInSearch() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");

        SearchRequest search = new SearchRequest().jql("KEY = HSP-2");
        SearchResult result = searchClient.getSearch(search);
        assertEquals(1, result.issues.size());
        Issue json = result.issues.get(0);
        assertNull(json.fields.comment);
    }

    public void testAddingCommentsInSearch() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");

        SearchRequest search = new SearchRequest().jql("KEY = HSP-2").fields("*navigable", "comment");
        SearchResult result = searchClient.getSearch(search);
        assertEquals(1, result.issues.size());
        Issue json = result.issues.get(0);
        assertNotNull(json.fields.comment);
        assertEquals(Integer.valueOf(0), json.fields.comment.getTotal());
    }

    public void testFieldsParamWithCustomFields() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");

        // Restrict the list of issue fields we want to see in the result
        final Issue json = issueClient.getPartially("HSP-2", fromList("summary", "status", "assignee", "customfield_10001"));
        final Issue.Fields fields = json.fields;
        assertNotNull(fields);

        // Fields we are expecting
        assertNotNull(fields.get("summary"));
        assertNotNull(fields.get("status"));
        assertNotNull(fields.get("assignee"));
        assertNotNull(fields.get("customfield_10001"));

        // Fields we are not expecting
        final Set<String> idSet = fields.idSet();
        idSet.remove("summary");
        idSet.remove("status");
        idSet.remove("assignee");
        idSet.remove("customfield_10001");

        for (final String id : idSet)
        {
            assertNull(fields.get(id));
        }
    }

    public void testFieldsNotShown() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");

        // HSP-2 should not have the sponsor field
        Issue json = issueClient.get("HSP-2");
        Issue.Fields fields = json.fields;
        assertNotNull(fields);
        try
        {
            fields.get("customfield_10100");
            fail("Customfield 10100 should not be present for this type of issue");
        }
        catch (IllegalStateException ex)
        {}
        // HSP-3 should have the sponsor field
        json = issueClient.get("HSP-3");
        fields = json.fields;
        assertNotNull(fields);
        assertNotNull(fields.get("customfield_10100"));
        assertEquals("Elizabeth", fields.get("customfield_10100"));

    }

    public void testPriorityIssueTypeAndStatusIconUrlShouldBeReturnedCorrectlyIfItIsAnAbsoluteUrl() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");

        Issue hsp2 = issueClient.get("HSP-2");
        assertThat(hsp2.fields.issuetype.iconUrl, equalTo("http://127.0.0.1:8090/jira/images/icons/task.gif"));
        assertThat(hsp2.fields.priority.iconUrl(), equalTo("http://127.0.0.1:8090/jira/images/icons/priority_critical.gif"));
        assertThat(hsp2.fields.status.iconUrl(), equalTo("http://127.0.0.1:8090/jira/images/icons/status_open.gif"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        searchClient = new SearchClient(getEnvironmentData());
    }

    private void checkFixVersions(final Issue.Fields fields)
    {
        assertNotNull(fields.fixVersions);
        final List<Version> versions = fields.fixVersions;
        assertEquals(getBaseUrl() + "/rest/api/2/version/10000", versions.get(0).self);
        assertEquals("Test Version Description 1", versions.get(0).description);
        assertEquals("New Version 1", versions.get(0).name);
        assertFalse(versions.get(0).archived);
        assertFalse(versions.get(0).released);

        assertEquals(getRestApiUrl("version/10002"), versions.get(1).self);
        assertEquals("Test Version Description 5", versions.get(1).description);
        assertEquals("New Version 5", versions.get(1).name);
        assertFalse(versions.get(1).archived);
        assertFalse(versions.get(1).released);
    }

    private void checkVersions(final Issue.Fields fields)
    {
        assertNotNull(fields.versions);
        final List<Version> versions = fields.versions;
        assertEquals(getRestApiUrl("version/10000"), versions.get(0).self);
        assertEquals("Test Version Description 1", versions.get(0).description);
        assertEquals("New Version 1", versions.get(0).name);
        assertFalse(versions.get(0).archived);
        assertFalse(versions.get(0).released);

        assertEquals(getRestApiUrl("version/10002"), versions.get(1).self);
        assertEquals("Test Version Description 5", versions.get(1).description);
        assertEquals("New Version 5", versions.get(1).name);
        assertFalse(versions.get(1).archived);
        assertFalse(versions.get(1).released);
    }

    private void checkComponents(final Issue.Fields fields)
    {
        assertNotNull(fields.components);
        final List<Component> components = fields.components;
        assertEquals(getBaseUrl() + "/rest/api/2/component/10001", components.get(0).self);
        assertEquals("New Component 2", components.get(0).name);

        assertEquals(getBaseUrl() + "/rest/api/2/component/10002", components.get(1).self);
        assertEquals("New Component 3", components.get(1).name);
    }

    private void checkPriority(final Issue.Fields fields)
    {
        assertNotNull(fields.priority);
        final Priority priority = fields.priority;
        assertEquals(getBaseUrl() + "/rest/api/2/priority/3", priority.self());
        assertEquals(getBaseUrl() + "/images/icons/priorities/major.png", priority.iconUrl());
        assertEquals("Major", priority.name());
        assertEquals("3", priority.id());
    }

    private void checkProgress(final Issue.Fields fields)
    {
        assertNotNull(fields.progress);
        final Progress progress = fields.progress;
        assertEquals(Long.valueOf(7200), progress.progress());
        assertEquals(Long.valueOf(648000), progress.total());
        assertEquals(Long.valueOf(1), progress.percent());

        assertNotNull(fields.aggregateprogress);
        final Progress aggregateprogress = fields.aggregateprogress;
        assertEquals(Long.valueOf(7200), aggregateprogress.progress());
        assertEquals(Long.valueOf(648000), aggregateprogress.total());
        assertEquals(Long.valueOf(1), aggregateprogress.percent());

        assertNotNull(fields.workratio);
        assertEquals(Long.valueOf(1), fields.workratio);
    }

    private void checkProject(final Issue.Fields fields)
    {
        assertNotNull(fields.project);
        final Project project = fields.project;
        assertEquals(getBaseUrl() + "/rest/api/2/project/10000", project.self);
        assertEquals("10000", project.id);
        assertEquals("HSP", project.key);

        assertThat(project.avatarUrls, equalTo(createProjectAvatarUrls(10000L,10011L)));
    }

    private void checkResolution(final Issue.Fields fields)
    {
        assertNotNull(fields.resolution);
        final Resolution resolution = fields.resolution;
        assertEquals(getBaseUrl() + "/rest/api/2/resolution/1", resolution.self);
        assertEquals("1", resolution.id);
        assertEquals("Fixed", resolution.name);
    }

    private void checkAssignee(final Issue.Fields fields)
    {
        assertNotNull(fields.assignee);
        final User user = fields.assignee;
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", user.self);
        assertEquals(ADMIN_USERNAME, user.name);
        assertEquals(ADMIN_FULLNAME, user.displayName);

        assertThat(user.avatarUrls, equalTo(createUserAvatarUrls(10062L)));
    }

    private void checkReporter(final Issue.Fields fields)
    {
        assertNotNull(fields.reporter);
        final User user = fields.reporter;
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", user.self);
        assertEquals(ADMIN_USERNAME, user.name);
        assertEquals(ADMIN_FULLNAME, user.displayName);

        assertThat(user.avatarUrls, equalTo(createUserAvatarUrls(10062L)));
    }

    private void checkStatus(final Issue.Fields fields)
    {
        assertNotNull(fields.status);
        final Status status = fields.status;
        assertEquals(getBaseUrl() + "/rest/api/2/status/5", status.self());
        assertEquals("Resolved", status.name());
        assertEquals("5", status.id());
        assertEquals(getBaseUrl() + "/images/icons/statuses/resolved.png", status.iconUrl());
    }

    private void checkIssueType(final Issue.Fields fields)
    {
        assertNotNull(fields.issuetype);
        final IssueType issueType = fields.issuetype;
        assertEquals(getBaseUrl() + "/rest/api/2/issuetype/1", issueType.self);
        assertEquals("1", issueType.id);
        assertEquals("Bug", issueType.name);
        assertEquals(getBaseUrl() + "/images/icons/issuetypes/bug.png", issueType.iconUrl);
        assertFalse(issueType.subtask);
    }

    private void checkLabels(final Issue.Fields fields)
    {
        assertNotNull(fields.labels);
        final List<String> labels = fields.labels;
        assertEquals(3, labels.size());
        assertEquals("bad", labels.get(0));
        assertEquals("big", labels.get(1));
        assertEquals("wolf", labels.get(2));
    }

    private Map<String, String> createUserAvatarUrls(Long avatarId)
    {
        return ImmutableMap.<String,String>builder()
            .put("24x24", getBaseUrlPlus("secure/useravatar?size=small&avatarId="+avatarId))
            .put("16x16", getBaseUrlPlus("secure/useravatar?size=xsmall&avatarId="+avatarId))
            .put("32x32", getBaseUrlPlus("secure/useravatar?size=medium&avatarId="+avatarId))
            .put("48x48", getBaseUrlPlus("secure/useravatar?avatarId="+avatarId))
// TODO JRADEV-20790 - Re-enable the larger avatar sizes.
//            .put("64x64", getBaseUrlPlus("secure/useravatar?size=xlarge&avatarId="+avatarId))
//            .put("96x96", getBaseUrlPlus("secure/useravatar?size=xxlarge&avatarId="+avatarId))
//            .put("128x128", getBaseUrlPlus("secure/useravatar?size=xxxlarge&avatarId="+avatarId))
//            .put("192x192", getBaseUrlPlus("secure/useravatar?size=xxlarge%402x&avatarId="+avatarId)) // %40 == "@"
//            .put("256x256", getBaseUrlPlus("secure/useravatar?size=xxxlarge%402x&avatarId="+avatarId))
            .build();
    }

    private Map<String, String> createProjectAvatarUrls(final Long projectId, final Long avatarId)
    {
        return ImmutableMap.<String, String>builder()
            .put("24x24", getBaseUrlPlus("secure/projectavatar?size=small&pid="+projectId+"&avatarId="+avatarId))
            .put("16x16", getBaseUrlPlus("secure/projectavatar?size=xsmall&pid="+projectId+"&avatarId="+avatarId))
            .put("32x32", getBaseUrlPlus("secure/projectavatar?size=medium&pid="+projectId+"&avatarId="+avatarId))
            .put("48x48", getBaseUrlPlus("secure/projectavatar?pid="+projectId+"&avatarId="+avatarId))
// TODO JRADEV-20790 - Re-enable the larger avatar sizes.
//            .put("64x64", getBaseUrlPlus("secure/projectavatar?size=xlarge&pid="+projectId+"&avatarId="+avatarId))
//            .put("96x96", getBaseUrlPlus("secure/projectavatar?size=xxlarge&pid="+projectId+"&avatarId="+avatarId))
//            .put("128x128", getBaseUrlPlus("secure/projectavatar?size=xxxlarge&pid="+projectId+"&avatarId="+avatarId))
//            .put("192x192", getBaseUrlPlus("secure/projectavatar?size=xxlarge%402x&pid="+projectId+"&avatarId="+avatarId)) // %40 == "@"
//            .put("256x256", getBaseUrlPlus("secure/projectavatar?size=xxxlarge%402x&pid="+projectId+"&avatarId="+avatarId))
            .build();
    }
}
