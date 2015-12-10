package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;

/**
 * Base interface for a Project Import Id Mapper.
 * Defines a value mapper used for project import. This allows us to map an old value with a new value in the target system.
 * Each instance of this interface represents a particular type of object in the system; eg IssueType, Issue, etc.
 *
 * @since v3.13
 */
@PublicApi
public interface ProjectImportIdMapper
{
    /**
     * Returns a collection of <code>IdKeyPair<code> representing objects from the import file.
     *
     * @return a collection of <code>IdKeyPair<code> representing objects from the import file.
     * @see IdKeyPair
     * @deprecated Use getRegisteredOldIds()
     */
    @Deprecated
    Collection<IdKeyPair> getValuesFromImport();

    /**
     * Returns a collection of ID's as String objects identifying objects from the import file that are required for the import.
     * @return a collection of ID's as String objects identifying objects from the import file that are required for the import.
     */
    Collection<String> getRequiredOldIds();

    /**
     * Returns a collection of ID's as String objects identifying all objects of the appropriate type found in the import file.
     * Note that that this should normally be a super set of the "required" ID's.
     * Each of these ID's will have a unique key associated with it that can be used for mapping old ID's in the import
     * file to "new" ID's in the current JIRA system.
     * You can use the {@link #getKey(String)} method to extract this key.
     *
     * @return a collection of ID's as String objects identifying all objects of the appropriate type found in the import file.
     * @see #getRequiredOldIds()
     * @see #getKey(String)
     */
    Collection<String> getRegisteredOldIds();

    /**
     * Returns the registered "key" for the given old ID, or <code>null</code> if none is registered.
     * This is the unique "name" that will be used for automatic mapping of objects in the import file to the objects in
     * the current JIRA system.
     *
     * @param oldId The old ID.
     * @return the registered "key" for the given old ID, or <code>null</code> if none is registered.
     * @see #getDisplayName(String)
     * @see #getRegisteredOldIds()
     */
    String getKey(final String oldId);

    /**
     * Returns a display name for the given old ID.
     * This will normally be the registered key for that ID, but if no key was registered we return the ID in square
     * brackets (eg "[1234]").
     *
     * @param oldId The old ID.
     * @return a display name for the given old ID.
     * @see #getKey(String)
     */
    String getDisplayName(final String oldId);

    /**
     * This method maps a value from the backup system to a valid value in the target system.
     *
     * @param oldId the string representation of the id of the backup value.
     * @param newId the string representation of the id of the valid mapped value in the target system.
     */
    void mapValue(String oldId, String newId);

    /**
     * Retrieves a String that corresponds to the id in the target JIRA system, null if none has been mapped.
     *
     * @param oldId identifies the mapping we are looking for.
     *
     * @return String that corresponds to the id in the target JIRA system, null if none has been mapped.
     */
    String getMappedId(String oldId);

    /**
     * Returns a Collection of all the new IDs that are mapped to.
     * 
     * @return a Collection of all the new IDs that are mapped to.
     */
    Collection<String> getAllMappedIds();

    /**
     * This will clear any mapped data that may have been entered into the mappers. All registered values and
     * values that have been flagged as required will not be changed. This method only affects the mapped data.
     * It is used to re-map and re-validate the data after the user has made changes to the current configuration.
     */
    void clearMappedValues();
}
