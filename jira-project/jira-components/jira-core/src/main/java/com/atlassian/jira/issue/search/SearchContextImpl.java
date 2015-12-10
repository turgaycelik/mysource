package com.atlassian.jira.issue.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ObjectUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class SearchContextImpl implements SearchContext
{
    private static final List<Long> ALL_PROJECTS = Collections.singletonList(null);
    private static final List<String> ALL_ISSUE_TYPES = Collections.singletonList(null);

    private static final Logger log = Logger.getLogger(SearchContextImpl.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    private static final Predicate LONG_PREDICATE = new AndPredicate(InstanceofPredicate.getInstance(Long.class), ObjectUtils.getIsSetPredicate());
    private static final Predicate STRING_PREDICATE = new AndPredicate(InstanceofPredicate.getInstance(String.class), ObjectUtils.getIsSetPredicate());

    // ------------------------------------------------------------------------------------------------- Type Properties
    protected List projectCategoryIds;
    protected List<Long> projectIds;
    protected List<String> issueTypeIds;
    protected List<Project> projects;
    protected List<IssueType> issueTypes;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public SearchContextImpl()
    {
        constantsManager = ComponentAccessor.getConstantsManager();
        projectManager = ComponentAccessor.getProjectManager();
    }

    public SearchContextImpl(List projectCategoryIds, List projectIds, List issueTypeIds)
    {
        this();
        setProjectCategoryIds(projectCategoryIds);
        setProjectIds(projectIds);
        setIssueTypeIds(issueTypeIds);
    }

    public SearchContextImpl(SearchContext searchContext)
    {
        this(searchContext.getProjectCategoryIds(), searchContext.getProjectIds(), searchContext.getIssueTypeIds());
    }


    // -------------------------------------------------------------------------------------------------- Public Methods
    public boolean isForAnyProjects()
    {
        return (projectCategoryIds == null || projectCategoryIds.isEmpty()) &&
               (projectIds == null || projectIds.isEmpty());
    }

    public boolean isForAnyIssueTypes()
    {
        return (issueTypeIds == null || issueTypeIds.isEmpty());
    }

    public boolean isSingleProjectContext()
    {
        return getProjectIds() != null && getProjectIds().size() == 1;
    }

    @Override
    public Project getSingleProject()
    {
        if (isSingleProjectContext())
        {
            return projectManager.getProjectObj(getProjectIds().get(0));
        }
        else
        {
            throw new IllegalStateException("This is not a single project context");
        }
    }

    public List getProjectCategoryIds()
    {
        return projectCategoryIds;
    }

    private void setProjectCategoryIds(List projectCategoryIds)
    {
        this.projectCategoryIds = prepareProjectList(projectCategoryIds);
    }

    public List<Long> getProjectIds()
    {
        return projectIds;
    }

    private void setProjectIds(List projectIds)
    {
        this.projectIds = prepareProjectList(projectIds);
        this.projects = null;
    }

    @Override
    public GenericValue getOnlyProject()
    {
        if (isSingleProjectContext())
        {
            Long projectId = getProjectIds().get(0);
            return projectManager.getProject(projectId);
        }
        else
        {
            log.warn("Trying to get the only the project but is not a single project context. Project ids are: " + getProjectIds());
            return null;
        }
    }

    public List<String> getIssueTypeIds()
    {
        return issueTypeIds;
    }

    public List<IssueContext> getAsIssueContexts()
    {
        List<IssueContext> issueContexts = new ArrayList<IssueContext>();
        List<Long> projectIds = (getProjectIds() != null && !getProjectIds().isEmpty()) ? getProjectIds() : ALL_PROJECTS;
        for (Long projectId : projectIds)
        {
            List<String> issueTypeIds = (getIssueTypeIds() != null && !getIssueTypeIds().isEmpty()) ? getIssueTypeIds() : ALL_ISSUE_TYPES;
            for (String issueTypeId : issueTypeIds)
            {
                issueContexts.add(new IssueContextImpl(projectId, issueTypeId));
            }
        }
        return issueContexts;
    }

    public void verify()
    {
        if (projectIds != null && !projectIds.isEmpty())
        {
            for (Iterator iterator = projectIds.iterator(); iterator.hasNext();)
            {
                Long projectId = (Long) iterator.next();
                if (projectManager.getProject(projectId) == null)
                {
                    log.warn("Project id " + projectId + " found in searchContext but is not valid. Being removed.");
                    iterator.remove();
                }
            }
        }

        if (issueTypeIds != null && !issueTypeIds.isEmpty())
        {
            for (Iterator iterator = issueTypeIds.iterator(); iterator.hasNext();)
            {
                String issueTypeId = (String) iterator.next();
                if (constantsManager.getIssueType(issueTypeId) == null)
                {
                    log.warn("Issue type id " + issueTypeId + " found in searchContext but is not valid. Being removed.");
                    iterator.remove();
                }
            }
        }
    }

    private void setIssueTypeIds(List issueTypeIds)
    {
        this.issueTypeIds = ListUtils.predicatedList(constantsManager.expandIssueTypeIds(issueTypeIds), STRING_PREDICATE);
        this.issueTypes = null;
    }

    // -------------------------------------------------------------------------------------------------- Helper methods
    private static List<Long> prepareProjectList(List list)
    {
        if (list == null)
        {
            return Collections.emptyList();
        }
        else if (list.size() == 1 && !ObjectUtils.isValueSelected(list.get(0)))
        {
            return Collections.emptyList();
        }
        else
        {
            return ListUtils.predicatedList(list, LONG_PREDICATE);
        }
    }

    public String toString()
    {
        return new ToStringBuilder(this)
                .append("projectCategoryIds", getProjectCategoryIds())
                .append("projectIds", getProjectIds())
                .append("issueTypeIds", getIssueTypeIds())
                .toString();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SearchContextImpl))
        {
            return false;
        }
        SearchContextImpl rhs = (SearchContextImpl) o;
        return new EqualsBuilder()
                .append(getProjectCategoryIds(), rhs.getProjectCategoryIds())
                .append(getProjectIds(), rhs.getProjectIds())
                .append(getIssueTypeIds(), rhs.getIssueTypeIds())
                .isEquals();
    }

    public int hashCode()
    {
        return new HashCodeBuilder(37, 47)
                .append(getProjectCategoryIds())
                .append(getProjectIds())
                .append(getIssueTypeIds())
                .toHashCode();
    }

    @Override
    public List<Project> getProjects()
    {
        if (null == projectIds)
        {
            return null;
        }
        if (null == projects)
        {
            projects = Lists.transform(projectIds, new Function<Long, Project>() {
                @Override
                public Project apply(@Nullable Long projectId)
                {
                    return projectManager.getProjectObj(projectId);
                }
            });
        }
        return projects;
    }

    @Override
    public List<IssueType> getIssueTypes()
    {
        if (null == issueTypeIds)
        {
            return null;
        }
        if (null == issueTypes)
        {
            issueTypes = Lists.transform(issueTypeIds, new Function<String, IssueType>() {
                @Override
                public IssueType apply(@Nullable String issueTypeId)
                {
                    return constantsManager.getIssueTypeObject(issueTypeId);
                }
            });
        }
        return issueTypes;
    }
}
