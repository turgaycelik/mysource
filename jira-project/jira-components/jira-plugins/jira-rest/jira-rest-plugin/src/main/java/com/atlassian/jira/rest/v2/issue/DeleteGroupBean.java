package com.atlassian.jira.rest.v2.issue;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Used to delete groups via REST
 *
 * @since v6.1
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class DeleteGroupBean
{
    private static final DeleteGroupBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new DeleteGroupBean();
        DOC_EXAMPLE.name = "beginner-users";
        DOC_EXAMPLE.swapName = "power-users";
    }

    @JsonProperty
    private String name;

    @JsonProperty
    private String swapName;

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getSwapName()
    {
        return swapName;
    }

    public void setSwapName(final String swapName)
    {
        this.swapName = swapName;
    }
}
