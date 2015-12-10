package com.atlassian.jira.help;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Searches and loads the {@code HelpUrls} using a {@link HelpUrlsLoaderKey}.
 *
 * @since v6.2.4
 */
public interface HelpUrlsLoader extends Function<HelpUrlsLoader.HelpUrlsLoaderKey, HelpUrls>
{
    /**
     * Return the {@link HelpUrlsLoader.HelpUrlsLoaderKey} associated with the calling user. It can safely be used as
     * a key to cache the result of a {@code HelpUrls} lookup.
     *
     * @return the loader key associated with the calling user.
     */
    @Nonnull
    HelpUrlsLoaderKey keyForCurrentUser();

    /**
     * Return the {@link HelpUrls} associated with the passed {@link HelpUrlsLoader.HelpUrlsLoaderKey}. The
     * passed {@code HelpUrlsLoaderKey} must have been returned from a previous call to {@link #keyForCurrentUser()}
     * on this instance. It is not safe to use {@code HelpUrlsLoaderKey} across instances of {@code HelpUrlsLoader}.
     *
     * @param input the {@code HelpUrlsLoaderKey} to query for. It must have been returned from a {@link #keyForCurrentUser()}
     * on this instance. Using {@code HelpUrlsLoaderKey} from other instances may result in runtime exceptions.
     *
     * @return the {@code HelpUrls} associated with the passed key.
     */
    @Override
    HelpUrls apply(@Nullable HelpUrlsLoaderKey input);

    /**
     * Marker interface for all the state that is required to get a {@link HelpUrls} instance. It can safely be used as
     * a key to cache the result of a {@code HelpUrls} lookup.
     *
     * The key should be be considered opaque and non-portable across {@code HelpUserLoader}s, that is, the
     * {@code HelpUrlsLoaderKey} returned by one instance of {@code HelpUserLoader} cannot be used to find the
     * {@code HelpUrls} in another.
     *
     * @since v6.2.4
     */
    @Immutable
    interface HelpUrlsLoaderKey
    {
    }
}
