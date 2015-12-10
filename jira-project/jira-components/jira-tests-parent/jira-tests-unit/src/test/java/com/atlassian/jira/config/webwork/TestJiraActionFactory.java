package com.atlassian.jira.config.webwork;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.internal.AnnotatedMethodsListenerHandler;
import com.atlassian.event.spi.ListenerInvoker;
import com.atlassian.jira.action.SafeAction;
import com.atlassian.jira.config.webwork.actions.ActionConfiguration;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.plugin.webwork.AutowireCapableWebworkActionRegistry;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfDefaults;
import com.atlassian.jira.security.xsrf.XsrfFailureException;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import junit.framework.Assert;
import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.action.CommandDriven;
import webwork.action.ResultException;
import webwork.config.Configuration;
import webwork.config.ConfigurationInterface;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;

/**
 * A integration test for JiraActionFactory.
 *
 * @since v3.13.2
 */
public class TestJiraActionFactory
{
    private final String PACKAGE_NAME = getClass().getPackage().getName();
    private final String SOME_SYSTEM_ACTION_NAME = PACKAGE_NAME + ".SomeSystemAction";
    private final String SOME_PLUGIN_ACTION_NAME = PACKAGE_NAME + ".SomePluginAction";
    private final String SOME_SHADOWED_ACTION_NAME = PACKAGE_NAME + ".SomeShadowedAction";

    @Rule
    public final TestRule initMockitoMock = MockitoMocksInContainer.forTest(this);
    @Rule
    public ClearStatics clearStatics = new ClearStatics();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @AvailableInContainer
    private final ActionConfiguration actionConfiguration = new ActionConfiguration()
    {
        @Override
        public Entry getActionCommand(final String alias)
        {
            return Entry.newBuilder().className(alias).build();
        }
    };
    @Mock
    @AvailableInContainer
    private XsrfDefaults xsrfDefaults;
    @Mock

    private XsrfCheckResult xsrfCheckResult;
    @Mock
    @AvailableInContainer
    private XsrfTokenGenerator xsrfTokenGenerator;
    @Mock
    @AvailableInContainer
    private XsrfInvocationChecker xsrfInvocationChecker;
    @Mock
    private ConfigurationInterface configurationInterface;
    @Mock
    @AvailableInContainer
    private EventPublisher eventPublisher;
    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    @AvailableInContainer
    private AutowireCapableWebworkActionRegistry autowireCapableWebworkActionRegistry;
    @Mock
    @AvailableInContainer
    private InternalWebSudoManager internalWebSudoManager;

    @Before
    public void setUp(){
        Configuration.setConfiguration(configurationInterface);
        Mockito.when(xsrfInvocationChecker.checkActionInvocation(any(Action.class), anyMap())).thenReturn(xsrfCheckResult);
    }

    public static class WebActionImpl implements Action
    {
        private Long id;

        public String execute() throws Exception
        {
            return "webaction";
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        /** @noinspection UnusedDeclaration*/
        public void setUnSafeParameter(List anyoldJunk)
        {
            Assert.fail("this should not have invoked");
        }
    }

    public static class SafeActionImpl implements Action, SafeAction
    {
        boolean setCalled = false;

        public String execute() throws Exception
        {
            return "backendaction";
        }

        /** @noinspection UnusedDeclaration*/
        public void setUnSafeParameter(List anyoldJunk)
        {
            setCalled = true;
        }
    }

    public static class XsrfAction extends JiraWebActionSupport implements CommandDriven
    {
        public String doExecute()
        {
            return "ok";
        }
    }

    @Test
    public void testActionDistinction() throws Exception
    {

        // set up the action context
        setupActionContext(EasyMap.build("unSafeParameter", EasyList.build(), "id", "1234"));


        final JiraActionFactory factory = new JiraActionFactory();
        final WebActionImpl webAction = (WebActionImpl) factory.getActionImpl(WebActionImpl.class.getName());
        Assert.assertNotNull(webAction);
        Assert.assertEquals(new Long(1234), webAction.id);

        final SafeActionImpl backendAction = (SafeActionImpl) factory.getActionImpl(SafeActionImpl.class.getName());
        Assert.assertNotNull(backendAction);
        Assert.assertTrue(backendAction.setCalled);
    }

    @Test
    public void testBadBackendActionParameters() throws Exception
    {
        // set up the action context
        setupActionContext(EasyMap.build("unSafeParameter", "This is not a List"));

        final JiraActionFactory factory = new JiraActionFactory();
        expectedException.expect(ResultException.class);
        factory.getActionImpl(SafeActionImpl.class.getName());
    }

    @Test
    public void testActionFailsXsrfCheck() throws Exception
    {
        // set up the action context
        setupActionContext(EasyMap.build("unSafeParameter", "This is not a List"));

        final JiraActionFactory factory = new JiraActionFactory();
        Mockito.when(xsrfCheckResult.isRequired()).thenReturn(true);
        Mockito.when(xsrfCheckResult.isGeneratedForAuthenticatedUser()).thenReturn(true);
        expectedException.expect(XsrfFailureException.class);
        factory.getActionImpl(XsrfAction.class.getName());

    }

