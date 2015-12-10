package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * This upgrade task adds new scheme entities to the eventIdDestination of all notification schemes in JIRA. It uses
 * the eventIdSource scheme entities as a template for what to create.
 *
 * For example UpgradeTask_Build207 uses this to grant the Comment Edited notification event to all entities who
 * currently have the Issue Commented event.
 */
public abstract class AbstractNotificationSchemeUpgradeTask extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(AbstractNotificationSchemeUpgradeTask.class);

    private final NotificationSchemeManager notificationSchemeManager;

    public AbstractNotificationSchemeUpgradeTask(NotificationSchemeManager notificationSchemeManager)
    {
        super(false);
        this.notificationSchemeManager = notificationSchemeManager;
    }

    public void doUpgrade(Long eventIdSource, Long eventIdDestination) throws GenericEntityException
    {
        try
        {
            final List<GenericValue> schemes = notificationSchemeManager.getSchemes();
            for (final GenericValue schemeGV : schemes)
            {
                // Copy all the scheme entities registered for the passed in event
                List<GenericValue> entities = notificationSchemeManager.getEntities(schemeGV, eventIdSource);
                for (final GenericValue schemeEntity : entities)
                {
                    addSchemeEntityForDestinationNotification(schemeGV, schemeEntity, eventIdDestination);
                }
            }
        }
        catch (GenericEntityException e)
        {
            LOG.error("Unable to retrieve all notification schemes.", e);
            throw e;
        }
    }

    private void addSchemeEntityForDestinationNotification(GenericValue schemeGV, GenericValue origSchemeEntity, Long eventIdDestination)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("About to add notification for event id: '" + eventIdDestination + "' for '" + origSchemeEntity.getString("type") + "' to notification scheme '"
                    + schemeGV.getString("name") + "'");
        }

        SchemeEntity schemeEntity = new SchemeEntity(origSchemeEntity.getString("type") ,origSchemeEntity.getString("parameter"), eventIdDestination, origSchemeEntity.getString("templateId"));
        try
        {
            notificationSchemeManager.createSchemeEntity(schemeGV, schemeEntity);
        }
        catch (GenericEntityException e)
        {
            LOG.error("Failed to add notification for event id: '"+ eventIdDestination + "' for '" + schemeEntity + "' to notification scheme '"
                    + schemeGV.getString("name") + "'!");
        }
    }
}
