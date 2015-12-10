/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.importer;

import webwork.action.Action;

public interface ImportEntity extends Action, Comparable
{
    public int getActionOrder();
}
