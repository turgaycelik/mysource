package com.atlassian.jira.transaction;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.mock.MockApplicationProperties;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class TransactionSupportImplTest
{
    private MockApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = new MockApplicationProperties();
    }

    @Test
    public void testStart() throws Exception
    {
        TransactionSupportImpl txnSupport = newTransactionSupport(applicationProperties);

        Transaction transaction = txnSupport.begin();
        assertLive(transaction);

        applicationProperties.setOption(APKeys.JIRA_DB_TXN_DISABLED, true);

        transaction = txnSupport.begin();
        assertNoOp(transaction);
    }

    private TransactionSupportImpl newTransactionSupport(final MockApplicationProperties applicationProperties)
    {
        return new TransactionSupportImpl(applicationProperties)
        {
            @Override
            boolean beginTxn() throws TransactionRuntimeException
            {
                return true;
            }
        };
    }

    private void assertLive(Transaction transaction)
    {
        assertNotNull(transaction);
        // I know this cheating a bit so be it
        assertTrue(transaction.getClass().getName().contains("TransactionImpl"));
    }

    private void assertNoOp(Transaction transaction)
    {
        assertNotNull(transaction);
        // I know this cheating a bIt so be it
        assertFalse(transaction.getClass().getName().contains("TransactionImpl"));
    }
}
