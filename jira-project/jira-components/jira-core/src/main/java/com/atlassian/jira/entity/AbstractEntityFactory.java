package com.atlassian.jira.entity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract EntityFactory that most implementations are expected to extend.
 *
 * @since v4.4
 */
public abstract class AbstractEntityFactory<E> implements EntityFactory<E>
{
    @Override
    public List<E> buildList(Collection<GenericValue> gvList)
    {
        if (gvList == null)
        {
            return null;
        }

        List<E> entities = new ArrayList<E>(gvList.size());
        for (GenericValue genericValue : gvList)
        {
            entities.add(build(genericValue));
        }
        return entities;
    }

    public GenericValue genericValueFrom(E entity)
    {
        OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        return ofBizDelegator.makeValue(getEntityName(), fieldMapFrom(entity));
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }
}
