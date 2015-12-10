package com.atlassian.jira.portal;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.util.dbc.Assertions;

public class PortletConfigurationManagerImpl implements PortletConfigurationManager
{
    private final PortletConfigurationStore portletConfigurationStore;

    public PortletConfigurationManagerImpl(final PortletConfigurationStore portletConfigurationStore)
    {
        this.portletConfigurationStore = Assertions.notNull("portletConfigurationStore", portletConfigurationStore);
    }

    public List<PortletConfiguration> getByPortalPage(final Long portalPageId)
    {
        return portletConfigurationStore.getByPortalPage(portalPageId);
    }

    public PortletConfiguration getByPortletId(final Long portletId)
    {
        return portletConfigurationStore.getByPortletId(portletId);
    }

    public void delete(final PortletConfiguration pc)
    {
        portletConfigurationStore.delete(pc);
    }

    public PortletConfiguration addGadget(final Long portalPageId, final Integer column, final Integer row, final URI gadgetXml, final Color color, final Map<String, String> userPreferences)
    {
        return portletConfigurationStore.addGadget(portalPageId, null, column, row, gadgetXml, color, userPreferences);
    }

    public void store(final PortletConfiguration pc)
    {
        portletConfigurationStore.store(pc);
    }
}
