package com.atlassian.jira.tenancy;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.plugin.predicate.PluginKeyPatternsPredicate;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.filter;

/**
 * This class simply returns a PluginKeyPatternsPredicate for use by the 2 phase plugin system. It reads a blacklist of
 * values from a properties file, the location of which can be overriden by system property.
 *
 * @since v6.3
 */
public class PluginKeyPredicateLoader
{
    private static final Logger LOG = LoggerFactory.getLogger(PluginKeyPredicateLoader.class);
    private static final String ATLASSIAN_PLUGINS_TENANT_SMART_PATTERNS = "atlassian.plugins.tenant.smart.patterns";
    private static final String DEFAULT_FILENAME = "tenant-smart-patterns.txt";
    private final String fileName;
    private final LazyReference<PluginKeyPatternsPredicate> pluginKeyPredicateRef = new LazyReference<PluginKeyPatternsPredicate>()
    {
        @Override
        protected PluginKeyPatternsPredicate create()
        {
            return new PluginKeyPatternsPredicate(PluginKeyPatternsPredicate.MatchType.MATCHES_ANY, load());
        }
    };


    public PluginKeyPredicateLoader()
    {

        String fileName = JiraSystemProperties.getInstance().getProperty(ATLASSIAN_PLUGINS_TENANT_SMART_PATTERNS);
        if (fileName == null)
        {
            fileName = getRealPathForDefaultFileName();
        }
        this.fileName = fileName;
    }

    private String getRealPathForDefaultFileName()
    {
        return ServletContextProvider.getServletContext().getRealPath("WEB-INF/classes/" + DEFAULT_FILENAME);
    }

    @VisibleForTesting
    PluginKeyPredicateLoader(String fileName)
    {
        this.fileName = fileName;
    }

    private List<String> load()
    {
        try
        {
            // Override by system property, so replace tenantSmartPatterns with the file contents
            final File file = new File(fileName);
            Iterable tenantSmartPatterns = filter((List<String>) FileUtils.readLines(file), new Predicate<String>()
            {
                public boolean apply(final String line)
                {

                    return (!line.isEmpty() && line.charAt(0) != '#');
                }
            });
            return ImmutableList.copyOf(tenantSmartPatterns);
        }
        catch (IOException eio)
        {
            LOG.error("Could not read tenant-smart pattern file '{}' (using defaults)", fileName);
        }
        return ImmutableList.of();
    }


    public PluginKeyPatternsPredicate getPluginKeyPatternsPredicate()
    {
        return pluginKeyPredicateRef.get();
    }

}
