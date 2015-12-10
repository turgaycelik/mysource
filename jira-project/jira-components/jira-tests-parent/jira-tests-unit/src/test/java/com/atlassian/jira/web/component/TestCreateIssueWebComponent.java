package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCreateIssueWebComponent
{

    @Test
    public void testShow() throws Exception
    {
        MockProject project = new MockProject(123);

        final MockControl mockPermissionManagerControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManagerControl.expectAndReturn(
                mockPermissionManager.hasPermission(Permissions.CREATE_ISSUE, project, (User) null),
                false);
        mockPermissionManagerControl.expectAndReturn(
                mockPermissionManager.hasPermission(Permissions.CREATE_ISSUE, project, (User) null),
                true);
        mockPermissionManagerControl.replay();

        final CreateIssueWebComponent component = new CreateIssueWebComponent(null, null, mockPermissionManager, null);

        assertFalse(component.show(null, null));
        assertFalse(component.show(project, null));
        assertTrue(component.show(project, null));

        mockPermissionManagerControl.verify();
    }

}
