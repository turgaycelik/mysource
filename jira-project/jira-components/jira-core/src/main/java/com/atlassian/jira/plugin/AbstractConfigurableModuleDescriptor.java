package com.atlassian.jira.plugin;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.configurable.ObjectConfigurationFactory;
import com.atlassian.configurable.ObjectDescriptor;
import com.atlassian.configurable.StringObjectDescription;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

import java.util.Map;

/**
 * An AbstractJiraModuleDescriptor that is able to be configured using an {@link ObjectConfiguration}.
 */
public abstract class AbstractConfigurableModuleDescriptor<T> extends AbstractJiraModuleDescriptor<T> implements ConfigurableModuleDescriptor
{
    private Element element;
    /**
     * The ObjectDescriptor was moved from protected to private, because it is no longer loaded in the init() method.
     * It is now loaded in enabled.
     *
     * If you need to get to this field, use the getObjectDescriptor() method.
     */
    private ObjectDescriptor objectDescriptor;
    private String objectDescriptorClassName = null;

    public AbstractConfigurableModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        this.element = element;

        // setup the object descriptor
        final Element objDescEl = element.element("objectdescriptor");
        if (objDescEl != null)
        {
            if (objDescEl.attribute("class") != null)
            {
                // Remember the configured class Name.
                // We cannot load the class yet as the OSGi bundle is not yet created.
                objectDescriptorClassName = objDescEl.attributeValue("class");
            }
            else
            {
                objectDescriptor = new StringObjectDescription(objDescEl.attributeValue("key"));
            }
        }
    }

    @Override
    public void enabled()
    {
        // if objectDescriptorClassName == null, then we already created a default StringObjectDescription.
        if (objectDescriptorClassName != null)
        {
            // Attempt to load the class that we can't do in init() any more because of OSGi bundle creation.
            try
            {
                objectDescriptor = (ObjectDescriptor) plugin.loadClass(objectDescriptorClassName, getClass()).newInstance();
            }
            catch (ClassNotFoundException e)
            {
                throw new PluginParseException("Error finding object descriptor class:" + objectDescriptorClassName, e);
            }
            catch (IllegalAccessException e)
            {
                throw new PluginParseException("Error creating object of class:" + objectDescriptorClassName, e);
            }
            catch (InstantiationException e)
            {
                throw new PluginParseException("Error creating object of class:" + objectDescriptorClassName, e);
            }
        }

        // We always call the parent implementation.
        super.enabled();
    }

    @Override
    public void disabled()
    {
        super.disabled();
        // We only want to throw away the loaded objectDescriptor if it was loaded in the enabled() method.
        if (objectDescriptorClassName != null)
        {
            objectDescriptor = null;
        }
    }

    protected ObjectDescriptor getObjectDescriptor() {
        if (objectDescriptor == null && objectDescriptorClassName != null)
        {
            throw new IllegalStateException("ObjectDescriptor class '" + objectDescriptorClassName + "' is not loaded. This means that the plugin was not enabled successfully.");
        }
        else
        {
            return objectDescriptor;
        }
    }

    public ObjectConfiguration getObjectConfiguration(Map params) throws ObjectConfigurationException
    {
        ObjectConfigurationFactory objectConfigurationFactory = ComponentAccessor.getComponent(ObjectConfigurationFactory.class);
        if (!objectConfigurationFactory.hasObjectConfiguration(getCompleteKey()))
        {
            objectConfigurationFactory.loadObjectConfigurationFromElement(element, getObjectDescriptor(), getCompleteKey(), getClassLoader());
        }
        return objectConfigurationFactory.getObjectConfiguration(getCompleteKey(), params);
    }
}
