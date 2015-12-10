package com.atlassian.jira.upgrade.util;

import com.opensymphony.module.propertyset.PropertySet;

import java.net.URI;
import java.util.Map;

/**
 * Responsible for providing the information required to converting a legacyPorltet over to a new Gadget.
 *
 * @since v4.0
 */
public interface LegacyPortletUpgradeTask
{
    /**
     * Returns the portletKey that this upgrade task can convert. Note that this is the full key that can be contained
     * via {@link com.atlassian.jira.portal.Portlet#getId()}. For example: 'com.atlassian.jira.plugin.system.portlets:inprogress'
     *
     * @return the portletKey that this upgrade task can convert.
     */
    String getPortletKey();

    /**
     * Returns the gadget URI to be used for this portlet. Should be a relative URI pointing to the gadget replacing
     * this portlet.  For example: 'rest/gadgets/1.0/g/com.atlassian.jira.gadgets/gadgets/filter-results-gadget.xml'
     *
     * @return the gadget URI to be used for this portlet.
     */
    URI getGadgetUri();

    /**
     * Converts the propertySet in use by this portletConfiguration to a Map<String,String>.  Please note that multi
     * value values (values separated by _*|*_ in the propertyset) should be converted to values simply separated by
     * '|'.  If '|' occurs in a value it needs to be % encoded to '%7C'.
     * <p/>
     * Implementations may choose to convert properties to userprefs depending on the new gadgets requirements. This
     * doesn't have to represent a 1 to 1 mapping.
     *
     * @param propertySet the old portletConfiguration propertySet to be converted
     * @return the propertySet converted to a map of key -> value pairs.
     */
    Map<String, String> convertUserPrefs(PropertySet propertySet);
}
