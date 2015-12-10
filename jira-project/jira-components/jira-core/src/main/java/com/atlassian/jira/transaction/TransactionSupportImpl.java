package com.atlassian.jira.transaction;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericTransactionException;

/**
 * @since v4.4.1
 */
public class TransactionSupportImpl implements TransactionSupport
{
    private static final Transaction NO_OP = new Transaction()
    {
        @Override
        public void commit()
        {
        }

        @Override
        public void rollback()
        {
        }

        @Override
        public void finallyRollbackIfNotCommitted() throws TransactionRuntimeException
        {
        }

        @Override
        public boolean isNewTransaction()
        {
            return false;
        }
    };


    private final ApplicationProperties applicationProperties;
    private static final Logger log = Logger.getLogger(TransactionSupportImpl.class);

    public TransactionSupportImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Transaction begin() throws TransactionRuntimeException
    {
        if (startedTransaction(false))
        {
            return new TransactionImpl();
        }
        return NO_OP;
    }

    private boolean startedTransaction(boolean forceTxn) throws TransactionRuntimeException
    {
        /*
         * We can squib and revert to our JIRA code heritage and no use database
         * transactions
         */
        if (!forceTxn && applicationProperties.getOption(APKeys.JIRA_DB_TXN_DISABLED))
        {
            return false;
        }
        return beginTxn();
    }

    // package level for testing
    boolean beginTxn() throws TransactionRuntimeException
    {
        try
        {
            return CoreTransactionUtil.begin();
        }
        catch (GenericTransactionException e)
        {
            throw new TransactionRuntimeException(e);
        }
    }

    private static class TransactionImpl implements Transaction
    {
        private boolean committed = false;

        @Override
        public void commit() throws TransactionRuntimeException
        {
            try
            {
                CoreTransactionUtil.commit(true);
                committed = true;
            }
            catch (GenericTransactionException e)
            {
                log.error("Unable to commit transaction : " + e.getMessage());
                throw new TransactionRuntimeException(e);
            }
        }

        @Override
        public void rollback() throws TransactionRuntimeException
        {
            if (committed)
            {
                throw new IllegalStateException("The transaction has already been committed and hence you cannot rollback");
            }
            try
            {
                CoreTransactionUtil.rollback(true);
            }
            catch (GenericTransactionException e)
            {
                log.error("Unable to rollback transaction : " + e.getMessage());
                throw new TransactionRuntimeException(e);
            }
        }

        @Override
        public void finallyRollbackIfNotCommitted()
        {
            if (!committed)
            {
                try
                {
                    rollback();
                }
                catch (TransactionRuntimeException ignored)
                {
                    // this is very deliberate.  See the method description
                }
            }
        }

        @Override
        public boolean isNewTransaction()
        {
            return true;
        }
    }
}
