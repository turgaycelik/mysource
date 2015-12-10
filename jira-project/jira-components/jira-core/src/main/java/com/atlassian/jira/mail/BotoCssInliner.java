package com.atlassian.jira.mail;

import com.atlassian.botocss.Botocss;
import com.atlassian.botocss.BotocssStyles;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Default implementation of {@link CssInliner}.
 */
public class BotoCssInliner implements CssInliner
{
    /**
     * Logger for BotoCssInliner.
     */
    private static final Logger logger = LoggerFactory.getLogger(BotoCssInliner.class);

    /**
     * Expire the BotocssStyles after this many seconds of idleness.
     */
    static final int EXPIRE_SECS = 30;

    /**
     * This Guava cache will only ever contain at most 1 value. This has got to be some kind of Guava anti-pattern but
     * it is an easy way to get resettable reference that clears itself out after a period of inactivity.
     */
    private final Cache<Object, BotocssStyles> botocssStyles;

    private final ImmutableList<String> cssFilenames;

    /**
     * Constructor
     */
    public BotoCssInliner()
    {
        cssFilenames = ImmutableList.<String>builder()
                .add("templates/email/css/aui-styles.css")
                .add("templates/email/css/all-clients.css")
                        //.add("templates/email/css/mobile-clients.css")
                        //.add("templates/email/css/jira-mobile-clients.css")
                .add("templates/email/css/wiki-renderer.css")
                .add("templates/email/css/jira-styles.css")
                .add("templates/email/css/jira-status-lozenges.css")
                .build();

        botocssStyles = CacheBuilder.newBuilder()
                .expireAfterAccess(EXPIRE_SECS, SECONDS)
                .removalListener(new BotocssRemovalListener())
                .build(new BotocssCreator());
    }

    @Override
    public String applyStyles(String html)
    {
        if (html == null)
        {
            return null;
        }

        return Botocss.inject(html, botocssStyles.getUnchecked(this));
    }


    void performCacheMaintenance()
    {
        botocssStyles.cleanUp();
    }

    private BotocssStyles createStyles()
    {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream stream = null;

        for (String filename : cssFilenames)
        {
            try
            {
                stream = Preconditions.checkNotNull(getClass().getClassLoader().getResourceAsStream(filename));
                stringBuilder.append(IOUtils.toString(stream, "UTF-8"));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                IOUtils.closeQuietly(stream);
            }
        }

        return BotocssStyles.parse(stringBuilder.toString());
    }

    private class BotocssCreator extends CacheLoader<Object, BotocssStyles>
    {
        @Override
        public BotocssStyles load(final Object key) throws Exception
        {
            BotocssStyles styles = createStyles();
            logger.trace("Creating new styles: {}", styles);
            return styles;
        }
    }

    private static class BotocssRemovalListener implements RemovalListener<Object, BotocssStyles>
    {
        @Override
        public void onRemoval(final RemovalNotification<Object, BotocssStyles> notification)
        {
            logger.trace("Removed styles: {}", notification.getValue());
        }
    }
}
