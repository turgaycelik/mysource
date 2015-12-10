package com.atlassian.jira.index.property;

import java.sql.Timestamp;

/**
 * Represents information about indexing of of entity properties.
 * @since v6.2
 */
public interface EntityPropertyIndexDocument
{
    public static final String ID = "id";
    public static final String PLUGIN_KEY = "pluginKey";
    public static final String MODULE_KEY = "moduleKey";
    public static final String ENTITY_KEY = "entityKey";
    public static final String DOCUMENT = "document";
    public static final String UPDATED = "updated";

    Long getId();
    String getPluginKey();
    String getModuleKey();
    String getEntityKey();
    String getDocument();
    Timestamp getUpdated();
}
