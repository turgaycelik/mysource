package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class TestUserIsProjectAdminCondition
{
    private final User mockUser = mock(User.class);
    private final PermissionManager mockPermissionManager = mock(PermissionManager.class);

    private final UserIsProjectAdminCondition condition = new UserIsProjectAdminCondition(mockPermissionManager);

    @Test
    public void testShouldDisplayWithNullJiraHelper()
    {
        assertFalse(condition.shouldDisplay(mockUser, null));
    }

    @Test
    public void testShouldDisplayWithNullUser()
    {
        assertFalse(condition.shouldDisplay(null, new JiraHelper()));
    }
}
