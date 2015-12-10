package com.atlassian.jira.project;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * Allows to retrieve and store arbitrary properties against a project.
 *
 * @since v6.1
 */
public interface ProjectPropertiesManager
{
    /**
     * Retrieve all properties associated with the given project.
     * <p/>
     * The returned PropertySet is mutable and could be used to directly update data in database.
     *
     * @param project project
     * @return the property set associated with the project
     *
     * @since v6.1
     */
    PropertySet getPropertySet(Project project);

}
