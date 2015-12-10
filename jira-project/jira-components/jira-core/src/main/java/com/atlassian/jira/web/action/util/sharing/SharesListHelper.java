package com.atlassian.jira.web.action.util.sharing;

import java.util.Collection;

/**
 * An interface to ensure that object that use the 'share-list.jsp' to render share permissions implement
 * the necessary methods.
 *
 * @since v3.13
 */
public interface SharesListHelper
{
    /**
     * Return the ID of the entity that owns the shares. Needs to be unique on a page.
     *
     * @return  the ID of the entity that owns the shared. Needs to be unique on a page.
     */
    Long getId();

    /**
     * Tells the caller whether or not the entity is has any shares or not.
     *
     * @return true if the entity is shared privately or false otherwise.
     */
    boolean isPrivate();

    /**
     * Returns the share permissions associated with the entity.
     *
     * @return a collection of shares associated with the entity.
     */
    Collection /*<SharePermission>*/getSharePermissions();
}
