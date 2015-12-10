package com.atlassian.jira.config.webwork;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.SafeAction;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.jelly.JellyHttpRequest;
import com.atlassian.jira.plugin.webwork.AutowireCapableWebworkActionRegistry;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfFailureException;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.webwork.JiraSafeActionParameterSetter;
import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import net.jcip.annotations.GuardedBy;
import org.apache.log4j.Logger;
import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.action.ResultException;
import webwork.action.factory.ActionFactory;
import webwork.action.factory.ActionFactoryProxy;
import webwork.action.factory.AliasingActionFactoryProxy;
import webwork.action.factory.ChainingActionFactoryProxy;
import webwork.action.factory.CommandActionFactoryProxy;
import webwork.action.factory.ContextActionFactoryProxy;
import webwork.action.factory.JavaActionFactory;
import webwork.action.factory.JspActionFactoryProxy;
import webwork.action.factory.PrefixActionFactoryProxy;
import webwork.action.factory.PrepareActionFactoryProxy;
import webwork.action.factory.ReloadHelperActionFactoryProxy;
import webwork.config.Configuration;
import webwork.util.BeanUtil;
import webwork.util.ClassLoaderUtils;
import webwork.util.ValueStack;
import webwork.util.injection.ObjectFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JiraActionFactory replaces the webwork1 {@link webwork.action.factory.DefaultActionFactory} and avoids is unsafe web
 * parameter setting as described in JRA-15664.
 * <p/>
 * This is class is designed to load actions that are "safe by default".
 * <p/>
 * The root ActionFactory it returns tries the regular, global application class loaders just like the JavaActionFactory
 * in webwork1 and caches any matches it finds, but will also try the plugins class loader when it is unable to find an
 * action class in the global class loaders.
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13
 */
public class JiraActionFactory extends ActionFactory
{
    private static final Logger log = Logger.getLogger(JiraActionFactory.class);

    private final ActionFactory factory;
    private final JiraPluginActionFactory rootActionFactory;

    public JiraActionFactory()
    {
        rootActionFactory = new JiraPluginActionFactory();
        /**
         * This code is taken directly from the base webwork1.  The key here is that we ourselves build the code steps
         * followed to get an action ( as opposed to using the supplied webwork1 ones).
         *
         * Specifically we have removed the {@link webwork.action.factory.ParametersActionFactoryProxy}
         * from the chain to prevent the unsafe parameter setting from occurring.
         *
         * The root in the chain of factories is is our own JiraPluginActionFactory.
         */
        ActionFactory factory = rootActionFactory;
        //
        // Not used by JIRA and hence not needed!
        //
        //factory = new ScriptActionFactoryProxy(factory);
        //
        // Not used by JIRA and hence not needed!
        //
        //factory = new XMLActionFactoryProxy(factory);
        factory = new PrefixActionFactoryProxy(factory);
        factory = new JspActionFactoryProxy(factory);
        factory = new CommandActionFactoryProxy(factory);
        factory = new LookupAliasActionFactoryProxy(factory);
        factory = new CommandActionFactoryProxy(factory);
        factory = new ContextActionFactoryProxy(factory);
        // JRA-21670 : required by plugins
        factory = new PrepareActionFactoryProxy(factory);
        //
        // we are replacing the ParametersActionFactoryProxy provided by webwork with our own
        factory = new SafeParameterSettingActionFactoryProxy(factory);
        //
        factory = new ChainingActionFactoryProxy(factory);
        try
        {
            final boolean reloadEnabled = "true".equalsIgnoreCase(Configuration.getString("webwork.configuration.xml.reload"));
            if (reloadEnabled)
            {
                factory = new ReloadHelperActionFactoryProxy(factory);
            }
        }
        catch (final IllegalArgumentException ignored)
        {}
        this.factory = factory;
    }

