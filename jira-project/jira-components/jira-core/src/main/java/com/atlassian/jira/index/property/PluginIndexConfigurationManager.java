package com.atlassian.jira.index.property;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.IndexDocumentConfiguration;

/**
 * Manager for objects describing how the entity properties are indexed.
 *
 * @since v6.2
 */
public interface PluginIndexConfigurationManager
{
    /**
     * @param entityKey name of entity eg. issue, project etc
     * @return all {@link com.atlassian.jira.index.property.EntityPropertyIndexDocument} for the given plugin key.
     */
    Iterable<PluginIndexConfiguration> getDocumentsForEntity(@Nonnull String entityKey);

    /**
     * Sets the description of entity's indexing for the given plugin and module's keys.
     * @param pluginKey the key of the plugin.
     * @param moduleKey the key of the module.
     * @param document the description of entity's indexing.
     */
    void put(@Nonnull String pluginKey, @Nonnull String moduleKey, @Nonnull IndexDocumentConfiguration document);

    /**
     * Removes the description of entity's indexing for the given plugin.
     *
     * @param pluginKey the key of the plugin.
     */
    void remove(@Nonnull String pluginKey);
}
