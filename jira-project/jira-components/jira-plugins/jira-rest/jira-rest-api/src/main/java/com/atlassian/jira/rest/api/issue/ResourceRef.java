package com.atlassian.jira.rest.api.issue;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * Bean that simply holds an id.
 */
@JsonSerialize (include = NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceRef
{
    public static ResourceRef withId(String id)
    {
        return new ResourceRef().id(id);
    }

    public static ResourceRef withKey(String key)
    {
        return new ResourceRef().key(key);
    }

    public static ResourceRef withName(String name)
    {
        return new ResourceRef().name(name);
    }

    public static ResourceRef withRubbish(String name)
    {
        return new ResourceRef().rubbish(name);
    }

    @JsonProperty
    private String id;

    @JsonProperty
    private String key;

    @JsonProperty
    private String name;

    @JsonProperty
    private String rubbish;

    public ResourceRef()
    {
    }

    public ResourceRef(String id, String key, String name, String rubbish)
    {
        this.id = id;
        this.key = key;
        this.name = name;
        this.rubbish = rubbish;
    }

    public String id()
    {
        return this.id;
    }

    public ResourceRef id(String id)
    {
        return new ResourceRef(id, key, name, rubbish);
    }

    public String key()
    {
        return this.key;
    }

    public ResourceRef key(String key)
    {
        return new ResourceRef(id, key, name, rubbish);
    }

    public String name()
    {
        return this.name;
    }

    public ResourceRef name(String name)
    {
        return new ResourceRef(id, key, name, rubbish);
    }
    
    public String rubbish()
    {
        return this.rubbish;
    }
    
    public ResourceRef rubbish(String rubbish)
    {
        return new ResourceRef(id, key, name, rubbish);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
