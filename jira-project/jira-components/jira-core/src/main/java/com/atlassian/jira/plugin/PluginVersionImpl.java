package com.atlassian.jira.plugin;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v3.13
 */
public class PluginVersionImpl implements PluginVersion
{
    private final Long id;
    private final String key;
    private final String name;
    private final String version;
    private final Date created;

    /**
     * Used to create a representation of a PluginVersion when the persistent id is not known.
     *
     * @param key plugin key.
     * @param name plugin name.
     * @param version plugin version.
     * @param created date this is being created
     */
    public PluginVersionImpl(final String key, final String name, final String version, final Date created)
    {
        notNull("key", key);
        notNull("version", version);

        id = null;
        this.key = key;
        this.name = name;
        this.version = version;
        this.created = created == null ? null : new Date(created.getTime());
    }

    /**
     * Used to create a representation of a PluginVersion which is stored in database.
     *
     * @param id the database id of the stored record.
     * @param key plugin key.
     * @param name plugin name.
     * @param version plugin version.
     * @param created date this is being created
     */
    public PluginVersionImpl(final Long id, final String key, final String name, final String version, final Date created)
    {
        notNull("id", id);
        notNull("key", key);
        notNull("version", version);

        this.id = id;
        this.key = key;
        this.name = name;
        this.version = version;
        this.created = created == null ? null : new Date(created.getTime());
    }

    public Long getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    public Date getCreated()
    {
        return created == null ? null : new Date(created.getTime());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final PluginVersionImpl that = (PluginVersionImpl) o;

        if (created != null ? !created.equals(that.created) : that.created != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (!key.equals(that.key))
        {
            return false;
        }
        if (!name.equals(that.name))
        {
            return false;
        }
        if (!version.equals(that.version))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + key.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (created == null ? 0 : created.hashCode());
        return result;
    }
}
