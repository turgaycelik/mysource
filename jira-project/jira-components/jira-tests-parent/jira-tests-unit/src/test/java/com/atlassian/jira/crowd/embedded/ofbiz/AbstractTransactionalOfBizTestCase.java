package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.After;
import org.junit.Before;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericDelegatorUtils;

import static org.junit.Assert.assertTrue;
import static org.ofbiz.core.entity.TransactionUtil.beginLocalTransaction;
import static org.ofbiz.core.entity.TransactionUtil.rollbackLocalTransaction;

public abstract class AbstractTransactionalOfBizTestCase
{
    private GenericDelegator delegator;
    private OfBizDelegator ofBizDelegator;

    @Before
    public void setUpOfBiz() throws Exception
    {
        delegator = GenericDelegatorUtils.createGenericDelegator("default");
        ofBizDelegator = new DefaultOfBizDelegator(delegator);
        assertTrue("Should get a new local transaction", beginLocalTransaction(getDataSourceName(), -1));
    }

    @After
    public void tearDownOfBiz() throws Exception
    {
        rollbackLocalTransaction(true);
        ofBizDelegator = null;
    }

    protected final GenericDelegator getGenericDelegator()
    {
        return delegator;
    }

    protected OfBizDelegator getOfBizDelegator()
    {
        return ofBizDelegator;
    }

    protected String getDataSourceName()
    {
        return "defaultDS";
    }

}
