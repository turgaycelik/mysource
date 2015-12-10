package com.atlassian.jira.dashboard;


import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecStore;

import java.net.URI;

/**
 * Stores gadget URIs added to the directory.
 *
 * @since v4.0
 */
public class JiraExternalGadgetSpecStore implements ExternalGadgetSpecStore
{
    private final com.atlassian.jira.portal.gadgets.ExternalGadgetStore externalGadgetStore;

    public JiraExternalGadgetSpecStore(com.atlassian.jira.portal.gadgets.ExternalGadgetStore externalGadgetStore)
    {
        this.externalGadgetStore = externalGadgetStore;
    }

    public Iterable<ExternalGadgetSpec> entries()
    {
        return externalGadgetStore.getAllGadgetSpecUris();
    }

    public ExternalGadgetSpec add(final URI uri)
    {
        return externalGadgetStore.addGadgetSpecUri(uri);
    }

    public void remove(final ExternalGadgetSpecId externalGadgetSpecId)
    {
        externalGadgetStore.removeGadgetSpecUri(externalGadgetSpecId);
    }

    public boolean contains(final URI uri)
    {
        return externalGadgetStore.containsSpecUri(uri);
    }
}
