package com.atlassian.jira.ofbiz;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityWhereString;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.model.MockModelEntity;
import org.ofbiz.core.entity.model.ModelEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test for the {@link com.atlassian.jira.ofbiz.CountedCondition}.
 *
 * @since v3.13
 */
public class TestCountedCondition
{
    @Test
    public void testConstrctorNullCondition()
    {
        try
        {
            new CountedCondition(null, 10);
            fail("Should not accept null condition.");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testConstructorZeroCount()
    {
        try
        {
            new CountedCondition(new EntityWhereString("a"), 0);
            fail("Should not zero count.");
        }
        catch (final IllegalArgumentException e)
        {
            //expeted.
        }
    }

    @Test
    public void testConstructorIllegalCount()
    {
        try
        {
            new CountedCondition(new EntityWhereString("a"), -1);
            fail("Should not accept illegal count.");
        }
        catch (final IllegalArgumentException e)
        {
            //expeted.
        }
    }

    @Test
    public void testConstructor()
    {
        final EntityCondition expr = new EntityWhereString("a");
        final CountedCondition condition = new CountedCondition(expr, 10);

        assertSame(expr, condition.getCondition());
        assertEquals(10, condition.getTermCount());
    }

    @Test
    public void testMakeWhereString()
    {
        final ModelEntity entity = new MockModelEntity("CoolTest");
        final List<String> params = CollectionBuilder.newBuilder("CoolTest Condition").asList();
        final AtomicBoolean called = new AtomicBoolean(false);

        final EntityCondition delegate = new EntityCondition()
        {
            @Override
            public String makeWhereString(final ModelEntity modelEntity, final List entityConditionParams)
            {
                assertSame(entity, modelEntity);
                assertSame(params, entityConditionParams);
                called.set(true);
                return "Executed";
            }

            @Override
            public void checkCondition(final ModelEntity modelEntity) throws GenericModelException
            {
                fail("Should not be calling this method.");
            }
        };

        final CountedCondition condition = new CountedCondition(delegate, 10);
        assertEquals("Executed", condition.makeWhereString(entity, params));
        assertTrue(called.get());
    }

    @Test
    public void testCheckCondition()
    {
        final ModelEntity entity = new MockModelEntity("CoolTest");
        final AtomicBoolean called = new AtomicBoolean(false);

        final EntityCondition delegate = new EntityCondition()
        {
            @Override
            public String makeWhereString(final ModelEntity modelEntity, final List entityConditionParams)
            {
                fail("Should not be calling this method.");

                return "";
            }

            @Override
            public void checkCondition(final ModelEntity modelEntity) throws GenericModelException
            {
                assertSame(entity, modelEntity);

                called.set(true);
            }
        };

        final CountedCondition condition = new CountedCondition(delegate, 10);
        try
        {
            condition.checkCondition(entity);
        }
        catch (final GenericModelException e)
        {
            fail("Unexpected exception throw.");
        }
        assertTrue(called.get());
    }

    @Test
    public void testCheckConditionWithException()
    {
        final ModelEntity entity = new MockModelEntity("CoolTest");
        final AtomicBoolean called = new AtomicBoolean(false);

        final EntityCondition delegate = new EntityCondition()
        {
            @Override
            public String makeWhereString(final ModelEntity modelEntity, final List entityConditionParams)
            {
                fail("Should not be calling this method.");

                return "";
            }

            @Override
            public void checkCondition(final ModelEntity modelEntity) throws GenericModelException
            {
                assertSame(entity, modelEntity);

                called.set(true);

                throw new GenericModelException("Test exception");
            }
        };

        final CountedCondition condition = new CountedCondition(delegate, 10);
        try
        {
            condition.checkCondition(entity);
            fail("Expected exception throw.");
        }
        catch (final GenericModelException e)
        {
            //expected.
        }
        assertTrue(called.get());
    }
}
