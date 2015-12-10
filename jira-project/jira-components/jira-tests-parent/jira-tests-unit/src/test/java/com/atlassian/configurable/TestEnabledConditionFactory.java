package com.atlassian.configurable;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestEnabledConditionFactory
{
    @Test
    public void testFactoryInstantiatesTheRightStuff()
    {
        EnabledCondition condition = EnabledConditionFactory.create(NotEnabledCondition.class.getName());
        assertNotNull(condition);
        assertTrue(condition instanceof NotEnabledCondition);
        assertFalse(condition.isEnabled());
    }

    @Test
    public void testFactoryReturnsNullForNull()
    {
        EnabledCondition condition = EnabledConditionFactory.create(null);
        assertNull(condition);
    }

    @Test
    public void testFactoryReturnsNullForGarbage()
    {
        EnabledCondition condition = EnabledConditionFactory.create("12*");
        assertNull(condition);
    }

    @Test
    public void testFactoryReturnsNullForNonImplementingClass()
    {
        EnabledCondition condition = EnabledConditionFactory.create(Object.class.getName());
        assertNull(condition);
    }
}
