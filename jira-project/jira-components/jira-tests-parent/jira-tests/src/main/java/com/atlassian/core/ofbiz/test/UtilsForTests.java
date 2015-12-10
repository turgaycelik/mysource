package com.atlassian.core.ofbiz.test;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockSequenceUtil;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.exception.DataAccessException;
import junit.framework.Assert;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.MemoryHelper;
import org.ofbiz.core.entity.model.ModelEntity;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UtilsForTests
{
    /**
     * Get a test Entity
     * @param entity the name of the entity
     * @param fields the field values
     * @return the created GenericValue
     * @throws EntityException if OfBiz throws a {@link GenericEntityException}
     */
    public static GenericValue getTestEntity(final String entity, final Map fields)
    {
        try
        {
            return EntityUtils.createValue(entity, fields);
        }
        catch (final DataAccessException e)
        {
            throw new EntityException(e);
        }
    }

    public static GenericValue getTestConstant(final String entity, Map params)
    {
        try
        {
            if (params == null)
            {
                params = new HashMap();
            }

            if (params.get("id") == null)
            {
                final String id = EntityUtils.getNextStringId(entity);
                params.put("id", id);
            }

            GenericValue v = CoreFactory.getGenericDelegator().makeValue(entity, params);
            v = v.create();
            return v;
        }
        catch (final GenericEntityException e)
        {
            throw new EntityException(e);
        }
    }

    public static void cleanWebWork()
    {
        ActionContext.setContext(new ActionContext());
    }

    public static void cleanOFBiz()
    {
        MemoryHelper.clearCache();

        final String helperName = CoreFactory.getGenericDelegator().getEntityHelperName("SequenceValueItem");
        final ModelEntity seqEntity = CoreFactory.getGenericDelegator().getModelEntity("SequenceValueItem");

        CoreFactory.getGenericDelegator().setSequencer(new MockSequenceUtil(helperName, seqEntity, "seqName", "seqId"));
    }

    /**
     * Check that a collection has only one element, and that is the object provided
     */
    public static void checkSingleElementCollection(final Collection collection, final Object expected)
    {
        Assert.assertEquals(1, collection.size());
        Assert.assertTrue(collection.contains(expected));
    }

    public static class EntityException extends RuntimeException
    {
        EntityException(final Exception ex)
        {
            super(ex);
        }
    }

}
