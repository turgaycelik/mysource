package com.atlassian.jira.issue.comparator;

import com.atlassian.annotations.PublicApi;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * This class is used to compare two instances of a component GenericValue
 * @deprecated since 4.0. use ProjectComponent instead of GenericValue and compare through {@link com.atlassian.jira.bc.project.component.ProjectComponentComparator}
 */
@Deprecated
@PublicApi
public class ComponentComparator implements Comparator
{
    public static final Comparator COMPARATOR = new ComponentComparator();

    public int compare(Object o1, Object o2)
    {
        if (o1 == o2)
            return 0;

        if (o1 == null)
            return 1;

        if (o2 == null)
            return -1;

        Long projectId1 = ((GenericValue) o1).getLong("project");
        Long projectId2 = ((GenericValue) o2).getLong("project");

        if (projectId1 == null && projectId2 == null)
            return 0;

        if (projectId1 == null)
            return 1;

        if (projectId2 == null)
            return -1;

        int projectComparison = projectId1.compareTo(projectId2);
        if (projectComparison != 0)
        {
            return projectComparison;
        }
        else
        {
            String componentName1 = ((GenericValue) o1).getString("name");
            String componentName2 = ((GenericValue) o2).getString("name");

            if (componentName1 == null && componentName2 == null)
                return 0;
            else if (componentName2 == null)
                return -1;
            else if (componentName1 == null)
                return 1;
            else
                return componentName1.compareToIgnoreCase(componentName2);
        }
    }
}
