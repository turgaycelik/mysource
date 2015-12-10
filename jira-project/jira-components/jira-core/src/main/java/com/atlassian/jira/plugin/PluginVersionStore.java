package com.atlassian.jira.plugin;

import java.util.List;

/**
 * Handles {@link com.atlassian.jira.plugin.PluginVersion}'s persistence.
 *
 * @since v3.13
 */
public interface PluginVersionStore
{
    /**
     * Creates a {@link com.atlassian.jira.plugin.PluginVersion} in the database. The id in the object will
     * be ignored.
     *
     * @param pluginVersion contains the key, name, and version to be persisted.
     * @return a {@link com.atlassian.jira.plugin.PluginVersion} with the database id populated.
     *
     * @throws IllegalArgumentException if pluginVersion is null.
     */
    PluginVersion create(PluginVersion pluginVersion);

    /**
     * Updates a plugin version in the datastore.
     *
     * @param pluginVersion contains the key, name, and version to update. The id will be used to find the record to
     * update.
     * @return a {@link com.atlassian.jira.plugin.PluginVersion} that represents the updated record.
     *
     * @throws IllegalArgumentException if pluginVersion or pluginVersion.getId is null or we can not find the
     * record with the provided id.
     */
    PluginVersion update(PluginVersion pluginVersion);

    /**
     * Deletes a plugin version from the datastore. If the provided id does not resolve to an existing record then
     * this method will return false and no records will be deleted.
     *
     * @param pluginVersionId identifies the plugin version record to delete, not null.
     * @return true if the record was deleted, false otherwise.
     *
     * @throws IllegalArgumentException if the pluginVersionId is null.
     */
    boolean delete(Long pluginVersionId);

    /**
     * Gets the {@link com.atlassian.jira.plugin.PluginVersion} specified by the id.
     * @param pluginVersionId identifies the plugin version record to retrieve.
     * @return the {@link com.atlassian.jira.plugin.PluginVersion} with the corresponding id or null if the record
     * is not found.
     */
    PluginVersion getById(Long pluginVersionId);

    /**
     * Returns a list of all {@link com.atlassian.jira.plugin.PluginVersion}'s.
     *
     * @return a list of all {@link com.atlassian.jira.plugin.PluginVersion}'s.
     */
    List<PluginVersion> getAll();

    /**
     * Deletes any {@link PluginVersion}s for the plugin with the given key.
     *
     * @param pluginKey the key for which to delete the version(s) (required)
     */
    void deleteByKey(String pluginKey);

    /**
     * Persists the given {@link PluginVersion}, performing an insert or update according to whether a record for this
     * plugin key already exists.
     *
     * @param pluginVersion the plugin version to save (required)
     * @return the id of the saved row
     */
    long save(PluginVersion pluginVersion);
}
