package com.atlassian.jira.security.xsrf;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import org.junit.Before;
import org.junit.Test;

import webwork.action.ActionSupport;
import webwork.action.CommandDriven;
import webwork.config.util.ActionInfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.1
 */
public class TestDefaultXsrfInvocationChecker extends MockControllerTestCase
{
    private ComponentLocator componentLocator;
    private XsrfDefaults xsrfDefaults;
    private XsrfTokenGenerator xsrfTokenGenerator;
    private DefaultXsrfInvocationChecker checker;
    private MockJiraWebActionSupport actionDoDefault;
    private ActionInfo actionInfo;
    private HttpServletRequest httpServletRequest;
    private Map actionParameters;
    private MockJiraWebActionSupport actionDoExecute;
    private MockJiraWebActionSupport actionNullCommand;
    private MockJiraWebActionSupport actionNoSuchMethod;
    private static final HashMap EMPTY_PARAMETERS = new HashMap();

    private class MockJiraWebActionSupport extends JiraWebActionSupport implements CommandDriven
    {
        private MockJiraWebActionSupport(String command)
        {
            this.command = command;
        }

        public String doDefault()
        {
            return "ok";
        }

        @RequiresXsrfCheck
        protected String doExecute()
        {
            return "ok";
        }

        @Override
        protected <T> T getComponentInstanceOfType(Class<T> clazz)
        {
            return null;
        }
    }

    private class BackendAction extends ActionSupport
    {
    }

    @Before
    public void setUp() throws Exception
    {
        xsrfDefaults = getMock(XsrfDefaults.class);
        xsrfTokenGenerator = getMock(XsrfTokenGenerator.class);
        componentLocator = getMock(ComponentLocator.class);
        actionInfo = getMock(ActionInfo.class);
        httpServletRequest = getMock(HttpServletRequest.class);

        expect(componentLocator.getComponentInstanceOfType(XsrfDefaults.class)).andStubReturn(xsrfDefaults);
        expect(componentLocator.getComponentInstanceOfType(XsrfTokenGenerator.class)).andStubReturn(xsrfTokenGenerator);

        actionParameters = new HashMap();
        actionDoDefault = new MockJiraWebActionSupport("default");
        actionDoExecute = new MockJiraWebActionSupport("execute");
        actionNullCommand = new MockJiraWebActionSupport(null);
        actionNoSuchMethod = new MockJiraWebActionSupport("nosuchmethod");

        checker = new DefaultXsrfInvocationChecker(componentLocator);
    }

    @Test
    public void testNoCheckNeeded_ActionGlobalXsrfProtectionIsOff()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(false);

        replay();

