package com.atlassian.jira.scheme;

import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public abstract class AbstractViewSchemes extends AbstractSchemeAwareAction
{
    @Deprecated
    public List<GenericValue> getSchemes() throws GenericEntityException
    {
        return getSchemeManager().getSchemes();
    }

    public List<Scheme> getSchemeObjects()
    {
        return getSchemeManager().getSchemeObjects();
    }

    @Deprecated
    public List<GenericValue> getProjects(GenericValue scheme) throws GenericEntityException
    {
        return getSchemeManager().getProjects(scheme);
    }

    public List<Project> getProjects(Scheme scheme)
    {
        return getSchemeManager().getProjects(scheme);
    }
}
