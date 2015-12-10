package com.atlassian.jira.soy;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Capable of flushing a soy cache in a single bound. Temporary measure until Atlassian Cache can coordinate plugin
 * cache flushing.
 *
 * @since v6.1
 */
@Component
public class SoyCacheFlusher implements InitializingBean, DisposableBean
{
    private EventPublisher eventPublisher;
    private SoyTemplateRenderer soyTemplateRenderer;

    @Autowired
    public SoyCacheFlusher(@ComponentImport EventPublisher eventPublisher, @ComponentImport SoyTemplateRenderer soyTemplateRenderer)
    {
        this.eventPublisher = eventPublisher;
        this.soyTemplateRenderer = soyTemplateRenderer;
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        flushSoyCache();
    }

    /**
     * Attempts to flush the cache in the SoyTemplateRenderer.
     *
     * @return true only if the request was successfully sent without exception.
     */
    public boolean flushSoyCache()
    {
        try
        {
            soyTemplateRenderer.clearAllCaches();
            return true;
        }
        catch (Exception anything)
        {
            // we really don't want to make a fuss about this
            return false;
        }
    }
}
