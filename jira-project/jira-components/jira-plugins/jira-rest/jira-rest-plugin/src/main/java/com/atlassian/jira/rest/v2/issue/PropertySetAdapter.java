package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
* @since v4.2
*/
public class PropertySetAdapter extends XmlAdapter<Map<String, Object>, PropertySet>
{

    @Override
    public PropertySet unmarshal(final Map<String, Object> map)
    {
        final MapPropertySet propertySet = new MapPropertySet();
        propertySet.setMap(map);
        return propertySet;
    }

    @Override
    public HashMap<String, Object> marshal(final PropertySet propertySet)
    {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        final Collection<String> keys = propertySet.getKeys();
        for (String key : keys)
        {
            // Only handle types that can sensibly be sent as JSON
            switch(propertySet.getType(key))
            {
                case PropertySet.BOOLEAN :
                case PropertySet.DOUBLE:
                case PropertySet.INT :
                case PropertySet.LONG :
                case PropertySet.STRING :
                case PropertySet.TEXT :
                    map.put(key, propertySet.getAsActualType(key));
                    break;
                case PropertySet.DATE:
                    try
                    {
                        map.put(key, new DateTimeAdapter().marshal(propertySet.getDate(key)));
                    }
                    catch (Exception e)
                    {
                        // do nothing.  Should never happen
                    }
                    break;
                default :
            }
        }
        return map;
    }
}
