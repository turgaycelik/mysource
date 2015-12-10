/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.amendment.CreateEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimaryEntityRelationCreate extends AbstractEntityRelation
{
    private final String relationField;
    private final Map fields;

    public PrimaryEntityRelationCreate(final OfBizDelegator ofBizDelegator, final int id, final String relationType, final String relationName, final String relationField, final Map fields)
    {
        super(ofBizDelegator, id, relationType, relationName);
        this.relationField = relationField;
        this.fields = fields;
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.primary.entity.relation.desc", getEntityName(), getRelationType() + getRelationName());
    }

    @Override
    protected Amendment previewAmendment(final GenericValue entity)
    {
        return new CreateEntityAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.primary.entity.relation.create.preview",
            getRelationType() + getRelationName(), getRelationName()), entity);
    }

    public List correct() throws IntegrityException
    {
        final List toUpdate = new ArrayList();

        OfBizListIterator allEntities = null;
        final List result = new ArrayList();

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
                        // The entity we are looking for is missing create a new one
                        // We HAVE to "clone" the map as an id gets generated and gets put into the map that is passed
                        // to the EntityUtils.createValue() method.
                        // If the fields map is not cloned the second time this loop is executed the fields map
                        // will already have an id, causing violation of unique index in the database.
                        final GenericValue relation = EntityUtils.createValue(getRelationName(), new HashMap(getFields()));
                        entity.set(getRelationField(), relation.get("id"));
                        result.add(new CreateEntityAmendment(Amendment.CORRECTION, getI18NBean().getText(
                            "admin.integrity.check.primary.entity.relation.create.message", getRelationName()), entity));
                        toUpdate.add(entity);
                    }
                }
                catch (final GenericEntityException e)
                {
                    throw new IntegrityException(e);
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

        if (!toUpdate.isEmpty())
        {
            try
            {
                ofBizDelegator.storeAll(toUpdate);
            }
            catch (final Exception e)
            {
                throw new IntegrityException(e);
            }
        }

        return result;
    }

    public boolean isAvailable()
    {
        return true;
    }

    private String getRelationField()
    {
        return relationField;
    }

    private Map getFields()
    {
        return fields;
    }
}