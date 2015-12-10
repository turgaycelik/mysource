package com.atlassian.jira.plugin;

import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.plugin.servlet.ContentTypeResolver;
import com.atlassian.plugin.webresource.PluginResourceLocator;
import com.atlassian.plugin.webresource.servlet.PluginResourceDownload;

/**
 * The sole reason for this class' existence is to handle the Pico dependency injection for the PluginResourceDownload.
 * It takes a String parameter but we have no way with Pico to signal statically what String should be injected there.
 * We take care of all that here by using an EncodingConfiguration.
 * @since v4.0
 */
public class JiraPluginResourceDownload extends PluginResourceDownload
{
    public JiraPluginResourceDownload(PluginResourceLocator pluginResourceLocator, ContentTypeResolver contentTypeResolver, EncodingConfiguration encodingConfig)
    {
        super(pluginResourceLocator, contentTypeResolver, encodingConfig.getEncoding());
    }
}
