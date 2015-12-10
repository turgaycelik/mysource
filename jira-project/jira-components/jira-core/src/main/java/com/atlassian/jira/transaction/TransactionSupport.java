package com.atlassian.jira.transaction;

import org.ofbiz.core.entity.GenericTransactionException;

/**
 * Represents the ability to peform a database transaction in JIRA.
 *
 * @since v4.4.1
 */
public interface TransactionSupport
{
    /**
     * This begins a new transaction if one is not already running for this thread.
     * <p/>
     * It will be a NoOp if a transaction is already running and in this case a call to {@link
     * Transaction#commit()} will also be a NoOp.  The outer caller is then reponsible
     * for the ultimate commit or rollback.
     * <p/>
     * It will also be a NoOp if a transaction support in JIRA is turned off.
     *
     * @return a {@link Transaction} context object that you can called commit or rollback on
     * @throws GenericTransactionException if the transaction can not be established at all
     */
    Transaction begin() throws TransactionRuntimeException;
}
