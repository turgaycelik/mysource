package com.atlassian.jira.web.bean;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPermissionCheckBean
{

    private JiraAuthenticationContext nullUserAuthContext;
    private JiraAuthenticationContext privilegedAuthContext;
    private JiraAuthenticationContext unprivilegedAuthContext;
    private PermissionManager permissionManager;
    private User privilegedUser;
    private User unprivilegedUser;

    @Before
    public void setUp() throws Exception
    {
        privilegedUser = new MockUser("test");
        unprivilegedUser = new MockUser("nong");

        nullUserAuthContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, EasyList.build(new Object()), DuckTypeProxy.RETURN_NULL);

        Object duckAuthContext = new Object()
        {
            public User getLoggedInUser()
            {
                return privilegedUser;
            }
        };
        privilegedAuthContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, EasyList.build(duckAuthContext), DuckTypeProxy.RETURN_NULL);

        duckAuthContext = new Object()
        {
            public User getLoggedInUser()
            {
                return unprivilegedUser;
            }
        };
        unprivilegedAuthContext = (JiraAuthenticationContext) DuckTypeProxy.getProxy(JiraAuthenticationContext.class, EasyList.build(duckAuthContext), DuckTypeProxy.RETURN_NULL);

        Object duckPM = new Object()
        {
            public boolean hasPermission(int permissionsId, Issue entity, User u)
            {
                return u == privilegedUser;
            }
        };
        permissionManager = (PermissionManager) DuckTypeProxy.getProxy(PermissionManager.class, EasyList.build(duckPM), DuckTypeProxy.RETURN_NULL);

    }

    @Test
    public void testBean()
    {
        Issue issue = getIssue(false);
        Issue subtaskIssue = getIssue(true);

        PermissionCheckBean permissionCheck;

        permissionCheck = new PermissionCheckBean(nullUserAuthContext, permissionManager);
        assertFalse(permissionCheck.isIssueVisible(issue));
        
         Object duckPM = new Object()
        {
            public boolean hasPermission(int permissionsId, Issue entity, User u)
            {
                return u == null || u == privilegedUser;
            }
        };
        PermissionManager nullUserHasBrowsePermissionManager =  (PermissionManager) DuckTypeProxy.getProxy(PermissionManager.class, EasyList.build(duckPM), DuckTypeProxy.RETURN_NULL);
        permissionCheck = new PermissionCheckBean(nullUserAuthContext, nullUserHasBrowsePermissionManager);
        assertTrue(permissionCheck.isIssueVisible(issue));

        permissionCheck = new PermissionCheckBean(unprivilegedAuthContext, permissionManager);
        assertFalse(permissionCheck.isIssueVisible(issue));
        assertFalse(permissionCheck.isIssueVisible(subtaskIssue));
        assertFalse(permissionCheck.isIssueVisible(subtaskIssue.getParentObject()));

        permissionCheck = new PermissionCheckBean(privilegedAuthContext, permissionManager);
        assertTrue(permissionCheck.isIssueVisible(issue));
        assertTrue(permissionCheck.isIssueVisible(subtaskIssue));
        assertTrue(permissionCheck.isIssueVisible(subtaskIssue.getParentObject()));

        try
        {
            permissionCheck = new PermissionCheckBean(null, permissionManager);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {

        }
        try
        {
            permissionCheck = new PermissionCheckBean(nullUserAuthContext, null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {

        }

        try
        {
            permissionCheck = new PermissionCheckBean(nullUserAuthContext, permissionManager);
            permissionCheck.isIssueVisible(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {

        }

    }

    private Issue getIssue(boolean subtask)
    {
        if (subtask)
        {
            final Issue parentIssue = getIssue(false);
            Issue issue = new MockIssue()
            {

                public boolean isSubTask()
                {
                    return true;
                }

                public Issue getParentObject()
                {
                    return parentIssue;
                }
            };
            return issue;
        }
        else
        {
            return new MockIssue();
        }
    }
}
