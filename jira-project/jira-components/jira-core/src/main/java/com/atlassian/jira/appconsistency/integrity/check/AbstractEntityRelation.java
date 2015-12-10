/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class AbstractEntityRelation<T extends Amendment> extends EntityCheckImpl<T>
{
    private final String relationType;
    private final String relationName;

    private static final Logger log = Logger.getLogger(AbstractEntityRelation.class);

    public AbstractEntityRelation(final OfBizDelegator ofBizDelegator, final int id, final String relationType, final String relationName)
    {
        super(ofBizDelegator, id);
        this.relationType = relationType;
        this.relationName = relationName;
    }

    protected String getRelationType()
    {
        return relationType;
    }

    protected String getRelationName()
    {
        return relationName;
    }

    public List<T> preview() throws IntegrityException
    {
        OfBizListIterator allEntities = null;
        final List<T> result = Lists.newArrayList();

        // Check if the relation exists for each entity
        try
        {
            allEntities = getEntities(getEntityName());
            GenericValue entity = allEntities.next();
            while (entity != null)
            {
                try
                {
                    final GenericValue relatedEntity = entity.getRelatedOne(getRelationType() + getRelationName());
                    if (relatedEntity == null)
                    {
                        // Then the entity we are looking at is invalid so add an amendment for this.
                        result.add(previewAmendment(entity));
                    }
                }
                catch (final GenericEntityException e)
                {
                    log.error(e, e);
                }
                entity = allEntities.next();
            }
        }
        catch (final Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }
        finally
        {
            if (allEntities != null)
            {
                // Close the iterator
                allEntities.close();
            }

        }
        return result;
    }

    protected abstract T previewAmendment(GenericValue entity);

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
