package com.atlassian.jira.jql.util;

import java.util.Collections;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link JqlIssueSupportImpl}.
 *
 * @since v4.0
 */
public class TestJqlIssueSupportImpl extends MockControllerTestCase
{
    @Test
    public void testGetIssuesEmptyKey() throws Exception
    {
        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);

        assertTrue(keySupport.getIssues(null, null).isEmpty());
        assertTrue(keySupport.getIssues("", null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesDoesNotExistKey() throws Exception
    {
        final String key = "key";
        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueByKeyIgnoreCase(key)).andReturn(null);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertTrue(keySupport.getIssues(key, null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesNoPermissionsKey() throws Exception
    {
        final String key = "key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueByKeyIgnoreCase(key)).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, (ApplicationUser) null)).andReturn(false);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertTrue(keySupport.getIssues(key, null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesSkipCheck() throws Exception
    {
        final String key = "key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueByKeyIgnoreCase(key)).andReturn(issue);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertEquals(Collections.<Issue>singletonList(issue), keySupport.getIssues(key));

        verify();
    }

    @Test
    public void testGetIssuesHappyPathKey() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("test");
        final String key = "key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueByKeyIgnoreCase(key)).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).andReturn(true);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertEquals(Collections.<Issue>singletonList(issue), keySupport.getIssues(key, user.getDirectoryUser()));

        verify();
    }


    @Test
    public void testGetIssueDoesNotExistId() throws Exception
    {
        final long id = 10;
        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(null);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertNull(keySupport.getIssue(id, (ApplicationUser) null));

        verify();
    }

    @Test
    public void testGetIssueNoPermissionsId() throws Exception
    {
        final long id = 11;
        final MockIssue issue = new MockIssue(id);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, (ApplicationUser) null)).andReturn(false);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertNull(keySupport.getIssue(id, (ApplicationUser) null));

        verify();
    }

    @Test
    public void testGetIssueSkipCheck() throws Exception
    {
        final long id = 12;
        final MockIssue issue = new MockIssue(id);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(issue);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertSame(issue, keySupport.getIssue(id));

        verify();
    }

    @Test
    public void testGetIssueHappyPathId() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("test");
        final long id = 12;
        final MockIssue issue = new MockIssue(id);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).andReturn(true);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertSame(issue, keySupport.getIssue(id, user.getDirectoryUser()));

        verify();
    }
}
