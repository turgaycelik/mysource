package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.BulkEditBean;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class UnavailableBulkEditAction implements BulkEditAction
{
    private final String fieldNameKey;
    private final String unavailableMessage;
    private final JiraAuthenticationContext authenticationContext;

    public UnavailableBulkEditAction(String fieldNameKey, String unavailableMessage, JiraAuthenticationContext authenticationContext)
    {
        this.fieldNameKey = fieldNameKey;
        this.unavailableMessage = unavailableMessage;
        this.authenticationContext = authenticationContext;
    }

    public boolean isAvailable(BulkEditBean bulkEditBean)
    {
        return false;
    }

    public String getUnavailableMessage()
    {
        return unavailableMessage;
    }

    public String getName()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public OrderableField getField()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getFieldName()
    {
        return authenticationContext.getI18nHelper().getText(fieldNameKey);
    }
}
