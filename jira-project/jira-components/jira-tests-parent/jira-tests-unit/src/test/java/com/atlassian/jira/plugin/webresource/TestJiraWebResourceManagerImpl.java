package com.atlassian.jira.plugin.webresource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.webresource.PluginResourceLocator;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestJiraWebResourceManagerImpl
{

    private JiraWebResourceManagerImpl webResourceManager;
    private WebResourceIntegration mockWebResourceIntegration;

    @Before
    public void setUp() throws Exception
    {
        mockWebResourceIntegration = mock(WebResourceIntegration.class);
        PluginResourceLocator pluginResourceLocator = mock(PluginResourceLocator.class);
        WebResourceUrlProvider webResourceUrlProvider = mock(WebResourceUrlProvider.class);
        ResourceBatchingConfiguration resourceBatchingConfiguration = mock(ResourceBatchingConfiguration.class);
        ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
        webResourceManager = new JiraWebResourceManagerImpl(applicationProperties, pluginResourceLocator, mockWebResourceIntegration, webResourceUrlProvider, resourceBatchingConfiguration);
    }

    @Test
    public void testGetMetaData()
    {
        final Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("some", "value");
        when(mockWebResourceIntegration.getRequestCache()).
                thenReturn(MapBuilder.<String, Object>newBuilder("jira.metadata.map", map).toMutableMap());

        Map<String, String> metadata = webResourceManager.getMetadata();
        assertEquals(MapBuilder.singletonMap("some", "value"), metadata);

        //can only retrieve meta data once.
        metadata = webResourceManager.getMetadata();
        assertTrue(metadata.isEmpty());
    }
    
    @Test
    public void testPutMetaData()
    {
        when(mockWebResourceIntegration.getRequestCache()).thenReturn(new HashMap<String, Object>());
        assertTrue(webResourceManager.putMetadata("some", "value"));

        Map<String, String> metadata = webResourceManager.getMetadata();
        assertEquals(MapBuilder.singletonMap("some", "value"), metadata);

        assertFalse(webResourceManager.putMetadata("can't add value after", "get"));
        metadata = webResourceManager.getMetadata();
        assertTrue(metadata.isEmpty());
    }
}
