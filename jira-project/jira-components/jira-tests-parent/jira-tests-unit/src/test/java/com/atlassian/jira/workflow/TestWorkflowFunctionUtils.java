package com.atlassian.jira.workflow;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.WorkflowContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestWorkflowFunctionUtils
{


    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    public static final String USERKEYFROMCONTEXT = "userkeyfromcontext";
    public static final String USERKEYFROMARGS = "userkeyfromargs";

    @Mock
    private WorkflowContext workflowContext;

    /**
     * Default call arguments populated with user data
     */
    private Map<String, Object> args;

    /**
     * Default workflow transient vars populated with context
     */
    private Map<String, Object> transientVars;

    ApplicationUser appUserFromArgs;
    ApplicationUser appUserFromContext;

    @Before
    public void before()
    {
        appUserFromArgs = new MockApplicationUser(USERKEYFROMARGS, "renamedJohnny");
        appUserFromContext = new MockApplicationUser(USERKEYFROMCONTEXT, "renamedJohnnyFromContext");
        when(userManager.getUserByKey(USERKEYFROMARGS)).thenReturn(appUserFromArgs);
        when(userManager.getUserByKey(USERKEYFROMCONTEXT)).thenReturn(appUserFromContext);

        args = ImmutableMap.<String, Object>of("userKey", USERKEYFROMARGS);

        transientVars = ImmutableMap.<String, Object>of("context", workflowContext);
        when(workflowContext.getCaller()).thenReturn(USERKEYFROMCONTEXT);

    }

    @Test
    public void populateParamsWithUserShouldPutValuesIntoMap()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        WorkflowFunctionUtils.populateParamsWithUser(params, "john");
        assertEquals("john", params.get("userKey"));
    }

    @Test
    public void populateParamsWithUserShouldPutValuesFromAppUserIntoMap()
    {
        Map<String, Object> params = new HashMap<String, Object>();

        WorkflowFunctionUtils.populateParamsWithUser(params, appUserFromArgs);
        assertEquals(1, params.size());
        assertEquals(appUserFromArgs.getKey(), params.get("userKey"));
    }

    @Test
    public void populateParamsWithUserShouldPutNullWhenNullUserGiven()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        WorkflowFunctionUtils.populateParamsWithUser(params, (ApplicationUser) null);
        assertTrue(params.containsKey("userKey"));
        assertNull(params.get("userKey"));
    }

    @Test
    public void getCallerKeyShouldTryGetKeyFromArgsAtFirst()
    {
        String result = WorkflowFunctionUtils.getCallerKey(transientVars, args);
        assertEquals(USERKEYFROMARGS, result);
        verify(workflowContext, never()).getCaller();
    }

    @Test
    public void getCallerKeyShouldGetKeyCallerWhenNoArgsGiven()
    {
        String result = WorkflowFunctionUtils.getCallerKey(transientVars, null);
        assertEquals(USERKEYFROMCONTEXT, result);
    }

    @Test
    public void getCallerKeyShouldGetKeyCallerWhenUserIsNotSetInArgs()
    {
        String result = WorkflowFunctionUtils.getCallerKey(transientVars, ImmutableMap.<String, Object>of());
        assertEquals(USERKEYFROMCONTEXT, result);
    }

    @Test
    public void getCallerUserFromArgsShouldReturnUserFromArgsByDefault()
    {
        ApplicationUser user = WorkflowFunctionUtils.getCallerUserFromArgs(transientVars, args);
        assertSame(appUserFromArgs, user);
    }

    @Test
    public void getCallerUserFromArgsShouldReturnUserFromContextWhenEmtyArgsGiven()
    {
        ApplicationUser user = WorkflowFunctionUtils.getCallerUserFromArgs(transientVars, ImmutableMap.<String, Object>of());
        assertSame(appUserFromContext, user);
    }


}
