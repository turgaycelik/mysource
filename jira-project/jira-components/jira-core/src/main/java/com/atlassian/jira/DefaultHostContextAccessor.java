package com.atlassian.jira;

import com.atlassian.sal.spi.HostContextAccessor;

import java.util.Map;

/**
 * This is necessary to provide a generic way for SAL to access a component that may not have been published the
 * plugins 2.0 way.  
 *
 * @since v4.0
 */
public class DefaultHostContextAccessor implements HostContextAccessor
{
    /**
     * Returns a mapping of all the component keys to instances that are registered in pico for the given iface class.
     *
     * @param iface The class that components returned need to implement.
     * @return A map of component key -> instance.
     */
    public <T> Map<String, T> getComponentsOfType(final Class<T> iface)
    {
        return ComponentManager.getComponentsOfTypeMap(iface);
    }

    /**
     * No transactions in JIRA so this is effectively a NoOp.
     *
     * @param hostTransactionCallback the action to be executed.
     * @return Result of the 'transaction'
     */
    public <T> T doInTransaction(final HostTransactionCallback<T> hostTransactionCallback)
    {
        return hostTransactionCallback.doInTransaction();
    }
}
