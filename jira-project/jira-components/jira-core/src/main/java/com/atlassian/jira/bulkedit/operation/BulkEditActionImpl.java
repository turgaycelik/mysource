/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.log4j.Logger;

public class BulkEditActionImpl implements BulkEditAction
{
    private static final Logger log = Logger.getLogger(BulkEditActionImpl.class);
    private String orderableFieldId;
    private final FieldManager fieldManager;
    private final JiraAuthenticationContext authenticationContext;
    // uSed to record whether the action has been checked for validity
    private boolean initialised;
    private String unavailableMessage;

    public BulkEditActionImpl(String orderableFieldId, FieldManager fieldManager, JiraAuthenticationContext authenticationContext)
    {
        this.orderableFieldId = orderableFieldId;
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
        this.initialised = false;
    }

    public boolean isAvailable(BulkEditBean bulkEditBean)
    {
        if (!initialised)
        {
            setUnavailableMessage(getField().availableForBulkEdit(bulkEditBean));
            initialised = true;
        }

        // If the field returns a null 'unavailable' message then it is available.
        return getUnavailableMessage() == null;
    }

    public OrderableField getField()
    {
        return fieldManager.getOrderableField(orderableFieldId);
    }

    public String getFieldName()
    {
        if (fieldManager.isCustomField(getField()))
        {
            return fieldManager.getCustomField(orderableFieldId).getFieldName();
        }
        else
        {
            return authenticationContext.getI18nHelper().getText(getField().getNameKey());
        }
    }

    public String getUnavailableMessage()
    {
        return unavailableMessage;
    }

    protected void setUnavailableMessage(String unavailableMessage)
    {
        this.unavailableMessage = unavailableMessage;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof BulkEditAction)) return false;

        final BulkEditAction bulkEditAction = (BulkEditAction) o;

        if (getField() != null ? !getField().equals(bulkEditAction.getField()) : bulkEditAction.getField() != null) return false;
        if (unavailableMessage != null ? !unavailableMessage.equals(bulkEditAction.getUnavailableMessage()) : bulkEditAction.getUnavailableMessage() != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (orderableFieldId != null ? orderableFieldId.hashCode() : 0);
        result = 29 * result + (unavailableMessage != null ? unavailableMessage.hashCode() : 0);
        return result;
    }
}
