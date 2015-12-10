package com.atlassian.jira.transaction;

public class MockTransactionSupport implements TransactionSupport
{
    @Override
    public Transaction begin() throws TransactionRuntimeException
    {
        return new MockTransaction();
    }

    private class MockTransaction implements Transaction
    {
        @Override
        public void commit() throws TransactionRuntimeException
        {
        }

        @Override
        public void rollback() throws TransactionRuntimeException
        {
        }

        @Override
        public void finallyRollbackIfNotCommitted()
        {
        }

        @Override
        public boolean isNewTransaction()
        {
            return false;
        }
    }
}
