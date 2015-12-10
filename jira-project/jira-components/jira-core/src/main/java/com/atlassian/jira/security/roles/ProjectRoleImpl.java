package com.atlassian.jira.security.roles;

public class ProjectRoleImpl implements ProjectRole
{
    private static final Long ZERO = 0L;

    private final Long id;
    private final String name;
    private final String description;

    public ProjectRoleImpl(String name, String description)
    {
        this.id = ZERO;
        this.name = name;
        this.description = description;
    }

    public ProjectRoleImpl(Long id, String name, String description)
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

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ProjectRoleImpl that = (ProjectRoleImpl) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (name != null ? name.hashCode() : 0);
    }

    public String toString() {
        return name;
    }
}
