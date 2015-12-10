package com.atlassian.jira.functest.config.sharing;

/**
 * Cleans the sharing data associated with a {@link ConfigSharedEntity}.
 *
 * @since v4.2
 */
public interface ConfigSharedEntityCleaner
{
    /**
     * Remove all the sharing data associated with the passed entity.
     *
     * @param entity the shared entity to be cleaned.
     * @return true if something changed, false otherwise.
     */
    boolean clean(ConfigSharedEntity entity);
}
