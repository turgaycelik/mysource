package com.atlassian.jira.project;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Named;
import com.atlassian.jira.util.NamedWithDescription;

import org.ofbiz.core.entity.GenericValue;

/**
 * Defines a project in JIRA.
 */
@PublicApi
public interface Project extends Named, NamedWithDescription, WithId, WithKey
{
    /**
     * @return the id of the project
     */
    @Override
    public Long getId();

    /**
     * @return the name of the project.
     */
    public String getName();

    /**
     * @return the project key.
     */
    public String getKey();

    /**
     * @return the project URL
     */
    public String getUrl();

    /**
     * @return the project email address from which email notifications are sent.
     */
    public String getEmail();

    /**
     * @return the Project Lead
     * @deprecated Use {@link #getProjectLead()}, which returns application users, instead. Since v6.0.
     */
    public User getLead();

    /**
     * Returns the Project Lead
     * @return the Project Lead
     * @deprecated Use {@link #getLead()} instead.
     */
    public User getLeadUser();

    /**
     * @return the user name of the project lead
     */
    public String getLeadUserName();

    /**
     * @return the project description
     */
    public String getDescription();

    /**
     * Returns the default assignee for issues that get created in this project.
     * Returns {@link AssigneeTypes#PROJECT_LEAD} or {@link AssigneeTypes#UNASSIGNED}.
     * Can return NULL if the default assignee has not been set for this project and this means the PROJECT LEAD is the default assignee.
     *
     * TODO: Write upgrade task to ensure default assignee is always set.
     *
     * @return the default assignee. NB: Can return NULL
     */
    public Long getAssigneeType();

    /**
     * @deprecated (since 5.0) The counter is not something users of project information should concern themselves with.
     * This call delegates through to ProjectManager.getCurrentCounterForProject().
     * @return the last number used to generate an issue key. E.g. Counter = 2, issue key: HSP-2
     */
    public Long getCounter();

    /**
     * Returns the components for this Project.
     * @deprecated Use {@link #getProjectComponents()}. Since v4.1.
     * @return the components for this Project.
     * @see #getProjectComponents()
     */
    public Collection<GenericValue> getComponents();

    /**
     * Returns the components for this Project.
     * @return the components for this Project.
     */
    public Collection<ProjectComponent> getProjectComponents();

    /**
     * @return a Collection of {@link Version} for this project
     */
    public Collection<Version> getVersions();

    /**
     * @return a Collection of {@link IssueType} for this project
     */
    public Collection<IssueType> getIssueTypes();

    /**
     * @return a GV containing the project category information for this project.
     *
     * @deprecated Use {@link #getProjectCategoryObject()} instead. Since v5.1.
     */
    public GenericValue getProjectCategory();

    /**
     * @return the project category information for this project.
     */
    public ProjectCategory getProjectCategoryObject();

    /**
     * @deprecated only use this if you need to utilize an older API method
     * @return the GenericValue backing this project object
     */
    public GenericValue getGenericValue();

    /**
     * Gives the currently-configured {@link com.atlassian.jira.avatar.Avatar} for this project.
     * @return the current Avatar (or default if the current one is removed), never null.
     */
    @Nonnull
    public Avatar getAvatar();

    /**
     * @return the Project Lead
     */
    public ApplicationUser getProjectLead();

    /**
     * @return the Project Lead's userkey
     */
    public String getLeadUserKey();

    /**
     * @return the project key with which Project was originally created
     */
    @Internal
    public String getOriginalKey();
}
