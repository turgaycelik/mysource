package com.atlassian.jira.issue.search.constants;

import java.util.Set;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.ClauseNames;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link SystemSearchConstants}. This test is here to
 * tell anyone changing these constants that it is important for JQL and searching.
 *
 * @since v4.0
 */
public class TestSystemSearchConstants
{

    private static final Set<String> expectedNames = ImmutableSet.of("affectedVersion", "assignee", "attachments", "category", "comment", "component",
            "created", "createdDate", "creator", "description", "due", "duedate", "environment", "filter", "fixVersion", "id", "issue", "issue.property", "issuekey", "issuetype",
            "key", "lastViewed", "level", "originalEstimate", "parent", "priority", "progress", "project", "remainingEstimate", "reporter", "request",
            "resolution", "resolved", "resolutiondate", "savedfilter", "searchrequest", "status", "statusCategory", "summary", "text", "timeestimate", "timeoriginalestimate",
            "timespent", "type", "updated", "updatedDate", "votes", "voter", "watcher", "watchers", "workratio", "labels");

    private static final Set<String> expectedIds = ImmutableSet.of("versions", "assignee", "category", "comment", "components",
            "created", "creator", "description", "duedate", "environment", "filter", "fixVersions", "issuekey", "issuetype",
            "lastViewed", "security", "parent", "priority", "project", "reporter",
            "resolution", "resolutiondate", "status", "statusCategory", "summary", "text", "timeestimate", "timeoriginalestimate",
            "timespent", "updated", "votes", "voter", "watcher", "workratio", "labels");

