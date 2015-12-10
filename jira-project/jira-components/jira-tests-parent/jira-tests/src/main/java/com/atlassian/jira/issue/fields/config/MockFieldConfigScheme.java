package com.atlassian.jira.issue.fields.config;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * @since v4.4
 */
public class MockFieldConfigScheme implements FieldConfigScheme
{
    private Long id;
    private String name;
    private List<GenericValue> affectedProjects = Lists.newArrayList();

    @Override
    public String getName()
    {
        return name;
    }

    public MockFieldConfigScheme setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, FieldConfig> getConfigs()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public MockFieldConfigScheme setId(Long id)
    {
        this.id = id;
        return this;
    }

    @Override
    public List<JiraContextNode> getContexts()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isInContext(IssueContext issueContext)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<GenericValue> getAssociatedProjectCategories()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<ProjectCategory> getAssociatedProjectCategoryObjects()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MockFieldConfigScheme setAssociatedProjects(List<GenericValue> values)
    {
        this.affectedProjects = values;
        return this;
    }

    public MockFieldConfigScheme addAssociatedProjects(GenericValue gv)
    {
        this.affectedProjects.add(gv);
        return this;
    }

    @Override
    public List<GenericValue> getAssociatedProjects()
    {
        return affectedProjects;
    }

    @Override
    public List<Project> getAssociatedProjectObjects()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Long> getAssociatedProjectIds()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<GenericValue> getAssociatedIssueTypes()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<IssueType> getAssociatedIssueTypeObjects()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nonnull
    @Override
    public Collection<String> getAssociatedIssueTypeIds()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isGlobal()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isAllProjects()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isAllIssueTypes()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isEnabled()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isBasicMode()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public MultiMap getConfigsByConfig()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FieldConfig getOneAndOnlyConfig()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ConfigurableField getField()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockFieldConfigScheme that = (MockFieldConfigScheme) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
