/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

public class SearchRequestRelationIntegrityCheck extends IntegrityCheckImpl
{
    public SearchRequestRelationIntegrityCheck(final int id, final String description, final Check... checks)
    {
        super(id, description, checks);
    }
}
