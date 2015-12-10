package com.atlassian.jira.issue;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.MockSubTaskManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.DefaultUserManager;
import com.atlassian.jira.user.util.MockUserKeyStore;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test Case for IssueImpl.
 *
 * @since v3.13
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIssueImpl
{
    private static final Long PROJECT_ID = 101L;
    private static final String STATUS_ID = "statusId";
    private static final String PRIORITY_ID = "priorityId";
    private static final String ASSIGNEE_KEY = "assigneeId";
    public static final String CREATOR_ID = "creatorId";
    public static final String PROJECT_KEY = "ARJ";
    public static final String ASSIGNEE_NAME = "assignee";
    private static final String CREATOR_NAME = "creator";

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Mock
    ProjectComponentManager projectComponentManager;

    @Mock
    ProjectManager projectManager;

    @Mock
    Project project;

    @Mock
    @AvailableInContainer
    UserManager userManager;

    @Mock (answer = Answers.RETURNS_MOCKS)
    ApplicationUser assignee;

    @Mock
    User assigneeUser;

    @Mock (answer = Answers.RETURNS_MOCKS)
    ApplicationUser creator;

    @Mock
    User creatorUser;

    @Mock
    @AvailableInContainer
    UserKeyService userKeyService;

    @Before
    public void setUp()
    {
        when(projectManager.getProjectObj(PROJECT_ID)).thenReturn(project);
        when(project.getKey()).thenReturn(PROJECT_KEY);

        when(projectComponentManager.findComponentsByIssueGV(any(IssueImpl.class))).thenReturn(Collections.<GenericValue>emptyList());
        when(userManager.getUserByKey(ASSIGNEE_KEY)).thenReturn(assignee);
        when(userManager.getUserByKey(CREATOR_ID)).thenReturn(creator);

        when(assignee.getDirectoryUser()).thenReturn(assigneeUser);
        when(creator.getDirectoryUser()).thenReturn(creatorUser);

        when(assigneeUser.getName()).thenReturn(ASSIGNEE_NAME);
        when(userKeyService.getKeyForUsername(ASSIGNEE_NAME)).thenReturn(ASSIGNEE_KEY);

        when(creatorUser.getName()).thenReturn(CREATOR_NAME);
        when(userKeyService.getKeyForUsername(CREATOR_NAME)).thenReturn(CREATOR_ID);
    }

    @Test
    public void testSetProjectID()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        MockProject mockProject = new MockProject(10, "TST", "Test");
        mockProjectManager.addProject(mockProject);

        // Set up an issue
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20)));
        IssueImpl issue = new IssueImpl(gvIssue_20, null, mockProjectManager, null, null, null, null, null, null, null, null, null);

        // test setProjectID with an issue that exists in the ProjectManager
        issue.setProjectId(new Long(10));
        assertEquals(new Long(10), issue.getProjectObject().getId());
        assertEquals("TST", issue.getProjectObject().getKey());
        assertEquals(new Long(10), issue.getProject().getLong("id"));
        assertEquals("TST", issue.getProject().getString("key"));
        //check the underlying GV has been updated.
        assertEquals(new Long(10), issue.getGenericValue().getLong("project"));

        //check the appropriate project entry has been made in the modified fields map.
        Map modifiedFields = issue.getModifiedFields();
        ModifiedValue modifiedValue = (ModifiedValue) modifiedFields.get("project");
        assertEquals(mockProject, modifiedValue.getNewValue());

        // test setProjectID with an issue that does not exist in the ProjectManager
        try
        {
            issue.setProjectId(new Long(12));
            fail("Should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException ex)
        {

            // Expected
            assertEquals("Invalid Project ID '12'.", ex.getMessage());
        }

        // test setProjectID with null ID
        issue.setProjectId(null);
        assertNull(issue.getProject());
        assertNull(issue.getProjectObject());
        assertNull(issue.getGenericValue().getLong("project"));
        //check the appropriate project entry has been made in the modified fields map.
        modifiedFields = issue.getModifiedFields();
        modifiedValue = (ModifiedValue) modifiedFields.get("project");
        assertNull(modifiedValue.getNewValue());
    }

    @Test
    public void testSetKey() throws Exception
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        MockProject mockProject = new MockProject(10, "ABC", "Test");
        mockProjectManager.addProject(mockProject);

        // Set up an issue
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20)));
        IssueImpl issue = new IssueImpl(gvIssue_20, null, mockProjectManager, null, null, null, null, null, null, null, null, null);

        issue.setKey("ABC-123");
        assertEquals("ABC-123", issue.getKey());
        assertEquals("ABC", issue.getProjectObject().getKey());
        assertEquals(new Long(123), issue.getNumber());
    }

    /**
     * Tests the setParentObject() method.
     *
     * @noinspection deprecation We are calling known deprecated methods as they are being tested.
     * Note that IDEA's statement-level suppression wasn't working for me in IDEA v6.04
     */
    @Test
    public void testSetParentObject()
    {
        // Create a Mock IssueManager and add a parent issue with ID 1
        MockIssueManager mockIssueManager = new MockIssueManager();
        MutableIssue oldParent = MockIssueFactory.createIssue(1);
        oldParent.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(10001))));
        mockIssueManager.addIssue(oldParent);

        // Create our subtask - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20)));
        IssueImpl issue = new IssueImpl(gvIssue_20, mockIssueManager, null, null, null, null, new MockSubTaskManager(), null, null, null, null, null);

        // assert parent ID is null by default.
        assertNull(issue.getParentId());
        // assert setParentId() works
        issue.setParentId(new Long(1));
        assertEquals(new Long(1), issue.getParentId());
        assertEquals(new Long(10001), issue.getParentObject().getSecurityLevelId());

        // Now modify a parent Issue and set this explicitly with setParentObject()
        MutableIssue newParent = MockIssueFactory.createIssue(12);
        newParent.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(10666))));
        issue.setParentObject(newParent);
        assertEquals(new Long(12), issue.getParentId());
        // The subtask should now be using the in-memory version of the parent.
        assertEquals(new Long(10666), issue.getParentObject().getSecurityLevelId());
        // Also we should get an in-memory Generic Value.
        assertEquals(new Long(10666), issue.getParent().getLong("security"));

        // resetting the subtask to use parentId, we should start looking up the IssueManager again.
        issue.setParentId(new Long(1));
        // first chekc the ParentID
        assertEquals(new Long(1), issue.getParentId());
        // The subtask should now be using the IssueManager version of the parent.
        assertEquals(new Long(10001), issue.getParentObject().getSecurityLevelId());
        // And the IssueManager version of the parent GenericValue
        assertEquals(new Long(10001), issue.getParent().getLong("security"));

        // Setting parentObject to null, should set ParentId to null as well
        issue.setParentObject(null);
        assertNull(issue.getParentId());
        assertNull(issue.getParentObject());
        assertNull(issue.getParent());
    }

    @Test
    public void testSetParentObjectIllegalArgument()
    {
        // Create our subtask - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20)));
        IssueImpl subTask = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, null, null);

        // Try to add an invalid Parent object
        // Now modify a parent Issue and set this explicitly with setParentObject()
        MockIssue newParent = new MockIssue((Long) null);
        try
        {
            subTask.setParentObject(newParent);
            fail("Should have thrown IllegalArgumentException.");
        }
        catch (IllegalArgumentException ex)
        {
            // Expected
        }

    }

    @Test
    public void testResolutionDate()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(1);
        final GenericValue issueGV = issue.getGenericValue();

        assertNull(issue.getResolutionDate());

        final Timestamp sometime = new Timestamp(new GregorianCalendar(2001, 2, 2).getTimeInMillis());
        final Timestamp now = new Timestamp(System.currentTimeMillis());

        //first try setting the resolution date directly
        issue.setResolutionDate(now);
        assertEquals(now, issue.getResolutionDate());
        assertEquals(now, issueGV.getTimestamp("resolutiondate"));

        issue.setResolutionDate(null);
        assertNull(issue.getResolutionDate());
        assertNull(issueGV.getTimestamp("resolutiondate"));

        //now try setting it via the resolution field.
        GenericValue mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10000)));
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        //now check the date is 'roughly now' i.e. within the last 100 millis of now
        assertTimeEqualsNow(issue.getResolutionDate().getTime());
        assertTimeEqualsNow(issueGV.getTimestamp("resolutiondate").getTime());

        //now try setting the resolution field to the same value.  The date should not change.
        mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10000)));
        issue.setResolutionDate(sometime);
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        assertEquals(sometime.getTime(), issue.getResolutionDate().getTime());
        assertEquals(sometime.getTime(), issueGV.getTimestamp("resolutiondate").getTime());

        //now try setting the resolution field to a different value.  The date should change.
        mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10010)));
        issue.setResolutionDate(sometime);
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        //now check the date is 'roughly now' i.e. within the last 100 millis of now
        assertTimeEqualsNow(issue.getResolutionDate().getTime());
        assertTimeEqualsNow(issueGV.getTimestamp("resolutiondate").getTime());

        //now try setting the resolution field to the same value, when date is not set.  The date should change.
        mockResolutionGV = new MockGenericValue("resolution", EasyMap.build("id", new Long(10010)));
        issue.setResolutionDate(null);
        issue.setResolution(mockResolutionGV);
        assertNotNull(issue.getResolutionDate());
        assertNotNull(issueGV.getTimestamp("resolutiondate"));
        //now check the date is 'roughly now' i.e. within the last 100 millis of now
        assertTimeEqualsNow(issue.getResolutionDate().getTime());
        assertTimeEqualsNow(issueGV.getTimestamp("resolutiondate").getTime());

        issue.setResolution(null);
        assertNull(issue.getResolutionDate());
        assertNull(issueGV.getTimestamp("resolutiondate"));
    }

    @Test
    public void testGetAssigneeUserNull()
    {

        // Create our GV - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20), "assignee", null));
        CrowdService crowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(crowdService, null, null, null, null, null);
        IssueImpl issue = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, userManager, null);
        assertEquals(null, issue.getAssigneeId());
        assertEquals(null, issue.getAssigneeUser());
    }

    @Test
    public void testGetAssigneeUserExists() throws InvalidUserException, InvalidCredentialException
    {
        final MockCrowdService crowdService = new MockCrowdService();
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(CrowdService.class, crowdService));

        // Create our GV - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20), "assignee", "dude"));
        crowdService.addUser(ImmutableUser.newUser().name("dude").displayName("Freaky Dude").toUser(), null);
        UserManager userManager = new DefaultUserManager(crowdService, null, null, new MockUserKeyStore(), null, null);
        IssueImpl issue = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, userManager, null);
        assertEquals("dude", issue.getAssigneeId());
        assertEquals("Freaky Dude", issue.getAssigneeUser().getDisplayName());
    }

    @Test
    public void testGetAssigneeUserAfterUserDeleted()
    {

        // Create our GV - issue 20
        MockGenericValue gvIssue_20 = new MockGenericValue("Issue", EasyMap.build("id", new Long(20), "assignee", "dude"));
        CrowdService crowdService = new MockCrowdService();
        UserManager userManager = new DefaultUserManager(crowdService, null, null, new MockUserKeyStore(), null, null);
        IssueImpl issue = new IssueImpl(gvIssue_20, null, null, null, null, null, null, null, null, null, userManager, null);
        assertEquals("dude", issue.getAssigneeId());
        // no user in system, but we should get a fake one with username as displayname
        assertEquals("dude", issue.getAssigneeUser().getDisplayName());
    }

    private void assertTimeEqualsNow(final long timeInMillis)
    {
        final long currentTimeInMillis = System.currentTimeMillis();
        assertTrue("date is not now (or 100ms before now)",
                timeInMillis > (currentTimeInMillis - 100) && timeInMillis <= currentTimeInMillis);
    }

    @Test
    public void clonedIssueFieldsClonedFromGVIfNotNull()
    {
        Map<String, Object> fields = Maps.newHashMap();
        fields.put("id", 227L);
        fields.put("key", IssueKey.format(project, 255L));
        fields.put(IssueFieldConstants.PROJECT, PROJECT_ID);
        fields.put(IssueFieldConstants.ISSUE_NUMBER, 255L);
        fields.put(IssueFieldConstants.SUMMARY, "another summary");
        fields.put(IssueFieldConstants.DESCRIPTION, "some description");
        fields.put(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, 44L);
        fields.put(IssueFieldConstants.TIME_ESTIMATE, 42L);
        fields.put(IssueFieldConstants.TIME_SPENT, 0L);
        fields.put(IssueFieldConstants.UPDATED, now(1));
        fields.put("type", "typeId");
        fields.put(IssueFieldConstants.ENVIRONMENT, "environment");
        fields.put(IssueFieldConstants.ASSIGNEE, ASSIGNEE_KEY);
        fields.put(IssueFieldConstants.REPORTER, "reporterId");
        fields.put(IssueFieldConstants.CREATOR, CREATOR_ID);
        fields.put(IssueFieldConstants.DUE_DATE, now(2));
        fields.put(IssueFieldConstants.SECURITY, 112L);
        fields.put(IssueFieldConstants.PRIORITY, PRIORITY_ID);
        fields.put(IssueFieldConstants.STATUS, STATUS_ID);
        fields.put(IssueFieldConstants.RESOLUTION, "resolutionId");
        fields.put(IssueFieldConstants.CREATED, now(3));
        fields.put(IssueFieldConstants.RESOLUTION_DATE, now(4));
        fields.put(IssueFieldConstants.VOTES, 10L);
        fields.put(IssueFieldConstants.WATCHES, 20L);
        fields.put("workflowId", 18L);

        MockIssue original = new MockIssue(new MockGenericValue("Issue", fields));

        IssueImpl clone = new IssueImpl(original, null, projectManager, null, null, null, null, null, null, projectComponentManager, userManager, null);

        assertIssuesEqual(original, clone);
    }

    @Test
    public void clonedIssueFieldsClonedFromIssueFieldsWhenGVIsNull()
    {

        MockIssue original = new MockIssue(227L) {
            @Override
            public GenericValue getGenericValue()
            {
                return null;
            }
        };
        original.setProjectId(PROJECT_ID);
        original.setStatusId(STATUS_ID);
        original.setPriorityId(PRIORITY_ID);
        original.setAssigneeId(ASSIGNEE_KEY);
        original.setCreatorId(CREATOR_ID);
        original.setNumber(1L);
        original.setKey(IssueKey.format(project, 1L));
        original.setIssueTypeId("issueTypeId");
        original.setSummary("summary");
        original.setDescription("description");
        original.setEnvironment("env");
        original.setReporterId("reporterId");
        original.setDueDate(now(1));
        original.setSecurityLevelId(4L);
        original.setResolutionId("resolutionId");
        original.setCreated(now(2));
        original.setUpdated(now(3));
        original.setResolutionDate(now(4));
        original.setOriginalEstimate(42L);
        original.setEstimate(1500100900L);
        original.setTimeSpent(Long.MAX_VALUE);
        original.setVotes(0L);
        original.setWatches(1L);
        original.setWorkflowId(0202122L);

        IssueImpl clone = new IssueImpl(original, null, projectManager, null, null, null, null, null, null, projectComponentManager, userManager, null);

        assertIssuesEqual(original, clone);
    }

    private Timestamp now(long plus)
    {
        return new Timestamp(System.currentTimeMillis() + plus);
    }

    private static void assertIssuesEqual(final Issue original, final Issue clone)
    {
        assertThat(clone.getProjectId(), is(original.getProjectId()));
        assertThat(clone.getNumber(), is(original.getNumber()));
        assertThat(clone.getIssueTypeId(), is(original.getIssueTypeId()));
        assertThat(clone.getSummary(), is(original.getSummary()));
        assertThat(clone.getDescription(), is(original.getDescription()));
        assertThat(clone.getEnvironment(), is(original.getEnvironment()));
        assertThat(clone.getAssigneeId(), is(original.getAssigneeId()));
        assertThat(clone.getReporterId(), is(original.getReporterId()));
        assertThat(clone.getCreatorId(), is(original.getCreatorId()));
        assertThat(clone.getDueDate(), is(original.getDueDate()));
        assertThat(clone.getSecurityLevelId(), is(original.getSecurityLevelId()));
        if (original.getPriorityObject() != null)
        {
            assertThat(clone.getPriorityObject().getId(), is(original.getPriorityObject().getId()));
        }
        if (original.getStatusObject() != null)
        {
            assertThat(clone.getStatusObject().getId(), is(original.getStatusObject().getId()));
        }
        assertThat(clone.getResolutionId(), is(original.getResolutionId()));
        assertThat(clone.getCreated(), is(original.getCreated()));
        assertThat(clone.getUpdated(), is(original.getUpdated()));
        assertThat(clone.getResolutionDate(), is(original.getResolutionDate()));
        assertThat(clone.getOriginalEstimate(), is(original.getOriginalEstimate()));
        assertThat(clone.getEstimate(), is(original.getEstimate()));
        assertThat(clone.getTimeSpent(), is(original.getTimeSpent()));
        if (original.getVotes() == null)
        {
            assertThat(clone.getVotes(), is(0L));
        }
        else
        {
            assertThat(clone.getVotes(), is(original.getVotes()));
        }
        assertThat(clone.getWatches(), is(original.getWatches()));
        assertThat(clone.getWorkflowId(), is(original.getWorkflowId()));
        assertThat(clone.getKey(), is(original.getKey()));
    }
}
