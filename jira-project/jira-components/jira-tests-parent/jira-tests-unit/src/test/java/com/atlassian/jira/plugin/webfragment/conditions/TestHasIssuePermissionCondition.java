package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestHasIssuePermissionCondition
{
    @Test(expected = PluginParseException.class)
    public void testNoPermissionExists()
    {
        final PermissionManager permissionManager = mock(PermissionManager.class);
        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);
        final Map<String,String> initParams = new HashMap<String,String>();

        condition.init(initParams);
    }

    @Test(expected = PluginParseException.class)
    public void testIncorrectPermission()
    {
        final PermissionManager permissionManager = mock(PermissionManager.class);
        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);
        final Map<String,String> initParams = new HashMap<String,String>();
        initParams.put("permission", "Be Crazy");

        condition.init(initParams);
    }

    @Test
    public void testNoIssue()
    {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        final JiraHelper jiraHelper = new JiraHelper(null, null, params);
        final PermissionManager permissionManager = mock(PermissionManager.class);
        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);
        final Map<String,String> initParams = new HashMap<String,String>();
        initParams.put("permission", "create");
        condition.init(initParams);

        assertFalse(condition.shouldDisplay(null, jiraHelper));
    }

    @Test
    public void testTrue()
    {
        final Issue issue = mock(Issue.class);
        final HashMap<String,Object> params = new HashMap<String, Object>();
        params.put("issue", issue);
        final JiraHelper jiraHelper = new JiraHelper(null, null, params);
        final PermissionManager permissionManager = mock(PermissionManager.class);
        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);
        final Map<String,String> initParams = new HashMap<String,String>();
        initParams.put("permission", "create");
        condition.init(initParams);

        when(permissionManager.hasPermission(11, issue, (ApplicationUser)null)).thenReturn(true);

        assertTrue(condition.shouldDisplay(null, jiraHelper));
    }

    @Test
    public void testFalse()
    {
        final Issue issue = mock(Issue.class);
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("issue", issue);
        final JiraHelper jiraHelper = new JiraHelper(null, null, params);
        final PermissionManager permissionManager = mock(PermissionManager.class);
        final HasIssuePermissionCondition condition = new HasIssuePermissionCondition(permissionManager);
        final Map<String, String> initParams = new HashMap<String,String>();
        initParams.put("permission", "browse");
        condition.init(initParams);

        assertFalse(condition.shouldDisplay(null, jiraHelper));
    }
}
