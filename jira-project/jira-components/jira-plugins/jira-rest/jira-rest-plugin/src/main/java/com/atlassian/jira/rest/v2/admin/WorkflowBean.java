package com.atlassian.jira.rest.v2.admin;

import org.codehaus.jackson.annotate.JsonAutoDetect;

/**
 * Transport for getting a workflow via REST
 *
 * @since v5.2
 */
@JsonAutoDetect
public class WorkflowBean
{
    private final String name;
    private final String description;
    private final String lastModifiedDate;
    private final String lastModifiedUser;
    private final Integer steps;
    private final boolean isDefault;

    WorkflowBean(String name, String description, String lastModifiedDate, String lastModifiedUser, Integer steps, boolean isDefault)
    {
        this.description = description;
        this.name = name;
        this.lastModifiedDate = lastModifiedDate;
        this.lastModifiedUser = lastModifiedUser;
        this.steps = steps;
        this.isDefault = isDefault;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public String getLastModifiedUser()
    {
        return lastModifiedUser;
    }

    public Integer getSteps()
    {
        return steps;
    }

    public boolean isDefault()
    {
        return isDefault;
    }
}