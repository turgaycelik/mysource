package com.atlassian.jira.issue.search;

/**
 * A SearchRequest data object.
 * This is a lightweight object that holds SearchRequest data as stored in the database.
 *
 * @since v5.2
 *
 * @see SearchRequest SearchRequest: the heavy weight version of this.
 */
public final class SearchRequestEntity
{
    private final Long id;
    private final String name;
    private final String nameLower;
    private final String author;
    private final String description;
    private final String user;
    private final String group;
    private final Long project;
    private final String request;
    private final Long favCount;

    public SearchRequestEntity(Long id, String name, String author, String description, String user, String group, Long project, String request, Long favCount)
    {
        this.id = id;
        this.name = name;
        this.nameLower = name.toLowerCase();
        this.author = author;
        this.description = description;
        this.user = user;
        this.group = group;
        this.project = project;
        this.request = request;
        this.favCount = favCount;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getNameLower()
    {
        return nameLower;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getDescription()
    {
        return description;
    }

    public String getUser()
    {
        return user;
    }

    public String getGroup()
    {
        return group;
    }

    public Long getProject()
    {
        return project;
    }

    public String getRequest()
    {
        return request;
    }

    public Long getFavCount()
    {
        return favCount;
    }
}
