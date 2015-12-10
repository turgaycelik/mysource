package com.atlassian.jira.propertyset;

import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a cache for PropertySet values as used by JiraCachingPropertySet.
 *
 * <p> The intention is that the cache is non-blocking for reads, and it is up to the JiraCachingPropertySet to synchronise
 * writes with reading/writing from the underlying Property Set.
 *
 * <p> This cache looks and works a lot like a PropertySet for obvious reasons, however it is not declared to implement
 * PropertySet because it does not implement all methods - only the ones we want to cache for. With that in mind, it
 * generally attempts to follow the contract of the {@link PropertySet} interface. Namely:
 *
 * <ul>
 * <li>If a property is retrieved that exists but contains a value of different
 * type, a
 * {@link com.opensymphony.module.propertyset.InvalidPropertyTypeException}
 * should be thrown.</li>
 * <li>If a property is retrieved that does not exist, null (or the primitive
 * equivalent) is returned.</li>
 * <ul>
 *
 * @see JiraCachingPropertySet
 * @see PropertySet
 * @since v4.0
 */
public class PropertySetCache
{
    private static final Logger log = Logger.getLogger(PropertySetCache.class);

    private final ConcurrentHashMap<String, Object> valueCache;
    private final ConcurrentHashMap<String, Integer> typeCache;
    private final ConcurrentHashMap<String, Boolean> existanceCache;

    // ConcurrentHashMap does not support null values, so we use a replacement token in the map.
    private static final Object NULL_TOKEN = new Object();

    PropertySetCache()
    {
        valueCache = new ConcurrentHashMap<String, Object>();
        typeCache = new ConcurrentHashMap<String, Integer>();
        existanceCache = new ConcurrentHashMap<String, Boolean>();
    }

    public void setBoolean(final String key, final boolean value)
    {
        setObject(key, Boolean.valueOf(value));
    }

    public boolean getBoolean(final String key) throws NoValueCachedException
    {
        Object value = valueCache.get(key);
        if (value == NULL_TOKEN)
        {
            // This behaviour is consistent with AbstractPropertySet.getBoolean()
            return false;
        }
        if (value == null)
        {
            // We don't know about this key.
            throw new NoValueCachedException();
        }
        // We got an actual value from the valueCache - return it
        try
        {
            return ((Boolean) value).booleanValue();
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a boolean, but it is the wrong type.");
        }
    }

    public void setData(final String key, final byte[] value)
    {
        setObject(key, value);
    }

    public byte[] getData(final String key) throws NoValueCachedException
    {
        try
        {
            return (byte[]) getObject(key);
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a byte[], but it is the wrong type.");
        }
    }

    public void setDate(final String key, final Date value)
    {
        setObject(key, value);
    }

    public Date getDate(final String key) throws NoValueCachedException
    {
        try
        {
            return (Date) getObject(key);
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a Date, but it is the wrong type.");
        }
    }

    public void setDouble(final String key, final double value)
    {
        setObject(key, Double.valueOf(value));
    }

    public double getDouble(final String key) throws NoValueCachedException
    {
        try
        {
            Double value = (Double) getObject(key);
            if (value == null)
            {
                return 0;
            }
            else
            {
                return value.doubleValue();
            }
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a double, but it is the wrong type.");
        }
    }

    public void setInt(final String key, final int value)
    {
        setObject(key, Integer.valueOf(value));
    }

    public int getInt(final String key) throws NoValueCachedException
    {
        try
        {
            Integer value = (Integer) getObject(key);
            if (value == null)
            {
                return 0;
            }
            else
            {
                return value.intValue();
            }
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as an int, but it is the wrong type.");
        }
    }

    public void setLong(final String key, final long value)
    {
        setObject(key, Long.valueOf(value));
    }

    public long getLong(final String key) throws NoValueCachedException
    {
        try
        {
            Long value = (Long) getObject(key);
            if (value == null)
            {
                return 0;
            }
            else
            {
                return value.longValue();
            }
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a long, but it is the wrong type.");
        }
    }

    public void setObject(final String key, final Object value)
    {
        if (value == null)
        {
            valueCache.put(key, NULL_TOKEN);
            typeCache.remove(key);
            // This seems to usually mean that the value "exists".
            // Just in case, we will allow the underlying PropertySet define how this works.
            existanceCache.remove(key);
        }
        else
        {
            valueCache.put(key, value);
            typeCache.remove(key);
            existanceCache.put(key, Boolean.TRUE);
        }
    }

    public Object getObject(final String key) throws NoValueCachedException
    {
        Object value = valueCache.get(key);
        if (value == NULL_TOKEN)
        {
            return null;
        }
        if (value == null)
        {
            // We don't know about this key.
            throw new NoValueCachedException();
        }
        // We got an actual value from the valueCache - return it
        return value;
    }

    public void setProperties(final String key, final Properties value)
    {
        setObject(key, value);
    }

