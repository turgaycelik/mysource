package com.atlassian.jira.upgrade.util;

import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation that provides a default way of converting properties to the new userpreferences format.
 *
 * @since v4.0
 */
public abstract class AbstractLegacyPortletUpgradeTask implements LegacyPortletUpgradeTask
{
    /**
     * Separator used by new gadgets
     */
    protected static final String MULTIVALUE_SEPARATOR = "|";
    /**
     * If a multivalue value contains a '|' then Shindig will %encode it to this
     */
    protected static final String SEPARATOR_ENCODED = "%7C";
    public static final String PORTLET_MULTI_VALUE_SEPARATOR = "_*|*_";

    public abstract String getPortletKey();

    public abstract URI getGadgetUri();

    public Map<String, String> convertUserPrefs(final PropertySet propertySet)
    {
        @SuppressWarnings ("unchecked")
        final Collection<String> keys = propertySet.getKeys();
        final Map<String, String> ret = new LinkedHashMap<String, String>();
        for (String key : keys)
        {
            final int type = propertySet.getType(key);
            String value;
            if (type == PropertySet.STRING)
            {
                value = propertySet.getString(key);
            }
            else
            {
                //if the value wasn't a string (very unlikely with portletconfigs) then call the toString() method
                //on the object returned.
                final Object o = propertySet.getAsActualType(key);
                value = o == null ? null : o.toString();
            }

            if (value != null && value.contains(PORTLET_MULTI_VALUE_SEPARATOR))
            {
                value = convertMultiSelectValue(value);
            }
            ret.put(key, value);
        }
        return ret;
    }

    protected String convertMultiSelectValue(final String values)
    {
        final List<String> valuesList = getListFromMultiSelectValue(values);
        final StringBuilder ret = new StringBuilder();
        for (Iterator<String> valueIterator = valuesList.iterator(); valueIterator.hasNext();)
        {
            String value = valueIterator.next();
            ret.append(value.replace(MULTIVALUE_SEPARATOR, SEPARATOR_ENCODED));
            //if there's any more values then append a separator.
            if (valueIterator.hasNext())
            {
                ret.append(MULTIVALUE_SEPARATOR);
            }
        }

        return ret.toString();
    }

    /**
     * Retrieve a list of values from delimited String.  Used for MultiSelect Object Configurables
     *
     * @param values Delimited String
     * @return List of Strings, never null
     */
    private List<String> getListFromMultiSelectValue(final String values)
    {
        final String[] vals = StringUtils.splitByWholeSeparator(values, PORTLET_MULTI_VALUE_SEPARATOR);
        return vals == null ? Collections.<String>emptyList() : Arrays.asList(vals);
    }
}
