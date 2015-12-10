package com.atlassian.jira.util.ofbiz;

import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * Class that providess guarantees about the usgae of this GenericValue,
 * in particular it should never be used for mutative operations, only 
 * for reading state.
 * <p>
 * All mutative operations will throw UnsupportedOperationException
 */
public final class ImmutableGenericValue extends GenericValue
{
    // HACK - this guy is false by default, then in the ctor it sets it to true so we can test for !constructed
    private final boolean constructed;
    
    public ImmutableGenericValue(GenericDelegator delegator, String entityName, Map fields)
    {
        super(delegator.getModelEntity(entityName));
        super.setDelegator(delegator);
        super.setFields(fields);
        constructed = true;
    }

    public void store() throws GenericEntityException
    {
        throw new UnsupportedOperationException("Cannot store an ImmutableGenericValue");
    }

    public void refresh() throws GenericEntityException
    {
        throw new UnsupportedOperationException("Cannot refresh an ImmutableGenericValue");
    }

    public void remove() throws GenericEntityException
    {
        throw new UnsupportedOperationException("Cannot remove an ImmutableGenericValue");
    }

    public Object remove(Object key)
    {
        throw new UnsupportedOperationException("Cannot remove fields in an ImmutableGenericValue");
    }

    public void removeRelated(String relationName) throws GenericEntityException
    {
        throw new UnsupportedOperationException("Cannot store an ImmutableGenericValue");
    }

    public void clear()
    {
        throw new UnsupportedOperationException("Cannot clear an ImmutableGenericValue");
    }

    public Object put(String key, Object value)
    {
        throw new UnsupportedOperationException("Cannot put into an ImmutableGenericValue");
    }

    public void putAll(Map map)
    {
        throw new UnsupportedOperationException("Cannot put all into an ImmutableGenericValue");
    }

    public Object set(String name, Object value, boolean setIfNull)
    {
        if (!constructed)
        {
            return super.set(name, value, setIfNull);
        }
        throw new UnsupportedOperationException("Cannot set or mutate an ImmutableGenericValue");
    }

    public void setFields(Map keyValuePairs)
    {
        throw new UnsupportedOperationException("Cannot set or mutate an ImmutableGenericValue");
    }

    public void setPKFields(Map fields, boolean setIfEmpty)
    {
        throw new UnsupportedOperationException("Cannot set or mutate an ImmutableGenericValue");
    }

    public void setDelegator(GenericDelegator internalDelegator)
    {
        throw new UnsupportedOperationException("Cannot set or mutate an ImmutableGenericValue");
    }
}