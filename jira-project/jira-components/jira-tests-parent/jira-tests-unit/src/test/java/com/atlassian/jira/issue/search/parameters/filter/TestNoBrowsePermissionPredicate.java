package com.atlassian.jira.issue.search.parameters.filter;

import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestNoBrowsePermissionPredicate
{
    private ApplicationUser theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockApplicationUser("fred");
    }

    @Test
    public void testEvaluate() throws Exception
    {
        final PermissionManager permissionManager = createMock(PermissionManager.class);
        final MockIssue issue = new MockIssue(123L);

        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, theUser)).andReturn(false);

        replay(permissionManager);

        NoBrowsePermissionPredicate predicate = new NoBrowsePermissionPredicate(theUser, permissionManager);
        assertTrue(predicate.evaluate(issue));

        verify(permissionManager);
    }
}
