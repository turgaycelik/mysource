package com.atlassian.jira.imports.project.core;

import java.util.Map;

/**
 * Represents a Entity and attributes that can be persisted via OfBiz
 *
 * @since v3.13
 */
public interface EntityRepresentation
{
    /**
     * Gets the OfBiz entity name for the "entity" that should be persisted (e.g. Issue for the Issue object).
     *
     * @return the entity name for this representation
     */
    String getEntityName();

    /**
     * Gets the attributes that will be persisted within the entity (i.e. column values).
     *
     * @return map containing column names as keys and string values
     */
    Map<String, String> getEntityValues();
}