    @Test
    public void testBadWebActionParameters() throws Exception
    {
        // set up the action context
        setupActionContext(EasyMap.build("id", "This is not a Long"));

        final JiraActionFactory factory = new JiraActionFactory();
        expectedException.expect(ResultException.class);
        factory.getActionImpl(WebActionImpl.class.getName());
    }

    @Test
    public void testRubbishActionName() throws Exception
    {
        final JiraActionFactory factory = new JiraActionFactory();
        expectedException.expect(ActionNotFoundException.class);
        factory.getActionImpl("SomeRubbishName");
        Assert.fail("Should have barfed on this action name");
    }

    public static class ThisIsNotALoveSong
    {
        public ThisIsNotALoveSong()
        {
            Assert.fail("This should not have been invoked!");
        }
    }

    @Test
    public void testValidClassButNotAnAction() throws Exception
    {
        final JiraActionFactory factory = new JiraActionFactory();
        expectedException.expect(IllegalArgumentException.class);
        factory.getActionImpl(ThisIsNotALoveSong.class.getName());
    }

    public static class PrivateDancer implements Action
    {
        private PrivateDancer()
        {
        }

        public String execute() throws Exception
        {
            return null;
        }
    }

    @Test
    public void testPrivateConstructor() throws Exception
    {
        final JiraActionFactory factory = new JiraActionFactory();
        expectedException.expect(IllegalArgumentException.class);
        factory.getActionImpl(PrivateDancer.class.getName());
    }

    @Test
    public void testJiraPluginActionFactory() throws Exception
    {
        final URL[] urls = { getClass().getResource("testActions.jar") };
        final ClassLoader pluginClassLoader = new URLClassLoader(urls);

        final JiraActionFactory factory = new JiraActionFactory();

        // We use counting verifications for calls to eventPublisher.register because in real usage
        // the eventPublisher object is changing around these calls, so the number of invocations at
        // various points is a proxy for mucking around changing the container contents.

        Mockito.verify(eventPublisher, Mockito.never()).register(any());

        verifySystemActionsOnly(factory);

        factory.setPluginClassLoader(pluginClassLoader);

        verifySystemAndPluginActions(factory, pluginClassLoader);

        verifyRegistrationAndDispatchShutdown(1);

        verifySystemActionsOnly(factory);

        factory.setPluginClassLoader(pluginClassLoader);

        verifySystemAndPluginActions(factory, pluginClassLoader);

        verifyRegistrationAndDispatchShutdown(2);

        verifySystemActionsOnly(factory);
    }

    private void verifySystemActionsOnly(final JiraActionFactory factory) throws Exception
    {
        // No pluginClassLoader yet, so SomeSystemAction and SomeShadowedAction from system ...
        final Action systemAction = factory.getActionImpl(SOME_SYSTEM_ACTION_NAME);
        Assert.assertSame(systemAction.getClass(), SomeSystemAction.class);
        final Action shadowedAction = factory.getActionImpl(SOME_SHADOWED_ACTION_NAME);
        Assert.assertSame(shadowedAction.getClass(), SomeShadowedAction.class);
        // ... and no SomePluginAction yet.
        try
        {
            factory.getActionImpl(SOME_PLUGIN_ACTION_NAME);
            Assert.fail("Found " + SOME_PLUGIN_ACTION_NAME + " before pluginClassLoader set");
        }
        catch (ActionNotFoundException anfe)
        {
            // Expected
        }
    }

    private void verifySystemAndPluginActions(final JiraActionFactory factory, final ClassLoader pluginClassLoader)
            throws Exception
    {
        // pluginClassLoader present, so SomeSystemAction from system ...
        final Action systemAction = factory.getActionImpl(SOME_SYSTEM_ACTION_NAME);
        Assert.assertSame(systemAction.getClass(), SomeSystemAction.class);
        // ... and SomeShadowedAction and SomePluginAction from plugin.
        final Action shadowedAction = factory.getActionImpl(SOME_SHADOWED_ACTION_NAME);
        Assert.assertSame(shadowedAction.getClass(), pluginClassLoader.loadClass(SOME_SHADOWED_ACTION_NAME));
        final Action pluginAction = factory.getActionImpl(SOME_PLUGIN_ACTION_NAME);
        Assert.assertSame(pluginAction.getClass(), pluginClassLoader.loadClass(SOME_PLUGIN_ACTION_NAME));
    }

    private void verifyRegistrationAndDispatchShutdown(final int registrationCount)
    {
        // Since the event publisher is a @Mock but the event handler is private, we jump through a hoop
        // to dispatch the shutdown event
        final ArgumentCaptor<Object> listener = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(eventPublisher, Mockito.times(registrationCount)).register(listener.capture());
        final AnnotatedMethodsListenerHandler handler = new AnnotatedMethodsListenerHandler();
        for (final ListenerInvoker invoker : handler.getInvokers(listener.getValue()))
        {
            invoker.invoke(Mockito.mock(PluginFrameworkShutdownEvent.class));
        }
    }

    private void setupActionContext(final Map parameterMap)
    {
        setupActionContext(parameterMap, null);
    }

    private void setupActionContext(final Map parameterMap, final Map<String, String> cookieMap)
    {
        final ActionContext actionContext = new ActionContext();
        actionContext.put(ActionContext.PARAMETERS, parameterMap);
        ActionContext.setContext(actionContext);

        final MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(cookieMap);
        ActionContext.setRequest(servletRequest);
    }
}
