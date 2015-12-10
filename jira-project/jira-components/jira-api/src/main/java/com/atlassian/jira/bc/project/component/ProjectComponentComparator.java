package com.atlassian.jira.bc.project.component;

import com.atlassian.annotations.PublicApi;

import java.util.Comparator;

/**
 * Comparator for ProjectComponent objects, singleton, use static instance.
 */
@PublicApi
public class ProjectComponentComparator implements Comparator<ProjectComponent>
{

    /**
     * Compares two ProjectComponents, grouping them by project and then
     * by case-insensitive component name.
     */
    public static final ProjectComponentComparator INSTANCE = new ProjectComponentComparator();

    public static final Comparator<String> COMPONENT_NAME_COMPARATOR = new Comparator<String>()
    {
        @Override
        public int compare(String componentName1, String componentName2)
        {
            if (componentName1 == null && componentName2 == null)
                return 0;
            else if (componentName2 == null)
                return -1;
            else if (componentName1 == null)
                return 1;
            else
                return componentName1.compareToIgnoreCase(componentName2);
        }
    };

    /**
     * Don't create these, use INSTANCE.
     */
    private ProjectComponentComparator()
    {
        // no state
    }

    public int compare(ProjectComponent projectComponent1,ProjectComponent projectComponent2)
    {

        if (projectComponent1 == projectComponent2)
            return 0;

        if (projectComponent1 == null)
            return 1;

        if (projectComponent2 == null)
            return -1;

        Long projectId1 = projectComponent1.getProjectId();
        Long projectId2 = projectComponent2.getProjectId();

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
            String componentName1 = projectComponent1.getName();
            String componentName2 = projectComponent2.getName();

            return COMPONENT_NAME_COMPARATOR.compare(componentName1, componentName2);
        }
    }
}
