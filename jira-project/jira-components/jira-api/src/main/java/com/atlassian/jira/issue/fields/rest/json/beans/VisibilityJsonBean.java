package com.atlassian.jira.issue.fields.rest.json.beans;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A common representation for setting visibility of something, e.g comment or worklog
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class VisibilityJsonBean
{
    public enum VisibilityType
    {
        group,
        role
    }

    @JsonProperty
    public VisibilityType type;
    @JsonProperty
    public String value;

    public VisibilityJsonBean() {}

    public VisibilityJsonBean(final VisibilityType type, final String value)
    {
        this.type = type;
        this.value = value;
    }

    public VisibilityType getType()
    {
        return type;
    }

    public String getValue()
    {
        return value;
    }
}
