package com.atlassian.jira.webwork;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder to help build String[] parameter maps
*
* @since v3.13.2
*/
public class ParameterMapBuilder
{
    private MultiMap map = new MultiValueMap();

    public ParameterMapBuilder()
    {
    }

    public ParameterMapBuilder(String paramName, String paramValue)
    {
        add(paramName, paramValue);
    }

    public ParameterMapBuilder add(String paramName, String paramValue)
    {
        map.put(paramName, paramValue);
        return this;
    }

    public Map toMap()
    {
        Map paramMap = new HashMap();
        for (Object key : map.keySet())
        {
            List list = (List) map.get(key);
            String[] vals = new String[list.size()];
            for (int i = 0; i < vals.length; i++)
            {
                vals[i] = String.valueOf(list.get(i));

            }
            paramMap.put(key, vals);
        }
        return paramMap;
    }
}
