package com.atlassian.jira.util;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.util.concurrent.ExceptionPolicy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @since v6.3
 */
public class TestCallableFunction
{
    @Test
    public void callableFunctionRunsTheFunction() throws Exception
    {
        Function f = Functions.identity();
        CallableFunction<Integer, Integer> cf = new CallableFunction<Integer, Integer>(f, ExceptionPolicy.Policies.THROW);
        assertEquals(Integer.valueOf(1), cf.apply(1).call());
    }

    @Test
    public void callableFunctionThrowsExceptionWhenPolicyIsToThrowException() throws Exception
    {
        Function f = new Function()
        {
            @Override
            public Object get(final Object input)
            {
                throw new DataAccessException("Not implemented");
            }
        };
        CallableFunction<Integer, Integer> cf = new CallableFunction<Integer, Integer>(f, ExceptionPolicy.Policies.THROW);
        try
        {
            cf.apply(1).call();
            fail("No exception thrown");
        }
        catch (DataAccessException e)
        {
            assertTrue("Correct exception thrown", true);
        }
    }

    @Test
    public void callableFunctionSwallowsExceptionWhenPolicyIsToIgnoreException() throws Exception
    {
        Function f = new Function()
        {
            @Override
            public Object get(final Object input)
            {
                throw new DataAccessException("Not implemented");
            }
        };
        CallableFunction<Integer, Integer> cf = new CallableFunction<Integer, Integer>(f, ExceptionPolicy.Policies.IGNORE_EXCEPTIONS);
        assertNull(cf.apply(1).call());
    }
}
