package com.atlassian.jira.oauth.consumer;

import junit.framework.Assert;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MockGV that can be used to assert the correct fields were updated.
 *
 * @since v4.0
 */
class MockGenericValue extends com.atlassian.jira.mock.ofbiz.MockGenericValue
{
    public final AtomicBoolean storeCalled = new AtomicBoolean(false);
    public Map<String, Object> expectedFields;

    public MockGenericValue(String entityName)
    {
        super(entityName);
    }

    public MockGenericValue(String entityName, Map fields)
    {
        super(entityName, fields);
    }

    public MockGenericValue(GenericValue value)
    {
        super(value);
    }

    @Override
    public void store() throws GenericEntityException
    {
        storeCalled.set(true);
    }

    @Override
    public void setNonPKFields(final Map fields)
    {
        //check the fields passed in match the expected fields!
        final OfBizMapArgsEqual argsEqual = new OfBizMapArgsEqual(expectedFields);
        Assert.assertTrue("update fields are not the same", argsEqual.matches(fields));
    }
}