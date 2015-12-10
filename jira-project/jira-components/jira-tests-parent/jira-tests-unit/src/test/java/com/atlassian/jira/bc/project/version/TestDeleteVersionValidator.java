package com.atlassian.jira.bc.project.version;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import org.easymock.MockControl;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.fail;

/**
 * Tests the {@link DeleteVersionValidator} construction. Note that the actual validation is already being tested in
 * the {@link TestDefaultVersionService}.
 */
public class TestDeleteVersionValidator
{
    @Mock
    @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;
    @Rule
    public MockitoContainer initMockitoMocks = MockitoMocksInContainer.rule(this);

    @Test
    public void testConstruction()
    {
        final JiraServiceContext context = new MockJiraServiceContext();
        final MockControl mockVersionManagerControl = MockControl.createControl(VersionManager.class);
        final VersionManager mockVersionManager = (VersionManager) mockVersionManagerControl.getMock();
        final PermissionManager permissionManager = MyPermissionManager.createPermissionManager(false);
        mockVersionManagerControl.replay();

        try
        {
            new DeleteVersionValidator(null, mockVersionManager, permissionManager);
            fail("Cannot construct a DeleteVersionValidator with a null JiraServiceContext");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        try
        {
            new DeleteVersionValidator(context, null, permissionManager);
            fail("Cannot construct a DeleteVersionValidator with a null VersionManager");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        try
        {
            new DeleteVersionValidator(context, mockVersionManager, null);
            fail("Cannot construct a DeleteVersionValidator with a null PermissionManager");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        new DeleteVersionValidator(context, mockVersionManager, permissionManager); // exception = fail
    }


}

