package com.atlassian.jira.help;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Builder for creating {@link HelpUrl} instances.
 *
 * @since v6.2.4
 */
@NotThreadSafe
public interface HelpUrlBuilder
{
    /**
     * Set the {@link HelpUrl#getKey()}} of the generated {@link HelpUrl}.
     *
     * @param key the key to set.
     * @return this object so that the builder may be chained.
     */
    HelpUrlBuilder key(String key);

    /**
     * Set the {@link HelpUrl#getAlt()} of the generated {@link HelpUrl}.
     *
     * @param alt the alternate text for the URL.
     * @return this object so that the builder may be chained.
     */
    HelpUrlBuilder alt(String alt);

    /**
     * Set the {@link HelpUrl#getTitle()} of the generated {@link HelpUrl}.
     *
     * @param title the title for the URL.
     * @return this object so that the builder may be chained.
     */
    HelpUrlBuilder title(String title);

    /**
     * Set the URL of the generated {@link HelpUrl}.
     *
     * @param url the URL for the generated {@code HelpUrl}.
     * @return this object so that the builder may be chained.
     */
    HelpUrlBuilder url(String url);

    /**
     * Set the {@link HelpUrl#isLocal()} of the generated {@link HelpUrl}.
     *
     * @param local if the generated {@code HelpUrl} is local or not.
     * @return this object so that the builder may be chained.
     */
    HelpUrlBuilder local(boolean local);

    /**
     * Creates an independent copy of the current state of the builder.
     *
     * @return an independent copy of the current builder.
     */
    HelpUrlBuilder copy();

    /**
     * Generate the {@link HelpUrl} associated with this builder.
     */
    HelpUrl build();

    @ThreadSafe
    interface Factory
    {
        /**
         * Create a {@link com.atlassian.jira.help.HelpUrlBuilder} that will produce a URLs with the passed prefix and
         * suffix.
         *
         * @param prefix the prefix to prepend to any generated URL.
         * @param suffix the suffix to append to any generated URL.
         * @return a {@code HelpUrlBuilder} that will produce a URLs with the passed prefix and
         * suffix.
         */
        HelpUrlBuilder get(final String prefix, final String suffix);
    }
}
