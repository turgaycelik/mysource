/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.issue.fields.OrderableField;

public interface BulkEditAction
{
    public boolean isAvailable(BulkEditBean bulkEditBean);

    public String getUnavailableMessage();

    public OrderableField getField();

    public String getFieldName();
}
