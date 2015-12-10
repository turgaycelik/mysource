package com.atlassian.jira.sharing.search;

import java.util.Collections;
import java.util.Comparator;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.apache.commons.collections.ComparatorUtils;
import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.sharing.search.GenericValueComparator}.
 *
 * @since v3.13
 */
public class TestGenericValueComparator
{
    private MockControl mockComparatorControl;
    private Comparator mockComparator;
    private GenericValue gv1;
    private GenericValue gv2;
    private GenericValue emptyGv;

    @Before
    public void setUp() throws Exception
    {
        mockComparatorControl = MockControl.createControl(Comparator.class);
        mockComparator = (Comparator) mockComparatorControl.getMock();
        gv1 = new MockGenericValue("ShearchRequest", EasyMap.build("id", new Long(50), "other", new Long(20)));
        gv2 = new MockGenericValue("ShearchRequest", EasyMap.build("id", new Long(10), "other", new Long(100)));
        emptyGv = new MockGenericValue("ShearchRequest", Collections.EMPTY_MAP);
    }

    @After
    public void tearDown() throws Exception
    {
        mockComparatorControl = null;
        mockComparator = null;
        gv1 = null;
        gv2 = null;
        emptyGv = null;
    }

    private GenericValueComparator getGenericValueComparator(final String field)
    {
        mockComparatorControl.replay();

        return new GenericValueComparator(field, mockComparator);
    }

    private void verify()
    {
        mockComparatorControl.verify();
    }

    @Test
    public void testConstructorNullDelegator()
    {
        try
        {
            new GenericValueComparator("field", null);
            fail("Should not accept null.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testConstrctorNullField()
    {
        try
        {
            new GenericValueComparator(null, ComparatorUtils.NATURAL_COMPARATOR);
            fail("Should not accept null field.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericValueComparator(null);
            fail("Should not accept null field.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testConstrctorEmptyField()
    {
        try
        {
            new GenericValueComparator("", ComparatorUtils.NATURAL_COMPARATOR);
            fail("Should not accept empty field.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericValueComparator("");
            fail("Should not accept empty field.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testComparatorLeftNull()
    {
        try
        {
            final Comparator comparator = getGenericValueComparator("id");
            comparator.compare(null, gv1);

            fail("Should not accept null GV.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testComparatorRightNull()
    {
        try
        {
            final Comparator comparator = getGenericValueComparator("id");
            comparator.compare(gv1, null);

            fail("Should not accept null GV.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testComparatorIllegalLeft()
    {
        try
        {
            final Comparator comparator = getGenericValueComparator("id");
            comparator.compare("balh", gv1);

            fail("Should not accept null GV.");
        }
        catch (ClassCastException e)
        {
            //expected.
        }
    }

    @Test
    public void testComparatorIllegalRight()
    {
        try
        {
            final Comparator comparator = getGenericValueComparator("id");
            comparator.compare(gv2, Collections.EMPTY_LIST);

            fail("Should not accept null GV.");
        }
        catch (ClassCastException e)
        {
            //expected.
        }
    }

    @Test
    public void testComparatorDelegate()
    {
        mockComparator.compare(new Long(50), new Long(10));
        mockComparatorControl.setReturnValue(1);

        final Comparator comparator = getGenericValueComparator("id");

        assertEquals(1, comparator.compare(gv1, gv2));

        verify();
    }

    @Test
    public void testComparatorDelegateOther()
    {
        mockComparator.compare(new Long(20), new Long(100));
        mockComparatorControl.setReturnValue(1);

        final Comparator comparator = getGenericValueComparator("other");

        assertEquals(1, comparator.compare(gv1, gv2));

        verify();
    }

    @Test
    public void testComparatorDelegateLeftNull()
    {
        mockComparator.compare(null, new Long(20));
        mockComparatorControl.setReturnValue(-1);

        final Comparator comparator = getGenericValueComparator("other");

        assertEquals(-1, comparator.compare(emptyGv, gv1));

        verify();
    }

    @Test
    public void testComparatorDelegateRightNull()
    {
        mockComparator.compare(new Long(10), null);
        mockComparatorControl.setReturnValue(-1);

        final Comparator comparator = getGenericValueComparator("id");

        assertEquals(-1, comparator.compare(gv2, emptyGv));

        verify();
    }

    @Test
    public void testComparatorNatural()
    {
        Comparator comparator = new GenericValueComparator("id");
        assertTrue(comparator.compare(gv1, gv2) > 0);
        assertTrue(comparator.compare(gv2, gv1) < 0);
        assertTrue(comparator.compare(gv1, gv1) == 0);

        comparator = new GenericValueComparator("other");
        assertTrue(comparator.compare(gv1, gv2) < 0);
        assertTrue(comparator.compare(gv2, gv1) > 0);
        assertTrue(comparator.compare(gv2, gv2) == 0);

        assertTrue(comparator.compare(emptyGv, emptyGv) == 0);
    }

    @Test
    public void testComparatorNaturalWithNulls()
    {
        Comparator comparator = new GenericValueComparator("id");
        assertTrue(comparator.compare(gv1, emptyGv) > 0);
        assertTrue(comparator.compare(emptyGv, gv2) < 0);

        assertTrue(comparator.compare(emptyGv, emptyGv) == 0);
    }
}
