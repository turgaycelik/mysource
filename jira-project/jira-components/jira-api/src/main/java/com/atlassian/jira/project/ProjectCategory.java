package com.atlassian.jira.project;

import com.atlassian.jira.util.Named;
import com.atlassian.jira.util.NamedWithDescription;

/**
 * Defines a project category in JIRA.
 *
 * @since v4.0
 */
public interface ProjectCategory extends Named, NamedWithDescription
{
    /**
     * @return the unique identifier for this project category
     */
    Long getId();

    /**
     * @return the user defined name for this project catetory
     */
    String getName();

    /**
     * @return the user defined description for this project category
     */
    String getDescription();
}
