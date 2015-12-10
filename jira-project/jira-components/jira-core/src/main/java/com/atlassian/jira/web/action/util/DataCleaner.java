package com.atlassian.jira.web.action.util;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.StringUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Cleans issue fields and comments of characters that won't export in XML (mainly control characters).
 * Derived from the abomination that was the DataClean back end action.
 *
 * @since v4.4
 */
class DataCleaner
{
    private static final Logger log = Logger.getLogger(DataCleaner.class);
    private ApplicationProperties applicationProperties;
    private OfBizDelegator ofBizDelegator;

    DataCleaner(ApplicationProperties applicationProperties, OfBizDelegator ofBizDelegator)
    {
        this.applicationProperties = applicationProperties;
        this.ofBizDelegator = ofBizDelegator;
    }

    void clean() throws DataAccessException
    {

        List<GenericValue> issues = ofBizDelegator.findAll("Issue");
        List<GenericValue> comments = ofBizDelegator.findByAnd("Action", EasyMap.build("type", "comment"));

        for (GenericValue issue : issues)
        {
            escapeField(issue, "summary");
            escapeField(issue, "environment");
            escapeField(issue, "description");

            // change items
            List<GenericValue> changeGroups = ofBizDelegator.findByAnd("ChangeGroup", EasyMap.build("issue", issue.getLong("id")), asList("created"));
            for (GenericValue changeGroup : changeGroups)
            {
                for (GenericValue changeItem : ofBizDelegator.getRelated("ChildChangeItem", changeGroup))
                {
                    escapeField(changeItem, "oldstring");
                    escapeField(changeItem, "newstring");
                }
            }
        }

        for (GenericValue comment : comments)
        {
            escapeField(comment, "body");
        }

        List<GenericValue> customFieldValues = ofBizDelegator.findAll("CustomFieldValue");
        for (GenericValue customFieldValue : customFieldValues)
        {
            escapeField(customFieldValue, "stringvalue");
        }

        List<GenericValue> changeItems = ofBizDelegator.findAll("ChangeItem");
        for (GenericValue changeItem : changeItems)
        {
            escapeField(changeItem, "oldstring");
            escapeField(changeItem, "newstring");
        }
    }

    private String getEncoding()
    {
        try
        {
            return applicationProperties.getEncoding();
        }
        catch (Exception e)
        {
            return "UTF-8";
        }
    }

    private String escapeString(String s)
    {
        return StringUtils.escapeCP1252(s, getEncoding());
    }

    private void escapeField(GenericValue gv, String fieldName)
    {
        String fieldValue = gv.getString(fieldName);
        if (TextUtils.stringSet(fieldValue))
        {
            String escapedValue = escapeString(fieldValue);
            if (!fieldValue.equals(escapedValue))
            {
                gv.set(fieldName, escapeString(fieldValue));
                try
                {
                    CoreFactory.getGenericDelegator().storeAll(asList(gv));
                }
                catch (GenericEntityException e)
                {
                    log.error("Error storing entity " + gv +" while escaping field " + fieldName, e);
                }
            }
        }
    }
}
