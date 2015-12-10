package com.atlassian.jira.workflow;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.JiraUtils;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.Register;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides a way for JIRA to configure OSWorkflow to lookup condition, validator, and post-function
 * classes by setting the <code>TypeResolver</code>.
 *
 * @since 3.13
 */
public class DefaultOSWorkflowConfigurator implements Startable, OSWorkflowConfigurator
{
    private static final Logger log = Logger.getLogger(DefaultOSWorkflowConfigurator.class);
    @ClusterSafe
    private final ConcurrentMap<String, TypeResolver> pluginTypeResolvers = new ConcurrentHashMap<String, TypeResolver>();

    private final TypeResolver legacyTypeResolver;


    public DefaultOSWorkflowConfigurator()
    {
        this.legacyTypeResolver = new LegacyJiraTypeResolver();
    }

    ///CLOVER:OFF
    public void start() throws Exception
    {
        TypeResolver.setResolver(new JiraTypeResolverDelegator());
    }
    ///CLOVER:ON

    public void registerTypeResolver(final String className, final TypeResolver typeResolver)
    {
        notNull("className", className);
        notNull("typeResolver", typeResolver);

        pluginTypeResolvers.put(className, typeResolver);
    }

    public void unregisterTypeResolver(final String className, final TypeResolver typeResolver)
    {
        notNull("className", className);
        notNull("typeResolver", typeResolver);

        pluginTypeResolvers.remove(className, typeResolver);
    }

    /**
     * This is the {@link TypeResolver} implementation registered with OSWorkflow. It delegates the class object
     * instantiation to the {@link com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor.PluginTypeResolver}}
     * that is registered to the class name in question in our {@link #pluginTypeResolvers} map.
     * <br/>
     * Note that this means we no longer need Class loading and caching as the classes have already been looked up upon
     * enabling the Workflow module descriptor.
     *
     * @see DefaultOSWorkflowConfigurator#registerTypeResolver(String, com.opensymphony.workflow.TypeResolver)
     * @see DefaultOSWorkflowConfigurator#unregisterTypeResolver(String, com.opensymphony.workflow.TypeResolver)
     */
    class JiraTypeResolverDelegator extends TypeResolver
    {
        private TypeResolver getDelegate(Map args)
        {
            final String className = (String)args.get(Workflow.CLASS_NAME);
            if (className == null)
            {
                return null;
            }

            final TypeResolver typeResolver = pluginTypeResolvers.get(className);
            if (typeResolver == null)
            {
                return legacyTypeResolver;
            }
            return typeResolver;
        }

        @Override
        public Condition getCondition(String type, Map args) throws WorkflowException
        {
            final TypeResolver delegate = getDelegate(args);
            if (delegate == null)
            {
                return null;
            }
            return SkippableCondition.of(delegate.getCondition(type, args));
        }

        @Override
        public FunctionProvider getFunction(String type, Map args) throws WorkflowException
        {
            final TypeResolver delegate = getDelegate(args);
            if (delegate == null)
            {
                return null;
            }
            return delegate.getFunction(type, args);
        }

        @Override
        public Register getRegister(String type, Map args) throws WorkflowException
        {
            final TypeResolver delegate = getDelegate(args);
            if (delegate == null)
            {
                return null;
            }
            return delegate.getRegister(type, args);
        }

        @Override
        public Validator getValidator(String type, Map args) throws WorkflowException
        {
            final TypeResolver delegate = getDelegate(args);
            if (delegate == null)
            {
                return null;
            }
            return SkippableValidator.of(delegate.getValidator(type, args));
        }
    }

    /**
     * This class is used to load classes for legacy workflow functions, that
     */
    class LegacyJiraTypeResolver extends TypeResolver
    {
        @Override
        protected Object loadObject(final String className)
        {
            try
            {
                Class objClass = ClassLoaderUtils.loadClass(className.trim(), this.getClass());
                if (objClass == null)
                {
                    log.error("Could not load class '" + className + "'");
                    return null;
                }
                return JiraUtils.loadComponent(objClass);
            }
            catch (final Exception e)
            {
                log.error("Could not load class '" + className + "'", e);
                return null;
            }
        }
    }

}
