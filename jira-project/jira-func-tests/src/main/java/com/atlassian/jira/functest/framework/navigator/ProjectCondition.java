package com.atlassian.jira.functest.framework.navigator;

/**
 * Represents a selection of projects in a Navigator search.
 *
 * @since v3.13
 */
public class ProjectCondition extends MultiSelectCondition
{
    public ProjectCondition()
    {
        super("pid");
    }

    /**
     * Used to construct condition for project picker custom field
     * @param elementName the name of the field e.g. <code>pid</code> or <code>customfield_10000</code>
     */
    public ProjectCondition(final String elementName)
    {
        super(elementName);
    }

    public ProjectCondition(ProjectCondition copy)
    {
        super(copy);
    }

    public ProjectCondition addProject(String project)
    {
        addOption(project);
        return this;
    }

    public ProjectCondition removeProject(String project)
    {
        removeOption(project);
        return this;
    }

    public String toString()
    {
        return "Projects: " + getOptions();
    }

    public NavigatorCondition copyCondition()
    {
        return new ProjectCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new ProjectCondition();
    }
}
