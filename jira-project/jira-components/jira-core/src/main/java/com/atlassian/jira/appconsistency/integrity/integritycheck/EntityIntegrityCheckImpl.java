/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.check.EntityCheck;

import java.util.Arrays;
import java.util.List;

public class EntityIntegrityCheckImpl extends AbstractIntegrityCheck implements EntityIntegrityCheck
{
    private final String entityName;
    private final List<EntityCheck> checks;

    public EntityIntegrityCheckImpl(final int id, final String description, final String entityName, EntityCheck... checks)
    {
        this(id, description, entityName, Arrays.asList(checks));
    }
    
    public EntityIntegrityCheckImpl(final int id, final String description, final String entityName, final List<EntityCheck> checks)
    {
        super(id, description);
        this.entityName = entityName;
        this.checks = checks;
        for (final EntityCheck entityCheck : checks)
        {
            entityCheck.setEntityIntegrityCheck(this);
        }
    }

    @Override
    public List<? extends Check> getChecks()
    {
        return checks;
    }

    public String getEntityName()
    {
        return entityName;
    }
}
