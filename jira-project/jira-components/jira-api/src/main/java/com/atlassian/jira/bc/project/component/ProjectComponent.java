package com.atlassian.jira.bc.project.component;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.project.ProjectConstant;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericValue;

/**
 * A key domain object representing a "working part" of a Project such that an
 * Issue can be raised against or be relevant only to some parts. Typical usage
 * in projects to develop a technology product have a ProjectComponent for each
 * subsystem or module, e.g. GUI, Database, Indexing, Importing.
 *
 * Components can have a lead, or user responsible for the issues raised against
 * that component.
 *
 * The AssigneeType value ({@link com.atlassian.jira.project.AssigneeTypes})
 * refers to the default assignee for issues raised on that component.
 */
@PublicApi
public interface ProjectComponent extends ProjectConstant
{
    /**
     * Returns the component ID.
     *
     * @return component ID
     */
    Long getId();

    /**
     * Returns the component description.
     *
     * @return component description
     */
    String getDescription();

    /**
     * Returns the key of the lead for this project component.
     *
     * @return userkey of the lead for this project component
     */
    String getLead();

    /**
     * Returns the lead for this project component.
     *
     * @return the lead for this project component
     */
    ApplicationUser getComponentLead();

    /**
     * Returns the name of this project component.
     *
     * @return name of this project component
     */
    String getName();

    /**
     * Returns the id of the project of this component.
     * @return the project's id.
     */
    Long getProjectId();


    /**
     * Returns the assignee type.
     * @return the assignee type.
     *
     * @see com.atlassian.jira.project.AssigneeTypes
     */
    long getAssigneeType();

    /**
     * @deprecated use this object instead of the stinky GenericValue!
     * @return the underlying GenericValue
     */
    GenericValue getGenericValue();

}
