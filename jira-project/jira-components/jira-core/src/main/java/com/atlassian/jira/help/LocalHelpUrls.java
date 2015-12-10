package com.atlassian.jira.help;

import java.util.Properties;
import javax.annotation.Nonnull;

/**
 * Load JIRA's local help URLs. These are the URLs to the help pages that are built into and served directly by JIRA.
 *
 * @since v6.2.4
 */
public interface LocalHelpUrls
{
    /**
     * Load the local help URLs stored in the passed properties.
     *
     * @param properties the properties to parse.
     * @return the local {@link com.atlassian.jira.help.HelpUrl} objects contained in the passed properties.
     */
    @Nonnull
    Iterable<HelpUrl> parse(@Nonnull Properties properties);

    /**
     * Load JIRA's default local help URLs.
     *
     * @return the local {@link com.atlassian.jira.help.HelpUrl} objects used in JIRA by default.
     */
    @Nonnull
    Iterable<HelpUrl> load();
}
