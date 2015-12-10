package com.atlassian.jira.ofbiz;

import com.atlassian.core.ofbiz.CoreFactory;
import org.ofbiz.core.entity.GenericDelegator;

/**
 * A factory for Unit Tests to get an OfBizDelegator or GenericDelegator.
 *
 * Normally it would be recommended to use a mock OfBizDelegator.
 * This factory is included for compatibility with existing tests that rely on getting a "real" GenericDelegator.
 *
 * @since v5.1
 */
public class OfBizFactory
{
    public static GenericDelegator getGenericDelegator()
    {
        return CoreFactory.getGenericDelegator();
    }

    public static OfBizDelegator getOfBizDelegator()
    {
        return new DefaultOfBizDelegator(getGenericDelegator());
    }

}
