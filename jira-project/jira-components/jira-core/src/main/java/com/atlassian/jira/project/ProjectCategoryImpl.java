package com.atlassian.jira.project;

/**
 * Immutable implementation of ProjectCategory
 *
 * @since v4.4
 */
public class ProjectCategoryImpl implements ProjectCategory
{
    private Long id;
    private String name;
    private String description;

    public ProjectCategoryImpl(Long id, String name, String description)
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }
}
