package com.atlassian.jira.rest.api.issue;

import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * JSON-marshelling version of JsonType
 *
 * @since v5.0
 */
public class JsonTypeBean
{
    @JsonProperty
    private String type;
    @JsonProperty
    private String items;
    @JsonProperty
    private String system;
    @JsonProperty
    private String custom;
    @JsonProperty
    private Long customId;

    public JsonTypeBean()
    {
    }

    public JsonTypeBean(String type, String items, String system, String custom, Long customid)
    {
        this.type = type;
        this.items = items;
        this.system = system;
        this.custom = custom;
        this.customId = customid;
    }

    public String getType()
    {
        return type;
    }

    public String getItems()
    {
        return items;
    }

    public String getSystem()
    {
        return system;
    }

    public String getCustom()
    {
        return custom;
    }

    public Long getCustomId()
    {
        return customId;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
