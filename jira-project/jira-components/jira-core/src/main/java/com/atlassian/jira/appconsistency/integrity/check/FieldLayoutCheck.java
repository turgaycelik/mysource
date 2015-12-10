package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FieldLayoutCheck extends CheckImpl
{
    private final static String CUSTOM_FIELD_STRING = "customfield";
    private FieldManager fieldManager;
    public FieldLayoutCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
        fieldManager = ComponentAccessor.getFieldManager();

    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.field.layout.check.desc");
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }

    // Ensure that the field layout item table does not contain references to custom fields that have been deleted.
    private List doCheck(boolean correct) throws IntegrityException
    {
        List results = new ArrayList();
        String message;
        List<GenericValue> fieldLayoutItems;
        HashSet<String> fieldsToRemove = new HashSet<String>();

        try
        {
            fieldLayoutItems = ofBizDelegator.findAll("FieldLayoutItem");

            for (final GenericValue fieldLayoutItemGV : fieldLayoutItems)
            {
                String fieldLayoutItemId = fieldLayoutItemGV.getString("fieldidentifier");
                if (TextUtils.stringSet(fieldLayoutItemId) && fieldLayoutItemId.startsWith(CUSTOM_FIELD_STRING))
                {
                    if (!fieldManager.isOrderableField(fieldLayoutItemId))
                    {
                        if (correct)
                        {
                            // Add to set of id references to remove
                            fieldsToRemove.add(fieldLayoutItemId);
                        }
                        else
                        {
                            // Just record the problem
                            message = getI18NBean().getText("admin.integrity.check.field.layout.check.preview", fieldLayoutItemGV.getLong("id").toString(), fieldLayoutItemId);
                            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4423"));
                        }
                    }
                }
            }

            if (correct)
            {
                // Fix the problem
                for (final String fieldLayoutItemId : fieldsToRemove)
                {
                    ofBizDelegator.removeByAnd("FieldLayoutItem", FieldMap.build("fieldidentifier", fieldLayoutItemId));

                    message = getI18NBean().getText("admin.integrity.check.field.layout.check.message", fieldLayoutItemId);
                    results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4423"));
                }
            }
        }
        catch (Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }

        return results;
    }

    public void setFieldManager(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
    }
}
