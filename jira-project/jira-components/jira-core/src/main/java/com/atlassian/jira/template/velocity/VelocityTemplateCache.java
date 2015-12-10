package com.atlassian.jira.template.velocity;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cache.GoogleCacheInstruments;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.velocity.htmlsafe.directive.DefaultDirectiveChecker;
import com.atlassian.velocity.htmlsafe.directive.DirectiveChecker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.velocity.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Caches per-template information so that we only calculate it at most once.
 *
 * @since v5.1
 */
@EventComponent
public class VelocityTemplateCache implements Startable
{
    private static final Logger log = LoggerFactory.getLogger(VelocityTemplateCache.class);

    /**
     * The maximum number of templates on which we'll cache info.
     */
    private static final int MAX_ENTRIES = 5000;

    /**
     * The directive checker to load cache values from.
     */
    private final DirectiveChecker directiveChecker;

    /**
     * The actual Google collections-backed cache.
     */
    private final Cache<Template, TemplateInfo> cache = CacheBuilder.newBuilder()
            .weakKeys()
            .maximumSize(MAX_ENTRIES)
            .removalListener(new TemplateInfoRemovalListener())
            .build(new TemplateInfoLoader());

    /**
     * Template cache instruments.
     */
    private AtomicReference<GoogleCacheInstruments> templateCacheInstruments = new AtomicReference<GoogleCacheInstruments>();

    /**
     * Directive cache instruments.
     */
    private final AtomicReference<GoogleCacheInstruments> directivesCacheInstruments = new AtomicReference<GoogleCacheInstruments>();

    /**
     * Creates a new VelocityDirectiveCache backed by a {@link DefaultDirectiveChecker}.
     */
    @SuppressWarnings ("UnusedDeclaration")
    public VelocityTemplateCache()
    {
        this(new DefaultDirectiveChecker());
    }

    /**
     * Creates a new VelocityDirectiveCache backed by the supplied DirectiveChecker.
     *
     * @param directiveChecker the DirectiveChecker to use
     */
    public VelocityTemplateCache(DirectiveChecker directiveChecker)
    {
        this.directiveChecker = directiveChecker;
    }

    @Override
    public void start() throws Exception
    {
        templateCacheInstruments.set(new GoogleCacheInstruments(getClass().getSimpleName()).addCache(cache).install());
        directivesCacheInstruments.set(new GoogleCacheInstruments(getClass().getSimpleName() + ".directives").install());
    }

    /**
     * Clears the cache upon receiving a ClearCacheEvent.
     *
     * @param clearCacheEvent a ClearCacheEvent
     */
    @EventListener
    @SuppressWarnings ({ "UnusedParameters", "UnusedDeclaration" })
    public void onClearCaches(ClearCacheEvent clearCacheEvent)
    {
        log.debug("Invalidating all entries in {}", this);
        cache.invalidateAll();
    }

    /**
     * Determines whether a directive with a specified name is present on a template instance.
     *
     * @param directiveName The name of the directive to look for.
     * @param template The template where we will search for the directive.
     * @return true, if the directive has been defined on the template; otherwise, false.
     */
    public boolean isDirectivePresent(String directiveName, Template template)
    {
        try
        {
            return cache.get(template).isDirectivePresent(directiveName);
        }
        catch (ExecutionException e)
        {
            log.warn(String.format("Error getting cached TemplateInfo for: %s", template), e);
            return directiveChecker.isPresent(directiveName, template);
        }
    }

    @ThreadSafe
    final class TemplateInfo extends CacheLoader<String, Boolean>
    {
        private final Template template;
        final Cache<String, Boolean> directivePresentCache;

        public TemplateInfo(Template template)
        {
            this.template = template;

            directivePresentCache = CacheBuilder.newBuilder().maximumSize(4).build(this);
            directivesCacheInstruments.get().addCache(directivePresentCache);
        }

        public boolean isDirectivePresent(String directiveName)
        {
            try
            {
                return directivePresentCache.get(directiveName);
            }
            catch (ExecutionException e)
            {
                log.warn(String.format("Error getting cached for (%s,%s)", directiveName, template), e);
                return directiveChecker.isPresent(directiveName, template);
            }
        }

        /**
         * Uses the <code>directiveChecker</code> to populate the value in the directives cache.
         */
        @Override
        public Boolean load(String key) throws Exception
        {
            return directiveChecker.isPresent(key, template);
        }
    }

    /**
     * Loader for Template->TemplateInfo cache.
     */
    final private class TemplateInfoLoader extends CacheLoader<Template, TemplateInfo>
    {
        @Override
        public TemplateInfo load(Template key) throws Exception
        {
            return new TemplateInfo(key);
        }
    }

    /**
     * Removes the TemplateInfo cache from the list of instrumented caches once it gets evicted. This is important to
     * prevent the GoogleCacheInstruments instance from keeping references to caches that have been removed.
     */
    final class TemplateInfoRemovalListener implements RemovalListener<Template, TemplateInfo>
    {
        @Override
        public void onRemoval(RemovalNotification<Template, TemplateInfo> removalNotification)
        {
            TemplateInfo removedTemplateInfo = removalNotification.getValue();
            if (removedTemplateInfo != null)
            {
                directivesCacheInstruments.get().removeCache(removedTemplateInfo.directivePresentCache);
            }
        }
    }
}
