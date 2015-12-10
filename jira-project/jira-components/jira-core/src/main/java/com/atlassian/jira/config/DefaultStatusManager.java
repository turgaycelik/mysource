package com.atlassian.jira.config;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.StatusImpl;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public class DefaultStatusManager extends AbstractIssueConstantsManager<Status> implements StatusManager, Startable
{
    private static final String CREATION_LOCK_NAME = DefaultStatusManager.class.getName() + ".creationLock";
    private final WorkflowManager workflowManager;
    private final IssueConstantFactory factory;
    private final StatusCategoryManager statusCategoryManager;
    private final ClusterLockService clusterLockService;
    private ClusterLock creationLock;

    // New statuses are given ids starting from 10000 - avoids conflict with future system statuses.
    private static final Long NEW_STATUS_START_ID = 10000L;

    public DefaultStatusManager(ConstantsManager constantsManager, OfBizDelegator ofBizDelegator,
            IssueIndexManager issueIndexManager, WorkflowManager workflowManager, IssueConstantFactory factory, StatusCategoryManager statusCategoryManager, final ClusterLockService clusterLockService)
    {
        super(constantsManager, ofBizDelegator, issueIndexManager);
        this.workflowManager = workflowManager;
        this.factory = factory;
        this.statusCategoryManager = statusCategoryManager;
        this.clusterLockService = clusterLockService;
    }

    @Override
    public void start()
    {
        creationLock = clusterLockService.getLockForName(CREATION_LOCK_NAME);
    }

    @Override
    public Status createStatus(String name, String description, String iconUrl, StatusCategory statusCategory)
    {
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        Assertions.notNull("statusCategory", statusCategory);
        creationLock.lock();
        try
        {
            for (Status status : constantsManager.getStatusObjects())
            {
                if (name.trim().equalsIgnoreCase(status.getName()))
                {
                    throw new DataAccessException("A status with the name '" + name + "' already exists.");
                }
            }
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("name", name);
            fields.put("description", description);
            fields.put("iconurl", iconUrl);
            fields.put("sequence", getMaxSequenceNo() + 1);
            fields.put("statuscategory", statusCategory.getId());
            String nextStringId = getNextStringId();
            Long nextId = Long.valueOf(nextStringId);
            if (nextId < NEW_STATUS_START_ID)
            {
                fields.put("id", NEW_STATUS_START_ID.toString());
            }
            else
            {
                fields.put("id", nextStringId);
            }
            GenericValue statusGv = createConstant(fields);
            return factory.createStatus(statusGv);
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException("Failed to create new status with name '" + name + "'", ex);
        }
        finally
        {
            creationLock.unlock();
            clearCaches();
        }
    }

    @Override
    public Status createStatus(final String name, final String description, final String iconUrl)
    {
        return createStatus(name, description, iconUrl, statusCategoryManager.getDefaultStatusCategory());
    }

    @Override
    public void editStatus(Status status, String name, String description, String iconUrl, StatusCategory statusCategory)
    {
        Assertions.notNull("status", status);
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        Assertions.notNull("statusCategory", statusCategory);
        for (Status st : getStatuses())
        {
            if (name.equalsIgnoreCase(st.getName()) && !status.getId().equals(st.getId()))
            {
                throw new IllegalStateException("Cannot rename status. A status with the name '" + name + "' exists already.");
            }
        }
        try
        {
            StatusImpl updatedStatus = (StatusImpl) factory.createStatus(status.getGenericValue());
            updatedStatus.setName(name);
            updatedStatus.setIconUrl(iconUrl);
            updatedStatus.setDescription(description);
            updatedStatus.setStatusCategory(statusCategory);
            updatedStatus.getGenericValue().store();
            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to update status '" + status.getName() + "'", e);
        }
    }

    @Override
    public void editStatus(final Status status, final String name, final String description, final String iconUrl)
    {
        Assertions.notNull("status", status);
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        for (Status st : getStatuses())
        {
            if (name.equalsIgnoreCase(st.getName()) && !status.getId().equals(st.getId()))
            {
                throw new IllegalStateException("Cannot rename status. A status with the name '" + name + "' exists already.");
            }
        }
        try
        {
            StatusImpl updatedStatus = (StatusImpl) factory.createStatus(status.getGenericValue());
            updatedStatus.setName(name);
            updatedStatus.setIconUrl(iconUrl);
            updatedStatus.setDescription(description);
            updatedStatus.getGenericValue().store();
            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to update status '" + status.getName() + "'", e);
        }
    }

    @Override
    public Collection<Status> getStatuses()
    {
        return constantsManager.getStatusObjects();
    }

    @Override
    public void removeStatus(final String id)
    {
        Status status = getStatusOrThrowIllegalArgumentException(id);
        final List<JiraWorkflow> existingWorkflows = workflowManager.getWorkflowsIncludingDrafts();
        for (JiraWorkflow workflow : existingWorkflows)
        {
            Collection linkStatuses = workflow.getLinkedStatuses();
            if (linkStatuses.contains(status.getGenericValue()))
            {
                throw new IllegalStateException("Cannot delete a status which is associated with a workflow. Status is associated with workflow " + workflow.getName());
            }
        }
        try
        {
            removeConstant(getIssueConstantField(), status, null);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to remove status with id '" + id + "'", e);
        }
        catch (IndexException e)
        {
            throw new DataAccessException("Failed to remove status with id '" + id + "'", e);
        }
    }

    @Override
    public Status getStatus(String id)
    {
        Assertions.notBlank("id", id);
        return constantsManager.getStatusObject(id);
    }

    @Override
    public void moveStatusUp(final String id)
    {
        Status status = getStatusOrThrowIllegalArgumentException(id);
        moveUp(status);
    }

    @Override
    public void moveStatusDown(final String id)
    {
        Status status = getStatusOrThrowIllegalArgumentException(id);
        moveDown(status);
    }

    private Status getStatusOrThrowIllegalArgumentException(final String id)
    {
        Status status = getStatus(id);
        if (status == null)
        {
            throw new IllegalArgumentException("A status with id '" + id + "' does not exist.");
        }
        return status;
    }

    @Override
    protected void postProcess(Status constant)
    {
    }

    @Override
    protected void clearCaches()
    {
        constantsManager.refreshStatuses();
    }

    @Override
    protected String getIssueConstantField()
    {
        return ConstantsManager.STATUS_CONSTANT_TYPE;
    }

    @Override
    protected List<Status> getAllValues()
    {
        return Lists.newArrayList(getStatuses());
    }
}
