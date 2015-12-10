package com.atlassian.jira.plugin.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.mock.plugin.MockOrderableModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestModuleDescriptorComparator
{
    @Test
    public void testModuleDescriptorComparator()
    {
        ModuleDescriptorComparator comp = ModuleDescriptorComparator.COMPARATOR;

        //positive order
        MockOrderableModuleDescriptor orderP1 = new MockOrderableModuleDescriptor(0);
        MockOrderableModuleDescriptor orderP2 = new MockOrderableModuleDescriptor(10);

        //negative order
        MockOrderableModuleDescriptor orderN1 = new MockOrderableModuleDescriptor(-10);
        MockOrderableModuleDescriptor orderN2 = new MockOrderableModuleDescriptor(-20);

        //both order is positive (not equal)
        assertTrue(comp.compare(orderP1, orderP2) < 0);
        assertTrue(comp.compare(orderP2, orderP1) > 0);

        //one negative, one positive
        assertTrue(comp.compare(orderN1, orderP1) < 0);
        assertTrue(comp.compare(orderP1, orderN1) > 0);

        //both order is negative (not equal)
        assertTrue(comp.compare(orderN1, orderN2) > 0);
        assertTrue(comp.compare(orderN2, orderN1) < 0);

        //both order is equal
        assertTrue(comp.compare(orderP1, orderP1) == 0);
        assertTrue(comp.compare(orderN1, orderN1) == 0);

        List<OrderableModuleDescriptor> expected = Arrays.<OrderableModuleDescriptor>asList(orderN2, orderN1, orderP1, orderP2);
        List<OrderableModuleDescriptor> actual = Arrays.<OrderableModuleDescriptor>asList(orderP1, orderP2, orderN2, orderN1);
        Collections.sort(actual, comp);
        assertEquals(expected, actual);

    }
}
