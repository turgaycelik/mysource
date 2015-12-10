package com.atlassian.jira.transaction;

import com.atlassian.jira.component.ComponentAccessor;
import org.ofbiz.core.entity.GenericTransactionException;

/**
 * A static version of {@link TransactionSupport} that calls statically to PICO to get the implementation needed.
 *
 * @since v4.4.1
 */
public class Txn
{
    /**
     * @return a {@link Transaction} state object
     * @throws TransactionRuntimeException if the transaction cannot be started
     * @see TransactionSupport#begin()
     */
    public static Transaction begin() throws TransactionRuntimeException
    {
        return getTxnSupport().begin();
    }

    private static TransactionSupport getTxnSupport()
    {
        return ComponentAccessor.getComponent(TransactionSupport.class);
    }

}
