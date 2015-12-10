package com.atlassian.jira.workflow.condition;

import java.util.Map;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.Maps;
import com.opensymphony.workflow.WorkflowContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


public class TestAllowOnlyReporter
{
    private AllowOnlyReporter condition;
    private Map<String, Object> transientVars;

    private ApplicationUser reporter;

    @Mock
    private WorkflowContext workflowContext;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    private MockIssue issue;

    @Rule
    public MockitoContainer init = MockitoMocksInContainer.rule(this);


    @Before
    public void setUp() throws Exception
    {
        reporter = new MockApplicationUser("george");
        when(userManager.getUserByKey("george")).thenReturn(reporter);

        issue = new MockIssue(123, "ISS-34");
        issue.setReporter(reporter.getDirectoryUser());

        transientVars = Maps.newHashMap();
        transientVars.put("originalissueobject", issue);
        transientVars.put("context", workflowContext);

        condition = new AllowOnlyReporter();
    }

    @Test
    public void shouldNotPassWhenCallerIsNull(){
        when(workflowContext.getCaller()).thenReturn(null);
        assertFalse(condition.passesCondition(transientVars, null, null));
    }

    @Test
    public void shouldNotPassWhenCallerIsDifferentUser(){
        ApplicationUser caller = new MockApplicationUser("fred");
        when(workflowContext.getCaller()).thenReturn(caller.getKey());

        assertFalse(condition.passesCondition(transientVars, null, null));
    }

    @Test
    public void shouldNotPassWhenReporterIsNull(){
        when(workflowContext.getCaller()).thenReturn(reporter.getKey());
        issue.setReporter(null);

        assertFalse(condition.passesCondition(transientVars, null, null));
    }

    @Test
    public void shouldPassWhenCallerIsTheSameUser(){
        when(workflowContext.getCaller()).thenReturn(reporter.getKey());

        assertTrue(condition.passesCondition(transientVars, null, null));
    }

}
