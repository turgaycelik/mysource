package com.atlassian.jira.plugin;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.splitPreserveAllTokens;
import static org.apache.commons.lang.StringUtils.stripStart;

public class PluginVelocityResourceLoader extends ResourceLoader
{
    private static final Logger log = Logger.getLogger(PluginVelocityResourceLoader.class);
    private static final Pattern PLUGINKEY_PREFIXED = Pattern.compile(""
            + "([^:/]+)"    // plugin key
            + ":"           // colon
            + "([^/:]+)"    // module key
            + "/"           // slash
            + "(.*)"        // template name
            + "");

    @Override
    public void init(final ExtendedProperties configuration)
    {
    }

    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException
    {
        PluginAccessor pluginAccessor = pluginAccessor();

        // does it have the pluginKey:moduleKey/template name syntax
        Matcher matcher = PLUGINKEY_PREFIXED.matcher(name);
        if (matcher.matches())
        {
            String pluginKey = matcher.group(1);
            String resourcePath = stripStart(matcher.group(3), "/");

            Plugin plugin = pluginAccessor.getPlugin(pluginKey);
            if (plugin != null)
            {
                InputStream resourceAsStream = plugin.getResourceAsStream(resourcePath);
                if (resourceAsStream != null)
                {
                    return resourceAsStream;
                }
            }
            //
            // Plugins can re-use JIRA templates (and any other peoples templates for that matter)
            // so we fall back to using the old way of ANY old template from any old place.
            //
            // Sigh!!!
            //
            name = resourcePath;
        }

        //
        // JRADEV-13927
        //
        // The old school way means that plugins with the same template name will clash because
        // we are using the dodgy pluginAccessor smoosh resource method.
        //
        // If two plugins have "/templates/mytemplate.vm" then we are in real trouble
        // as one will win over the other in no particular order.
        //
        // This code is retained while we sort out all cases where this might happen.  Template renderer
        // inside atlassian-plugins uses class path loading tricks to ensure templates are loaded from
        // the plugin from which they originate.
        //
        //  This codes for hire, even if its just stabbing in the dark!
        //                                                              - The Boss!
        //
        String resourceName = stripStart(name, "/");
        return pluginAccessor.getDynamicResourceAsStream(resourceName);
    }

    @VisibleForTesting
    PluginAccessor pluginAccessor()
    {
        return ComponentAccessor.getPluginAccessor();
    }

    /**
     * @return always returns 0
     */
    @Override
    public long getLastModified(final Resource resource)
    {
        return 0;
    }

    /**
     * @return always returns true
     */
    @Override
    public boolean isSourceModified(final Resource resource)
    {
        return true;
    }
}
