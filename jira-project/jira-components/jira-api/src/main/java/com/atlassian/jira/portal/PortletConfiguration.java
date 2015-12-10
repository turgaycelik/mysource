package com.atlassian.jira.portal;

import com.atlassian.gadgets.dashboard.Color;

import java.net.URI;
import java.util.Map;

/**
 * A representation of a configuration for a Google Gadget.
 * <p/>
 * Only the main implementation (PortletConfigurationImpl), properly uses
 * the full interface.
 * <p/>
 */
public interface PortletConfiguration extends Comparable<PortletConfiguration>
{
    /**
     * Return the id of the PortletConfiguration.
     *
     * @return the id of the PortletConfiguration.
     */
    public Long getId();

    /**
     * Represents the column that the configured portlet resides in.
     *
     * @return the column number starting from 1.
     */
    public Integer getColumn();

    /**
     * Sets the column for the Portlet, effectively moving the portlet left or right on the page.
     *
     * @param column the column number starting from 1.
     */
    public void setColumn(Integer column);

    /**
     * Represents the row that the configured portlet resides in.
     *
     * @return the row number starting from 1.
     */
    public Integer getRow();

    /**
     * Sets the row for the Portlet, effectively moving the portlet up or down on the page.
     *
     * @param row the row number starting from 1.
     */
    public void setRow(Integer row);

    /**
     * Provides the dashboard page id.
     *
     * @return the dashboard page id.
     */
    public Long getDashboardPageId();

    /**
     * Sets the dashboard page id.
     *
     * @param portalPageId the dashboard page id.
     */
    public void setDashboardPageId(Long portalPageId);

    /**
     * Returns the URI pointing to the Gadget XML for this particular portlet.  May return null for
     * legacy portlets (that don't implement the Gadget spec).
     *
     * @see http://code.google.com/apis/gadgets/docs/reference.html
     * @return URI pointing to the Gadget XML or null
     */
    URI getGadgetURI();

    /**
     * Returns the color to use when rendering the Chrome of this gadget.
     *
     * @return color to use when rendering the Chrome of this gadget
     */
    Color getColor();

    /**
     * Set the color of the chrome for a gadget.
     *
     * @param color the color of the chrome for a gadget.
     */
    void setColor(Color color);

    /**
     * An unmodifiable map of user preferences stored for this gadget.  Will return an empty map in the case
     * of a legacy gadget.
     *
     * @see http://code.google.com/apis/gadgets/docs/reference.html#Userprefs_Ref
     * @return map of user preferences stored for this gadget.
     */
    Map<String, String> getUserPrefs();

    /**
     * Sets the userPreferences for this portletconfig.
     * @param userPrefs A map of key value pairs
     */
    void setUserPrefs(Map<String, String> userPrefs);
}
