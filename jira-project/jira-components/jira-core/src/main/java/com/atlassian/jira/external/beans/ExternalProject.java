package com.atlassian.jira.external.beans;

import java.util.Map;

import com.atlassian.jira.ofbiz.FieldMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.ofbiz.core.entity.GenericValue;

public class ExternalProject implements NamedExternalObject
{
    private GenericValue projectGV;
    private String id;
    private String name;
    private String key;
    private String originalKey;
    private String url;
    private String lead;
    private String description;
    private String projectCategoryName;
    private String assigneeType;
    private String emailSender;
    private String counter;

    public ExternalProject()
    {
    }

    public Map<String, Object> toFieldsMap()
    {
        FieldMap fields = new FieldMap();

        // Note: the ID is deliberately excluded

        putIfNotEmpty(fields, "name", name);
        putIfNotEmpty(fields, "url", url);
        putIfNotEmpty(fields, "lead", lead);
        putIfNotEmpty(fields, "description", description);
        putIfNotEmpty(fields, "key", key);

        if (StringUtils.isNotEmpty(assigneeType))
        {
            fields.put("assigneetype", Long.valueOf(assigneeType));
        }

        return fields;
    }

    private static void putIfNotEmpty(final FieldMap fields, final String key, final String value)
    {
        if (StringUtils.isNotEmpty(value))
        {
            fields.put(key, value);
        }
    }



    public GenericValue getProjectGV()
    {
        return projectGV;
    }

    public void setProjectGV(GenericValue projectGV)
    {
        this.projectGV = projectGV;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getLead()
    {
        return lead;
    }

    public void setLead(String lead)
    {
        this.lead = lead;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getProjectCategoryName()
    {
        return projectCategoryName;
    }

    public void setProjectCategoryName(String projectCategoryName)
    {
        this.projectCategoryName = projectCategoryName;
    }

    public String getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(final String assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    public String getEmailSender()
    {
        return emailSender;
    }

    public void setEmailSender(final String emailSender)
    {
        this.emailSender = emailSender;
    }

    public String getCounter()
    {
        return counter;
    }

    public void setCounter(final String pcounter)
    {
        this.counter = pcounter;
    }

    public boolean equals(Object o)
    {
        if ( !(o instanceof ExternalProject) )
        {
            return false;
        }

        ExternalProject rhs = (ExternalProject) o;
        return new EqualsBuilder()
                .append(getId(), rhs.getId())
                .append(getKey(), rhs.getKey())
                .append(getName(), rhs.getName())
                .append(getDescription(), rhs.getDescription())
                .append(getEmailSender(), rhs.getEmailSender())
                .append(getCounter(), rhs.getCounter())
                .isEquals();
    }

    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).
                append(getId()).
                append(getKey()).
                append(getName()).
                append(getDescription()).
                append(getEmailSender()).
                append(getCounter()).
                toHashCode();
    }

    public String toString()
    {
        final String name = this.name;
        return (name != null) ? name : key;
    }

    public boolean isValid()
    {
        return StringUtils.isNotEmpty(key);
    }

    public String getOriginalKey()
    {
        return originalKey;
    }

    public void setOriginalKey(final String originalKey)
    {
        this.originalKey = originalKey;
    }
}
