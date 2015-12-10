package com.atlassian.jira.less.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.web.less.LookAndFeelLessProvider;
import com.atlassian.lesscss.spi.UriResolver;
import com.atlassian.lesscss.spi.UriResolverStateChangedEvent;
import com.atlassian.plugin.StateAware;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.webresource.WebResourceIntegration;

import org.springframework.beans.factory.annotation.Autowired;

/**
 */
public class DynamicLookAndFeelUriResolver implements UriResolver, StateAware
{
    private LookAndFeelLessProvider lessGenerator;
    private final PluginEventManager pluginEventManager;

    @Autowired
    public DynamicLookAndFeelUriResolver(
            @ComponentImport WebResourceIntegration webResourceIntegration,
            @ComponentImport ApplicationProperties applicationProperties,
            @ComponentImport PluginEventManager pluginEventManager
    )
    {
        this.lessGenerator = new LookAndFeelLessProvider(applicationProperties, webResourceIntegration);
        this.pluginEventManager = pluginEventManager;
    }

    @Override
    public void enabled() {
        pluginEventManager.register(this);
    }

    @Override
    public void disabled() {
        pluginEventManager.unregister(this);
    }

    @EventListener
    public void onStateChanged(LookAndFeelBean.LookAndFeelChangedEvent event) {
        pluginEventManager.broadcast(new DynamicUriResolverStateChangedEvent("look-and-feel"));
    }

    @Override
    public boolean exists(final URI uri)
    {
        return isDynamicLookAndFeelURI(uri);
    }

    @Override
    public String encodeState(final URI uri)
    {
        return lessGenerator.encodeState();
    }

    @Override
    public InputStream open(final URI uri) throws IOException
    {
        String less = lessGenerator.makeLookAndFeelLess();
        return new ByteArrayInputStream(less.getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public boolean supports(final URI uri)
    {
        return isDynamicLookAndFeelURI(uri);
    }

    private static boolean isDynamicLookAndFeelURI(final URI uri)
    {
        if ("dynamic".equals(uri.getScheme()))
        {
            String special = uri.getSchemeSpecificPart();
            if ("lookandfeel.less".equals(special))
            {
                return true;
            }
        }
        return false;
    }

    public static class DynamicUriResolverStateChangedEvent extends UriResolverStateChangedEvent
    {
        public DynamicUriResolverStateChangedEvent(final Object source)
        {
            super(source);
        }

        @Override
        public boolean hasChanged(final URI uri)
        {
            return isDynamicLookAndFeelURI(uri);
        }
    }
}