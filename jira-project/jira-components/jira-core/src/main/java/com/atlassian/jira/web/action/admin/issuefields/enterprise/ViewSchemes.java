package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class ViewSchemes extends JiraWebActionSupport
{
    private String fieldLayoutSchemeName;
    private String fieldLayoutSchemeDescription;

    private final FieldLayoutManager fieldLayoutManager;
    private Map<Long, Collection<GenericValue>> schemeProjectsMap = Maps.newHashMap();

    public ViewSchemes(final FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public List<FieldLayoutScheme> getFieldLayoutScheme()
    {
        try
        {
            return getFieldLayoutManager().getFieldLayoutSchemes();
        }
        catch (DataAccessException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.fieldlayout.could.not.retrieve"));
            return Collections.emptyList();
        }
    }

    public Collection<GenericValue> getSchemeProjects(final FieldLayoutScheme fieldLayoutScheme)
    {
        if (fieldLayoutScheme == null)
            throw new IllegalArgumentException(getText("admin.errors.fieldlayout.fls.must.not.be.null"));

        if (!schemeProjectsMap.containsKey(fieldLayoutScheme.getId()))
        {
            try
            {
                final Collection<GenericValue> projects = Lists.newArrayList(
                        getFieldLayoutManager().getProjects(fieldLayoutScheme));
                Collections.sort((List) projects, OfBizComparators.NAME_COMPARATOR);
                schemeProjectsMap.put(fieldLayoutScheme.getId(), projects);
            }
            catch (DataAccessException e)
            {
                log.error(e, e);
                addErrorMessage(getText("admin.errors.fieldlayout.could.not.retrieve.projects",fieldLayoutScheme));
                return Collections.emptyList();
            }
        }

        return schemeProjectsMap.get(fieldLayoutScheme.getId());
    }

    protected FieldLayoutManager getFieldLayoutManager()
    {
        return fieldLayoutManager;
    }

    public String getFieldLayoutSchemeName()
    {
        return fieldLayoutSchemeName;
    }

    public void setFieldLayoutSchemeName(final String fieldLayoutSchemeName)
    {
        this.fieldLayoutSchemeName = fieldLayoutSchemeName;
    }

    public String getFieldLayoutSchemeDescription()
    {
        return fieldLayoutSchemeDescription;
    }

    public void setFieldLayoutSchemeDescription(final String fieldLayoutSchemeDescription)
    {
        this.fieldLayoutSchemeDescription = fieldLayoutSchemeDescription;
    }

}
