package com.atlassian.jira.workflow.function.issue;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

/** @since v3.12 */
public class TestAssignToCurrentUserFunction
{
    private ApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("admin");
    }

    @Test
    public void testAssignToSelfWithAssignableUserPermission()
    {
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.expectVoid("setAssignee", new Constraint[] { P.eq(user.getDirectoryUser()) });
        mockIssue.setStrict(true);
        MutableIssue issue = (MutableIssue) mockIssue.proxy();

        final MockControl mockPermissionManagerControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();

        mockPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, issue, user);
        mockPermissionManagerControl.setReturnValue(true);

        mockPermissionManagerControl.replay();
        AssignToCurrentUserFunction assignToCurrentUserFunction = new AssignToCurrentUserFunction()
        {

            PermissionManager getPermissionManager()
            {
                return mockPermissionManager;
            }

            protected ApplicationUser getCallerUser(Map transientVars, Map args)
            {
                 return user;
            }
        };

        Map transientVars = new HashMap();
        transientVars.put("issue", issue);
        assignToCurrentUserFunction.execute(transientVars, null, null);
        mockPermissionManagerControl.verify();
        mockIssue.verify();
    }

    @Test
    public void testAssignToSelfWithoutAssignableUserPermission()
    {
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        MutableIssue issue = (MutableIssue) mockIssue.proxy();

        final MockControl mockPermissionManagerControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();

        mockPermissionManager.hasPermission(Permissions.ASSIGNABLE_USER, issue, user);
        mockPermissionManagerControl.setReturnValue(false);

        mockPermissionManagerControl.replay();
        AssignToCurrentUserFunction assignToCurrentUserFunction = new AssignToCurrentUserFunction()
        {

            PermissionManager getPermissionManager()
            {
                return mockPermissionManager;
            }

            protected ApplicationUser getCallerUser(Map transientVars, Map args)
            {
                 return user;
            }
        };

        Map transientVars = new HashMap();
        transientVars.put("issue", issue);
        assignToCurrentUserFunction.execute(transientVars, null, null);
        mockPermissionManagerControl.verify();
        mockIssue.verify();
    }

    @Test
    public void testAssignToSelfWithNoUser()
    {
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        MutableIssue issue = (MutableIssue) mockIssue.proxy();

        AssignToCurrentUserFunction assignToCurrentUserFunction = new AssignToCurrentUserFunction()
        {

            PermissionManager getPermissionManager()
            {
                return null;
            }

            protected ApplicationUser getCallerUser(Map transientVars, Map args)
            {
                return null;
            }
        };

        Map transientVars = new HashMap();
        transientVars.put("issue", issue);
        assignToCurrentUserFunction.execute(transientVars, null, null);
        mockIssue.verify();
    }
}
