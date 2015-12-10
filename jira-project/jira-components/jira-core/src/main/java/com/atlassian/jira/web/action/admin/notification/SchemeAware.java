/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public interface SchemeAware
{
    Long getSchemeId();

    void setSchemeId(Long schemeId);

    GenericValue getScheme() throws GenericEntityException;
}
