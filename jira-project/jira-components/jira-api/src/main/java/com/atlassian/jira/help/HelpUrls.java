package com.atlassian.jira.help;

import com.atlassian.annotations.ExperimentalApi;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A mapping of keys to {@link com.atlassian.jira.help.HelpUrl}s.
 *
 * @since v6.2.4
 */
@ExperimentalApi
@ThreadSafe
public interface HelpUrls extends Iterable<HelpUrl>
{
    /**
     * Returns a {@link com.atlassian.jira.help.HelpUrl} associated with the passed key. The {@link #getDefaultUrl()}
     * URL is returned if the {@code key} has no associated URL.
     *
     * @param key the key to search for.
     *
     * @return the {@code HelpUrl}
     */
    @Nonnull
    HelpUrl getUrl(@Nonnull String key);

    /**
     * Returns a {@link com.atlassian.jira.help.HelpUrl} that can be used for generic JIRA help. It commonly points
     * at the JIRA help index/landing page.
     *
     * @return the default {@code HelpUrl} for this instance.
     */
    @Nonnull
    HelpUrl getDefaultUrl();

    /**
     * Return all the keys that have an associated {@link com.atlassian.jira.help.HelpUrl}.
     *
     * @return all the keys that have an associated {@code HelpUrl}.
     */
    @Nonnull
    Set<String> getUrlKeys();
}
