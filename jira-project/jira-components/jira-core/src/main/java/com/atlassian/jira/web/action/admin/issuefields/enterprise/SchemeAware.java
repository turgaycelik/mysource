/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import org.ofbiz.core.entity.GenericValue;

public interface SchemeAware
{
    public static final String STORAGE_EXCEPTION = "Could not retrieve the Field Layout Scheme.";

    Long getSchemeId();

    void setSchemeId(Long schemeId);

    GenericValue getScheme();

    String getInvalidSchemeId();
}
