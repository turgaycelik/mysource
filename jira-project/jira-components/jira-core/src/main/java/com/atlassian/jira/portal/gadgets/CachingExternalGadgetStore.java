package com.atlassian.jira.portal.gadgets;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.atlassian.event.api.EventListener;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.event.ClearCacheEvent;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Eagerly caching implementation on the external Gadget store.  The contains() method needs to be especially
 * performant, since every gadget will be checked when displaying a dashboard.
 *
 * @since v4.0
 */
@EventComponent
public class CachingExternalGadgetStore implements ExternalGadgetStore
{
    @ClusterSafe
    public final Map<ExternalGadgetSpecId, ExternalGadgetSpec> specCache = new ConcurrentHashMap<ExternalGadgetSpecId, ExternalGadgetSpec>();
    public final Set<URI> uriCache = new CopyOnWriteArraySet<URI>();
    private final ExternalGadgetStore delegateStore;

    public CachingExternalGadgetStore(ExternalGadgetStore delegateStore)
    {
        this.delegateStore = notNull("delegateStore", delegateStore);
        init(delegateStore);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        specCache.clear();
        uriCache.clear();
        init(delegateStore);
    }

    public Set<ExternalGadgetSpec> getAllGadgetSpecUris()
    {
        return Collections.unmodifiableSet(new HashSet<ExternalGadgetSpec>(specCache.values()));
    }

    public ExternalGadgetSpec addGadgetSpecUri(final URI uri)
    {
        final ExternalGadgetSpec addedSpec = delegateStore.addGadgetSpecUri(uri);
        specCache.put(addedSpec.getId(), addedSpec);
        uriCache.add(addedSpec.getSpecUri());
        return addedSpec;
    }

    public void removeGadgetSpecUri(final ExternalGadgetSpecId id)
    {
        delegateStore.removeGadgetSpecUri(id);
        final ExternalGadgetSpec removedSpec = specCache.remove(id);
        uriCache.remove(removedSpec.getSpecUri());
    }

    public boolean containsSpecUri(final URI uri)
    {
        return uriCache.contains(uri);
    }

    private void init(ExternalGadgetStore delegateStore)
    {
        final Set<ExternalGadgetSpec> specs = delegateStore.getAllGadgetSpecUris();
        for (ExternalGadgetSpec spec : specs)
        {
            specCache.put(spec.getId(), spec);
            uriCache.add(spec.getSpecUri());
        }
    }
}
