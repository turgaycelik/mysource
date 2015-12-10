package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;

/**
 * Manages {@link IssueLinkType}s.
 */
@PublicApi
public interface IssueLinkTypeManager
{
    // Issue Link Types
    void createIssueLinkType(String name, String outward, String inward, String style);

    void updateIssueLinkType(IssueLinkType issueLinkType, String name, String outward, String inward);

    /**
     * This method removed the issue link type from the database<br>
     * <b>WARNING</b>: This method DOES NOT check if there are any existing issue links of this issue link type.
     * It simply removes the record from the datastore. Use {@link IssueLinkTypeDestroyer} to 'nicely'
     * remove the issue link type taking care of existing issue links.
     *
     * @param issueLinkTypeId the id of the {@link IssueLinkType} to remove
     */
    void removeIssueLinkType(Long issueLinkTypeId);

    /**
     * Returns only user-defined (non-system) IssueLinkType objects
     * @return the collection
     */
    Collection<IssueLinkType> getIssueLinkTypes();

    /**
     * @see #getIssueLinkTypes()
     * @param excludeSystemLinks whether or not to exclude system links
     * @return the collection of IssueLinkType objects
     */
    Collection<IssueLinkType> getIssueLinkTypes(boolean excludeSystemLinks);

    /**
     * Returns a user-defined (non-system) issue link type object for the specified ID.
     * @param id the id
     * @return the issue link type object
     */
    IssueLinkType getIssueLinkType(Long id);

    /**
     * @see #getIssueLinkType(Long)
     * @param id the id of the issue link type
     * @param excludeSystemLinks whether or not to exclude system links
     * @return the issue link type object
     */
    IssueLinkType getIssueLinkType(Long id, boolean excludeSystemLinks);

    /**
     * Returns a collection of {@link IssueLinkType}s with the given name.
     * Actually this collection should always have either one or zero elements - it is illegal to have two link types
     * with the same name.
     *
     * @param name The name to search for Issue Link Types by.
     * @return a collection of {@link IssueLinkType}s, or an empty collection if <code>name</code> isn't a link type.
     * TODO: Reconsider this method. The GUI enforces unique names.
     */
    Collection<IssueLinkType> getIssueLinkTypesByName(String name);

    /**
     * Returns a collection of {@link IssueLinkType}s with the given description as the inward description.
     * <p>
     * There is no restriction on inward and outward descriptions being unique across issue link types, which is why this
     * may return more than one {@link IssueLinkType}.
     *
     * @param desc the inward description to search on
     * @return the collection of matched issue link types; never null.
     * @since v4.0
     */
    Collection<IssueLinkType> getIssueLinkTypesByInwardDescription(String desc);

    /**
     * Returns a collection of {@link IssueLinkType}s with the given description as the outward description.
     * <p>
     * There is no restriction on inward and outward descriptions being unique across issue link types, which is why this
     * may return more than one {@link IssueLinkType}.
     *
     * @param desc the outward description to search on
     * @return the collection of matched issue link types; never null.
     * @since v4.0
     */
    Collection<IssueLinkType> getIssueLinkTypesByOutwardDescription(String desc);

    Collection<IssueLinkType> getIssueLinkTypesByStyle(String style);
}
