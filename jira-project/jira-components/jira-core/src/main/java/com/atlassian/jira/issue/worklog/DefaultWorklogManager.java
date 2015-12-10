package com.atlassian.jira.issue.worklog;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.EntityFactory;
import com.atlassian.jira.entity.EntityPagedList;
import com.atlassian.jira.entity.WorklogEntityFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.collect.PagedList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;

import java.util.List;

public class DefaultWorklogManager implements WorklogManager
{
    private ProjectRoleManager projectRoleManager;
    private final WorklogStore worklogStore;
    private final TimeTrackingIssueUpdater timeTrackingIssueUpdater;

    public DefaultWorklogManager(ProjectRoleManager projectRoleManager, WorklogStore worklogStore, TimeTrackingIssueUpdater timeTrackingIssueUpdater)
    {
        this.projectRoleManager = projectRoleManager;
        this.worklogStore = worklogStore;
        this.timeTrackingIssueUpdater = timeTrackingIssueUpdater;
    }

    @Override
    public boolean delete(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklog(worklog, false);

        timeTrackingIssueUpdater.updateIssueOnWorklogDelete(user, worklog, newEstimate, dispatchEvent);

        return worklogStore.delete(worklog.getId());
    }

    @Override
    public Worklog create(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklog(worklog, true);

        Worklog newWorklog = worklogStore.create(worklog);

        // Update the issues time tracking fields
        timeTrackingIssueUpdater.updateIssueOnWorklogCreate(user, newWorklog, newEstimate, dispatchEvent);

        return newWorklog;
    }

    @Override
    public Worklog update(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklog(worklog, false);

        // We need to lookup the value as stored in the DB so that we can determine the original value of the
        // timeSpent so that we can correctly recalculate the issues total time spent field.
        Worklog originalWorklog = getById(worklog.getId());
        if(originalWorklog == null)
        {
            throw new IllegalArgumentException("Unable to find a worklog in the datastore for the provided id: '" + worklog.getId() + "'");
        }

        Long originalTimeSpent = originalWorklog.getTimeSpent();
        Worklog newWorklog = worklogStore.update(worklog);

        // Update the issues time tracking fields
        timeTrackingIssueUpdater.updateIssueOnWorklogUpdate(user, originalWorklog, newWorklog, originalTimeSpent, newEstimate, dispatchEvent);

        return newWorklog;
    }

    @Override
    public Worklog getById(Long id)
    {
        return worklogStore.getById(id);
    }

    @Override
    public List<Worklog> getByIssue(Issue issue)
    {
        if(issue == null)
        {
            throw new IllegalArgumentException("Cannot resolve worklogs for null issue.");
        }
        return worklogStore.getByIssue(issue);
    }

    @Override
    public PagedList<Worklog> getByIssue(final Issue issue, final int pageSize)
    {
        final WorklogEntityFactory worklogEntityFactory = new WorklogEntityFactory(issue);
        EntityCondition condition = new EntityFieldMap(ImmutableMap. <String, Object>of("issue", issue.getId()), EntityOperator.EQUALS);
        List<String> orderBy = Lists.newArrayList("created");
        return new EntityPagedList<Worklog> (pageSize, worklogEntityFactory, condition, orderBy );
    }

    @Override
    public int swapWorklogGroupRestriction(String groupName, String swapGroup)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        if (swapGroup == null)
        {
            throw new IllegalArgumentException("You must provide a non null swap group name.");
        }

        return worklogStore.swapWorklogGroupRestriction(groupName, swapGroup);
    }

    @Override
    public long getCountForWorklogsRestrictedByGroup(String groupName)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }
        return worklogStore.getCountForWorklogsRestrictedByGroup(groupName);
    }
    
    @Override
    public ProjectRole getProjectRole(Long projectRoleId)
    {
        return projectRoleManager.getProjectRole(projectRoleId);
    }

    void validateWorklog(Worklog worklog, boolean create)
    {
        if(worklog == null)
        {
            throw new IllegalArgumentException("Worklog must not be null.");
        }

        if(worklog.getIssue() == null)
        {
            throw new IllegalArgumentException("The worklogs issue must not be null.");
        }

        if(!create && worklog.getId() == null)
        {
            throw new IllegalArgumentException("Can not modify a worklog with a null id.");
        }
    }

}
