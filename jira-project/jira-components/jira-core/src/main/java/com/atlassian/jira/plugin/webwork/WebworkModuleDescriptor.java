package com.atlassian.jira.plugin.webwork;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import electric.xml.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.w3c.dom.NodeList;
import webwork.config.ConfigurationInterface;
import webwork.config.WebworkConfigurationNotFoundException;
import webwork.config.util.XMLConfigurationReader;

import java.util.Iterator;

import static java.lang.String.format;

/**
 * This module descriptor allows for plugins to include webwork actions inside plugins.
 * The XML looks something like this:
 * <code><pre>
 *  &lt;webwork key="webwork-test" name="Test webwork plugin" >
 *      &lt;actions>
 *          &lt;action name="PluginActionClassName" alias="PluginAction">
 *              &lt;view name="success">/views/administrators.vm&lt;/view>
 *          &lt;/action>
 *      &lt;/actions>
 *  &lt;/webwork>
 * </pre></code>
 */
public class WebworkModuleDescriptor extends AbstractJiraModuleDescriptor<Void> implements ConfigurationInterface
{
    public static final Logger log = Logger.getLogger(WebworkModuleDescriptor.class);

    private XMLConfigurationReader configurationReader;
    private final AutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry;
    private org.w3c.dom.Element actionElement;
    private Document webworkDocument;

    public WebworkModuleDescriptor(final JiraAuthenticationContext authenticationContext, final AutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.autowireCapabaleWebworkActionRegistry = autowireCapabaleWebworkActionRegistry;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        try
        {
            webworkDocument = new Document(element.asXML());
            final electric.xml.Element root = webworkDocument.getRoot(); //hackiness needed to convert dom4j element to w3c element
            final NodeList actionsNodes = root.getElementsByTagName("actions");
            if (actionsNodes.getLength() != 1)
            {
                throw new PluginParseException(
                        root.getName() + " module " + (root.hasAttribute("key") ? "with key='" + root.getAttribute("key") + "' " : "") + (actionsNodes.getLength() == 0 ? "requires an <actions> block." : "can have only 1 <actions> block (" + actionsNodes.getLength() + " found)."));
            }

            actionElement = (org.w3c.dom.Element) actionsNodes.item(0);
            //
            // by capturing the plugin key here with the action config
            // we can reconstruct what plugin gave off that configuration
            //
            configurationReader = new XMLConfigurationReader(actionElement, getCompleteKey());
        }
        catch (final Exception xmlActionConfigurationParsingException)
        {
            log.error
                    (
                            format("Unable to parse the webwork plugin module: '%s' due to invalid XML.", getCompleteKey()),
                            xmlActionConfigurationParsingException
                    );
        }
    }

    // Webwork plugins don't get instantiated in the normal way. They do not come through getModule(). Instead they
    // get constructed via the webwork stack which looks in webwork.properties and uses webwork.injection.objectcreator
    // to create the object. this, in turn, ends up going through the JiraPluginActionFactory. the magic for
    // "autowiring" a plugins-2 plugin needs to happen in there.
    @Override
    public void disabled()
    {
        super.disabled();
        //unregister all actions for this plugin when this module is disabled.
        autowireCapabaleWebworkActionRegistry.unregisterPluginModule(this);
    }

    @Override
    public void enabled()
    {
        super.enabled();
        //re-register the actions for this plugin.
        if (getPlugin() instanceof AutowireCapablePlugin)
        {
            registerActionClassNames();
        }
    }

    @Override
    public Void getModule()
    {
        throw new IllegalArgumentException("There is no module for WebworkModuleDescriptor.  Access the Descriptor directly");
    }

    public Object getImpl(final String aName) throws IllegalArgumentException
    {
        final Object actionMapping = configurationReader.getActionMapping(aName);
        if (actionMapping == null)
        {
            throw new WebworkConfigurationNotFoundException(this.getClass(), "No such setting", aName);
        }
        return actionMapping;
    }

    public void setImpl(final String aName, final Object aValue)
            throws IllegalArgumentException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("This configuration does not support updating a setting");
    }

    public Iterator listImpl()
    {
        return configurationReader.getActionMappingNames().iterator();
    }

    public Document getWebworkDocument()
    {
        return webworkDocument;
    }

    private void registerActionClassNames()
    {
        final NodeList actions = actionElement.getElementsByTagName("action");

        int length = actions.getLength();
        for (int i = 0; i < length; i++)
        {
            org.w3c.dom.Element action = (org.w3c.dom.Element) actions.item(i);
            String actionName = action.getAttribute("name");
            if (StringUtils.isNotEmpty(actionName))
            {
                String className = StringUtils.substringAfterLast(actionName, ".");
                //if the string didn't contain '.' simply return the whole String.
                if (StringUtils.isEmpty(className))
                {
                    className = actionName;
                }
                autowireCapabaleWebworkActionRegistry.registerAction(className, this);
            }
        }
    }
}
