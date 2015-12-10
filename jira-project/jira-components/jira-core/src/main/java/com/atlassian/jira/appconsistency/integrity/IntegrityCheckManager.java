/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;

import java.util.List;

public interface IntegrityCheckManager
{
    public List getIntegrityChecks();

    public Check getCheck(Long checkId);

    public IntegrityCheck getIntegrityCheck(Long id);
}
