package com.atlassian.jira.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

/**
 * Bean implementation of Project interface but doesn't believe in GenericValues. Equals and hashCode are based on
 * id alone.
 */
public class MockProject implements Project
{
    private Long id;
    private String name;
    private String key;
    private String url;
    private String email;
    private ApplicationUser lead;
    private String description;
    private Long assigneeType;
    private Long counter;
    private Collection<GenericValue> components;
    private Collection<ProjectComponent> projectComponents = Collections.emptyList();
    private Collection<Version> versions = Collections.emptyList();
    private GenericValue projectGV;
    private GenericValue projectCategoryGV;
    private Avatar avatar;
    private Collection<IssueType> types;

    public MockProject(final GenericValue gv)
    {
        this(gv.getLong("id"), gv.getString("key"), gv.getString("name"), gv);
    }

    public MockProject()
    {
    }

    public MockProject(final long id)
    {
        this(id, null, null, null);
    }

    public MockProject(final Long id)
    {
        this(id, null, null, null);
    }

    public MockProject(final long id, final String key)
    {
        this(id, key, key, null);
    }

    public MockProject(final long id, final String key, final String name)
    {
        this(id, key, name, null);
    }

    public MockProject(final Long id, final String key, final String name)
    {
        this(id, key, name, null);
    }

    public MockProject(final long id, final String key, final String name, final GenericValue projectGV)
    {
        this(new Long(id), key, name, projectGV);
    }

    public MockProject(final Long id, final String key, final String name, final GenericValue projectGV)
    {
        this.id = id;
        this.key = key;
        this.name = name;
        this.projectGV = projectGV;
    }

    @Override
    public Long getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(final Long assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    @Override
    public Collection<ProjectComponent> getProjectComponents()
    {
        return projectComponents;
    }

    public void setProjectComponents(final Collection<ProjectComponent> projectComponents)
    {
        this.projectComponents = projectComponents;
    }

    @Override
    public Collection<GenericValue> getComponents()
    {
        return components;
    }

    public void setComponents(final Collection<GenericValue> components)
    {
        this.components = components;
    }

    @Override
    public Long getCounter()
    {
        return counter;
    }

    public void setCounter(final Long counter)
    {
        this.counter = counter;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public void setKey(final String key)
    {
        this.key = key;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public MockProject setEmail(final String email)
    {
        this.email = email;
        return this;
    }

    @Override
    public String getEmail()
    {
        return email;
    }

    @Override
    public Collection<Version> getVersions()
    {
        return versions;
    }

    @Override
    public Collection<IssueType> getIssueTypes()
    {
        return types;
    }

    public MockProject setIssueTypes(final IssueType...types)
    {
        return setIssueTypes(Arrays.asList(types));
    }

    public MockProject setIssueTypes(final Collection<IssueType> types)
    {
        this.types = types;
        return this;
    }

    public MockProject setIssueTypes(final String...types)
    {
        final Collection<IssueType> output = new ArrayList<IssueType>(types.length);
        for (final String type : types)
        {
            output.add(new MockIssueType(type, type));
        }
        return setIssueTypes(output);
    }

    @Override
    public GenericValue getProjectCategory()
    {
        return projectCategoryGV;
    }

    @Override
    public ProjectCategory getProjectCategoryObject()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setProjectCategoryGV(final GenericValue projectCategoryGV)
    {
        this.projectCategoryGV = projectCategoryGV;
    }

    public void setVersions(final Collection<Version> versions)
    {
        this.versions = versions;
    }

    @Override
    public GenericValue getGenericValue()
    {
        if (projectGV != null)
            return projectGV;
        // Create one on the fly...
        // TODO: Add other fields.
        final MockGenericValue gv = new MockGenericValue("Project");
        gv.set("id", getId());
        gv.set("name", getName());
        gv.set("key", getKey());
        gv.set("description", getDescription());
        gv.set("lead", ApplicationUsers.getKeyFor(lead));
        return gv;
    }

    @Override
    public User getLead()
    {
        return ApplicationUsers.toDirectoryUser(lead);
    }

    @Override
    public User getLeadUser()
    {
        return ApplicationUsers.toDirectoryUser(lead);
    }

    public void setLead(final ApplicationUser lead)
    {
        this.lead = lead;
    }

    public void setLead(final User lead)
    {
        this.lead = ApplicationUsers.from(lead);
    }

    @Override
    public String getLeadUserName()
    {
        return lead.getName();
    }

    @Override
    @Nonnull
    public Avatar getAvatar()
    {
        return avatar;
    }

    @Override
    public ApplicationUser getProjectLead()
    {
        return lead;
    }

    @Override
    public String getLeadUserKey()
    {
        return ApplicationUsers.getKeyFor(lead);
    }

    @Override
    public String getOriginalKey()
    {
        return key;
    }

    public void setAvatar(final Avatar avatar)
    {
        this.avatar = avatar;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockProject that = (MockProject) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public String toString()
    {
        return "Project: " + getName() + '(' + getId() + ')';
    }
}
