package com.atlassian.jira.config.properties;

import com.atlassian.util.concurrent.Supplier;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;
import org.w3c.dom.Document;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A PropertySet implementation that delegates first to memory PropertySet and then can be told to switch
 * over to delegating to another (database backed) property set. It actually doesn't really care what the
 * two delegate implementations are but its purpose is to permit a pre-database configuration PropertySet
 * to work and then to be able to migrate the contents to a persistent one once the database is available.
 *
 * @since v4.4
 */
class MemorySwitchToDatabasePropertySet implements PropertySet
{
    AtomicReference<PropertySet> active;
    private PropertySet memoryPropertySet;
    private Supplier<PropertySet> databasePropertySet;

    /**
     * PropertySet starts off delegating to memoryPropertySet.
     *
     * @param memoryPropertySet the initial delegate.
     * @param databasePropertySet the subsequent delegate that can be migrated to.
     */
    public MemorySwitchToDatabasePropertySet(PropertySet memoryPropertySet, final PropertySet databasePropertySet)
    {
        this(memoryPropertySet,  new Supplier<PropertySet>()
        {
            @Override
            public PropertySet get()
            {
                return databasePropertySet;
            }

        });
    }

    public MemorySwitchToDatabasePropertySet(PropertySet memoryPropertySet, Supplier<PropertySet> propertySetBRef)
    {
        this.memoryPropertySet = memoryPropertySet;
        this.databasePropertySet = propertySetBRef;
        active = new com.atlassian.util.concurrent.atomic.AtomicReference<PropertySet>(memoryPropertySet);
    }

    public void switchToDatabaseMode()
    {
        active.compareAndSet(memoryPropertySet, databasePropertySet.get());
    }

    @Override
    public void setSchema(PropertySetSchema schema) throws PropertyException
    {
        active.get().setSchema(schema);
    }

    @Override
    public PropertySetSchema getSchema() throws PropertyException
    {
        return active.get().getSchema();
    }

    @Override
    public void setAsActualType(String key, Object value) throws PropertyException
    {
        active.get().setAsActualType(key, value);
    }

    @Override
    public Object getAsActualType(String key) throws PropertyException
    {
        return active.get().getAsActualType(key);
    }

    @Override
    public void setBoolean(String key, boolean value) throws PropertyException
    {
        active.get().setBoolean(key, value);
    }

    @Override
    public boolean getBoolean(String key) throws PropertyException
    {
        return active.get().getBoolean(key);
    }

    @Override
    public void setData(String key, byte[] value) throws PropertyException
    {
        active.get().setData(key, value);
    }

    @Override
    public byte[] getData(String key) throws PropertyException
    {
        return active.get().getData(key);
    }

    @Override
    public void setDate(String key, Date value) throws PropertyException
    {
        active.get().setDate(key, value);
    }

    @Override
    public Date getDate(String key) throws PropertyException
    {
        return active.get().getDate(key);
    }

    @Override
    public void setDouble(String key, double value) throws PropertyException
    {
        active.get().setDouble(key, value);
    }

    @Override
    public double getDouble(String key) throws PropertyException
    {
        return active.get().getDouble(key);
    }

    @Override
    public void setInt(String key, int value) throws PropertyException
    {
        active.get().setInt(key, value);
    }

    @Override
    public int getInt(String key) throws PropertyException
    {
        return active.get().getInt(key);
    }

    @Override
    public Collection getKeys() throws PropertyException
    {
        return active.get().getKeys();
    }

    @Override
    public Collection getKeys(int type) throws PropertyException
    {
        return active.get().getKeys(type);
    }

    @Override
    public Collection getKeys(String prefix) throws PropertyException
    {
        return active.get().getKeys(prefix);
    }

    @Override
    public Collection getKeys(String prefix, int type) throws PropertyException
    {
        return active.get().getKeys(prefix, type);
    }

    @Override
    public void setLong(String key, long value) throws PropertyException
    {
        active.get().setLong(key, value);
    }

    @Override
    public long getLong(String key) throws PropertyException
    {
        return active.get().getLong(key);
    }

    @Override
    public void setObject(String key, Object value) throws PropertyException
    {
        active.get().setObject(key, value);
    }

    @Override
    public Object getObject(String key) throws PropertyException
    {
        return active.get().getObject(key);
    }

    @Override
    public void setProperties(String key, Properties value) throws PropertyException
    {
        active.get().setProperties(key, value);
    }

    @Override
    public Properties getProperties(String key) throws PropertyException
    {
        return active.get().getProperties(key);
    }

    @Override
    public boolean isSettable(String property)
    {
        return active.get().isSettable(property);
    }

    @Override
    public void setString(String key, String value) throws PropertyException
    {
        active.get().setString(key, value);
    }

    @Override
    public String getString(String key) throws PropertyException
    {
        return active.get().getString(key);
    }

    @Override
    public void setText(String key, String value) throws PropertyException
    {
        active.get().setText(key, value);
    }

    @Override
    public String getText(String key) throws PropertyException
    {
        return active.get().getText(key);
    }

    @Override
    public int getType(String key) throws PropertyException
    {
        return active.get().getType(key);
    }

    @Override
    public void setXML(String key, Document value) throws PropertyException
    {
        active.get().setXML(key, value);
    }

    @Override
    public Document getXML(String key) throws PropertyException
    {
        return active.get().getXML(key);
    }

    @Override
    public boolean exists(String key) throws PropertyException
    {
        return active.get().exists(key);
    }

    @Override
    public void init(Map config, Map args)
    {
        active.get().init(config, args);
    }

    @Override
    public void remove(String key) throws PropertyException
    {
        active.get().remove(key);
    }

    @Override
    public void remove() throws PropertyException
    {
        active.get().remove();
    }

    @Override
    public boolean supportsType(int type)
    {
        return active.get().supportsType(type);
    }

    @Override
    public boolean supportsTypes()
    {
        return active.get().supportsTypes();
    }

    PropertySet getMemoryPropertySet()
    {
        return memoryPropertySet;
    }

    PropertySet getDatabasePropertySet()
    {
        return databasePropertySet.get();
    }
}
