/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheck;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;

public abstract class EntityCheckImpl<T extends Amendment> extends CheckImpl<T> implements EntityCheck<T>
{
    private EntityIntegrityCheck entityIntegrityCheck;

    protected EntityCheckImpl(final OfBizDelegator ofBizDelegator, final int id)
    {
        super(ofBizDelegator, id);
    }

    public EntityIntegrityCheck getEntityIntegrityCheck()
    {
        return entityIntegrityCheck;
    }

    public void setEntityIntegrityCheck(final EntityIntegrityCheck entityIntegrityCheck)
    {
        this.entityIntegrityCheck = entityIntegrityCheck;
    }

    @Override
    public IntegrityCheck getIntegrityCheck()
    {
        return entityIntegrityCheck;
    }

    @Override
    public void setIntegrityCheck(final IntegrityCheck integrityCheck)
    {
        if (integrityCheck instanceof EntityIntegrityCheck)
        {
            setEntityIntegrityCheck((EntityIntegrityCheck) integrityCheck);
        }
        else
        {
            throw new IllegalArgumentException("Entity Check must have an Entity Integrity Check");
        }
    }

    protected String getEntityName()
    {
        return getEntityIntegrityCheck().getEntityName();
    }

    protected OfBizListIterator getEntities(final String entityName) throws IntegrityException
    {
        return ofBizDelegator.findListIteratorByCondition(entityName, null);
    }

    public String getUnavailableMessage()
    {
        return "";
    }
}
