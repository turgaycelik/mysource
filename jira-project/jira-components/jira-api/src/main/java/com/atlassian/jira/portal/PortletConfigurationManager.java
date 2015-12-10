package com.atlassian.jira.portal;

import com.atlassian.gadgets.dashboard.Color;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Manager for the {@link com.atlassian.jira.portal.PortletConfiguration} domain object.
 * <p>
 * It has a legacy name, but actually manages the Google Gadgets.
 */
public interface PortletConfigurationManager
{
    /**
     * Get all the {@link com.atlassian.jira.portal.PortletConfiguration} associated with the passed Portal Page.
     *
     * @param portalPageId the portal page to query.
     * @return a list of portlet configurations on the passed page.
     */
    List<PortletConfiguration> getByPortalPage(Long portalPageId);

    /**
     * Get the passed portlet configuration.
     *
     * @param portletId the id of the portlet configuration to return.
     * @return the porlet configuration identified by the passed id.
     */
    PortletConfiguration getByPortletId(Long portletId);

    /**
     * Remove the passed portlet configuration.
     *
     * @param pc the portlet configuration to remove.
     */
    void delete(PortletConfiguration pc);

    /**
     * Update the passed portlet configuration.
     *
     * @param pc the portlet configuration to change.
     */
    void store(PortletConfiguration pc);

    /**
     * Create a new portlet configuration for the passed parameters.
     *
     * @param portalPageId the portal page the configuration will belong to.
     * @param column       the column location for the new configuration.
     * @param row          the row location for the new configuration.
     * @param gadgetXml  A URI specifying the location of the gadget XML.  May be null if this is a legacy portlet.
     * @param color      The chrome color for the gadget.
     * @param userPreferences A map of key -> value user preference pairs used to store gadget configuration.
     * @return the new portlet configuration
     */
    PortletConfiguration addGadget(Long portalPageId, Integer column, Integer row, URI gadgetXml, Color color, Map<String, String> userPreferences);
}