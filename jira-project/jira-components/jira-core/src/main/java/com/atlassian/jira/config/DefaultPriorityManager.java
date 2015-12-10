package com.atlassian.jira.config;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.priority.PriorityImpl;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public class DefaultPriorityManager extends AbstractIssueConstantsManager<Priority> implements PriorityManager, Startable
{
    private static final String CREATION_LOCK_NAME = DefaultPriorityManager.class.getName() + ".creationLock";
    private final ApplicationProperties applicationProperties;
    private final IssueConstantFactory issueConstantFactory;
    private final ClusterLockService clusterLockService;
    private ClusterLock creationLock;

    private static final Logger log = Logger.getLogger(DefaultPriorityManager.class);

    public DefaultPriorityManager(ConstantsManager constantsManager, OfBizDelegator ofBizDelegator,
            IssueIndexManager issueIndexManager, ApplicationProperties applicationProperties,
            IssueConstantFactory issueConstantFactory, final ClusterLockService clusterLockService)
    {
        super(constantsManager, ofBizDelegator, issueIndexManager);
        this.applicationProperties = applicationProperties;
        this.issueConstantFactory = issueConstantFactory;
        this.clusterLockService = clusterLockService;
    }

    @Override
    public void start()
    {
        creationLock = clusterLockService.getLockForName(CREATION_LOCK_NAME);
    }

    @Override
    public Priority createPriority(String name, String description, String iconUrl, String color)
    {
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        Assertions.notBlank("color", color);

        creationLock.lock();
        try
        {
            for (Priority priority : constantsManager.getPriorityObjects())
            {
                if (name.trim().equalsIgnoreCase(priority.getName()))
                {
                    throw new IllegalStateException("A priority with the name '" + name + "' already exists.");
                }
            }
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("name", name);
            fields.put("description", description);
            fields.put("id", getNextStringId());
            fields.put("iconurl", iconUrl);
            fields.put("statusColor", color);
            fields.put("sequence", getMaxSequenceNo() + 1);

            return issueConstantFactory.createPriority(createConstant(fields));
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException("Failed to create a priority with name '" + name + "'", ex);
        }
        finally
        {
            creationLock.unlock();
            constantsManager.refreshPriorities();
        }
    }

    @Override
    public void editPriority(Priority priority, String name, String description, String iconUrl, String color)
    {
        Assertions.notNull("priority", priority);
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        Assertions.notBlank("color", color);
        for (Priority prio : getPriorities())
        {
            if (name.equalsIgnoreCase(prio.getName()) && !priority.getId().equals(prio.getId()))
            {
                throw new IllegalStateException("Cannot rename priority. A priority with the name '" + name + "' exists already.");
            }
        }
        try
        {
            PriorityImpl updatedPriority = (PriorityImpl) issueConstantFactory.createPriority(priority.getGenericValue());

            updatedPriority.setName(name);
            updatedPriority.setDescription(description);
            updatedPriority.setIconUrl(iconUrl);
            updatedPriority.setStatusColor(color);
            updatedPriority.getGenericValue().store();
            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to update priority '" + priority.getName() + "'", e);
        }
    }

    @Override
    public List<Priority> getPriorities()
    {
        return Lists.newArrayList(constantsManager.getPriorityObjects());
    }

    @Override
    public void removePriority(String id, String newPriorityId)
    {
        Assertions.notBlank("id", id);
        Assertions.notBlank("newPriorityId", newPriorityId);
        Priority priority = getPriority(id);
        if (priority == null)
        {
            throw new IllegalArgumentException("A priority with id '" + id + "' does not exist.");
        }
        Priority newPriority = getPriority(newPriorityId);
        if (newPriority == null)
        {
            throw new IllegalArgumentException("A priority with id '" + newPriorityId + "' does not exist.");
        }
        try
        {
            removeConstant(IssueFieldConstants.PRIORITY, priority, newPriority.getId());
        }
        catch (Exception ex)
        {
            throw new DataAccessException("Failed to remove priority '" + id + "'", ex);
        }
    }

    @Override
    public Priority getPriority(String id)
    {
        Assertions.notBlank("id", id);
        return constantsManager.getPriorityObject(id);
    }

    @Override
    public void setDefaultPriority(String id)
    {
        if (id != null && getPriority(id) == null)
        {
            throw new IllegalArgumentException("A priority with id '" + id + "' does not exist.");
        }
        applicationProperties.setString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY, id);
    }

    @Override
    public Priority getDefaultPriority()
    {
        String priorityId = applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY);
        if (StringUtils.isNotEmpty(priorityId))
        {
            Priority priority = getPriority(priorityId);
            if (priority == null)
            {
                log.warn("Default priority with id '" + priorityId + "' does not exist.");
            }
            return priority;
        }
        return null;
    }

    @Override
    public void movePriorityUp(String id)
    {
        Priority priority = getPriority(id);
        if (priority == null)
        {
            throw new IllegalArgumentException("A priority with id '" + id + "' does not exist.");
        }
        moveUp(priority);
    }

    @Override
    public void movePriorityDown(String id)
    {
        Priority priority = getPriority(id);
        if (priority == null)
        {
            throw new IllegalArgumentException("A priority with id '" + id + "' does not exist.");
        }
        moveDown(priority);
    }

    protected List<Priority> getValues()
    {
        return getPriorities();
    }

    @Override
    protected void postProcess(Priority priority)
    {
        if (priority.getId().equals(applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY)))
        {
            applicationProperties.setString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY, null);
        }
    }

    @Override
    protected void clearCaches()
    {
        constantsManager.refreshPriorities();
    }

    @Override
    protected String getIssueConstantField()
    {
        return ConstantsManager.PRIORITY_CONSTANT_TYPE;
    }

    @Override
    protected List<Priority> getAllValues()
    {
        return getPriorities();
    }
}
