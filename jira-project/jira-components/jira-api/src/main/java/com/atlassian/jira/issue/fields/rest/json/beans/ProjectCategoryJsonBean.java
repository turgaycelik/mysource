package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.util.JiraUrlCodec;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since v6.3
 */
public class ProjectCategoryJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String description;

    @JsonProperty
    private String name;


    public String getSelf()
    {
        return self;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return name;
    }

    public static ProjectCategoryJsonBean bean(final ProjectCategory projectCategoryObject, final JiraBaseUrls urls)
    {
        if (projectCategoryObject == null)
        {
            return null;
        }

        final ProjectCategoryJsonBean bean = new ProjectCategoryJsonBean();
        bean.id = projectCategoryObject.getId().toString();
        bean.name = projectCategoryObject.getName();
        bean.description = projectCategoryObject.getDescription();
        bean.self = urls.restApi2BaseUrl() + "projectCategory/" + JiraUrlCodec.encode(bean.getId());

        return bean;
    }

}