    public Properties getProperties(final String key) throws NoValueCachedException
    {
        try
        {
            return (Properties) getObject(key);
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a Properties object, but it is the wrong type.");
        }
    }

    public void setString(final String key, final String value)
    {
        setObject(key, value);
    }

    public String getString(final String key) throws NoValueCachedException, InvalidPropertyTypeException
    {
        try
        {
            return (String) getObject(key);
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a String, but it is the wrong type.");
        }
    }

    public void setText(final String key, final String value)
    {
        setObject(key, value);
    }

    public String getText(final String key) throws NoValueCachedException
    {
        return getString(key);
    }

    public void setXML(final String key, final Document value)
    {
        setObject(key, value);
    }

    public Document getXML(final String key) throws NoValueCachedException
    {
        try
        {
            return (Document) getObject(key);
        }
        catch (ClassCastException ex)
        {
            // A value exists, but it is the wrong type. Throw InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key + "' as a DOM Document, but it is the wrong type.");
        }
    }

    public void remove(final String key)
    {
        valueCache.put(key, NULL_TOKEN);
        typeCache.remove(key);
        existanceCache.put(key, Boolean.FALSE);
    }

    public void clear()
    {
        valueCache.clear();
        typeCache.clear();
        existanceCache.clear();
    }

    /**
     * Eagerly loads all the values from the given PropertySet into this cache.
     *
     * @param source The PropertySet to bulk load from.
     */
    public void bulkLoad(final PropertySet source)
    {
        for (final Object key : source.getKeys())
        {
            try
            {
                cloneProperty((String)key, source);
            }
            catch (PropertyException ex)
            {
                // JRADEV-22946. We are seeing this error on startup in OnDemand.
                // Presumably another thread is deleting while we are reading, so the whole idea that we can have a cache
                // for this property set would seem flawed. Meaning that this really _is_ an error.
                // However, propagating the RuntimeException just makes it worse (eg it causes plugins to fail to load).
                // It seems the best thing to do here is to log an error with stacktrace but let the bulk load complete.
                log.error("Unable to clone property '" + key + "' in PropertySet.", ex);
            }
        }
    }

    /**
     * Copy individual property from source to this cache.
     *
     * <p> This is copied from PropertySetCloner.
     *
     * @param key    The key to clone.
     * @param source The PropertySet we are cloning.
     *
     * @see com.opensymphony.module.propertyset.PropertySetCloner
     * @throws PropertyException if the property cannot be cloned (eg it no longer exists - see JRADEV-22946)
     */
    private void cloneProperty(String key, PropertySet source) throws PropertyException
    {
        switch (source.getType(key))
        {
            case PropertySet.BOOLEAN:
                this.setBoolean(key, source.getBoolean(key));
                break;

            case PropertySet.INT:
                this.setInt(key, source.getInt(key));
                break;

            case PropertySet.LONG:
                this.setLong(key, source.getLong(key));
                break;

            case PropertySet.DOUBLE:
                this.setDouble(key, source.getDouble(key));
                break;

            case PropertySet.STRING:
                this.setString(key, source.getString(key));
                break;

            case PropertySet.TEXT:
                this.setText(key, source.getText(key));
                break;

            case PropertySet.DATE:
                this.setDate(key, source.getDate(key));
                break;

            case PropertySet.OBJECT:
                this.setObject(key, source.getObject(key));
                break;

            case PropertySet.XML:
                this.setXML(key, source.getXML(key));
                break;

            case PropertySet.DATA:
                this.setData(key, source.getData(key));
                break;

            case PropertySet.PROPERTIES:
                this.setProperties(key, source.getProperties(key));
                break;
        }
    }

    /**
     * Returns the cached value for whether the given key exists in the underlying PropertySet.
     *
     * <p> It is important not to confuse this method with the {@link java.util.Map#containsKey} method.
     *
     * @param key The property key.
     * @return A Boolean object containing the cached existance of a value for the key, or null if we haven't cached the
     *         existance for this key.
     * @see com.atlassian.jira.propertyset.JiraCachingPropertySet#exists(String)
     */
    public Boolean exists(final String key)
    {
        return existanceCache.get(key);
    }

    public void cacheExistance(final String key, final boolean keyExists)
    {
        existanceCache.put(key, Boolean.valueOf(keyExists));
    }

    public void setType(String key, int type)
    {
        typeCache.put(key, type);
    }

    public int getType(String key) throws NoValueCachedException
    {
        Integer type = typeCache.get(key);
        if (type == null)
        {
            // We don't know about this key.
            throw new NoValueCachedException();
        }
        // We got an actual value from the valueCache - return it
        return type;
    }

    final class NoValueCachedException extends Exception
    {
        @Override
        public Throwable fillInStackTrace()
        {
            // This Exception is used for control flow only.
            // Don't do this expensive operation.
            return this;
        }
    }
}
