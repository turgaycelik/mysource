package com.atlassian.jira.plugin.webresource;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.plugin.webresource.PluginResourceLocator;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.plugin.webresource.WebResourceManagerImpl;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple subclass of WebResourceManagerImpl that allows us to override
 * some of its behaviour
 *
 * @since v4.4
 */
public class JiraWebResourceManagerImpl extends WebResourceManagerImpl implements JiraWebResourceManager
{
    private static final Logger log = Logger.getLogger(JiraWebResourceManagerImpl.class);

    private static final String REQUEST_CACHE_METADATA_ADDED = "jira.metadata.added";
    private static final String REQUEST_CACHE_METADATA_KEY = "jira.metadata.map";


    public JiraWebResourceManagerImpl(ApplicationProperties appProps, PluginResourceLocator pluginResourceLocator,
            WebResourceIntegration webResourceIntegration, WebResourceUrlProvider webResourceUrlProvider,
            ResourceBatchingConfiguration batchingConfiguration)
    {
        super(pluginResourceLocator, webResourceIntegration, webResourceUrlProvider, batchingConfiguration);

    }

    public boolean putMetadata(String key, String value)
    {
        final Map<String, Object> cache = webResourceIntegration.getRequestCache();
        if (cache.get(REQUEST_CACHE_METADATA_ADDED) != null)
        {
            log.debug("Web Metadata already retrieved for this request - could not add key/value pair: '" + key + "' / '" + value + "'");
            return false;
        }

        getMetadataMap().put(key, value);
        return true;
    }

    public Map<String, String> getMetadata()
    {
        final Map<String, Object> cache = webResourceIntegration.getRequestCache();
        if (cache.get(REQUEST_CACHE_METADATA_ADDED) != null)
        {
            log.warn("Web Metadata cannot be retrieved more than once in a request");
            return Collections.emptyMap();
        }
        cache.put(REQUEST_CACHE_METADATA_ADDED, true);

        return getMetadataMap();
    }

    private Map<String, String> getMetadataMap()
    {
        final Map<String, Object> cache = webResourceIntegration.getRequestCache();
        @SuppressWarnings("unchecked")
        Map<String, String> metadataMap = (LinkedHashMap<String, String>) cache.get(REQUEST_CACHE_METADATA_KEY);
        if (metadataMap == null)
        {
            metadataMap = new LinkedHashMap<String, String>();
            cache.put(REQUEST_CACHE_METADATA_KEY, metadataMap);
        }
        return metadataMap;
    }
}
