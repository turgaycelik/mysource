/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheck;

public interface EntityCheck<T extends Amendment> extends Check<T>
{
    EntityIntegrityCheck getEntityIntegrityCheck();

    void setEntityIntegrityCheck(EntityIntegrityCheck entityIntegrityCheck);
}