    /**
     * Added to allow the {@link JiraActionFactory.NonWebActionFactory} to set the action instantiation chain.
     *
     * @param factory The factory chain that will handle instantiating the passed in action names.
     * @param rootActionFactory The root of the action factory chain, responsible for actual creation of {@link Action}
     * instances.
     *
     * @deprecated since 5.0.7
     */
    @Deprecated
    public JiraActionFactory(final ActionFactory factory, final JiraPluginActionFactory rootActionFactory)
    {
        this.factory = factory;
        this.rootActionFactory = rootActionFactory;
    }


        /**
        * This {@link webwork.action.factory.ActionFactoryProxy} is responsible for setting the parameters into the action
        * before it is executed.  It replaces the security hole riddled webwork provided {@link
        * webwork.action.factory.ParametersActionFactoryProxy}
        */
    static class SafeParameterSettingActionFactoryProxy extends ActionFactoryProxy
    {
        private final JiraSafeActionParameterSetter parameterSetter;

        SafeParameterSettingActionFactoryProxy(final ActionFactory actionFactory)
        {
            super(actionFactory);
            parameterSetter = new JiraSafeActionParameterSetter();

        }

        @Override
        public Action getActionImpl(final String actionAlias) throws Exception
        {
            // Get action from the next factory
            final Action action = getNextFactory().getActionImpl(actionAlias);
            if (action != null)
            {
                checkXsrfStatus(action);
                checkWebSudoStatus(action.getClass());
                setActionParameters(action);
            }
            // Return action
            return action;
        }

        /**
         * Check the status of the XSRF token and take a course of action if things are not well.
         *
         * @param action the {@link webwork.action.Action} to be executed
         */
        private void checkXsrfStatus(Action action)
        {
            //No Xsrf checking for Jelly related requests
            if (ActionContext.getRequest() instanceof JellyHttpRequest)
            {
                return;
            }

            @SuppressWarnings ("unchecked")
            final Map<String, ?> parameters = ActionContext.getParameters();
            XsrfInvocationChecker xsrfInvocationChecker = getXsrfInvocationChecker();
            final XsrfCheckResult xsrfCheckResult = xsrfInvocationChecker.checkActionInvocation(action, parameters);
            if (xsrfCheckResult.isRequired())
            {
                final boolean sessionExpired = sessionExpired(xsrfCheckResult);
                if ((!xsrfCheckResult.isValid() || sessionExpired))
                {
                    throw new XsrfFailureException();
                }
            }
        }

        private void checkWebSudoStatus(Class<? extends Action> action)
        {
            InternalWebSudoManager webSudoManager = getInternalWebSudoManager();
            if (webSudoManager.isEnabled())
            {
                // The URI is basically ignored, we check if the action has the RequiresWebSudo annotation
                if (webSudoManager.matches(action))
                {
                    if (webSudoManager.hasValidSession(ActionContext.getRequest().getSession()))
                    {
                        webSudoManager.markWebSudoRequest(ActionContext.getRequest());
                    }
                    else
                    {
                        // Until JRADEV-4610 is fixed. If the customer allows anonymous people to have admin
                        // priveledges who is websudo to stop them?
                        final PermissionManager permissionManager = ComponentManager.getComponent(PermissionManager.class);
                        final JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getComponent(JiraAuthenticationContext.class);
                        if (jiraAuthenticationContext.getUser() == null && permissionManager.hasPermission(Permissions.ADMINISTER, (ApplicationUser)null))
                        {
                            webSudoManager.startSession(ActionContext.getRequest(), ActionContext.getResponse());
                        }
                        else
                        {
                            throw new WebSudoSessionException("No websudo session and it is required");
                        }
                    }
                }
            }
        }

        private boolean sessionExpired(XsrfCheckResult xsrfCheckResult)
        {
            final ApplicationUser currentUser = getAuthenticationContext().getUser();
            return xsrfCheckResult.isGeneratedForAuthenticatedUser() && currentUser == null;
        }

        JiraAuthenticationContext getAuthenticationContext()
        {
            return ComponentAccessor.getComponent(JiraAuthenticationContext.class);
        }

        XsrfInvocationChecker getXsrfInvocationChecker()
        {
            return ComponentAccessor.getComponent(XsrfInvocationChecker.class);
        }

