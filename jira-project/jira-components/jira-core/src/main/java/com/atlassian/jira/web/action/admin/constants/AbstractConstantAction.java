/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.constants;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public abstract class AbstractConstantAction extends JiraWebActionSupport
{
    protected abstract String getConstantEntityName();

    protected abstract String getNiceConstantName();

    protected abstract String getIssueConstantField();

    protected abstract GenericValue getConstant(String id);

    protected abstract String getRedirectPage();

    /**
     * Get a collection of this constant
     */
    protected abstract Collection<GenericValue> getConstants();

    /**
     * Clear caches related to this constant
     */
    protected abstract void clearCaches();

    /**
     * Is the constant passed in the default constant
     */
}
