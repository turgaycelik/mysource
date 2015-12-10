/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

import java.util.List;

public interface IntegrityCheck extends Comparable<IntegrityCheck>
{
    Long getId();

    String getDescription();

    List<? extends Check> getChecks();

    boolean isAvailable();
}
