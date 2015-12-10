package com.atlassian.jira.jelly;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaClass;

import java.util.Iterator;
import java.util.Map;

public class ActionTagSupportDynaBean extends BasicDynaBean
{

    /**
     * Construct a new <code>ActionTagSupportDynaBean</code> associated with the specified
     * <code>DynaClass</code> instance.
     *
     * @param dynaClass The DynaClass we are associated with
     */
    public ActionTagSupportDynaBean(DynaClass dynaClass)
    {
        super(dynaClass);
    }

    public Map getProperties()
    {
        return values;
    }

    /**
     * Intelligent toString
     * @return returns all the properties contained within
     */
    public String toString()
    {
        Iterator iterator = values.entrySet().iterator();
        StringBuilder buff = new StringBuilder();
        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            buff.append("(").append(entry.getKey().toString()).append("=");

            Object value = entry.getValue();
            if (value == null)
            {
                buff.append((String) null).append(")");
            }
            else
            {
                buff.append(value.toString());
            }
            buff.append(")");
        }
        return buff.toString();
    }
}
