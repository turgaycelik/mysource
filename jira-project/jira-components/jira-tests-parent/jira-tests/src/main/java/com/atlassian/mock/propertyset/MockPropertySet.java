package com.atlassian.mock.propertyset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;

import org.w3c.dom.Document;

/**
 * Simple mock @{link PropertySet} for dashboard tests. * Not completely implemented *.
*
* @since v3.13
*/
public class MockPropertySet implements PropertySet
{
    private PropertySetSchema propertySetSchema;
    private Map<String, Object> map;

    public MockPropertySet()
    {
        this (new HashMap<String, Object>());
    }

    public MockPropertySet(Map<String, Object> map)
    {
        this.map = map;
    }

    public Map getMap()
    {
        return map;
    }

    public void setSchema(final PropertySetSchema propertySetSchema)
    {
        this.propertySetSchema = propertySetSchema;
    }

    public PropertySetSchema getSchema() throws PropertyException
    {
        return propertySetSchema;
    }

    public void setAsActualType(final String s, final Object o)
    {
        map.put(s, o);
    }

    public Object getAsActualType(final String s)
    {
        return map.get(s);
    }

    public void setBoolean(final String s, final boolean b)
    {
        map.put(s, b);
    }

    public boolean getBoolean(final String s) throws PropertyException
    {
        return getCast(s, Boolean.class);
    }

    public void setData(final String s, final byte[] bytes)
    {
        map.put(s, bytes);
    }

    public byte[] getData(final String s) throws PropertyException
    {
        final Object o = map.get(s);
        if (o instanceof byte[])
        {
            return (byte[]) o;
        }
        else
        {
            throw new PropertyException(String.format("'%s' is not a byte[].", o));
        }
    }

    public void setDate(final String s, final Date date) throws PropertyException
    {
        map.put(s, date);
    }

    public Date getDate(final String s) throws PropertyException
    {
        return getCast(s, Date.class);
    }

    public void setDouble(final String s, final double v) throws PropertyException
    {
        map.put(s, v);
    }

    public double getDouble(final String s) throws PropertyException
    {
        return getCast(s, Double.class);
    }

    public void setInt(final String s, final int i) throws PropertyException
    {
        map.put(s, i);
    }

    public int getInt(final String s) throws PropertyException
    {
        return getCast(s, Integer.class);
    }

    public Collection getKeys() throws PropertyException
    {
        //wrapping the result in a list here. If you iterate over them and remove() keys you'll get a
        //concurrentmodification excpeiton otherwise.
        return new ArrayList<String>(map.keySet());
    }

    public Collection getKeys(final int i) throws PropertyException
    {
        throw new UnsupportedOperationException();
    }

    public Collection getKeys(final String s) throws PropertyException
    {
        throw new UnsupportedOperationException();
    }

    public Collection getKeys(final String s, final int i) throws PropertyException
    {
        throw new UnsupportedOperationException();
    }

    public void setLong(final String s, final long l) throws PropertyException
    {
        map.put(s, l);
    }

    public long getLong(final String s) throws PropertyException
    {
        return getCast(s, Long.class);
    }

    public void setObject(final String s, final Object o) throws PropertyException
    {
        map.put(s, o);
    }

    public Object getObject(final String s) throws PropertyException
    {
        return map.get(s);
    }

    public void setProperties(final String s, final Properties properties) throws PropertyException
    {
        map.put(s, properties);
    }

    public Properties getProperties(final String s) throws PropertyException
    {
        return getCast(s, Properties.class);
    }

    public boolean isSettable(final String s)
    {
        return true;
    }

    public void setString(final String s, final String s1) throws PropertyException
    {
        map.put(s, s1);
    }

    public String getString(final String s) throws PropertyException
    {
        return getCast(s, String.class);
    }

    public void setText(final String s, final String s1) throws PropertyException
    {
        map.put(s, new TextValue(s1));
    }

    public String getText(final String s) throws PropertyException
    {
        Object value = map.get(s);
        return value != null ? String.valueOf(value) : null;
    }

    public int getType(final String key) throws PropertyException
    {
        Object value = map.get(key);
        if (value instanceof String)
        {
            return PropertySet.STRING;
        }
        else if (value instanceof TextValue)
        {
            return PropertySet.TEXT;
        }
        else if (value instanceof Boolean)
        {
            return PropertySet.BOOLEAN;
        }
        else if (value instanceof Long)
        {
            return PropertySet.LONG;
        }
        else if (value instanceof Integer)
        {
            return PropertySet.INT;
        }
        else if (value instanceof Date)
        {
            return PropertySet.DATE;
        }
        else if (value instanceof Document)
        {
            return PropertySet.XML;
        }
        else
        {
            return PropertySet.OBJECT;
        }
    }

    public void setXML(final String s, final Document document) throws PropertyException
    {
        map.put(s, document);
    }

    public Document getXML(final String s) throws PropertyException
    {
        return getCast(s, Document.class);
    }

    public boolean exists(final String s) throws PropertyException
    {
        return map.containsKey(s);
    }

    public void init(final Map map, final Map map1)
    {
    }

    public void remove(final String s) throws PropertyException
    {
        map.remove(s);
    }

    public void remove() throws PropertyException
    {
        map.clear();
    }

    public boolean supportsType(final int i)
    {
        return true;
    }

    public boolean supportsTypes()
    {
        return true;
    }

    private <T> T getCast(String key, final Class<T> type)
    {
        final Object o = map.get(key);
        if (o == null)
        {
            return null;
        }
        else if (type.isInstance(o))
        {
            return type.cast(o);
        }
        else
        {
            throw new PropertyException(String.format("'%s' is not of the correct type. Expecting %s but got %s.", o, type, o.getClass()));
        }
    }

    /**
     * A special object that can be used to indicate a PropertySet.TEXT value
     */
    private static class TextValue
    {
        private final String value;

        public TextValue(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        public String toString()
        {
            return value;
        }

        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final TextValue textValue = (TextValue) o;

            if (value != null ? !value.equals(textValue.value) : textValue.value != null)
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            return (value != null ? value.hashCode() : 0);
        }
    }

    public Map<String, Object> asMap()
    {
        Map<String, Object> result = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (value instanceof TextValue)
            {
                result.put(key, ((TextValue) value).getValue());
            }
            else
            {
                result.put(key, value);
            }
        }
        return result;
    }
}
