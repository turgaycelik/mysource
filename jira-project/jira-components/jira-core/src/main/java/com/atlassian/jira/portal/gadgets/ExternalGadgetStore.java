package com.atlassian.jira.portal.gadgets;


import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;

import java.net.URI;
import java.util.Set;

/**
 * Provides storage mechanism for gadgets whitelisted by an admin in JIRA.
 *
 * @since v4.0
 */
public interface ExternalGadgetStore
{
    /**
     * Retrieves a set of all external gadgets whitelisted in the directory.
     *
     * @return a set of all external gadgets whitelisted in the directory.
     */
    Set<ExternalGadgetSpec> getAllGadgetSpecUris();

    /**
     * Adds the URI specified to the whitelist.
     *
     * @param uri The gadget URI to add
     * @throws IllegalStateException if the store already contains the uri specified
     * @return The newly created ExternalGAdgetSpec
     */
    ExternalGadgetSpec addGadgetSpecUri(URI uri);

    /**
     * Removes the URI specified from the whitelist.
     *
     * @param id The id to remove
     */
    void removeGadgetSpecUri(ExternalGadgetSpecId id);

    /**
     * Checks if the specified URI is already contained in the store
     * @param uri the URI to check for
     * @return true if the URI already exists in the store
     */
    boolean containsSpecUri(URI uri);
}