        checker = newXsrfInvocationChecker();
        assertTrue(checker.checkActionInvocation(actionDoDefault, actionParameters).isValid());
    }

    @Test
    public void testNoCheckNeeded_WebRequestGlobalXsrfProtectionIsOff()
    {
        expect(httpServletRequest.getParameterMap()).andReturn(EMPTY_PARAMETERS);
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(false);

        replay();

        checker = newXsrfInvocationChecker();
        assertTrue(checker.checkWebRequestInvocation(httpServletRequest).isValid());
    }

    @Test
    public void testNoCheckNeeded_BackendAction()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        checker = newXsrfInvocationChecker();

        replay();

        final BackendAction backendAction = new BackendAction();
        assertTrue(checker.checkActionInvocation(backendAction, actionParameters).isValid());
    }

    @Test
    public void testNoCheckNeeded_ActionCommandWithoutXsrfAnnotation()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        replay();
        checker = newXsrfInvocationChecker();
        assertTrue(checker.checkActionInvocation(actionDoDefault, actionParameters).isValid());
    }

    @Test
    public void testNoCheckNeeded_ActionHasOverrideHeader()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn("no-check");

        replay();
        checker = newXsrfInvocationChecker();
        assertTrue(checker.checkActionInvocation(actionDoDefault, actionParameters).isValid());
    }

    @Test
    public void testNoCheckNeeded_WebRequestHasOverrideHeader()
    {
        expect(httpServletRequest.getParameterMap()).andReturn(EMPTY_PARAMETERS);
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn("no-check");

        replay();

        checker = newXsrfInvocationChecker();
        XsrfCheckResult checkResult = checker.checkWebRequestInvocation(httpServletRequest);
        assertTrue(checkResult.isValid());
        assertFalse(checkResult.isRequired());
    }

    @Test
    public void testNoCheckNeeded_ActionNoCommandMethodDefined()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        replay();

        checker = newXsrfInvocationChecker();
        XsrfCheckResult xsrfCheckResult = checker.checkActionInvocation(actionNoSuchMethod, actionParameters);
        assertTrue(xsrfCheckResult.isValid());
        assertFalse(xsrfCheckResult.isRequired());
    }

    @Test
    public void testCheckNeeded_ActionWithoutToken()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);
        expect(xsrfTokenGenerator.validateToken(httpServletRequest, null)).andReturn(false);
        expect(xsrfTokenGenerator.generatedByAuthenticatedUser(null)).andReturn(false);

        replay();

        checker = newXsrfInvocationChecker();
        assertFalse(checker.checkActionInvocation(actionDoExecute, actionParameters).isValid());
    }

    @Test
    public void testCheckNeeded_WebRequestWithoutToken()
    {
        expect(httpServletRequest.getParameterMap()).andReturn(EMPTY_PARAMETERS);
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);

        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        expect(xsrfTokenGenerator.validateToken(httpServletRequest, null)).andReturn(false);
        expect(xsrfTokenGenerator.generatedByAuthenticatedUser(null)).andReturn(false);

        replay();

        checker = newXsrfInvocationChecker();
        XsrfCheckResult checkResult = checker.checkWebRequestInvocation(httpServletRequest);
        assertFalse(checkResult.isValid());
        assertTrue(checkResult.isRequired());
    }

    @Test
    public void testCheckNeeded_ActionWithBadToken()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        actionParameters.put(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY, new String[] { "abc123" });


        expect(xsrfTokenGenerator.validateToken(httpServletRequest, "abc123")).andReturn(false);
        expect(xsrfTokenGenerator.generatedByAuthenticatedUser("abc123")).andReturn(false);

        replay();
        checker = newXsrfInvocationChecker();
        XsrfCheckResult checkResult = checker.checkActionInvocation(actionDoExecute, actionParameters);
        assertFalse(checkResult.isValid());
        assertTrue(checkResult.isRequired());
        assertFalse(checkResult.isGeneratedForAuthenticatedUser());
    }

    @Test
    public void testCheckNeeded_WebRequestWithBadToken()
    {
        Map parametersWithToken = new HashMap();
        parametersWithToken.put(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY, new String[] { "abc123" });

        expect(httpServletRequest.getParameterMap()).andReturn(parametersWithToken);
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        expect(xsrfTokenGenerator.validateToken(httpServletRequest, "abc123")).andReturn(false);
        expect(xsrfTokenGenerator.generatedByAuthenticatedUser("abc123")).andReturn(false);

        replay();

        checker = newXsrfInvocationChecker();
        assertFalse(checker.checkWebRequestInvocation(httpServletRequest).isValid());
    }

    @Test
    public void testCheckNeeded_ActionWithGoodToken()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        actionParameters.put(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY, new String[] { "abc123" });

        expect(xsrfTokenGenerator.validateToken(httpServletRequest, "abc123")).andReturn(true);
        expect(xsrfTokenGenerator.generatedByAuthenticatedUser("abc123")).andReturn(true);

        replay();

        checker = newXsrfInvocationChecker();
        XsrfCheckResult checkResult = checker.checkActionInvocation(actionDoExecute, actionParameters);
        assertTrue(checkResult.isValid());
        assertTrue(checkResult.isRequired());
        assertTrue(checkResult.isGeneratedForAuthenticatedUser());
    }

    @Test
    public void testCheckNeeded_WebRequestWithGoodToken()
    {
        Map parametersWithToken = new HashMap();
        parametersWithToken.put(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY, new String[] { "abc123" });

        expect(httpServletRequest.getParameterMap()).andReturn(parametersWithToken);
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        expect(xsrfTokenGenerator.validateToken(httpServletRequest, "abc123")).andReturn(true);
        expect(xsrfTokenGenerator.generatedByAuthenticatedUser("abc123")).andReturn(false);

        replay();

        checker = newXsrfInvocationChecker();
        assertTrue(checker.checkWebRequestInvocation(httpServletRequest).isValid());
    }

    @Test
    public void testCheckNeeded_ActionWithGoodTokenButNullCommand()
    {
        expect(httpServletRequest.getHeader("X-Atlassian-Token")).andReturn(null);
        expect(xsrfDefaults.isXsrfProtectionEnabled()).andReturn(true);

        actionParameters.put(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY, new String[] { "abc123" });

        expect(xsrfTokenGenerator.validateToken(httpServletRequest, "abc123")).andReturn(true);
        expect(xsrfTokenGenerator.generatedByAuthenticatedUser("abc123")).andReturn(false);

        replay();

        checker = newXsrfInvocationChecker();
        XsrfCheckResult checkResult = checker.checkActionInvocation(actionNullCommand, actionParameters);
        assertTrue(checkResult.isValid());
        assertTrue(checkResult.isRequired());
        assertFalse(checkResult.isGeneratedForAuthenticatedUser());
    }

    private DefaultXsrfInvocationChecker newXsrfInvocationChecker()
    {
        return new DefaultXsrfInvocationChecker(componentLocator)
        {
            @Override
            ActionInfo getActionInfo(final String magicKey)
            {
                return actionInfo;
            }

            @Override
            HttpServletRequest getActionHttpRequest()
            {
                return httpServletRequest;
            }
        };
    }
}