    @Test
    public void testPriority() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forPriority();
        assertEquals(new ClauseNames("priority"), constants.getJqlClauseNames());
        assertEquals("priority", constants.getIndexField());
        assertEquals("priority", constants.getUrlParameter());
        assertEquals("priority", constants.getSearcherId());
        assertEquals("priority", constants.getFieldId());
    }

    @Test
    public void testProject() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forProject();
        assertEquals(new ClauseNames("project"), constants.getJqlClauseNames());
        assertEquals("projid", constants.getIndexField());
        assertEquals("pid", constants.getUrlParameter());
        assertEquals("project", constants.getSearcherId());
        assertEquals("project", constants.getFieldId());
    }

    @Test
    public void testIssueType() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forIssueType();
        assertEquals(new ClauseNames("issuetype", "type"), constants.getJqlClauseNames());
        assertEquals("type", constants.getIndexField());
        assertEquals("type", constants.getUrlParameter());
        assertEquals("issuetype", constants.getSearcherId());
        assertEquals("issuetype", constants.getFieldId());
    }

    @Test
    public void testAffectedVersion() throws Exception
    {
        final SimpleFieldSearchConstantsWithEmpty constants = SystemSearchConstants.forAffectedVersion();
        assertEquals(new ClauseNames("affectedVersion"), constants.getJqlClauseNames());
        assertEquals("version", constants.getIndexField());
        assertEquals("version", constants.getUrlParameter());
        assertEquals("version", constants.getSearcherId());
        assertEquals("versions", constants.getFieldId());
    }

    @Test
    public void testFixVersion() throws Exception
    {
        final SimpleFieldSearchConstantsWithEmpty constants = SystemSearchConstants.forFixForVersion();
        assertEquals(new ClauseNames("fixVersion"), constants.getJqlClauseNames());
        assertEquals("fixfor", constants.getIndexField());
        assertEquals("fixfor", constants.getUrlParameter());
        assertEquals("fixfor", constants.getSearcherId());
        assertEquals("fixVersions", constants.getFieldId());
    }

    @Test
    public void testResolution() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forResolution();
        assertEquals(new ClauseNames("resolution"), constants.getJqlClauseNames());
        assertEquals("resolution", constants.getIndexField());
        assertEquals("resolution", constants.getUrlParameter());
        assertEquals("resolution", constants.getSearcherId());
        assertEquals("resolution", constants.getFieldId());
    }

    @Test
    public void testStatus() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forStatus();
        assertEquals(new ClauseNames("status"), constants.getJqlClauseNames());
        assertEquals("status", constants.getIndexField());
        assertEquals("status", constants.getUrlParameter());
        assertEquals("status", constants.getSearcherId());
        assertEquals("status", constants.getFieldId());
    }

    @Test
    public void testSummary() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forSummary();
        assertEquals(new ClauseNames("summary"), constants.getJqlClauseNames());
        assertEquals("summary", constants.getIndexField());
        assertEquals("summary", constants.getUrlParameter());
        assertEquals("summary", constants.getSearcherId());
        assertEquals("summary", constants.getFieldId());
    }

    @Test
    public void testDescription() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forDescription();
        assertEquals(new ClauseNames("description"), constants.getJqlClauseNames());
        assertEquals("description", constants.getIndexField());
        assertEquals("description", constants.getUrlParameter());
        assertEquals("description", constants.getSearcherId());
        assertEquals("description", constants.getFieldId());
    }

    @Test
    public void testEnvironment() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forEnvironment();
        assertEquals(new ClauseNames("environment"), constants.getJqlClauseNames());
        assertEquals("environment", constants.getIndexField());
        assertEquals("environment", constants.getUrlParameter());
        assertEquals("environment", constants.getSearcherId());
        assertEquals("environment", constants.getFieldId());
    }

    @Test
    public void testComments() throws Exception
    {
        final CommentsFieldSearchConstants constants = SystemSearchConstants.forComments();
        assertEquals(new ClauseNames("comment"), constants.getJqlClauseNames());
        assertEquals("body", constants.getUrlParameter());
        assertEquals("comment", constants.getFieldId());
    }

    @Test
    public void testCreatedDate() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forCreatedDate();
        assertEquals(new ClauseNames("created", "createdDate"), constants.getJqlClauseNames());
        assertEquals("created", constants.getUrlParameter());
        assertEquals("created", constants.getIndexField());
        assertEquals("created", constants.getSearcherId());
        assertEquals("created", constants.getFieldId());

    }

    @Test
    public void testUpdatedDate() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forUpdatedDate();
        assertEquals(new ClauseNames("updated", "updatedDate"), constants.getJqlClauseNames());
        assertEquals("updated", constants.getUrlParameter());
        assertEquals("updated", constants.getIndexField());
        assertEquals("updated", constants.getSearcherId());
        assertEquals("updated", constants.getFieldId());
    }

    @Test
    public void testDueDate() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forDueDate();
        assertEquals(new ClauseNames("due", "duedate"), constants.getJqlClauseNames());
        assertEquals("duedate", constants.getUrlParameter());
        assertEquals("duedate", constants.getIndexField());
        assertEquals("duedate", constants.getSearcherId());
        assertEquals("duedate", constants.getFieldId());
    }

    @Test
    public void testResolutionDate() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forResolutionDate();
        assertEquals(new ClauseNames("resolved", "resolutiondate"), constants.getJqlClauseNames());
        assertEquals("resolutiondate", constants.getUrlParameter());
        assertEquals("resolutiondate", constants.getIndexField());
        assertEquals("resolutiondate", constants.getSearcherId());
        assertEquals("resolutiondate", constants.getFieldId());
    }

    @Test
    public void testLastViewedDate() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forLastViewedDate();
        assertEquals(new ClauseNames("lastViewed"), constants.getJqlClauseNames());
        assertEquals("lastViewed", constants.getUrlParameter());
        assertEquals(DocumentConstants.ISSUE_ID, constants.getIndexField());
        assertEquals("lastViewed", constants.getSearcherId());
        assertEquals("lastViewed", constants.getFieldId());
    }

    @Test
    public void testReporter() throws Exception
    {
        final UserFieldSearchConstantsWithEmpty constants = SystemSearchConstants.forReporter();
        assertEquals(new ClauseNames("reporter"), constants.getJqlClauseNames());
        assertEquals("issue_author", constants.getIndexField());
        assertEquals("reporter", constants.getSearcherId());
        assertEquals("reporter", constants.getFieldUrlParameter());
        assertEquals("reporterSelect", constants.getSelectUrlParameter());
        assertEquals("issue_current_user", constants.getCurrentUserSelectFlag());
        assertEquals("issue_no_reporter", constants.getEmptySelectFlag());
        assertEquals("specificgroup", constants.getSpecificGroupSelectFlag());
        assertEquals("specificuser", constants.getSpecificUserSelectFlag());
        assertEquals("issue_no_reporter", constants.getEmptyIndexValue());
        assertEquals("reporter", constants.getFieldId());
    }

    @Test
    public void testAssignee() throws Exception
    {
        final UserFieldSearchConstantsWithEmpty constants = SystemSearchConstants.forAssignee();
        assertEquals(new ClauseNames("assignee"), constants.getJqlClauseNames());
        assertEquals("issue_assignee", constants.getIndexField());
        assertEquals("assignee", constants.getSearcherId());
        assertEquals("assignee", constants.getFieldUrlParameter());
        assertEquals("assigneeSelect", constants.getSelectUrlParameter());
        assertEquals("issue_current_user", constants.getCurrentUserSelectFlag());
        assertEquals("unassigned", constants.getEmptySelectFlag());
        assertEquals("specificgroup", constants.getSpecificGroupSelectFlag());
        assertEquals("specificuser", constants.getSpecificUserSelectFlag());
        assertEquals("unassigned", constants.getEmptyIndexValue());
        assertEquals("assignee", constants.getFieldId());
    }

    @Test
    public void testCreator() throws Exception
    {
        final UserFieldSearchConstantsWithEmpty constants = SystemSearchConstants.forCreator();
        assertEquals(new ClauseNames("creator"), constants.getJqlClauseNames());
        assertEquals("issue_creator", constants.getIndexField());
        assertEquals("creator", constants.getSearcherId());
        assertEquals("creator", constants.getFieldUrlParameter());
        assertEquals("creatorSelect", constants.getSelectUrlParameter());
        assertEquals("issue_current_user", constants.getCurrentUserSelectFlag());
        assertEquals("issue_anonymous_creator", constants.getEmptySelectFlag());
        assertEquals("specificgroup", constants.getSpecificGroupSelectFlag());
        assertEquals("specificuser", constants.getSpecificUserSelectFlag());
        assertEquals("issue_anonymous_creator", constants.getEmptyIndexValue());
        assertEquals("creator", constants.getFieldId());
    }

    @Test
    public void testIssueId() throws Exception
    {
        assertSame(IssueIdConstants.getInstance(), SystemSearchConstants.forIssueId());
    }

    @Test
    public void testIssueKey() throws Exception
    {
        assertSame(IssueKeyConstants.getInstance(), SystemSearchConstants.forIssueKey());
    }

    @Test
    public void testWorkRatio() throws Exception
    {
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forWorkRatio();
        assertEquals(new ClauseNames("workratio"), constants.getJqlClauseNames());
        assertEquals("workratio", constants.getUrlParameter());
        assertEquals("workratio", constants.getIndexField());
        assertEquals("workratio", constants.getSearcherId());
        assertEquals("workratio", constants.getFieldId());
    }

    @Test
    public void testCurrentEstimate() throws Exception
    {
        final DefaultClauseInformation constants = SystemSearchConstants.forCurrentEstimate();
        final ClauseNames names = constants.getJqlClauseNames();
        assertEquals("remainingEstimate", names.getPrimaryName());
        assertTrue(names.contains("timeestimate"));
        assertEquals("timeestimate", constants.getIndexField());
        assertEquals("timeestimate", constants.getFieldId());
    }

    @Test
    public void testOriginalEstimate() throws Exception
    {
        final DefaultClauseInformation constants = SystemSearchConstants.forOriginalEstimate();
        final ClauseNames names = constants.getJqlClauseNames();
        assertEquals("originalEstimate", names.getPrimaryName());
        assertTrue(names.contains("timeoriginalestimate"));
        assertEquals("timeoriginalestimate", constants.getIndexField());
        assertEquals("timeoriginalestimate", constants.getFieldId());
    }

    @Test
    public void testTimeSpent() throws Exception
    {
        final DefaultClauseInformation constants = SystemSearchConstants.forTimeSpent();
        assertEquals(new ClauseNames("timespent"), constants.getJqlClauseNames());
        assertEquals("timespent", constants.getIndexField());
        assertEquals("timespent", constants.getFieldId());
    }

    @Test
    public void testSecurityLevel() throws Exception
    {
        final DefaultClauseInformation constants = SystemSearchConstants.forSecurityLevel();
        assertEquals(new ClauseNames("level"), constants.getJqlClauseNames());
        assertEquals("issue_security_level", constants.getIndexField());
        assertEquals("security", constants.getFieldId());
    }

    @Test
    public void testVotes() throws Exception
    {
        final DefaultClauseInformation constants = SystemSearchConstants.forVotes();
        assertEquals(new ClauseNames("votes"), constants.getJqlClauseNames());
        assertEquals("issue_votes", constants.getIndexField());
        assertEquals("votes", constants.getFieldId());
    }

    //NOTE: If this test starts failing, then you have added some extra system names to JQL. You *MUST* create
    //an upgrade task to migrate any search clauses that refer the custom fields by name to their cf[1001] variants
    //if that name has become a system name. For example, the clause "newsystemname ~ bad" would need to be migrated to "cf[xxxx] ~ bad"
    //if "newsystemname" is added here.
    @Test
    public void testSystemNames() throws Exception
    {
        assertEquals(expectedNames, SystemSearchConstants.getSystemNames());
    }

    @Test
    public void testGetClauseInformationById()
    {
        for (String expectedId : expectedIds)
        {
            assertNotNull("\"" + expectedId + "\" not found.", SystemSearchConstants.getClauseInformationById(expectedId));
        }
    }
}
