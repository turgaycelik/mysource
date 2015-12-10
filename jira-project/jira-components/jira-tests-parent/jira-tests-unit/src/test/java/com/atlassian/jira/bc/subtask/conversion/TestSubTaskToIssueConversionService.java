package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSubTaskToIssueConversionService
{
    private final Issue subTask = new MockIssue()
    {
        public boolean isSubTask()
        {
            return true;
        }
    };

    private final Issue normalIssue = new MockIssue();

    private PermissionManager positivePermMgr;
    private PermissionManager negativePermMgr;

    @AvailableInContainer @Mock
    private PluginEventManager pluginEventManager;
    @AvailableInContainer @Mock
    private PluginAccessor pluginAccessor;
    @Mock
    private ProjectPermissionTypesManager projectPermissionTypesManager;

    @Rule
    public MockitoContainer initMockitoMocks = MockitoMocksInContainer.rule(this);


    private ApplicationUser testUser;
    JiraServiceContext context;

    @Before
    public void setUp() throws Exception
    {
        positivePermMgr = new MockPermissionManager(true);
        negativePermMgr = new MockPermissionManager(false);
        testUser = new MockApplicationUser("TestSubTaskToIssueConversionService");
        context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());
    }

    @Test
    public void testCanConvertIssueWithPermission()
    {
        SubTaskToIssueConversionService convService = getService(positivePermMgr);
        assertFalse(convService.canConvertIssue(context, normalIssue));
    }

    @Test
    public void testCanConvertIssueWithoutPermission()
    {
        SubTaskToIssueConversionService convService = getService(negativePermMgr);
        assertFalse(convService.canConvertIssue(context, normalIssue));
    }

    @Test
    public void testCanConvertSubTaskWithPermission()
    {
        SubTaskToIssueConversionService convService = getService(positivePermMgr);
        assertTrue(convService.canConvertIssue(context, subTask));
    }

    @Test
    public void testCanConvertSubTaskWithoutPermission()
    {
        SubTaskToIssueConversionService convService = getService(negativePermMgr);
        assertFalse(convService.canConvertIssue(context, subTask));
    }

    @Test
    public void testCanConvertNullIssue()
    {
        SubTaskToIssueConversionService convService = getService(negativePermMgr);
        try
        {
            convService.canConvertIssue(null, null);
            fail("Null Issue is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            convService.canConvertIssue(context, null);
            fail("Null Issue is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    private SubTaskToIssueConversionService getService(PermissionManager permManager)
    {
        return new DefaultSubTaskToIssueConversionService(permManager, null, null, null, null, null, null, null, null, null)
        {

            protected String getText(String key)
            {
                return key;
            }

            protected String getText(String key, Object param)
            {
               return key + param;
            }

            protected String getText(String key, String param0, String param1)
            {
                return key+ param0 + param1;
            }
        };
    }

    private class MockPermissionManager extends DefaultPermissionManager
    {
        private final boolean hasPermission;

        public MockPermissionManager(boolean hasPermission)
        {
            super(projectPermissionTypesManager);
            this.hasPermission = hasPermission;
        }

        public boolean hasPermission(int permissionsId, Issue issue, ApplicationUser user)
        {
            return hasPermission;
        }
    }

}
