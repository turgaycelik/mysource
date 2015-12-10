package com.atlassian.jira.workflow.validator;

import java.util.Map;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestPermissionValidator
{
    @Rule
    public final RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    PermissionManager mockPermissionManager;

    @Mock
    @AvailableInContainer
    UserManager mockUserManager;

    private final Map mockTransientVars = emptyMap();
    private final Map mockArgs = emptyMap();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final ApplicationUser testUser = new MockApplicationUser("test-user-key", "test-user-name", "Test User", "test@localhost");

    @Test
    public void testGetCallerReturnsUserWhenUserExists() throws Exception
    {
        when(mockUserManager.getUserByKey(testUser.getKey())).thenReturn(testUser);

        final PermissionValidator permissionValidator = spy(new PermissionValidator());
        // get caller key delegates to WorkflowUtil.getCallerKey - it have it's own tests, mock it!
        doReturn(testUser.getKey()).when(permissionValidator).getCallerKey(mockTransientVars);

        final ApplicationUser user = permissionValidator.getCaller(mockTransientVars);

        assertEquals(testUser, user);
    }

    @Test
    public void testGetCallerShouldThrowExceptionWhenUserNotExists() throws Exception
    {
        // mockUserManager doesn't contain any users so the user won't be found

        final PermissionValidator permissionValidator = spy(new PermissionValidator());
        // get caller key delegates to WorkflowUtil.getCallerKey - it have it's own tests, mock it!
        doReturn(testUser.getKey()).when(permissionValidator).getCallerKey(mockTransientVars);

        expectedException.expect(InvalidInputExceptionMatcher.withGenericError(
                String.format("You don't have the correct permissions - user (%s) not found", testUser.getKey())
        ));
        permissionValidator.getCaller(mockTransientVars);
    }

    @Test
    public void testValidateGetsUserFromGetCallerAndDelegatesValidationToHasUserPermission() throws Exception
    {
        // This test is not a best example to follow. It was written for existing code and because someone used
        // inheritance instead of composition we do some dirty tests for the implementation here
        // but thanks to this other methods can be tested in more clean way
        final PropertySet propertySet = mock(PropertySet.class);

        final PermissionValidator permissionValidator = spy(new PermissionValidator());
        // getCaller is tested above
        doReturn(testUser).when(permissionValidator).getCaller(mockTransientVars);
        // hasUserPermission is tested in TestAbstractPermissionValidator
        doNothing().when(permissionValidator).hasUserPermission(mockTransientVars, mockArgs, testUser);
        permissionValidator.validate(mockTransientVars, mockArgs, propertySet);

        // verify that permission checking was delegated to hasUserPermission
        verify(permissionValidator).hasUserPermission(mockTransientVars, mockArgs, testUser);
    }

    @Test
    public void testMakeDescriptor() throws Exception
    {
        final String permission = "my-permission";

        final ValidatorDescriptor validatorDescriptor = PermissionValidator.makeDescriptor(permission);

        final Map validatorDescriptorArgs = validatorDescriptor.getArgs();
        assertEquals(ImmutableMap.of(
                "class.name", PermissionValidator.class.getName(),
                "permission", permission
        ), validatorDescriptorArgs);
        assertEquals("class", validatorDescriptor.getType());
    }
}
