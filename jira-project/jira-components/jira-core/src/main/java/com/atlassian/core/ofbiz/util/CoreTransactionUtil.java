/*
 * Atlassian Source Code Template.
 * User: Administrator
 * Created: Oct 18, 2002
 * Time: 12:29:13 PM
 * CVS Revision: $Revision: 1.3 $
 * Last CVS Commit: $Date: 2005/10/10 12:07:06 $
 * Author of last CVS Commit: $Author: amazkovoi $
 */
package com.atlassian.core.ofbiz.util;

import com.atlassian.core.ofbiz.CoreFactory;
import org.ofbiz.core.entity.GenericTransactionException;
import org.ofbiz.core.entity.TransactionUtil;

import java.sql.Connection;

/**
 * This class is a simple wrapper around OFBiz TransactionUtil class.
 * <p/>
 * You can set JIRA to use transactions or not with the useTransactions flag.
 * <p/>
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class CoreTransactionUtil
{
    static boolean useTransactions = true;
    static int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;

    public static boolean begin() throws GenericTransactionException
    {
        if (useTransactions)
        {
            return TransactionUtil.beginLocalTransaction(CoreFactory.getGenericDelegator().getGroupHelperName("default"), isolationLevel);
        }

        return true;
    }

    public static void commit(boolean began) throws GenericTransactionException
    {
        if (useTransactions)
        {
            TransactionUtil.commitLocalTransaction(began);
        }
    }

    public static void rollback(boolean began) throws GenericTransactionException
    {
        if (useTransactions)
        {
            TransactionUtil.rollbackLocalTransaction(began);
        }
    }

    public static void setUseTransactions(boolean useTransactions)
    {
        CoreTransactionUtil.useTransactions = useTransactions;
    }

    public static int getIsolationLevel()
    {
        return isolationLevel;
    }

    public static void setIsolationLevel(int isolationLevel)
    {
        CoreTransactionUtil.isolationLevel = isolationLevel;
    }

}
