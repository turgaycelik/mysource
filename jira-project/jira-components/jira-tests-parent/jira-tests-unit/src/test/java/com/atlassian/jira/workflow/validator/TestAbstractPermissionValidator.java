package com.atlassian.jira.workflow.validator;

import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.WorkflowException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TestAbstractPermissionValidator
{
    @Rule
    public final RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @Mock
    @AvailableInContainer
    private I18nHelper i18nHelper;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    private ProjectPermission permission;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private final ApplicationUser testUser = new MockApplicationUser("test-user-key", "test-user-name", "Test User", "test@localhost");

    // -- Tests for hasUserPermissionForIssue
    @Test
    public void testHasUserPermissionForIssueWhenGenericValueIsNullAndProjectIsNullShouldThrowException() throws Exception
    {
        final AbstractPermissionValidator validator = createAbstractPermissionValidator();
        final Issue issue = mock(Issue.class);

        expectedException.expect(InvalidInputExceptionMatcher.withGenericError("Invalid project specified."));
        validator.hasUserPermissionForIssue(issue, ADMINISTER_PROJECTS, testUser, permissionManager);
    }

    @Test
    public void testHasUserPermissionForIssueWhenGenericValueIsNotNullAndUserHasPermissionShouldReturnTrue()
            throws Exception
    {
        testHasUserPermissionForIssueImpl(mockIssueWitGenericValue(), true);
    }

    @Test
    public void testHasUserPermissionForIssueWhenGenericValueIsNotNullAndUserDoesntHavePermissionShouldReturnFalse()
            throws Exception
    {
        testHasUserPermissionForIssueImpl(mockIssueWitGenericValue(), false);
    }

    public void testHasUserPermissionForIssueImpl(final Issue issue, final boolean hasPermission)
            throws InvalidInputException
    {
        when(permissionManager.hasPermission(ADMINISTER_PROJECTS, issue, testUser)).thenReturn(hasPermission);

        final AbstractPermissionValidator validator = createAbstractPermissionValidator();

        final boolean hasPermissionResult = validator.hasUserPermissionForIssue(issue, ADMINISTER_PROJECTS, testUser, permissionManager);
        assertEquals("Validator should return the same result as permission manager",
                     hasPermission, hasPermissionResult);
    }

    private Issue mockIssueWitGenericValue()
    {
        final Issue issue = mock(Issue.class);
        when(issue.getGenericValue()).thenReturn(mock(GenericValue.class));
        return issue;
    }

    @Test
    public void testHasUserPermissionForIssueWhenGenericValueIsNullAndProjectIsNotNullAndUserHasPermissionShouldReturnTrue()
            throws Exception
    {
        testHasUserPermissionForIssueWhenGenericValueIsNullImpl(mockIssueWithProjectObject(), true);
    }

    @Test
    public void testHasUserPermissionForIssueWhenGenericValueIsNullAndProjectIsNotNullAndUserDoesntHavePermissionShouldReturnFalse()
            throws Exception
    {
        testHasUserPermissionForIssueWhenGenericValueIsNullImpl(mockIssueWithProjectObject(), false);
    }

    public void testHasUserPermissionForIssueWhenGenericValueIsNullImpl(final Issue issue, final boolean hasPermission)
            throws InvalidInputException
    {
        when(permissionManager.hasPermission(ADMINISTER_PROJECTS, issue.getProjectObject(), testUser)).thenReturn(hasPermission);

        final AbstractPermissionValidator validator = createAbstractPermissionValidator();

        final boolean hasPermissionResult = validator.hasUserPermissionForIssue(issue, ADMINISTER_PROJECTS, testUser, permissionManager);
        assertEquals("Validator should return the same result as permission manager",
                     hasPermission, hasPermissionResult);
    }

    private Issue mockIssueWithProjectObject()
    {
        final Issue issue = mock(Issue.class);
        when(issue.getProjectObject()).thenReturn(mock(Project.class));
        return issue;
    }

    // -- Tests for hasUserPermission
    @Test
    public void testHasUserPermissionDoesntThrowsExceptionWhenUserHavePermission() throws InvalidInputException
    {
        testHasUserPermissionImpl(true, ADMINISTER_PROJECTS.permissionKey(), false);
    }

    @Test
    public void testHasUserPermissionDoesntThrowsExceptionWhenUserHavePermissionWithLegacyConfiguration() throws InvalidInputException
    {
        testHasUserPermissionImpl(true, Permissions.getShortName(Permissions.PROJECT_ADMIN), true);
    }

    @Test
    public void testHasUserPermissionThrowsExceptionWhenUserDoesntHavePermission() throws InvalidInputException
    {
        testHasUserPermissionThrowsExceptionWhenUserDoesntHavePermission(ADMINISTER_PROJECTS.permissionKey(), false);
    }

    @Test
    public void testHasUserPermissionThrowsExceptionWhenUserDoesntHavePermissionWithLegacyConfiguration() throws InvalidInputException
    {
        testHasUserPermissionThrowsExceptionWhenUserDoesntHavePermission(Permissions.getShortName(Permissions.PROJECT_ADMIN), true);
    }

    private void testHasUserPermissionThrowsExceptionWhenUserDoesntHavePermission(String permissionArgument, boolean legacy)
            throws InvalidInputException
    {
        when(permission.getNameI18nKey()).thenReturn("permission.name.key");
        when(authenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText("permission.name.key")).thenReturn("Permission name");

        expectedException.expect(InvalidInputExceptionMatcher.withGenericError(
                String.format("User '%s' doesn't have the '%s' permission", testUser.getName(), "Permission name")
        ));
        testHasUserPermissionImpl(false, permissionArgument, legacy);
    }

    private void testHasUserPermissionImpl(boolean hasPermission, String permissionArgument, boolean legacy) throws InvalidInputException
    {
        final Issue issue = mockIssueWitGenericValue();

        final Map transientVars = ImmutableMap.of("issue", issue);
        final Map args = ImmutableMap.of(legacy ? "permission" : "permissionKey", permissionArgument);

        when(permissionManager.getProjectPermission(ADMINISTER_PROJECTS)).thenReturn(option(permission));

        final AbstractPermissionValidator validator = spy(createAbstractPermissionValidator());
        // hasUserPermissionForIssue has it's own tests - mock it
        doReturn(hasPermission).when(validator).hasUserPermissionForIssue(issue, ADMINISTER_PROJECTS, testUser, permissionManager);

        validator.hasUserPermission(args, transientVars, testUser);
    }

    @Test
    public void returnsTrueIfPermissionDoesNotExist() throws InvalidInputException
    {
        testPermissionDoesNotExist(false);
    }

    @Test
    public void returnsTrueIfPermissionDoesNotExistWithLegacyConfiguration() throws InvalidInputException
    {
        testPermissionDoesNotExist(true);
    }

    private void testPermissionDoesNotExist(boolean legacy) throws InvalidInputException
    {
        Issue issue = mockIssueWitGenericValue();

        Map transientVars = ImmutableMap.of("issue", issue);
        Map args = ImmutableMap.of(legacy ? "permission": "permissionKey", "notexisting");

        when(permissionManager.getProjectPermission(new ProjectPermissionKey("notexisting"))).thenReturn(Option.<ProjectPermission>none());

        AbstractPermissionValidator validator = spy(createAbstractPermissionValidator());
        validator.hasUserPermission(args, transientVars, testUser);
    }

    private AbstractPermissionValidator createAbstractPermissionValidator()
    {
        return new AbstractPermissionValidator()
        {
            @Override
            public void validate(final Map transientVars, final Map args, final PropertySet ps)
                    throws WorkflowException
            {
                throw new UnsupportedOperationException("This method should not be used by AbstractPermissionValidator");
            }
        };
    }
}
