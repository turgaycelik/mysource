package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * This upgrade task creates the notification event for the
 * {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENT_DELETED_ID} event.
 */
public class UpgradeTask_Build801 extends AbstractNotificationSchemeUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build801.class);

    public static final String ID_STRING = "id";
    public static final String NAME_STRING = "name";
    public static final String DESC_STRING = "description";
    public static final String TYPE_STRING = "type";

    private OfBizDelegator ofBizDelegator;
    private EventTypeManager eventTypeManager;
    static final String ISSUE_COMMENT_DELETED_NAME_KEY = "event.type.issuecommentdeleted.name";
    static final String ISSUE_COMMENT_DELETED_DESC_KEY = "event.type.issuecommentdeleted.desc";

    static final String ISSUE_COMMENT_DELETED_NAME = "Issue Comment Deleted";
    static final String ISSUE_COMMENT_DELETED_DESC = "This is the 'Issue Comment Deleted' event type.";

    /**
     * Constructs a new instance with given {@link com.atlassian.jira.ofbiz.OfBizDelegator} and {@link com.atlassian.jira.event.type.EventTypeManager}.
     *
     * @param ofBizDelegator   OFBiz delegator
     * @param eventTypeManager event type manager
     */
    public UpgradeTask_Build801(OfBizDelegator ofBizDelegator, EventTypeManager eventTypeManager, NotificationSchemeManager notificationSchemeManager)
    {
        super(notificationSchemeManager);
        this.ofBizDelegator = ofBizDelegator;
        this.eventTypeManager = eventTypeManager;
    }

    public String getShortDescription()
    {
        return "Creates the EventTypes for the ISSUE_COMMENT_DELETED events.";
    }

    public String getBuildNumber()
    {
        return "801";
    }

    /**
     * Runs the core task which is to create the new Issue Comment Edited and Issue Comment Deleted event type and
     * update the eventTypeManager with this change
     *
     * @throws com.atlassian.jira.exception.DataAccessException
     *          if something goes wrong with database access
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode) throws DataAccessException, GenericEntityException
    {
        try
        {
            GenericValue value = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build(ID_STRING, EventType.ISSUE_COMMENT_DELETED_ID));
            if (value == null)
            {
                // Create the notification event for the issue comment deleted event
                Map eventTypeParamasMap = EasyMap.build(
                        ID_STRING, EventType.ISSUE_COMMENT_DELETED_ID,
                        NAME_STRING, getI18nTextWithDefault(ISSUE_COMMENT_DELETED_NAME_KEY, ISSUE_COMMENT_DELETED_NAME),
                        DESC_STRING, getI18nTextWithDefault(ISSUE_COMMENT_DELETED_DESC_KEY, ISSUE_COMMENT_DELETED_DESC),
                        TYPE_STRING, EventType.JIRA_SYSTEM_EVENT_TYPE);
                ofBizDelegator.createValue(EventType.EVENT_TYPE, eventTypeParamasMap);
            }
            else
            {
                log.warn("Not creating 'Comment Deleted' event as it already exists.  This should only happen if this upgrade task is run twice.");
            }
        }
        catch (DataAccessException e)
        {
            log.error("JIRA was unable to create the new notification event type of 'Issue Comment Deleted' with an id of 16.");
            throw e;
        }

        // Clear the cache of the manager
        eventTypeManager.clearCache();

        try
        {
            doUpgrade(EventType.ISSUE_UPDATED_ID, EventType.ISSUE_COMMENT_DELETED_ID);
        }
        catch (GenericEntityException e)
        {
            log.error("Unable to retrieve all notification schemes.", e);
            throw e;
        }

    }

    private String getI18nTextWithDefault(String key, String defaultResult)
    {
        String result = getApplicationI18n().getText(key);
        if (result.equals(key))
        {
            return defaultResult;
        }
        else
        {
            return result;
        }
    }

    I18nHelper getApplicationI18n()
    {
        return new I18nBean();
    }
}
