package com.atlassian.jira.upgrade.util;

import com.opensymphony.module.propertyset.PropertySet;

import java.net.URI;
import java.util.Map;

/**
 * Simple legacy portlet upgrade task where no extra work needs to be done to map from existing user preferences
 * to new user preferences.
 */
public class SimpleLegacyPortletUpgradeTask extends AbstractLegacyPortletUpgradeTask
{
    private final String portletKey;
    private final URI gadgetUri;

    public SimpleLegacyPortletUpgradeTask(String portletKey, URI gadgetUri)
    {
        this.portletKey = portletKey;
        this.gadgetUri = gadgetUri;
    }

    public SimpleLegacyPortletUpgradeTask(String portletKey, String gadgetUri)
    {
        this(portletKey, URI.create(gadgetUri));
    }

    public String getPortletKey()
    {
        return portletKey;
    }

    public URI getGadgetUri()
    {
        return gadgetUri;
    }

    public Map<String, String> convertUserPrefs(final PropertySet propertySet)
    {
        Map<String, String> prefMap = super.convertUserPrefs(propertySet);
        prefMap.put("isConfigured", "true");
        return prefMap;
    }
}
