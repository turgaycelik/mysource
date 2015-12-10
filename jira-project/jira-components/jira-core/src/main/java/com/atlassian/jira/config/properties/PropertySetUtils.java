package com.atlassian.jira.config.properties;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;


import java.util.HashMap;
import java.util.Map;

/**
 * Static methods to get the default property set.  This currently holds:
 * <ul>
 * <li>Application properties</li>
 * <li>Plugin settings</li>
 * </ul>
 * It used to also hold plugin states, but those have been moved to their own table.  This
 * should only ever be called by {@link BackingPropertySetManager} implementations; any
 * other code should use the {@link com.atlassian.jira.propertyset.JiraPropertySetFactory},
 * instead.
 *
 * @since v4.4
 */
class PropertySetUtils
{
    private final static String SEQUENCE = "jira.properties";
    private final static long ID = 1;

    private PropertySetUtils() {}

    static PropertySet createDatabaseBackedPropertySet(OfBizConnectionFactory ofBizConnectionFactory)
    {
        return PropertySetManager.getInstance("ofbiz-cached", FieldMap.build(
                "delegator.name", ofBizConnectionFactory.getDelegatorName(),
                "entityName", SEQUENCE,
                "entityId", ID ));
    }
}
