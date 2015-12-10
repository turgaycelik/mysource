package com.atlassian.jira.jql.context;

/**
 * Represents the specicial case of all projects, in an unenumerated form.
 *
 * @since v4.0
 */
public class AllProjectsContext implements ProjectContext
{
    public static final AllProjectsContext INSTANCE = new AllProjectsContext();

    public AllProjectsContext getInstance()
    {
        return INSTANCE;
    }

    private AllProjectsContext()
    {
        //Don't create me.
    }

    public Long getProjectId()
    {
        return null;
    }

    public boolean isAll()
    {
        return true;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return 13;
    }

    @Override
    public String toString()
    {
        return "All Projects Context";
    }
}
