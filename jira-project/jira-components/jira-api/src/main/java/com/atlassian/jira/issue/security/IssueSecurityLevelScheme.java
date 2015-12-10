package com.atlassian.jira.issue.security;

/**
 * @since v5.2
 */
public final class IssueSecurityLevelScheme
{
    private final Long id;
    private final String name;
    private final String description;
    private final Long defaultSecurityLevelId;

    public IssueSecurityLevelScheme(Long id, String name, String description, Long defaultSecurityLevelId)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultSecurityLevelId = defaultSecurityLevelId;
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

    public Long getDefaultSecurityLevelId()
    {
        return defaultSecurityLevelId;
    }
}
