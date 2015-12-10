package com.atlassian.jira.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.type.TypeForTesting;
import com.atlassian.jira.notification.type.TypeForTesting2;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.JiraUtils.get24HourTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestJiraUtils
{
    private ApplicationProperties applicationProperties;
    private MockIssueManager mockIssueManager;
    private MockProjectManager mockProjectManager;

    @Before
    public void setUp() throws Exception
    {
        mockIssueManager = new MockIssueManager();
        mockProjectManager = new MockProjectManager();
        MockIssueFactory.setProjectManager(mockProjectManager);
        new MockComponentWorker().init()
                .addMock(IssueManager.class, mockIssueManager)
                .addMock(ProjectManager.class, mockProjectManager)
                .addMock(UserManager.class, new MockUserManager());
        applicationProperties = ComponentAccessor.getApplicationProperties();
    }

    @After
    public void tearDown() throws Exception
    {
        MockIssueFactory.setProjectManager(null);
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testGet24HourTime()
    {
        assertEquals(0, get24HourTime("AM", 12));
        assertEquals(0, get24HourTime("am", 12));
        assertEquals(12, get24HourTime("PM", 12));
        assertEquals(12, get24HourTime("pm", 12));
        assertEquals(11, get24HourTime("AM", 11));
        assertEquals(1, get24HourTime("AM", 1));
        assertEquals(17, get24HourTime("PM", 5));
        assertEquals(23, get24HourTime("PM", 11));
        assertEquals(23, get24HourTime("pm", 11));
    }

    @Test
    public void testLoadTypes()
    {
        // Invoke
        final Map<String, NotificationType> types = JiraTypeUtils.loadTypes("test-notification-event-types.xml", this.getClass());

        // Check
        assertEquals(2, types.size());
        assertTrue(types.get("TEST_TYPE_1") instanceof TypeForTesting);
        assertTrue(types.get("TEST_TYPE_2") instanceof TypeForTesting2);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void gettingProjectByNullEntityShouldReturnNull()
    {
        // Invoke and check
        assertNull(JiraEntityUtils.getProject(null));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void projectShouldNotBeGettableByInappropriateEntity()
    {
        // Set up
        final GenericValue uhuh = new MockGenericValue("Priority", EasyMap.build("id", "foo"));

        // Invoke and check
        assertNull(JiraEntityUtils.getProject(uhuh));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void projectShouldBeGettableByProjectGenericValue()
    {
        // Set up
        final GenericValue project = new MockGenericValue("Project", EasyMap.build("id", 1L));
        final GenericValue project2 = new MockGenericValue("Project", EasyMap.build("id", 2L));
        mockProjectManager.addProject(project);
        mockProjectManager.addProject(project2);

        // Invoke and check
        assertEquals(project, JiraEntityUtils.getProject(project));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void projectShouldBeGettableByIssue()
    {
        // Set up
        final GenericValue project = new MockGenericValue("Project", EasyMap.build("id", 1L));
        mockProjectManager.addProject(project);
        final GenericValue issue = new MockGenericValue("Issue", EasyMap.build("id", 2L, "project", 1L));
        mockIssueManager.addIssue(issue);

        // Invoke and check
        assertEquals(project, JiraEntityUtils.getProject(issue));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void projectShouldBeGettableByAction()
    {
        // Set up
        final GenericValue project = new MockGenericValue("Project", EasyMap.build("id", 1L));
        mockProjectManager.addProject(project);
        final GenericValue issue = new MockGenericValue("Issue", EasyMap.build("id", 2L, "project", 1L));
        mockIssueManager.addIssue(issue);
        final GenericValue action = new MockGenericValue("Action", EasyMap.build("id", 3L, "issue", 2L));

        // Invoke and check
        assertEquals(project, JiraEntityUtils.getProject(action));
    }

    @Test
    public void jiraShouldBeInPublicModeByDefault()
    {
        // Invoke and check
        assertTrue(JiraUtils.isPublicMode());
    }

    @Test
    public void explicitlySettingPropertyShouldPutJiraIntoPublicMode()
    {
        // Set up
        applicationProperties.setString(APKeys.JIRA_MODE, "public");

        // Invoke and check
        assertTrue(JiraUtils.isPublicMode());
    }

    @Test
    public void explicitlySettingPropertyShouldPutJiraIntoPrivateMode()
    {
        // Set up
        applicationProperties.setString(APKeys.JIRA_MODE, "private");

        // Invoke and check
        assertFalse(JiraUtils.isPublicMode());
    }

    @Test
    public void testCreateEntityMap()
    {
        // Set up
        final GenericValue gv = new MockGenericValue("Project", EasyMap.build("id", 1L, "name", "foo"));
        final GenericValue gv2 = new MockGenericValue("Project", EasyMap.build("id", 2L, "name", "bar"));

        // Invoke
        final Map result = JiraEntityUtils.createEntityMap(Lists.newArrayList(gv, gv2), "id", "name");

        // Check
        final Map<Long, String> expectedResult = new LinkedHashMap<Long, String>();
        expectedResult.put(1L, "foo");
        expectedResult.put(2L, "bar");
        assertEquals(expectedResult, result);
    }
}