        InternalWebSudoManager getInternalWebSudoManager()
        {
            return ComponentAccessor.getComponent(InternalWebSudoManager.class);
        }

        /**
         * This will set the parameters into the action.  This uses a web safe strategy for setting parameters
         *
         * @param action the action to set parameters into
         * @throws ResultException if an IllegalArgumentException is thrown, just like the old webwork1 code
         */
        private void setActionParameters(final Action action) throws ResultException
        {
            @SuppressWarnings ("unchecked")
            final Map<String, ?> parameters = ActionContext.getParameters();
            if (log.isDebugEnabled())
            {
                debugActionParameters(action, parameters);
            }
            try
            {
                if (action instanceof SafeAction)
                {
                    //
                    // its marked as a back end action and hence we trust it to accept any old map of parameters
                    //
                    BeanUtil.setProperties(parameters, action);
                }
                else
                {
                    //
                    // JRA-15664 - We are responsible for setting parameters into web actions not webwork1
                    //
                    parameterSetter.setSafeParameters(action, parameters);
                }
            }
            catch (final IllegalArgumentException iae)
            {
                // this is what webwork1 did if it got a IllegalArgumentException during the setting of parameters
                // so we do too
                throw new ResultException(Action.ERROR);
            }
        }

        ///CLOVER:OFF
        /**
         * Only needed for extreme debug purposes
         *
         * @param action the Action in play
         * @param parameters the map of parameters
         */
        private <C extends Comparable<C>> void debugActionParameters(final Action action, final Map<C, ?> parameters)
        {
            if (log.isDebugEnabled() && (action != null))
            {
                final Set<C> keySet = parameters.keySet();
                if (!keySet.isEmpty())
                {
                    final List<C> keys = new ArrayList<C>(keySet);
                    Collections.sort(keys);
                    String requestURL = "BackEnd Action";
                    if ((ActionContext.getRequest() != null) && (ActionContext.getRequest().getRequestURL() != null))
                    {
                        requestURL = ActionContext.getRequest().getRequestURL().toString();
                    }

                    log.debug("JAFP " + action.getClass().getName() + " - " + requestURL);
                    for (final Object key : keys)
                    {
                        log.debug("JAFP param=" + key + " value" + valStr(parameters.get(key)));
                    }
                    log.debug("JAFP ------");
                }
            }
        }

        private String valStr(final Object value)
        {
            final StringBuilder sb = new StringBuilder();
            if (value != null)
            {
                sb.append(value.getClass().getName()).append(" - ");
            }
            if (value instanceof String[])
            {
                for (final String val : ((String[]) value))
                {
                    sb.append(val).append(", ");
                }
            }
            else
            {
                sb.append(value);
            }
            return sb.toString();
        }
        ///CLOVER:ON
    }

    /**
     * This method is invoked by webwork1 to load an {@link webwork.action.Action} for a given action name.  The
     * parameters for that action are also set into the action before it is returned
     *
     * @param actionName the name of the action to load
     * @throws Exception because that is how webwork1 has declared its exception structure
     * @throws ResultException if the parameters cannot be set into the action successfully AND the action is not
     * declared {@link webwork.action.IllegalArgumentAware}
     */
    @Override
    public Action getActionImpl(final String actionName) throws Exception
    {
        return factory.getActionImpl(actionName);
    }

    /**
     * If the set of loaded plugins (and hence loaded actions) has changed then this method must be called to flush the
     * cache of action names to action classes.
     */
    @Override
    public void flushCaches()
    {
        //flush caches of the whole action factory chain!
        factory.flushCaches();
        // JRADEV-6571. If we don't clear this cache then we can't garbage collect reloadable plugins.
        ValueStack.clearMethods();
    }

    /**
     * Uses to set into the class loader to be used to load plugin code
     *
     * @param classLoader the classLoader to use when loading plugin code
     */
    void setPluginClassLoader(final ClassLoader classLoader)
    {
        rootActionFactory.setPluginClassLoader(classLoader);
    }

