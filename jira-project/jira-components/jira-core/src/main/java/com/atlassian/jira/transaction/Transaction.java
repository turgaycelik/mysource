package com.atlassian.jira.transaction;

import org.ofbiz.core.entity.GenericTransactionException;

/**
 * This represents the state of a running transaction that can be comitted or rolled back
 *
 * @since v4.4.1
 */
public interface Transaction
{
    /**
     * This will commit the transaction.
     *
     * @throws GenericTransactionException if the transaction cannot be commited
     */
    void commit() throws TransactionRuntimeException;

    /**
     * This will rollback the transaction.
     *
     * @throws GenericTransactionException if the transaction cannot be rollbacked
     */
    void rollback() throws TransactionRuntimeException;

    /**
     * This is designed to be called in a top level finally block and it will attempt to rollback the transaction IF it
     * has not already been committed.  This allows you to simplify your try/catch/finally blocks in and around
     * transaction code.
     * <p/>
     * Note it DOES NOT throw a {@link TransactionRuntimeException) if the roll back cannot be performed.  Since this is
     * intended to be inside a finally block it most likely to late to know about this you probably dont want to do
     * anything since rollback hasnt worked either.  So it logs this condition is an ignores it to make calling code
     * more simple.
     */
    void finallyRollbackIfNotCommitted();


    /**
     * @return if this represents a new transaction and hence whether calling {@link #commit()} or {@link #rollback()}
     *         will actually do anything
     */
    boolean isNewTransaction();
}
