package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.project.Project;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compare {@link com.atlassian.jira.project.Project} by its name
 */
public class ProjectNameComparator implements Comparator<Project>, Serializable
{
    static final long serialVersionUID = -3101004265788503795L;

    public static final Comparator<Project> COMPARATOR = new ProjectNameComparator();

    public int compare(final Project o1, final Project o2)
    {
        if (o1 == o2)
        {
            return 0;
        }
        else if (o1 == null) // null is less than any value
        {
            return -1;
        }
        else if (o2 == null) // any value is greater than null
        {
            return 1;
        }

        final String name1 = o1.getName();
        final String name2 = o2.getName();

        // check nulls
        if ((name1 == null) && (name2 == null))
        {
            return 0;
        }
        else if (name1 == null) // null is less than any value
        {
            return -1;
        }
        else if (name2 == null) // any value is greater than null
        {
            return 1;
        }

        return name1.compareTo(name2);
    }
}