    /**
     * This delegate class is used to load actions from the plugin world first and then from the system class world
     * later.
     */
    private static class JiraPluginActionFactory extends ActionFactory
    {
        // We use a helper class since the Listener needs to be public, but we don't want to make
        // JiraPluginActionFactory public
        public class Listener
        {
            @EventListener
            public void onShutdown(final PluginFrameworkShutdownEvent event)
            {
                pluginClassLoader = null;
                // All cache entries are now invalid. We discard proactively because it can contain
                // classes from the pluginClassLoader which we want to make GC-able asap.
                flushCaches();
            }
        }

        private final Listener shutdownListener = new Listener();

        /**
         * A cache for the action name -> action class
         */
        @ClusterSafe
        private final ConcurrentMap<String, Class<Action>> actionMappingCache = new ConcurrentHashMap<String, Class<Action>>();
        //
        // made volatile to be doubly sure its set correctly.
        //
        private volatile ClassLoader pluginClassLoader;
        //
        // the current thread context class loader
        //
        private final ClassLoader systemClassLoader;

        JiraPluginActionFactory()
        {
            systemClassLoader = Thread.currentThread().getContextClassLoader();
        }

        /**
         * Called to instantiate an {@link webwork.action.Action} instance from an action name
         *
         * @param actionName the name of the action
         * @return an Action instance
         * @throws ActionNotFoundException if the action called <code>actionName</code> is not found
         * @throws Exception if an action cant be loaded
         */
        @GuardedBy ("cacheLock.readLock")
        @Override
        public Action getActionImpl(final String actionName) throws Exception
        {
            // Check cache first
            Action action;
            Class<Action> actionClass = actionMappingCache.get(actionName);
            if (actionClass == null)
            {
                actionClass = loadFromPluginClassLoader(actionName);
                if (actionClass == null)
                {
                    actionClass = loadFromSystemClassLoaders(actionName);
                }
                if (actionClass == null)
                {
                    throw new ActionNotFoundException(actionName);
                }
            }
            final String actionSimpleName = actionClass.getSimpleName();
            final AutowireCapableWebworkActionRegistry autowireCapableWebworkActionRegistry = getAutowireCapableWebworkActionRegistry();
            if (autowireCapableWebworkActionRegistry.containsAction(actionSimpleName))
            {
                try
                {
                    action = autowireCapableWebworkActionRegistry.getPlugin(actionSimpleName).autowire(actionClass);
                }
                catch (final Exception ex)
                {
                    // We need to log this error here, else it can get swallowed by Webwork, if it still has other actionNames it wants to try.
                    log.error("Error autowiring Action '" + actionClass.getName() + "'.", ex);
                    throw ex;
                }
            }
            else
            {
                // Can we actually instantiate this action class.  Its not valid until we can
                action = instantiateAction(actionName, actionClass);
            }

            // if we are in dev mode then we want these to be non cached so that JRebel acts ok.  This is a dev speed thing.  Otherwise we cache the
            // class definition so we dont have to look it up again.
            if (!JiraSystemProperties.isDevMode())
            {
                // Put action name --> action class in cache
                actionMappingCache.putIfAbsent(actionName, actionClass);
            }
            return action;
        }

        private AutowireCapableWebworkActionRegistry getAutowireCapableWebworkActionRegistry()
        {
            // Get a new one each time, because the ActionFactory is a permanent singleton, we don't know when Pico is reloaded.
            // Alternatively, we could use setter injection via Pico managed configurator like setPluginClassLoader(final ClassLoader classLoader)
            return ComponentAccessor.getComponentOfType(AutowireCapableWebworkActionRegistry.class);
        }

