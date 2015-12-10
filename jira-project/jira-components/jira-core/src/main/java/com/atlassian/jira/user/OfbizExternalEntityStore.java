package com.atlassian.jira.user;

/**
 * This used to exist to create entities in the ExternalEntity table which was used to generate system id's for
 * usernames that exist in external systems.
 *
 * Nowadays we use "ApplicationUser" as the key for the Property IDs.
 * This class is not actually used for anything other than OfbizExternalEntityStore.class.getName() and the public
 * constant used in Upgrade Tasks and may be removed once those upgrade tasks are removed.
 */
public class OfbizExternalEntityStore
{
    /**
     * Name of the entity referenced in the entitymodel.xml
     */
    public static final String ENTITY_NAME_EXTERNAL_ENTITY = "ExternalEntity";

}
