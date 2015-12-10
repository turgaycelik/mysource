package com.atlassian.jira.ofbiz;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.exception.DataAccessException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;

/**
 * The methods in these class should not be used. They are here for backwards compatibility with GenericValues
 * Use debug to determine what still uses these methods and change the code to uses the actual getter/setter
 * methods of the bean.
 */
@Internal
public abstract class AbstractOfBizValueWrapper implements OfBizValueWrapper
{
    private static final Logger log = Logger.getLogger(AbstractOfBizValueWrapper.class);
    protected final GenericValue genericValue;
    private boolean debug = false;

    protected AbstractOfBizValueWrapper(GenericValue genericValue)
    {
        if (genericValue == null)
        {
            throw new IllegalArgumentException("GenericValue cannot be null.");
        }

        this.genericValue = genericValue;
    }


    public String getString(String name)
    {
        if (debug)
            Thread.dumpStack();
        return genericValue.getString(name);
    }

    public Timestamp getTimestamp(String name)
    {
        if (debug)
            Thread.dumpStack();
        return genericValue.getTimestamp(name);
    }

    public Long getLong(String name)
    {
        if (debug)
            Thread.dumpStack();
        return genericValue.getLong(name);
    }

    public GenericValue getGenericValue()
    {
        if (debug)
            Thread.dumpStack();
        return genericValue;
    }

    public void store()
    {
        try
        {
            genericValue.store();
        }
        catch (GenericEntityException e)
        {
            log.error("Exception whilst trying to store genericValue " + genericValue + ".", e);
            throw new DataAccessException(e);
        }
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof AbstractOfBizValueWrapper)) return false;

        final AbstractOfBizValueWrapper abstractOfBizValueWrapper = (AbstractOfBizValueWrapper) o;

        if (genericValue != null ? !genericValue.equals(abstractOfBizValueWrapper.genericValue) : abstractOfBizValueWrapper.genericValue != null) return false;

        return true;
    }

    public int hashCode()
    {
        return (genericValue != null ? genericValue.hashCode() : 0);
    }
}
