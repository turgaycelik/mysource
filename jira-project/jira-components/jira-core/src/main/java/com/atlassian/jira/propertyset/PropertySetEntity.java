package com.atlassian.jira.propertyset;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Entity definitions related to property set entries.
 *
 * @since v6.2
 */
interface PropertySetEntity
{
    // Entity name for the entry storage
    final String PROPERTY_ENTRY = "OSPropertyEntry";

    // Entity names for the value storage entities, which reuse the ID from the entry
    final String PROPERTY_STRING = "OSPropertyString";
    final String PROPERTY_TEXT = "OSPropertyText";
    final String PROPERTY_DATE = "OSPropertyDate";
    final String PROPERTY_DATA = "OSPropertyData";
    final String PROPERTY_NUMBER = "OSPropertyNumber";
    final String PROPERTY_DECIMAL = "OSPropertyDecimal";

    // The field names for OSPropertyEntry
    final String ID = "id";
    final String ENTITY_NAME = "entityName";
    final String ENTITY_ID = "entityId";
    final String PROPERTY_KEY = "propertyKey";
    final String TYPE = "type";

    // The field name used for the value in all of the value storage entities
    final String VALUE = "value";

    // Field selectors to limit the data that is returned for each row to whatever we actually need
    final Set<String> SELECT_ID_KEY_AND_TYPE = ImmutableSet.of(ID, PROPERTY_KEY, TYPE);
    final Set<String> SELECT_ID_AND_TYPE = ImmutableSet.of(ID, TYPE);
    final Set<String> SELECT_KEY = ImmutableSet.of(PROPERTY_KEY);
}
