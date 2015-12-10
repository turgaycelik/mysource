package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TestAbstractIssueCondition
{
    @Test
    public void testNoIssue()
    {
        assertFalse("Should always be false when issue is not set", checkCondition(true, null));
    }

    @Test
    public void testTrue()
    {
        assertTrue(checkCondition(true, mock(Issue.class)));
    }

    @Test
    public void testFalse()
    {
        assertFalse(checkCondition(false, mock(Issue.class)));
    }

    private boolean checkCondition(final boolean returnValue, Issue issue)
    {
        final Map<String,Object> params = new HashMap<String,Object>();
        if (issue != null)
        {
            params.put("issue", issue);
        }

        final AbstractIssueCondition condition = new AbstractIssueCondition()
        {
            public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
            {
                return returnValue;
            }
        };
        return condition.shouldDisplay(null, new JiraHelper(null, null, params));
    }
}
