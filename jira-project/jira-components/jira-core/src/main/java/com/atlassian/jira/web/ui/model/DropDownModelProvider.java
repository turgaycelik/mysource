package com.atlassian.jira.web.ui.model;

/**
 * An interface to model a drop down menu, so we can pump it into a UI component easier
 *
 * @since v4.4.1
 */
public interface DropDownModelProvider<T>
{
    /**
     * @param domainObject some domain object
     * @param listIndex this parameter indicates the position in of the domain object in a list of domain objects.
     * Often generated markup uses position in the list to generate ids and the like
     * @return a drop down model based on the values in domainObject
     */
    DropDownModel getDropDownModel(T domainObject, int listIndex);
}
