package com.atlassian.jira.functest.config.service;

import com.atlassian.jira.functest.config.ConfigObjectWithId;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a JiraService from an XML backup.
 *
 * &lt;ServiceConfig id="10000" time="60000" clazz="com.atlassian.jira.service.services.mail.MailQueueService" name="Mail Queue Service"/&gt;
 */
final public class ConfigService implements ConfigObjectWithId
{
    private Long id;
    private Long timeout;
    private String clazz;
    private String name;
    private ConfigPropertySet propertySet;

    public ConfigService()
    {
    }

    public ConfigService(Long id, Long timeout, String clazz, String name, ConfigPropertySet propertySet)
    {
        this.id = id;
        this.timeout = timeout;
        this.clazz = clazz;
        this.name = name;
        this.propertySet = propertySet;
    }

    public Long getId()
    {
        return id;
    }

    public Long getTimeout()
    {
        return timeout;
    }

    public String getClazz()
    {
        return clazz;
    }

    public String getName()
    {
        return name;
    }

    public ConfigService setId(final Long id)
    {
        this.id = id;
        return this;
    }

    public ConfigService setTimeout(final Long timeout)
    {
        this.timeout = timeout;
        return this;
    }

    public ConfigService setClazz(final String clazz)
    {
        this.clazz = clazz;
        return this;
    }

    public ConfigService setName(final String name)
    {
        this.name = name;
        return this;
    }

    public ConfigPropertySet getPropertySet()
    {
        return propertySet;
    }

    public ConfigService setPropertySet(ConfigPropertySet propertySet)
    {
        this.propertySet = propertySet;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

        final ConfigService service = (ConfigService) o;

        if (clazz != null ? !clazz.equals(service.clazz) : service.clazz != null)
        {
            return false;
        }
        if (id != null ? !id.equals(service.id) : service.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(service.name) : service.name != null)
        {
            return false;
        }
        if (propertySet != null ? !propertySet.equals(service.propertySet) : service.propertySet != null)
        {
            return false;
        }
        if (timeout != null ? !timeout.equals(service.timeout) : service.timeout != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (propertySet != null ? propertySet.hashCode() : 0);
        return result;
    }
}
