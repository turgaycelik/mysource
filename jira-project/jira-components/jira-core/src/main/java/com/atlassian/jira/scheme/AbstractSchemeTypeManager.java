package com.atlassian.jira.scheme;

import com.atlassian.jira.util.JiraTypeUtils;

import java.util.Map;

public abstract class AbstractSchemeTypeManager<T> implements SchemeTypeManager<T>
{
    public abstract String getResourceName();

    /**
     * Get a particular permission type based on the id
     * @param id The Id of the permission type
     * @return The permission type object
     */
    public T getSchemeType(String id)
    {
        return getTypes().get(id);
    }

    public abstract Map<String, T> getSchemeTypes();

    public abstract void setSchemeTypes(Map<String, T> schemeType);

    /**
     * Get the different types for a scheme.
     * @return Map of scheme types, eg. for permission types:
     *  {"reporter" -> com.atlassian.jira.security.type.CurrentReporter,
     *   "lead" -> com.atlassian.jira.security.type.ProjectLead,
     *   ...
     * }
     */
    public Map<String, T>  getTypes()
    {
        Map<String, T> schemeTypes = getSchemeTypes();
        if (schemeTypes == null)
        {
            Map<String, T> types = JiraTypeUtils.loadTypes(getResourceName(), this.getClass());
            setSchemeTypes(types);
            return getSchemeTypes();
        }
        return schemeTypes;
    }
}
