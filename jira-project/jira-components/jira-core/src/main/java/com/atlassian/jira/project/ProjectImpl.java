package com.atlassian.jira.project;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserUtils;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

/**
 * Represents an immutable Project domain object for JIRA.
 */
public class ProjectImpl implements Project
{
    private final GenericValue projectGV;
    private final String key;
    private User lead;

    public ProjectImpl(GenericValue projectGv)
    {
        this.projectGV = projectGv;
        key = getStringFromGV("key");
    }

    public Long getId()
    {
        return getLongFromGV("id");
    }

    public String getName()
    {
        return getStringFromGV("name");
    }

    public String getKey()
    {
        return key;
    }

    public String getUrl()
    {
        return getStringFromGV("url");
    }

    @Override
    public String getEmail()
    {
        return OFBizPropertyUtils.getPropertySet(projectGV).getString(ProjectKeys.EMAIL_SENDER);
    }

    @Override
    public ApplicationUser getProjectLead()
    {
        return ComponentAccessor.getUserManager().getUserByKey(getLeadUserKey());
    }

    public User getLead()
    {
        if (getLeadUserName() != null && lead == null)
        {
            lead = getUser(getLeadUserName());
        }
        return lead;
    }

    @Override
    public User getLeadUser()
    {
        return getLead();
    }

    @Override
    public String getLeadUserName()
    {
        ApplicationUser lead = getProjectLead();
        if (lead == null)
        {
            return getLeadUserKey();
        }
        return lead.getUsername();
    }

    @Override
    public String getLeadUserKey()
    {
        return getStringFromGV("lead");
    }

    @Override
    public String getOriginalKey()
    {
        return getStringFromGV("originalkey");
    }

    public String getDescription()
    {
        String value = getStringFromGV("description");
        return value == null ? "" : value;
    }

    public Long getAssigneeType()
    {
        return getLongFromGV("assigneetype");
    }

    public Long getCounter()
    {
        return ComponentAccessor.getProjectManager().getCurrentCounterForProject(getId());
    }

    public Collection<GenericValue> getComponents()
    {
        //noinspection deprecation
        return ComponentAccessor.getProjectManager().getComponents(projectGV);
    }

    public Collection<ProjectComponent> getProjectComponents()
    {
        return ComponentAccessor.getProjectComponentManager().findAllForProject(getId());
    }

    public Collection<Version> getVersions()
    {
        return ComponentAccessor.getVersionManager().getVersions(projectGV.getLong("id"));
    }

    @Override
    public Collection<IssueType> getIssueTypes()
    {
        return ComponentAccessor.getComponent(IssueTypeSchemeManager.class).getIssueTypesForProject(this);
    }

    public GenericValue getProjectCategory()
    {
        return ComponentAccessor.getProjectManager().getProjectCategoryFromProject(projectGV);
    }

    @Override
    public ProjectCategory getProjectCategoryObject()
    {
        return ComponentAccessor.getProjectManager().getProjectCategoryForProject(this);
    }

    @Nonnull
    public Avatar getAvatar()
    {
        final AvatarManager avatarManager = ComponentAccessor.getAvatarManager();
        final Avatar projectAvatar = avatarManager.getById(getLongFromGV("avatar"));
        if (null == projectAvatar)
        {
            final Long defaultAvatarId = avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
            final Avatar defaultAvatar = avatarManager.getById(defaultAvatarId);
            if (null == defaultAvatar)
            {
                throw new NoSuchElementException("There is no project default avatar - configuration failure!");
            }

            return defaultAvatar;
        }
        else
        {
            return projectAvatar;
        }
    }

    public GenericValue getGenericValue()
    {
        return projectGV;
    }

    private String getStringFromGV(String key)
    {
        if (projectGV != null)
        {
            return projectGV.getString(key);
        }
        return null;
    }

    private Long getLongFromGV(String key)
    {
        if (projectGV != null)
        {
            return projectGV.getLong(key);
        }
        return null;
    }

    private User getUser(String username)
    {
        return UserUtils.getUserEvenWhenUnknown(username);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Project))
        {
            return false;
        }

        final Project other = (Project) o;
        // JRA-20184, JRADEV-21134: All the Project properties can change except ID

        if (getId() == null)
        {
            return other.getId() == null;
        }
        else
        {
            return getId().equals(other.getId());
        }
    }

    @Override
    public int hashCode()
    {
        final Long id = getId();
        return id != null ? id.hashCode() : 0;
    }

    public String toString()
    {
        return "Project: " + getKey();
    }
}
