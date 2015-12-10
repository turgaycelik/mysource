package com.atlassian.jira.cluster;

import java.util.Collections;

import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.cluster.DefaultNodeStateService.FORBIDDEN_MESSAGE_CODE;
import static com.atlassian.jira.cluster.Node.NodeState.ACTIVATING;
import static com.atlassian.jira.cluster.Node.NodeState.ACTIVE;
import static com.atlassian.jira.cluster.Node.NodeState.PASSIVATING;
import static com.atlassian.jira.cluster.Node.NodeState.PASSIVE;
import static com.atlassian.jira.security.Permissions.SYSTEM_ADMIN;
import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link DefaultNodeStateService}.
 */
public class TestDefaultNodeStateService
{
    // Constants
    private static final String FORBIDDEN_MESSAGE = "Access forbidden";

    // Fixture
    private NodeStateService nodeStateService;
    @Mock private ApplicationUser mockUser;
    @Mock private GlobalPermissionManager mockGlobalPermissionManager;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private Node mockNode;
    @Mock private NodeStateManager mockNodeStateManager;

    @Before
    public void setUpService()
    {
        MockitoAnnotations.initMocks(this);
        when(mockI18nHelper.getText(FORBIDDEN_MESSAGE_CODE)).thenReturn(FORBIDDEN_MESSAGE);
        nodeStateService =
                new DefaultNodeStateService(mockGlobalPermissionManager, mockI18nHelper, mockNodeStateManager);
    }

    @Test
    public void serviceShouldDelegateActivationIfUserIsSysAdmin() throws ClusterStateException
    {
        // Set up
        setSysAdmin(true);

        // Invoke
        final ServiceResult result = nodeStateService.activate(mockUser);

        // Check
        assertTrue("SysAdmin should be able to activate a node", result.isValid());
        verify(mockNodeStateManager).activate();
        verifyNoMoreInteractions(mockNodeStateManager);
    }

    @Test
    public void serviceShouldDelegateDeactivationIfUserIsSysAdmin() throws Exception
    {
        // Set up
        setSysAdmin(true);

        // Invoke
        final ServiceResult result = nodeStateService.deactivate(mockUser);

        // Check
        assertTrue("SysAdmin should be able to deactivate a node", result.isValid());
        verify(mockNodeStateManager).deactivate();
        verifyNoMoreInteractions(mockNodeStateManager);
    }

    @Test
    public void serviceShouldRejectActivationIfUserIsNotSysAdmin()
    {
        // Set up
        setSysAdmin(false);

        // Invoke
        final ServiceResult result = nodeStateService.activate(mockUser);

        // Check
        assertForbidden(result, FORBIDDEN_MESSAGE);
    }

    @Test
    public void serviceShouldRejectDeactivationIfUserIsNotSysAdmin()
    {
        // Set up
        setSysAdmin(false);

        // Invoke
        final ServiceResult result = nodeStateService.deactivate(mockUser);

        // Check
        assertForbidden(result, FORBIDDEN_MESSAGE);
    }

    @Test
    public void activationShouldBeForbiddenWhenNodeBeingActivatedIsNotClustered() throws Exception
    {
        // Set up
        setSysAdmin(true);
        doThrow(new NotClusteredException()).when(mockNodeStateManager).activate();

        // Invoke
        final ServiceResult result = nodeStateService.activate(mockUser);

        // Check
        assertForbidden(result, new NotClusteredException().getMessage());
    }

    @Test
    public void deactivationShouldBeForbiddenWhenNodeBeingActivatedIsNotClustered() throws Exception
    {
        // Set up
        setSysAdmin(true);
        doThrow(new NotClusteredException()).when(mockNodeStateManager).deactivate();

        // Invoke
        final ServiceResult result = nodeStateService.deactivate(mockUser);

        // Check
        assertForbidden(result, new NotClusteredException().getMessage());
    }

    @Test
    public void isActiveShouldReturnTrueWhenNodeIsActive()
    {
        assertIsActive(ACTIVE, true);
    }

    @Test
    public void isActiveShouldReturnFalseWhenNodeIsActivating()
    {
        assertIsActive(ACTIVATING, false);
    }

    @Test
    public void isActiveShouldReturnFalseWhenNodeIsPassive()
    {
        assertIsActive(PASSIVE, false);
    }

    @Test
    public void isActiveShouldReturnFalseWhenNodeIsPassivating()
    {
        assertIsActive(PASSIVATING, false);
    }

    private void assertIsActive(final Node.NodeState nodeState, final boolean expectedValue)
    {
        // Set up
        when(mockNodeStateManager.getNode()).thenReturn(mockNode);
        when(mockNode.getState()).thenReturn(nodeState);

        // Invoke
        final boolean active = nodeStateService.isActive();

        // Check
        assertEquals(expectedValue, active);
    }

    private void assertForbidden(final ServiceResult result, final String expectedMessage)
    {
        assertNotNull(result);
        assertFalse("Expected a failure", result.isValid());
        final ErrorCollection errorCollection = result.getErrorCollection();
        assertNotNull(errorCollection);
        assertEquals(Collections.singleton(FORBIDDEN), errorCollection.getReasons());
        assertEquals(Collections.singletonList(expectedMessage), errorCollection.getErrorMessages());
    }

    private void setSysAdmin(final boolean isSysAdmin)
    {
        when(mockGlobalPermissionManager.hasPermission(SYSTEM_ADMIN, mockUser)).thenReturn(isSysAdmin);
    }
}
