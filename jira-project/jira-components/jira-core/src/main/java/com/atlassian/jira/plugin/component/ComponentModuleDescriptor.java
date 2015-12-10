/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 18, 2004
 * Time: 5:21:27 PM
 */
package com.atlassian.jira.plugin.component;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.descriptors.RequiresRestart;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;
import org.picocontainer.MutablePicoContainer;

@RequiresRestart
/**
 * This JIRA-based ComponentModuleDescriptor manages plugins1 components.
 * Plugins2 components live in the framework module descriptor {@link com.atlassian.plugin.osgi.factory.descriptor.ComponentModuleDescriptor}.
 *
 * <p> Plugins 1 components don't have to include an interface, will be put into our PicoContainer, and can replace core JIRA components.
 * <p> Plugins 2 components have to include an interface because they are managed by the Spring dependency manager, and CANNOT replace core JIRA components.
 * @see com.atlassian.plugin.osgi.factory.descriptor.ComponentModuleDescriptor
 */
public class ComponentModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    Class<?> interfaceClazz;
    private String interfaceClazzName;

    public ComponentModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        if (element.element("interface") != null)
        {
            interfaceClazzName = element.element("interface").getTextTrim();
            // We cannot load a class from OSGi yet, as the bundle will not be built.
        }
    }

    @Override
    public void enabled()
    {
        // Note that components loaded as plugins2 will use the com.atlassian.plugin.osgi.factory.descriptor.ComponentModuleDescriptor
        if (interfaceClazzName != null)
        {
            // Load the interfaceClazz now that the OSGi bundle will be available.
            try
            {
                interfaceClazz = plugin.loadClass(interfaceClazzName, getClass());
            }
            catch (final Exception e)
            {
                throw new PluginException("Unable to load interface class: " + interfaceClazzName, e);
            }
        }
        super.enabled();
    }

    @Override
    public void disabled()
    {
        super.disabled();
        // Unload the interfaceClazz.
        interfaceClazz = null;
    }

    @Override
    public Void getModule()
    {
        throw new UnsupportedOperationException("You cannot retrieve a component instance - Pico-ified");
    }

    public void registerComponents(final MutablePicoContainer container)
    {
        // Check that we have classloaded the interface if required.
        if (interfaceClazzName != null && interfaceClazz == null)
        {
            throw new PluginException("Cannot register component '" + interfaceClazzName + "' in plugin '" + getKey() +
                "' because we haven't loaded the class. This means that this plugin has not been successfully enabled.");
        }

        // Unregister the component adapter if one already exists for the given key.
        // This will not work very well if the container actually returns the adapter from its parent container. However,
        // at the moment its our best shot.
        // Because this Module Descriptor only manages the plugins 1 components, having an interface is optional.
        if (container.getComponentAdapter(interfaceClazz) != null)
        {
            container.removeComponent(interfaceClazz);
        }

        if (interfaceClazz != null)
        {
            container.addComponent(interfaceClazz, getModuleClass());
        }
        else
        {
            container.addComponent(getModuleClass());
        }
    }

    public void unregisterComponents(final MutablePicoContainer container)
    {
        // Check that we have classloaded the interface if required.
        if (interfaceClazzName != null && interfaceClazz == null)
        {
            throw new PluginException("Cannot unregister component '" + interfaceClazzName + "' in plugin '" + getKey() +
                "' because we haven't loaded the class. This means that this plugin has not been successfully enabled.");
        }

        if (container.getComponentAdapter(interfaceClazz) != null)
        {
            container.removeComponent(interfaceClazz);
        }
        // TODO: Should this be in an else clause? It is probably harmless, but also pointless.
        container.removeComponent(getModuleClass());
    }
}