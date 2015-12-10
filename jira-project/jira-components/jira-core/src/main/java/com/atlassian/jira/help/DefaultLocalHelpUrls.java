package com.atlassian.jira.help;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;

import static org.apache.commons.lang.StringUtils.endsWith;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @since v6.2.4
 */
public class DefaultLocalHelpUrls implements LocalHelpUrls
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLocalHelpUrls.class);

    private static final String RESOURCE_NAME = "internal-help-paths.properties";
    private static final String PATH_SUFFIX = ".path";
    private static final String TITLE_SUFFIX = ".title";

    private final String resourceName;

    @ClusterSafe
    private LazyReference<Iterable<HelpUrl>> lazyReference = new LazyReference<Iterable<HelpUrl>>()
    {
        @Override
        protected Iterable<HelpUrl> create()
        {
            return parse(DefaultLocalHelpUrls.this.loadProperties());
        }
    };

    public DefaultLocalHelpUrls()
    {
        this(RESOURCE_NAME);
    }

    @VisibleForTesting
    DefaultLocalHelpUrls(final String resourceName)
    {
        this.resourceName = resourceName;
    }

    @Nonnull
    @Override
    public Iterable<HelpUrl> parse(@Nonnull final Properties properties)
    {
        final Map<String, HelpUrl> helpPaths = Maps.newHashMap();
        for (final String keyStr : properties.stringPropertyNames())
        {
            if (endsWith(keyStr, PATH_SUFFIX))
            {
                final String key = stripToNull(removeEnd(keyStr, PATH_SUFFIX));
                if (key != null)
                {
                    final String url = stripToNull(properties.getProperty(keyStr));
                    final String title = stripToNull(properties.getProperty(key + TITLE_SUFFIX));

                    if (url != null && title != null)
                    {
                        helpPaths.put(key, new ImmutableHelpUrl(key, url, title, null, true));
                    }
                }
            }
        }
        return ImmutableList.copyOf(helpPaths.values());
    }

    @SuppressWarnings ("ConstantConditions")
    @Nonnull
    @Override
    public Iterable<HelpUrl> load()
    {
        return lazyReference.get();
    }

    private Properties loadProperties()
    {
        Properties properties = new Properties();
        InputStream is = ClassLoaderUtils.getResourceAsStream(resourceName, getClass());
        if (is != null)
        {
            try
            {
                properties.load(is);
            }
            catch (IOException e)
            {
                LOG.error("Error loading '" + resourceName + "'.", e);
            }
            finally
            {
                IOUtils.closeQuietly(is);
            }
        }
        else
        {
            LOG.error("Unable to find '{}' on the classpath.", resourceName);
        }
        return properties;
    }
}
