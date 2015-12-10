package com.atlassian.jira.user.util;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * This class provides methods for counting both internal users and external users (if external user mgmt is enabled) It
 * also provides methods for updating global preferences.
 */
public class GlobalUserPreferencesUtilImpl implements GlobalUserPreferencesUtil
{
    private final OfBizDelegator ofBizDelegator;
    private final UserPreferencesManager userPreferencesManager;

    public GlobalUserPreferencesUtilImpl(OfBizDelegator ofBizDelegator, final UserPreferencesManager userPreferencesManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.userPreferencesManager = userPreferencesManager;
    }

    public long getTotalUpdateUserCountMailMimeType(String mimetype)
    {
        List<GenericValue> valExternalEntity = getEntriesForMailWithMimetype(mimetype);
        return valExternalEntity.size();
    }

    public void updateUserMailMimetypePreference(String mimetype) throws GenericEntityException
    {
        // get all the ids of the values that need updating.
        List<GenericValue> vals = getEntriesForMailWithMimetype(mimetype);
        List<Long> keys = new ArrayList<Long>();
        for (GenericValue genericValue : vals)
        {
            keys.add(genericValue.getLong("id"));
        }

        // update the values
        ofBizDelegator.bulkUpdateByPrimaryKey("OSPropertyString", FieldMap.build("value", mimetype), keys);

        // make sure we clear the user property cache.
        userPreferencesManager.clearCache();
    }

    private List<GenericValue> getEntriesForMailWithMimetype(String mimetype)
    {
        String updateMimetype = (mimetype.equalsIgnoreCase("html")) ? "text" : "html";
        List<GenericValue> matches = new ArrayList<GenericValue>();
        // JRA-8526 - this can not be as efficient as it should be cause some db's (mssql) can not do an ='s compare
        // on the propertyValue's data type
        List<GenericValue> vals = ofBizDelegator.findByAnd("OSUserPropertySetView",
                FieldMap.build("propertyKey", "user.notifications.mimetype", "entityName", Entity.APPLICATION_USER.getEntityName()));
        for (GenericValue genericValue : vals)
        {
            if (updateMimetype.equals(genericValue.getString("propertyValue")))
            {
                matches.add(genericValue);
            }
        }
        return matches;
    }
}
