package com.atlassian.jira.bc.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @since v4.4
 */
public class TestProjectAction
{
    @Rule public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock @AvailableInContainer
    private GlobalPermissionManager globalPermissionManager;

    @Test
    public void testCorrectPermissions() throws Exception
    {
        assertThat(ProjectAction.EDIT_PROJECT_CONFIG.getPermissions(),
                is(new int[]{Permissions.ADMINISTER, Permissions.PROJECT_ADMIN}));

        assertThat(ProjectAction.VIEW_ISSUES.getPermissions(),
                is(new int[]{Permissions.BROWSE}));

        assertThat(ProjectAction.VIEW_PROJECT.getPermissions(),
                is(new int[]{Permissions.ADMINISTER, Permissions.BROWSE, Permissions.PROJECT_ADMIN}));
    }

    @Test
    public void testCheckActionPermission() throws Exception
    {
        final Project mockProject1 = new MockProject(11781L, "ABC");
        final User mockUser = new MockUser("admin");

        IMocksControl control = EasyMock.createControl();
        final PermissionManager permissionManager = control.createMock(PermissionManager.class);

        int[] perms = ProjectAction.VIEW_PROJECT.getPermissions();
        for (int i = -1; i < perms.length; i++)
        {
            control.reset();

            boolean success = false;
            for (int j = 0; !success && j < perms.length; j++)
            {
                int perm = perms[j];
                success = i == j;
                if (Permissions.isGlobalPermission(perm))
                {
                    EasyMock.expect(permissionManager.hasPermission(perm, mockUser)).andReturn(success);
                }
                else
                {
                    EasyMock.expect(permissionManager.hasPermission(perm, mockProject1, mockUser)).andReturn(success);
                }

            }
            control.replay();
            assertEquals(success, ProjectAction.VIEW_PROJECT.hasPermission(permissionManager, mockUser, mockProject1));
            control.verify();
        }
    }
}
