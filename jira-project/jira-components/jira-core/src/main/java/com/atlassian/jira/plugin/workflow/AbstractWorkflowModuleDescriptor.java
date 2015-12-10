package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import org.apache.log4j.Logger;
import org.dom4j.Element;

//@RequiresRestart
public abstract class AbstractWorkflowModuleDescriptor<T> extends AbstractJiraModuleDescriptor<T>
        implements Comparable<AbstractWorkflowModuleDescriptor<T>>
{
    private static final Logger log = Logger.getLogger(AbstractWorkflowModuleDescriptor.class);

    private final OSWorkflowConfigurator workflowConfigurator;
    private final ComponentClassManager componentClassManager;

    private TypeResolver typeResolver;
    private Class<T> implementationClass;
    private String implementationClassName;

    public AbstractWorkflowModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final OSWorkflowConfigurator workflowConfigurator, final ComponentClassManager componentClassManager, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.workflowConfigurator = workflowConfigurator;
        this.componentClassManager = componentClassManager;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        // Get the module's classname
        implementationClassName = element.element(getParameterName()).getTextTrim();
        // Since Atlassian-Plugins v2.2, we can no longer load the class in init() because we haven't built the OSGi bundle for it yet.
        // We will load it in enabled() instead.
    }

    public void enabled()
    {
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Plugin '%s' is attempting to register workflow module '%s'.", getKey(), implementationClassName));
        }

        try
        {
            implementationClass = getPlugin().loadClass(implementationClassName, getClass());
        }
        catch (final ClassNotFoundException ex)
        {
            throw new PluginException("Cannot load condition class '" + implementationClassName + "'. " + ex.getMessage());
        }
        // Load the implementation class now that the OSGi bundle will be available
        super.enabled();

        registerWorkflowTypeResolver();
    }

    private void registerWorkflowTypeResolver()
    {
        typeResolver = createPluginTypeResolver();
        workflowConfigurator.registerTypeResolver(implementationClassName, typeResolver);
    }

    public void disabled()
    {
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Plugin '%s' is attempting to unregister workflow module '%s'.", getKey(), implementationClassName));
        }

        super.disabled();
        // unload the implementation class
        implementationClass = null;
        unregisterWorkflowTypeResolver();
    }

    private void unregisterWorkflowTypeResolver()
    {
        workflowConfigurator.unregisterTypeResolver(implementationClassName, typeResolver);
        typeResolver = null;
    }

    protected abstract String getParameterName();

    protected String getParamValue(final Element element, final String paramName)
    {
        if ((element.element(paramName) != null) && (element.element(paramName).getTextTrim() != null))
        {
            return element.element(paramName).getTextTrim();
        }

        return null;
    }

    public Class<T> getImplementationClass()
    {
        return implementationClass;
    }

    public int compareTo(final AbstractWorkflowModuleDescriptor<T> o)
    {
        if (o == null)
        {
            return 1;
        }

        if (o.getName() != null)
        {
            if (getName() != null)
            {
                return getName().compareTo(o.getName());
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if (getName() == null)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
    }

    /**
     * Get rendered HTML from a resource defined in the plugin descriptor.
     * @param resourceName Eg. "view" or "input-parameters"
     * @param descriptor ??
     * @return HTML rendered from resource (eg. velocity template or JSP)
     */
    public abstract String getHtml(String resourceName, AbstractDescriptor descriptor);

    public abstract boolean isOrderable();

    public abstract boolean isUnique();

    public abstract boolean isDeletable();

    public abstract boolean isAddable(String actionType);

    public boolean isEditable()
    {
        // Check if edit view exists
        final ResourceDescriptor resourceDescriptor = getResourceDescriptor(JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY,
            JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS);
        return resourceDescriptor != null;
    }

    protected PluginTypeResolver createPluginTypeResolver()
    {
        return new PluginTypeResolver();
    }

    /**
     * Responsible for instantiating a specific plugin module. This allows us to differentiate between V1 and V2 plugins.
     * <br/>
     * This {@link TypeResolver} is then registered with the {@link com.atlassian.jira.workflow.DefaultOSWorkflowConfigurator} so that it can be delegated
     * to by the {@link com.atlassian.jira.workflow.DefaultOSWorkflowConfigurator.JiraTypeResolverDelegator}.
     *
     * @since v4.1.1
     */
    class PluginTypeResolver extends TypeResolver
    {
        @Override
        protected Object loadObject(String clazz)
        {
            Plugin plugin = getPlugin();
            if (plugin == null)
            {
                throw new IllegalStateException("Plugin '" + getKey() + "' not available unexpectedly");
            }
            if (!plugin.getPluginState().equals(PluginState.ENABLED))
            {
                log.warn("Plugin " + getKey() + " not enabled.");
                return null;
            }
            return componentClassManager.newInstanceFromPlugin(getImplementationClass(), plugin);
        }
    }
}
