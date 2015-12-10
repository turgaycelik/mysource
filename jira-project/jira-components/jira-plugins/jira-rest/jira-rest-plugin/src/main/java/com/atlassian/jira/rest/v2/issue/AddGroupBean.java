package com.atlassian.jira.rest.v2.issue;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Used to create groups via REST
 *
 * @since v6.1
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class AddGroupBean
{
    private static final AddGroupBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new AddGroupBean();
        DOC_EXAMPLE.name = "power-users";
    }

    @JsonProperty
    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
}
