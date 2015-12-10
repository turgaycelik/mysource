package com.atlassian.jira.bc.issue.util;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.atlassian.jira.matchers.ErrorCollectionMatchers.containsFieldError;
import static com.atlassian.jira.matchers.ErrorCollectionMatchers.containsSystemError;
import static com.atlassian.jira.matchers.ErrorCollectionMatchers.isEmpty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestDefaultVisibilityValidator
{
    private final String i18nPrefix = "worklog";
    private ApplicationUser user;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private ErrorCollection errorCollection;
    private JiraServiceContextImpl jiraServiceContext;
    private DefaultVisibilityValidator visibilityValidator;

    @Mock
    private Issue issue;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private ProjectRoleManager roleManager;
    @Mock
    private GroupManager groupManager;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        user = new MockApplicationUser("fred");
        errorCollection = new SimpleErrorCollection();
        jiraAuthenticationContext = new MockSimpleAuthenticationContext(null);
        visibilityValidator = new DefaultVisibilityValidator(applicationProperties, jiraAuthenticationContext, roleManager, groupManager);
        jiraServiceContext = new JiraServiceContextImpl(user, errorCollection);
    }

    @Test
    public void testAnonymousUser()
    {
        final boolean result = visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl((ApplicationUser) null, errorCollection), i18nPrefix, issue, null, "12345");

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsSystemError("You cannot add a comment for specific groups or roles, as your session has expired. Please log in and try again."));
    }

    @Test
    public void testIsValidVisibilityDataBothGroupAndRoleProvided()
    {
        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, "testGroup", "12345");

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "Selecting worklog visibility can be for group or role, not both!"));
    }

    @Test
    public void testIsValidVisibilityDataNullIssue()
    {
        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, null, null, null);

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsSystemError("Can not modify a worklog without an issue specified."));
    }

    @Test
    public void testIsValidVisibilityDataForPublicVisibility()
    {
        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, null, null);

        assertThat(result, equalTo(true));
        assertThat(errorCollection, isEmpty());
    }

    @Test
    public void testIsValidVisibilityDataForGroup()
    {
        final String group = "testGroup";
        when(applicationProperties.getOption(eq(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS))).thenReturn(true);
        when(groupManager.groupExists(eq(group))).thenReturn(true);
        when(groupManager.isUserInGroup(eq(user.getName()), eq(group))).thenReturn(true);

        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, group, null);

        assertThat(result, equalTo(true));
        assertThat(errorCollection, isEmpty());
    }

    @Test
    public void testIsValidVisibilityDataGroupVisibilityDisabled()
    {
        when(applicationProperties.getOption(eq(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS))).thenReturn(false);

        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, "testGroup", null);

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "Group level visibility has been disabled."));
    }

    @Test
    public void testIsValidVisibilityDataGroupDoesntExist()
    {
        final String group = "testGroup";
        when(applicationProperties.getOption(eq(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS))).thenReturn(true);
        when(groupManager.groupExists(eq(group))).thenReturn(false);

        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, group, null);

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "Group: " + group + " does not exist."));
    }

    @Test
    public void testIsValidVisibilityDataUserNotInGroup()
    {
        final String group = "testGroup";
        when(applicationProperties.getOption(eq(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS))).thenReturn(true);
        when(groupManager.groupExists(eq(group))).thenReturn(true);
        when(groupManager.isUserInGroup(eq(user.getName()), eq(group))).thenReturn(false);

        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, group, null);

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "You are currently not a member of the group: " + group + "."));
    }

    @Test
    public void testIsValidVisibilityForRole()
    {
        final Long roleLevelId = 1234L;
        final ProjectRole projectRole = new ProjectRoleImpl(roleLevelId, "Test Role", "Test Desc");

        when(roleManager.getProjectRole(anyLong())).thenReturn(projectRole);
        when(roleManager.isUserInProjectRole(eq(user), eq(projectRole), isNull(Project.class))).thenReturn(true);

        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, null, roleLevelId.toString());

        assertThat(result, equalTo(true));
        assertThat(errorCollection, isEmpty());
    }

    @Test
    public void testIsValidVisibilityDataRoleVisibilityDisabled()
    {
        final DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, null, null)
        {
            public boolean isProjectRoleVisibilityEnabled()
            {
                return false;
            }
        };
        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, null, "12345");

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "Project Role level visibility has been disabled."));
    }

    @Test
    public void testIsValidVisibilityDataRoleDoesNotExist()
    {
        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, null, "1234");

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "Role with id: 1234 does not exist."));
    }

    @Test
    public void testIsValidVisibilityDataUserNotInRole()
    {
        final Long roleLevelId = 1234L;
        final ProjectRole projectRole = new ProjectRoleImpl(roleLevelId, "Test Role", "Test Desc");

        when(roleManager.getProjectRole(anyLong())).thenReturn(projectRole);
        when(roleManager.isUserInProjectRole(eq(user), eq(projectRole), isNull(Project.class))).thenReturn(false);

        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, null, roleLevelId.toString());

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "You are currently not a member of the project role: Test Role."));
    }

    @Test
    public void testIsIsValidVisibilityDataRoleIdNotNumber()
    {
        final boolean result = visibilityValidator.isValidVisibilityData(jiraServiceContext, i18nPrefix, issue, null, "1234abc");

        assertThat(result, equalTo(false));
        assertThat(errorCollection, containsFieldError("commentLevel", "Role ID must be a number!"));
    }


}
