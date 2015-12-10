package com.atlassian.jira.security.util;

import com.atlassian.jira.notification.NotificationSchemeManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: owenfellows
 * Date: 02-Aug-2004
 * Time: 14:46:41
 * To change this template use File | Settings | File Templates.
 */
public class GroupToNotificationSchemeMapper extends AbstractGroupToSchemeMapper
{
    public static final String GROUP_DROPDOWN = "Group_Dropdown";

    public GroupToNotificationSchemeMapper(NotificationSchemeManager notificationSchemeManager) throws GenericEntityException
    {
        super(notificationSchemeManager);
    }

    /**
     * Go through all the Notification Schemes and create a Map, where the key is the group name
     * and values are Sets of Schemes
     */
    protected Map init() throws GenericEntityException
    {
        Map mapping = new HashMap();

        // Get all Permission Schmes
        final List<GenericValue> schemes = getSchemeManager().getSchemes();
        for (GenericValue notificationScheme : schemes)
        {
            final List<GenericValue> entities = getSchemeManager().getEntities(notificationScheme);
            for (GenericValue notificationRecord : entities)
            {
                if (GROUP_DROPDOWN.equals(notificationRecord.getString("type")))
                {
                    addEntry(mapping, notificationRecord.getString("parameter"), notificationScheme);
                }
            }
        }

        return mapping;
    }
}
