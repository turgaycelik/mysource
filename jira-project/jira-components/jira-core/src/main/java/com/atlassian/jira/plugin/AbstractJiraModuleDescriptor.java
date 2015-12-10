package com.atlassian.jira.plugin;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;
import static com.atlassian.jira.template.TemplateSources.fragment;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Base class for JIRA Plugin ModuleDescriptors.
 * <p/>
 * Note: This class should not be considered as actually part of the API. It is in the jira-api module for legacy
 * compatibility only.
 *
 * @param <T> The Plugin interface for this ModuleDescriptor.
 */
public abstract class AbstractJiraModuleDescriptor<T> extends AbstractModuleDescriptor<T>
        implements JiraResourcedModuleDescriptor<T>
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    private volatile String descriptionKey;
    private volatile String i18nNameKey;
    @ClusterSafe("Driven by plugin state, which is kept in synch across the cluster")
    private final ResettableLazyReference<T> moduleReference = new ResettableLazyReference<T>()
    {
        @Override
        protected T create() throws Exception
        {
            return createModule();
        }
    };

    // ----------------------------------------------------------------------------------------------- Type Dependencies
    private final JiraAuthenticationContext authenticationContext;

    // ------------------------------------------------------------------------------------------------------- Constants
    private static final Logger log = Logger.getLogger(AbstractJiraModuleDescriptor.class);

    // ---------------------------------------------------------------------------------------------------- Constructors

    protected AbstractJiraModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final Element descriptionEl = element.element("description");
        if (descriptionEl != null)
        {
            if (descriptionEl.attribute("key") != null)
            {
                descriptionKey = descriptionEl.attributeValue("key");
            }
        }

        this.i18nNameKey = element.attributeValue("i18n-name-key");
    }

    protected void assertResourceExists(final String type, final String name) throws PluginParseException
    {
        if (getResourceDescriptor(type, name) == null)
        {
            throw new PluginParseException("Resource with type: " + type + " and name " + name + " does not exist but should.");
        }
    }

    // --------------------------------------------------------------------------------------------------------- Methods

    public String getHtml(final String resourceName)
    {
        return getHtml(resourceName, new HashMap<String, Object>());
    }

    public String getHtml(final String resourceName, final Map<String, ?> startingParams)
    {
        try
        {
            StringWriter sw = new StringWriter();
            writeHtml(resourceName, startingParams, sw);
            return sw.toString();
        }
        catch (IOException e)
        {
            return "";
        }
    }

    @Override
    public void writeHtml(final String resourceName, final Map<String, ?> startingParams, final Writer writer)
            throws IOException
    {
        final ResourceDescriptor resource = getResourceDescriptor(JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY, resourceName);
        if (resource == null)
        {
            return; // no resource of this name
        }

        @SuppressWarnings ("unchecked")
        final Map<String, Object> velocityParams = createVelocityParams((Map<String, Object>) startingParams);

        if (isNotBlank(resource.getLocation()))
        {
            getTemplatingEngine().render(getTemplateSource(resource)).applying(velocityParams).asHtml(writer);
        }
        else
        {
            getTemplatingEngine().render(fragment(resource.getContent())).applying(velocityParams).asHtml(writer);
        }
    }

    private TemplateSource getTemplateSource(final ResourceDescriptor resource)
    {
        //
        // JRADEV-13927 - we need to retain the pluginKey:moduleKey that provided this template.  That way we can access it correctly on the on the other end
        // when we try to resolve it via PluginVelocityResourceLoader
        //
        return file(getCompleteKey() + "/" + resource.getLocation());
    }


    @VisibleForTesting
    VelocityTemplatingEngine getTemplatingEngine()
    {
        return ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
    }

    @Override
    public T getModule()
    {
        return moduleReference.get();
    }

    /**
     * Creates an instance of the Module.
     * <p/>
     * This is called by the lazy load logic of getModule() and is suitable for overriding in subclasses if they want to
     * customize how the Module class is instantiated.
     *
     * @return a new instance of the module.
     */
    protected T createModule()
    {
        final T module = moduleFactory.createModule(moduleClassName, this);
        // this sucks that we have to use reflection to figure out whether or not we need to call init
        // but now is not the time to refactor dozens of modules and their descriptors. Instead we'll shove
        // the ugly code in a ghetto class that you don't have to look at if you don't want to.
        new GhettoInitter(module, this).maybeInit();
        return module;
    }

    private VelocityRequestContextFactory getVelocityRequestContextFactory()
    {
        // This would probably be better injected, but you would have to update all the subclasses' constructors
        return ComponentAccessor.getComponentOfType(VelocityRequestContextFactory.class);
    }

    @Override
    public String getDescription()
    {
        if (descriptionKey != null)
        {
            return getI18nBean().getText(descriptionKey);
        }

        return super.getDescription();
    }

    @Override
    public String getName()
    {
        if (i18nNameKey != null)
        {
            return getI18nBean().getText(i18nNameKey);
        }

        return super.getName();
    }

    protected JiraAuthenticationContext getAuthenticationContext()
    {
        return authenticationContext;
    }

    /**
     * @param key the property key
     * @return the translated text
     * @deprecated Please use i18nBean.getText() instead. Since 5.0
     */
    @Deprecated
    public String getText(final String key)
    {
        return getI18nBean().getText(key);
    }

    public String getText(final String key, final Object params)
    {
        return getI18nBean().getText(key, params);
    }

    @Override
    public I18nHelper getI18nBean()
    {
        return authenticationContext.getI18nHelper();
    }

    protected Map<String, Object> createVelocityParams(final Map<String, Object> startingParams)
    {
        final Map<String, Object> params = getVelocityRequestContextFactory().getDefaultVelocityParams(startingParams, authenticationContext);
        final Map<String, Object> result = new HashMap<String, Object>();
        if (!params.containsKey("i18n"))
        {
            result.put("i18n", getI18nBean());
        }
        //JRA-29836
        if (!params.containsKey("ctx"))
        {
            result.put("ctx", params);
        }
        result.put("descriptor", this);
        return CompositeMap.of(result, params);
    }

    protected boolean isResourceExist(final String resourceName)
    {
        return getResourceDescriptor(JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY, resourceName) != null;
    }

    protected ClassLoader getClassLoader()
    {
        return getPlugin().getClassLoader();
    }

    @Override
    public void enabled()
    {
        super.enabled();
        //in case we enable an already enabled plugin we should reset the module reference as well
        //to avoid hanging on to expired references.  This caused issues with selenium tests where
        //a plugin was being enabled twice and could no longer reference certain components.
        moduleReference.reset();
    }

    @Override
    public void disabled()
    {
        super.disabled();
        moduleReference.reset();
    }

    /**
     * Back in the day, each Descriptor knew how to init the module it was in charge of.
     */
    private class GhettoInitter
    {
        private final Object maybeNeedsInit;
        private final Object argumentForInit;

        public GhettoInitter(final Object maybeNeedsInit, final Object argumentForInit)
        {
            this.maybeNeedsInit = maybeNeedsInit;
            this.argumentForInit = argumentForInit;
        }

        /**
         * Looks for a method named init that takes the specified argument and calls it.
         */
        public void maybeInit()
        {
            try
            {
                for (final Method m : maybeNeedsInit.getClass().getMethods())
                {
                    if (m.getName().equals("init") && (m.getParameterTypes().length == 1) && m.getParameterTypes()[0].isAssignableFrom(argumentForInit.getClass()))
                    {
                        m.invoke(maybeNeedsInit, argumentForInit);
                        break;
                    }
                }
            }
            catch (final IllegalAccessException e)
            {
                log.warn("Unable to instantiate module class: " + maybeNeedsInit.getClass(), e);
                throw new RuntimeException(e);
            }
            catch (final InvocationTargetException e)
            {
                log.warn("Unable to instantiate module class: " + maybeNeedsInit.getClass(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
