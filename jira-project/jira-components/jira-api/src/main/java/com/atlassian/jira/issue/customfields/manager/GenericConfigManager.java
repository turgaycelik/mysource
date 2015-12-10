package com.atlassian.jira.issue.customfields.manager;

import javax.annotation.Nullable;

/**
 * Used to store Generic configuration values (mostly default values for certain fields).  Implementations should aim
 * to manage any plain java object.  Two implementations of this interface exist in JIRA: An in memory cache
 * (CachedGenericConfigManager) and an implementation responsible for storing the config values in the DB
 * (DefaultGenericConfigManager).  For DB persistence, objects are serialized to XML.
 */
public interface GenericConfigManager
{
    String ENTITY_TABLE_NAME = "GenericConfiguration";
    String ENTITY_ID = "id";
    String ENTITY_DATA_TYPE = "datatype";
    String ENTITY_DATA_KEY = "datakey";
    String ENTITY_XML_VALUE = "xmlvalue";

    /**
     * Stores a new entry for the config manager. For example a customfield may have a default value.
     *
     * @param dataType The datatype for which to store the generic config (e.g.: DefaultValue)
     * @param key      A key that identifies the element (e.g.: 10000)
     * @param obj      The actual object to store.
     */
    void create(String dataType, String key, @Nullable Object obj);

    /**
     * Updates an entry.
     *
     * @param dataType The datatype for which to store the generic config (e.g.: DefaultValue)
     * @param key      A key that identifies the element (e.g.: 10000)
     * @param obj      The actual object to store.
     */
    void update(String dataType, String key, @Nullable Object obj);

    /**
     * Retrieves a particular entry.
     *
     * @param dataType The datatype for which to store the generic config (e.g.: DefaultValue)
     * @param key      A key that identifies the element (e.g.: 10000)
     *
     * @return The entry, or null if it doesn't exist.
     */
    Object retrieve(String dataType, String key);

    /**
     * Removes a generic config value.
     *
     * @param dataType The datatype for which to store the generic config (e.g.: DefaultValue)
     * @param key      A key that identifies the element (e.g.: 10000)
     */
    void remove(String dataType, String key);
}
