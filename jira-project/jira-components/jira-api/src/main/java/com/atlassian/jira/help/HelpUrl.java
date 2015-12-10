package com.atlassian.jira.help;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Represents the URL of a JIRA help page. The URL can point to a page within the current JIRA instance
 * (e.g. {@code isLocal() == true}) or to a page hosted on an external site.
 *
 * @since v6.2.4
 */
@ExperimentalApi
public interface HelpUrl
{
    /**
     * The URL for the help.
     *
     * @return the URL for the help.
     */
    String getUrl();

    /**
     * The alternate text for the URL.
     *
     * @return The alternate text for the URL.
     */
    String getAlt();

    /**
     * The title for the URL.
     *
     * @return The title for the URL.
     */
    String getTitle();

    /**
     * The key the URL is registered under.
     *
     * @return the key the URL is registered under.
     */
    String getKey();

    /**
     * Indicates if the URL is for JIRA internal help (i.e. help page served by JIRA).
     *
     * @return {@code true} when the URL is designed to be served by JIRA or {@code false} otherwise.
     */
    boolean isLocal();
}
