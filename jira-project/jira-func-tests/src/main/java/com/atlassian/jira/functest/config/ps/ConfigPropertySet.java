package com.atlassian.jira.functest.config.ps;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A representation of a property set.
 *
 * @since v4.0
 */
final public class ConfigPropertySet
{
    private final Map<String, ConfigPropertySetEntry> entries;
    private final String entityName;
    private final Long entityId;

    public ConfigPropertySet()
    {
        this(null, null);
    }

    public ConfigPropertySet(final String entityName, final Long entityId)
    {
        this(entityName, entityId, Collections.<String, ConfigPropertySetEntry>emptyMap());
    }

    public ConfigPropertySet(ConfigPropertySet cps)
    {
        this(cps.entityName, cps.entityId, cps.entries);
    }

    private ConfigPropertySet(final String entityName, final Long entityId, Map<String, ConfigPropertySetEntry> entries)
    {
        this.entityName = entityName;
        this.entityId = entityId;

        //We want this to be a LinkedHashMap so there is some reliability about order entries are added. This allows
        //there to be predictability about the order in which things are written to disk.
        this.entries = new LinkedHashMap<String, ConfigPropertySetEntry>(entries);
    }

    public String getEntityName()
    {
        return entityName;
    }

    public Long getEntityId()
    {
        return entityId;
    }

    public boolean contains(final String propertyName)
    {
        return entries.containsKey(propertyName);
    }

    public String getStringProperty(final String propertyName)
    {
        final ConfigPropertySetEntry entry = this.entries.get(propertyName);
        if (entry != null)
        {
            return entry.asString();
        }
        else
        {
            return null;
        }
    }

    public String getStringPropertyDefault(final String propertyKey, final String defaultValue)
    {
        final String string = getStringProperty(propertyKey);
        return string == null ? defaultValue : string;
    }

    public boolean setStringProperty(final String propertyName, final String propertyValue)
    {
        if (propertyValue == null)
        {
            return removeProperty(propertyName);
        }
        else
        {
            return entries.put(propertyName, ConfigPropertySetEntry.createStringEntry(propertyName, propertyValue)) != null;
        }
    }

    public String getTextProperty(final String propertyName)
    {
        return getStringProperty(propertyName);
    }

    public boolean setTextProperty(final String propertyName, final String propertyValue)
    {
        if (propertyValue == null)
        {
            return removeProperty(propertyName);
        }
        else
        {
            return entries.put(propertyName, ConfigPropertySetEntry.createTextEntry(propertyName, propertyValue)) != null;
        }
    }

    public Long getLongProperty(final String propertyName)
    {
        final ConfigPropertySetEntry entry = this.entries.get(propertyName);
        if (entry != null)
        {
            return entry.asLong();
        }
        else
        {
            return null;
        }
    }

    public Long getLongPropertyDefault(final String propertyName, final Long def)
    {
        final Long longValue = getLongProperty(propertyName);
        return longValue == null ? def : longValue;
    }

    public boolean setLongProperty(final String propertyName, final Long propertyValue)
    {
        if (propertyValue == null)
        {
            return removeProperty(propertyName);
        }
        else
        {
            return entries.put(propertyName, ConfigPropertySetEntry.createLongEntry(propertyName, propertyValue)) != null;
        }
    }

    public Integer getIntegerProperty(final String propertyName)
    {
        final ConfigPropertySetEntry entry = this.entries.get(propertyName);
        if (entry != null)
        {
            return entry.asInteger();
        }
        else
        {
            return null;
        }
    }

    public boolean setIntegerProperty(final String propertyName, final Integer propertyValue)
    {
        if (propertyValue == null)
        {
            return removeProperty(propertyName);
        }
        else
        {
            return entries.put(propertyName, ConfigPropertySetEntry.createIntegerEntry(propertyName, propertyValue)) != null;
        }
    }

    public Boolean getBooleanProperty(final String propertyName)
    {
        final ConfigPropertySetEntry entry = this.entries.get(propertyName);
        if (entry != null)
        {
            return entry.asBoolean();
        }
        else
        {
            return null;
        }
    }

    public Boolean getBooleanPropertyDefault(final String propertyName, final Boolean defaultValue)
    {
        final Boolean value = getBooleanProperty(propertyName);
        return value == null ? defaultValue : value;
    }

    public boolean setBooleanProperty(final String propertyName, final Boolean propertyValue)
    {
        if (propertyValue == null)
        {
            return removeProperty(propertyName);
        }
        else
        {
            return entries.put(propertyName, ConfigPropertySetEntry.createBooleanEntry(propertyName, propertyValue)) != null;
        }
    }

    public Object getObjectProperty(final String propertyName)
    {
        final ConfigPropertySetEntry entry = this.entries.get(propertyName);
        if (entry != null)
        {
            return entry.asObject();
        }
        else
        {
            return null;
        }
    }

    public boolean removeProperty(final String propertyName)
    {
        return entries.remove(propertyName) != null;
    }

    public ConfigPropertySet copyForEntity(final String entity, final Long id)
    {
        return new ConfigPropertySet(entity, id, this.entryMap());
    }

    public Collection<ConfigPropertySetEntry> entries()
    {
        return Collections.unmodifiableCollection(entries.values());
    }

    public Map<String, ConfigPropertySetEntry> entryMap()
    {
        return Collections.unmodifiableMap(entries);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Entity: ").append(entityName).append(", ID: ").append(entityId).append(" {");

        for (Iterator<ConfigPropertySetEntry> iterator = entries.values().iterator(); iterator.hasNext();)
        {
            ConfigPropertySetEntry entry = iterator.next();
            builder.append(entry);
            if (iterator.hasNext())
            {
                builder.append(", ");
            }
        }
        return builder.append("}").toString();
    }

    @Override
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

        final ConfigPropertySet that = (ConfigPropertySet) o;

        if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null)
        {
            return false;
        }
        if (entityName != null ? !entityName.equals(that.entityName) : that.entityName != null)
        {
            return false;
        }
        if (entries != null ? !entries.equals(that.entries) : that.entries != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = entries != null ? entries.hashCode() : 0;
        result = 31 * result + (entityName != null ? entityName.hashCode() : 0);
        result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
        return result;
    }
}
