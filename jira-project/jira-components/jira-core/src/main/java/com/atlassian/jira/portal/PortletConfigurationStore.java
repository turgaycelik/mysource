package com.atlassian.jira.portal;

import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.util.collect.EnclosedIterable;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Store for the {@link com.atlassian.jira.portal.PortletConfiguration} domain object.
 */
public interface PortletConfigurationStore
{
    /**
     * Get all {@link com.atlassian.jira.portal.PortletConfiguration} objects for a given {@link
     * com.atlassian.jira.portal.PortalPage} id.
     *
     * @param portalPageId The id of the page to retreive all configurations for.
     * @return The configurations associated with the given page.
     */
    public List<PortletConfiguration> getByPortalPage(Long portalPageId);
    
    /**
     * Gall a {@link com.atlassian.jira.portal.PortletConfiguration} by its id.
     *
     * @param portletId The id of the portlet configuration
     * @return The configuration of the given id.
     */
    public PortletConfiguration getByPortletId(Long portletId);

    /**
     * Deletes the given {@link com.atlassian.jira.portal.PortletConfiguration}.
     *
     * @param pc The PortletConfiguration to delete.
     */
    public void delete(PortletConfiguration pc);

    /**
     * Saves the given {@link com.atlassian.jira.portal.PortletConfiguration}.
     *
     * @param pc The PortletConfiguration to save.
     */
    public void store(PortletConfiguration pc);

    /**
     * Given a gadget, this method will update it's row, column and parent dashboard id.
     *
     * @param gadgetId The id of the gadget being updated
     * @param row The new row value for this gadget
     * @param column The new column value for this gadget
     * @param dashboardId The new parent dashboard id value for this gadget
     */
    void updateGadgetPosition(Long gadgetId, int row, int column, Long dashboardId);

    /**
     * Given a gadget, this method will update the color value for this gadget.
     *
     * @param gadgetId The id of the gadget being updated
     * @param color The new color value for this gadget
     */
    void updateGadgetColor(Long gadgetId, Color color);

    /**
     * Given a gadget, this method updates all userprefs for this gadget.
     *
     * @param gadgetId The id of the gadget being updated
     * @param userPrefs The new userprefs to set for this gadget.
     */
    void updateUserPrefs(Long gadgetId, Map<String, String> userPrefs);

    /**
     * Creates and adds a new {@link com.atlassian.jira.portal.PortletConfiguration} to given PortalPage.  This should
     * be used to add a gadget.
     *
     * @param pageId The id of the page to add the configuration to.
     * @param portletConfigurationId The id to use for adding the gadget. This will correspond to {@link
     * com.atlassian.jira.dashboard.JiraGadgetStateFactory#createGadgetState(java.net.URI)}. May be null for a generated
     * id.
     * @param column The column position of the portlet.
     * @param row The row position of the portlet
     * @param gadgetXml A URI specifying the location of the gadget XML.  May be null if this is a legacy portlet.
     * @param color The chrome color for the gadget.
     * @param userPreferences A map of key -> value user preference pairs used to store gadget configuration.
     * @return The new PortletConfiguration with the id set.
     */
    PortletConfiguration addGadget(Long pageId, Long portletConfigurationId, Integer column, Integer row, URI gadgetXml, Color color, Map<String, String> userPreferences);

    /**
     * Returns an iterable over all PortletConfigurations available in the database.
     *
     * @return iterable over all PortletConfigurations available in the database
     */
    EnclosedIterable<PortletConfiguration> getAllPortletConfigurations();
}
