package com.atlassian.jira.functest.config.sharing;

import org.dom4j.Document;

import java.util.Arrays;

/**
 * Default implementation of {@link ConfigSharedEntityCleaner}.
 *
 * @since v4.2
 */
public class DefaultConfigSharedEntityCleaner implements ConfigSharedEntityCleaner
{
    private final Iterable<ConfigSharedEntityCleaner> cleaners;

    public DefaultConfigSharedEntityCleaner(final Document document)
    {
        this(new FavouritesCleaner(document), new SharePermissionsCleaner(document));
    }

    public DefaultConfigSharedEntityCleaner(final ConfigSharedEntityCleaner...cleaners)
    {
        this.cleaners = Arrays.asList(cleaners);
    }

    public boolean clean(final ConfigSharedEntity entity)
    {
        boolean result = false;
        for (ConfigSharedEntityCleaner cleaner : cleaners)
        {
            result = result | cleaner.clean(entity);
        }
        return result;
    }
}
