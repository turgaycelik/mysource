package com.atlassian.jira.functest.config.crowd;

import com.atlassian.jira.functest.config.ConfigObjectWithId;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents an "&lt;Application ...&gt; from an XML backup.
 *
 * &lt;Application id="1" name="crowd-embedded" lowerName="crowd-embedded" active="1" applicationType="CROWD"/&gt;
 * @since v4.3
 */
public class ConfigCrowdApplication implements ConfigObjectWithId
{
    private Long id;
    private String name;
    private String lowerName;
    private Boolean active;
    private String applicationType;

    public Long getId()
    {
        return id;
    }

    public ConfigCrowdApplication setId(Long id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public ConfigCrowdApplication setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getLowerName()
    {
        return lowerName;
    }

    public ConfigCrowdApplication setLowerName(String lowerName)
    {
        this.lowerName = lowerName;
        return this;
    }

    public Boolean isActive()
    {
        return active;
    }

    public ConfigCrowdApplication setActive(Boolean active)
    {
        this.active = active;
        return this;
    }

    public String getApplicationType()
    {
        return applicationType;
    }

    public ConfigCrowdApplication setApplicationType(String applicationType)
    {
        this.applicationType = applicationType;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ConfigCrowdApplication that = (ConfigCrowdApplication) o;

        if (active != null ? !active.equals(that.active) : that.active != null) { return false; }
        if (applicationType != null ? !applicationType.equals(that.applicationType) : that.applicationType != null)
        { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (lowerName != null ? !lowerName.equals(that.lowerName) : that.lowerName != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (lowerName != null ? lowerName.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        result = 31 * result + (applicationType != null ? applicationType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
