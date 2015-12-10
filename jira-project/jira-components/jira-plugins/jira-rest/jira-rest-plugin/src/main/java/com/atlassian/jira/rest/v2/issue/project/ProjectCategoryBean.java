package com.atlassian.jira.rest.v2.issue.project;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.rest.v2.issue.Examples;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since v6.3
 */
public class ProjectCategoryBean
{
    /**
     * Project Category bean example used in auto-generated documentation.
     */
    public static final ProjectCategoryBean DOC_EXAMPLE1;
    public static final ProjectCategoryBean DOC_EXAMPLE2;

    public static final ProjectCategoryBean DOC_EXAMPLE_CREATE;
    public static final ProjectCategoryBean DOC_EXAMPLE_CREATED;

    public static final ProjectCategoryBean DOC_EXAMPLE_UPDATE;
    public static final ProjectCategoryBean DOC_EXAMPLE_UPDATED;

    static
    {
        ProjectCategoryBean category = new ProjectCategoryBean();
        category.id = "10000";
        category.name = "FIRST";
        category.description = "First Project Category";
        category.self = Examples.restURI("projectCategory/" + category.id);
        DOC_EXAMPLE1 = category;

        ProjectCategoryBean category2 = new ProjectCategoryBean();
        category2.id = "10001";
        category2.name = "SECOND";
        category2.description = "Second Project Category";
        category2.self = Examples.restURI("projectCategory/" + category2.id);
        DOC_EXAMPLE2 = category2;

        ProjectCategoryBean category3 = new ProjectCategoryBean();
        category3.name = "CREATED";
        category3.description = "Created Project Category";
        DOC_EXAMPLE_CREATE = category3;

        ProjectCategoryBean category4 = new ProjectCategoryBean();
        category4.id = "10100";
        category4.name = category3.name;
        category4.description = category3.description;
        category4.self = Examples.restURI("projectCategory/" + category4.id);
        DOC_EXAMPLE_CREATED = category4;

        ProjectCategoryBean category5 = new ProjectCategoryBean();
        category5.name = "UPDATED";
        category5.description = "Updated Project Category";
        DOC_EXAMPLE_UPDATE = category5;

        ProjectCategoryBean category6 = new ProjectCategoryBean();
        category6.id = "10100";
        category6.name = category5.name;
        category6.description = category5.description;
        category6.self = Examples.restURI("projectCategory/" + category6.id);
        DOC_EXAMPLE_UPDATED = category6;

    }

    public static final List<ProjectCategoryBean> PROJECT_CATEGORIES_EXAMPLE;
    static
    {
        PROJECT_CATEGORIES_EXAMPLE = new ArrayList<ProjectCategoryBean>();
        PROJECT_CATEGORIES_EXAMPLE.add(DOC_EXAMPLE1);
        PROJECT_CATEGORIES_EXAMPLE.add(DOC_EXAMPLE2);
    }


    @JsonProperty
    private URI self;
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private String description;


    public ProjectCategoryBean(final ProjectCategory projectCategory, URI self)
    {
        this.self = self;
        id = projectCategory.getId() == null ? null : projectCategory.getId().toString();
        name = projectCategory.getName();
        description = projectCategory.getDescription();
    }

    public ProjectCategoryBean()
    {
    }

    public URI getSelf()
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

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