        private Action instantiateAction(final String actionName, final Class<? extends Action> actionClass)
        {
            // ok we have a class but is it really an Action.  Its not like a I dont trust you its just that.... I dont trust you
            if (!Action.class.isAssignableFrom(actionClass))
            {
                throw new IllegalArgumentException("Attempt to invoke a class that is not an action '" + actionName + "'");
            }
            try
            {
                return (Action) ObjectFactory.instantiate(actionClass);
            }
            catch (final Exception e)
            {
                throw new IllegalArgumentException("Action '" + actionName + "' could not be instantiated - " + e);
            }
            catch (final NoClassDefFoundError e)
            {
                throw new IllegalArgumentException(
                        "Action '" + actionName + "' could not be instantiated. Class is invalid or static initializers have failed to run");
            }
        }

        /**
         * If the set of loaded plugins (and hence loaded actions) has changed then this method must be called to flush
         * the cache of action names to action classes.
         */
        @GuardedBy ("cacheLock.writeLock")
        @Override
        public void flushCaches()
        {
            actionMappingCache.clear();
        }

        /**
         * Sets a plugin class loader into this class.
         * <p/>
         * THREAD-SAFETY - Uses a volatile variable
         *
         * @param classLoader the class load to use
         */
        private void setPluginClassLoader(final ClassLoader classLoader)
        {
            pluginClassLoader = classLoader;
            final EventPublisher eventPublisher = ComponentAccessor.getComponentOfType(EventPublisher.class);
            eventPublisher.register(shutdownListener);
            // We need to flush here despite the flush in onShutdown - we've registered a new source
            // of actions, and one of the new ones might shadow a systemClassLoader action loaded in
            // the meantime.
            flushCaches();
        }

        /**
         * Loads an action from the pluginClassLoader if there is one
         * <p/>
         *
         * @param name the action name
         * @return the class for for an action or null if one cant be loaded
         */
        private Class<Action> loadFromPluginClassLoader(final String name)
        {
            final ClassLoader pluginClassLoader = this.pluginClassLoader;
            if (pluginClassLoader != null)
            {
                try
                {
                    @SuppressWarnings ("unchecked")
                    final Class<Action> result = (Class<Action>) pluginClassLoader.loadClass(name);
                    return result;
                }
                catch (final Exception e)
                {
                    // Not found or could not be instantiated
                }
            }
            return null;
        }

        private Class<Action> loadFromSystemClassLoaders(final String name)
        {
            // Find class using systemClassLoader, or else try Class.forName() as a backup
            try
            {
                @SuppressWarnings ("unchecked")
                final Class<Action> actionClass = (Class<Action>) systemClassLoader.loadClass(name);
                return actionClass;
            }
            catch (final ClassNotFoundException e)
            {
                try
                {
                    return ClassLoaderUtils.<Action>loadClass(name, getClass());
                }
                catch (final Exception e2)
                {
                    // Not found or could not be instantiated
                    return null;
                }
            }            
        }
    }

    /**
     * <p>An {@link webwork.action.factory.ActionFactory} that is able to instantiate non-web actions (a.k.a. back-end actions)
     * defined in JIRA.<p>
     *
     * <p>Usage of this class is highly <strong>discouraged</strong>, all remaining back-end actions have been deprecated
     * and have suitable API replacements.</p>
     *
     * <p>This class has only been added to assist keeping compatibility for back-end actions until they can be completely
     * removed.</p>
     *
     * @deprecated since 5.0.7
     * @since v5.0.7
     */
    public static class NonWebActionFactory extends JiraActionFactory
    {
        /**
         * Initialize action factory proxy delegation chain.
         */
        public NonWebActionFactory()
        {
            super(buildActionFactoryChain(), null);
        }

        private static ActionFactory buildActionFactoryChain()
        {
            ActionFactory factory = new JavaActionFactory();
            factory = new PrefixActionFactoryProxy(factory);
            factory = new JspActionFactoryProxy(factory);
            factory = new CommandActionFactoryProxy(factory);
            factory = new AliasingActionFactoryProxy(factory);
            factory = new CommandActionFactoryProxy(factory);
            factory = new ContextActionFactoryProxy(factory);
            factory = new PrepareActionFactoryProxy(factory);
            factory = new SafeParameterSettingActionFactoryProxy(factory);
            return new ChainingActionFactoryProxy(factory);
        }
    }
}
