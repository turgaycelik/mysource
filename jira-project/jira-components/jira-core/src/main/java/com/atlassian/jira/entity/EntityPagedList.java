package com.atlassian.jira.entity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.collect.PagedList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityListIterator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.model.ModelEntity;

import java.util.Iterator;
import java.util.List;

/**
 * Wraps an ListIterator in a paged implementation.  You can directly retrieve a specific page via get(n) or get an
 * {@link Iterator} and iterate the results a page at a time.
 *
 * @since v6.1
 */
public class EntityPagedList<E> implements PagedList<E>
{
    private final String entityName;
    private final EntityFactory<E> entityFactory;
    private final EntityCondition entityCondition;
    private final List<String> orderBy;
    private final int pageSize;
    private final int count;
    private final DelegatorInterface genericDelegator;

    private final Logger log = Logger.getLogger(this.getClass());

    /**
     *
     * @param pageSize   desired pageSize, must be greater than 0
     * @param entityFactory EntityFactory used to return List<E> from a List<GenericValue>
     * @param entityCondition  Condition to perform search in database
     * @param orderBy  List of fields to order by
     */
    public EntityPagedList(int pageSize, EntityFactory<E> entityFactory, EntityCondition entityCondition, List<String> orderBy)
    {
        this(pageSize, entityFactory, entityCondition, orderBy, ComponentAccessor.getComponent(DelegatorInterface.class));
    }

    @VisibleForTesting
    EntityPagedList(int pageSize, EntityFactory<E> entityFactory, EntityCondition entityCondition, List<String> orderBy, DelegatorInterface genericDelegator)
    {
        this.pageSize = pageSize;
        this.entityFactory = entityFactory;
        this.entityCondition =entityCondition;
        this.orderBy = orderBy;
        this.entityName = entityFactory.getEntityName();
        this.genericDelegator = genericDelegator;
        this.count = getItemCount();
    }

    /**
     *
     * @param pageNumber  page to retrieve, if the page does not exist throws an IllegalArgumentException
     * @return  Lsit containing the items in the page
     */

    @Override
    public List<E> getPage(int pageNumber)
    {
        EntityListIterator entityListIterator = null;
        if (pageNumberInvalid(pageNumber))
        {
            throw new IllegalArgumentException("pageNumber should be between 0 and "+ (count /pageSize -1));
        }
        try
        {
            entityListIterator = getListIterator(pageNumber);
            return entityFactory.buildList(entityListIterator.getCompleteList());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Problem retrieving page.", e);
        }
        finally
        {
            if (entityListIterator != null)
            {
                try
                {
                    entityListIterator.close();
                }
                catch (GenericEntityException ignore)
                {

                }
            }
        }
    }

    /**
     * Iterate over the database results a page at a time
     */
    @Override
    public Iterator<List<E>> iterator()
    {
        return new PagedListIterator(this);
    }

    @Override
    public List<E> getCompleteList()
    {
        List<E> list = Lists.newArrayList();
        Iterator<List<E>> iterator = iterator();
        while (iterator.hasNext())
        {
            list.addAll(iterator.next());
        }
        return list;
    }

    private EntityListIterator getListIterator(int pageNumber) throws GenericEntityException {
        GenericHelper helper = genericDelegator.getEntityHelper(entityName);
        ModelEntity modelEntity = genericDelegator.getModelEntity(entityName);
        EntityFindOptions entityFindOptions = new EntityFindOptions();
        entityFindOptions.setOffset(pageNumber * pageSize);
        entityFindOptions.setMaxResults(pageSize);
        return helper.findListIteratorByCondition(modelEntity, entityCondition, null, null, orderBy, entityFindOptions);
    }

    @Override
    public int getSize()
    {
        return count;
    }

    private int getItemCount()
    {
        int itemCount = 0;
        try
        {
            itemCount = genericDelegator.countByCondition(entityName, null, entityCondition, null);
        }
        catch (GenericEntityException e)
        {
            log.error("Exception thrown trying to count rows", e);
        }
        return itemCount;
    }

    @Override
    public int getPageSize()
    {
        return pageSize;
    }

    boolean pageNumberInvalid(int pageNumber) {
        if (count > 0)
        {
            return pageNumber < 0 || pageNumber * pageSize >= getSize();
        }
        else
        {
            return pageNumber != 0;
        }
    }


}
