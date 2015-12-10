package com.atlassian.jira.config;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.resolution.ResolutionImpl;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public class DefaultResolutionManager extends AbstractIssueConstantsManager<Resolution> implements ResolutionManager, Startable
{
    private static final String CREATION_LOCK_NAME = DefaultResolutionManager.class.getName() + ".creationLock";
    private final ClusterLockService clusterLockService;
    private static final Logger log = Logger.getLogger(DefaultResolutionManager.class);

    private final ApplicationProperties applicationProperties;
    private final IssueConstantFactory factory;
    private ClusterLock creationLock;

    public DefaultResolutionManager(final ConstantsManager constantsManager, IssueIndexManager issueIndexManager,
            OfBizDelegator ofBizDelegator, ApplicationProperties applicationProperties, IssueConstantFactory factory, final ClusterLockService clusterLockService)
    {
        super(constantsManager, ofBizDelegator, issueIndexManager);
        this.applicationProperties = applicationProperties;
        this.factory = factory;
        this.clusterLockService = clusterLockService;
    }

    @Override
    public void start()
    {
        creationLock = clusterLockService.getLockForName(CREATION_LOCK_NAME);
    }

    @Override
    public Resolution createResolution(final String name, final String description)
    {
        Assertions.notBlank("name", name);
        creationLock.lock();
        try
        {
            for (Resolution resolution : constantsManager.getResolutionObjects())
            {
                if (name.trim().equalsIgnoreCase(resolution.getName()))
                {
                    throw new IllegalStateException("A resolution with the name '" + name + "' already exists.");
                }
            }
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("name", name);
            fields.put("description", description);
            fields.put("id", getNextStringId());
            fields.put("sequence", getMaxSequenceNo() + 1);
            GenericValue resolutionGenericValue = createConstant(fields);
            return factory.createResolution(resolutionGenericValue);
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException("Failed to create a resolution with name '" + name + "'", ex);
        }
        finally
        {
            creationLock.unlock();
            clearCaches();
        }
    }

    @Override
    public void editResolution(Resolution resolution, String name, String description)
    {
        Assertions.notNull("resolution", resolution);
        Assertions.notBlank("name", name);
        for (Resolution res : getResolutions())
        {
            if (name.equalsIgnoreCase(res.getName()) && !resolution.getId().equals(res.getId()))
            {
                throw new IllegalArgumentException("Cannot rename resolution. A resolution with the name '" + name + "' exists already.");
            }
        }
        try
        {
            ResolutionImpl updatedResolution = (ResolutionImpl) factory.createResolution(resolution.getGenericValue());
            updatedResolution.setName(name);
            updatedResolution.setDescription(description);
            updatedResolution.getGenericValue().store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to update resolution with name '" + resolution.getName() + "'", e);
        }
        finally
        {
            clearCaches();
        }
    }

    @Override
    public List<Resolution> getResolutions()
    {
        return Lists.newArrayList(constantsManager.getResolutionObjects());
    }

    @Override
    public void removeResolution(String resolutionId, String newResolutionId)
    {
        Assertions.notBlank("resolutionId", resolutionId);
        Assertions.notBlank("newResolutionId", newResolutionId);
        Resolution resolution = getResolution(resolutionId);
        if (resolution == null)
        {
            throw new IllegalArgumentException("A resolution with the name '" + resolutionId + "' does not exist.");
        }
        Resolution newResolution = getResolution(newResolutionId);
        if (newResolution == null)
        {
            throw new IllegalArgumentException("A resolution with the name '" + newResolutionId + "' does not exist.");
        }
        try
        {
            removeConstant(IssueFieldConstants.RESOLUTION, resolution, newResolution.getId());
        }
        catch (Exception ex)
        {
            throw new DataAccessException("Failed to remove resolution with id '" + resolutionId + "'", ex);
        }
    }

    @Override
    public Resolution getResolutionByName(String name)
    {
        for (Resolution resolution : getResolutions())
        {
            if (resolution.getName().equalsIgnoreCase(name))
            {
                return resolution;
            }
        }
        return null;
    }

    @Override
    public Resolution getResolution(String id)
    {
        return constantsManager.getResolutionObject(id);
    }

    @Override
    public void moveResolutionUp(String id)
    {
        Resolution resolution = checkResolutionExists(id);
        moveUp(resolution);
    }

    @Override
    public void moveResolutionDown(String id)
    {
        Resolution resolution = checkResolutionExists(id);
        moveDown(resolution);
    }

    @Override
    public void setDefaultResolution(String id)
    {
        if (id != null)
        {
            checkResolutionExists(id);
        }
        applicationProperties.setString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION, id);
    }

    @Override
    public Resolution getDefaultResolution()
    {
        String defaultResolutionId = applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION);
        if (StringUtils.isNotEmpty(defaultResolutionId))
        {
            Resolution resolution = getResolution(defaultResolutionId);
            if (resolution == null)
            {
                log.warn("Default resolution with id '" + defaultResolutionId + "' does not exist.");
            }
            return resolution;
        }
        return null;
    }

    private Resolution checkResolutionExists(String id)
    {
        Assertions.notBlank("id", id);
        Resolution resolution = getResolution(id);
        if (resolution == null)
        {
            throw new IllegalArgumentException("A resolution with the '" + id + "' does not exist.");
        }
        return resolution;
    }

    @Override
    protected void postProcess(Resolution resolution)
    {
        if (resolution.getId().equals(applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION)))
        {
            applicationProperties.setString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION, null);
        }
    }

    @Override
    protected void clearCaches()
    {
        constantsManager.refreshResolutions();
    }

    @Override
    protected String getIssueConstantField()
    {
        return ConstantsManager.RESOLUTION_CONSTANT_TYPE;
    }

    @Override
    protected List<Resolution> getAllValues()
    {
        return getResolutions();
    }

}
