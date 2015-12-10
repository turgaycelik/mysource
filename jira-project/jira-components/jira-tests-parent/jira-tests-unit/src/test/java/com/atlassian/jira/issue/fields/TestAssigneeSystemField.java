package com.atlassian.jira.issue.fields;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.handlers.AssigneeSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.AssigneeStatisticsMapper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.user.MockUserHistoryManager;
import com.atlassian.jira.plugin.assignee.AssigneeResolver;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.plugin.webresource.WebResourceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestAssigneeSystemField
{
    AssigneeSystemField systemUnderTest;

    @Mock
    private VelocityTemplatingEngine templatingEngine;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private AssigneeStatisticsMapper assigneeStatisticsMapper;
    @Mock
    private AssigneeResolver assigneeResolver;
    //@Mock
    private AssigneeSearchHandlerFactory assigneeSearchHandlerFactory;
    @Mock
    private UserManager userManager;
    @Mock
    private WebResourceManager webResourceManager;
    @Mock
    private FeatureManager featureManager;
    @Mock
    private JiraBaseUrls jiraBaseUrls;
    @Mock
    private Assignees assignees;
    @Mock
    private MockUserHistoryManager userHistoryManager;
    @Mock
    private ApplicationUser currentUser;
    @Mock
    private MockIssue issue;
    @Mock
    private ApplicationUser someOtherUser;
    @Mock
    private EmailFormatter emailFormatter;

    @Before
    public void setUp() throws Exception
    {
        userHistoryManager = new MockUserHistoryManager();
        currentUser = new MockApplicationUser("selector");
        someOtherUser = new MockApplicationUser("someone");
        when(authenticationContext.getLoggedInUser()).thenReturn(currentUser.getDirectoryUser());
        when(permissionManager.hasPermission(any(Integer.class), any(User.class))).thenReturn(true);
        when(userManager.getUserByName("someone")).thenReturn(someOtherUser);
        when(userManager.getUserByName("selector")).thenReturn(currentUser);
        systemUnderTest = new AssigneeSystemField(
                templatingEngine,
                permissionManager,
                applicationProperties,
                authenticationContext,
                assigneeStatisticsMapper,
                assigneeResolver,
                assigneeSearchHandlerFactory,
                userManager,
                webResourceManager,
                featureManager,
                jiraBaseUrls,
                assignees,
                userHistoryManager,
                emailFormatter);
    }

    private Object accessPrivateField(Object instance, String fieldName)
            throws NoSuchFieldException, IllegalAccessException
    {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object fieldValue = field.get(instance);
        return fieldValue;
    }

    private void setPrivateFieldValue(Object instance, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException
    {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, newValue);
        field.setAccessible(false);
    }

    private static class MockFieldLayoutItem implements FieldLayoutItem
    {
        @Override
        public OrderableField getOrderableField()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getFieldDescription()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getRawFieldDescription()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean isHidden()
        {
            return false;
        }

        @Override
        public boolean isRequired()
        {
            return true;
        }

        @Override
        public String getRendererType()
        {
            return "";
        }

        @Override
        public FieldLayout getFieldLayout()
        {
            return null;
        }

        @Override
        public int compareTo(FieldLayoutItem fieldLayoutItem)
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    @Test
    public void testUpdateIssueAddsUserToUsedUserHistory() throws Exception
    {
        Map<String, ModifiedValue> modifiedFields = new HashMap<String, ModifiedValue>();
        modifiedFields.put("assignee", new ModifiedValue(currentUser.getDirectoryUser(),someOtherUser.getDirectoryUser()));
        when(issue.getModifiedFields()).thenReturn(modifiedFields);
        HashMap<String,String> fieldValuesHolder = new HashMap<String, String>();
        fieldValuesHolder.put("assignee","someone");
        systemUnderTest.updateIssue(new MockFieldLayoutItem(), issue, fieldValuesHolder);
        assertTrue(userHistoryManager.getAddedUsers().contains("someone"));
    }

    @Test
    public void testUpdateIssueDoesNotAddUserToHistoryIfNull() throws Exception
    {
        Map<String, ModifiedValue> modifiedFields = new HashMap<String, ModifiedValue>();
        modifiedFields.put("assignee", new ModifiedValue(currentUser.getDirectoryUser(),null));
        when(issue.getModifiedFields()).thenReturn(modifiedFields);
        HashMap<String,String> fieldValuesHolder = new HashMap<String, String>();
        fieldValuesHolder.put("assignee","someone");
        systemUnderTest.updateIssue(new MockFieldLayoutItem(), issue, fieldValuesHolder);
        assertTrue(userHistoryManager.getAddedUsers().size() == 0);
    }

    @Test
    public void testUpdateIssueDoesNotAddUserToHistoryIfCurrentUser() throws Exception
    {
        Map<String, ModifiedValue> modifiedFields = new HashMap<String, ModifiedValue>();
        modifiedFields.put("assignee", new ModifiedValue(someOtherUser.getDirectoryUser(),currentUser.getDirectoryUser()));
        when(issue.getModifiedFields()).thenReturn(modifiedFields);
        HashMap<String,String> fieldValuesHolder = new HashMap<String, String>();
        fieldValuesHolder.put("assignee","selector");
        systemUnderTest.updateIssue(new MockFieldLayoutItem(), issue, fieldValuesHolder);
        assertTrue(userHistoryManager.getAddedUsers().size() == 0);
    }

    @Test
    public void testUpdateIssueDoesNotAddUserToHistoryIfDefault() throws Exception
    {
        Map<String, ModifiedValue> modifiedFields = new HashMap<String, ModifiedValue>();
        modifiedFields.put("assignee", new ModifiedValue(someOtherUser.getDirectoryUser(),currentUser.getDirectoryUser()));
        when(issue.getModifiedFields()).thenReturn(modifiedFields);
        HashMap<String,String> fieldValuesHolder = new HashMap<String, String>();
        fieldValuesHolder.put("assignee","-1");
        systemUnderTest.updateIssue(new MockFieldLayoutItem(), issue, fieldValuesHolder);
        assertTrue(userHistoryManager.getAddedUsers().size() == 0);
    }
}
