package com.atlassian.core.ofbiz;

import org.ofbiz.core.entity.GenericDelegator;

/**
 * This is the Factory for "atlassian-core".
 * <p/>
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 * <p/>
 * @deprecated See individual methods for individual replacements. Since v5.0.
 */
public class CoreFactory
{
    private static GenericDelegator genericDelegator;

    public static void globalRefresh()
    {
        genericDelegator = null;
    }

    /**
     * Old way to get a GenericDelegator.
     * <p>
     * Normally you would use {@link com.atlassian.jira.ofbiz.OfBizDelegator} instead.
     * Get OfBizDelegator injected or use {@link com.atlassian.jira.component.ComponentAccessor#getOfBizDelegator()}.
     * <p>
     * If you really want the raw Entity Engine "delegator", get the instance of DelegatorInterface from Pico.
     * You can call <tt>ComponentAccessor#getComponent(DelegatorInterface.class)</tt> if you need static access.
     *
     * @return GenericDelegator
     *
     * @deprecated Use {@link com.atlassian.jira.ofbiz.OfBizDelegator} or get {@link org.ofbiz.core.entity.DelegatorInterface} from Pico instead. Since v5.0.
     */
    public static GenericDelegator getGenericDelegator()
    {
        if (genericDelegator == null)
        {
            genericDelegator = GenericDelegator.getGenericDelegator("default");
        }
        return genericDelegator;
    }
}
