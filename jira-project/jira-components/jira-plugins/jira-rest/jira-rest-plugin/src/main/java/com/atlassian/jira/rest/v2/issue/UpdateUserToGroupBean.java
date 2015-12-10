package com.atlassian.jira.rest.v2.issue;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * User to add user to the group
 *
 * @since v6.1
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class UpdateUserToGroupBean
{
    private static final UpdateUserToGroupBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new UpdateUserToGroupBean();
        DOC_EXAMPLE.name = "charlie";
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
