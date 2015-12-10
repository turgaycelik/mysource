package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.crowd.model.application.RemoteAddress;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.crowd.embedded.ofbiz.PrimitiveMap.builder;
import static java.util.Collections.emptySet;

class RemoteAddressEntity
{
    static final String ENTITY = "RemoteAddress";
    static final String APPLICATION_ID = "applicationId";
    static final String ADDRESS = "address";

    private RemoteAddressEntity()
    {
    }

    static Map<String, Object> getData(final Long applicationId, final String remoteAddress)
    {
        return builder().put(APPLICATION_ID, applicationId).put(ADDRESS, remoteAddress).build();
    }

    static Set<RemoteAddress> toRemoteAddresses(final List<GenericValue> remoteAddresses)
    {
        if (remoteAddresses == null)
        {
            return emptySet();
        }
        final ImmutableSet.Builder<RemoteAddress> addresses = ImmutableSet.builder();
        for (final GenericValue remoteAddressGv : remoteAddresses)
        {
            addresses.add(new RemoteAddress(remoteAddressGv.getString(ADDRESS)));
        }
        return addresses.build();
    }

}
