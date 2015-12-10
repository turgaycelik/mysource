/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;

import java.util.List;

public interface Check<T extends Amendment>
{
    public Long getId();

    public String getDescription();

    public List<T> preview() throws IntegrityException;

    public List<T> correct() throws IntegrityException;

    public IntegrityCheck getIntegrityCheck();

    public void setIntegrityCheck(IntegrityCheck integrityCheck);

    public boolean isAvailable();

    public String getUnavailableMessage();
}
