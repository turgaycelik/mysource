package com.atlassian.jira.util.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.util.collect.IdentitySet}.
 *
 * @since v4.0
 */
public class TestIdentitySet extends MockControllerTestCase
{
    @Test
    public void testNewSet()
    {
        final IdentitySet<Object> objectIdentitySet = IdentitySet.newSet();
        assertNotNull(objectIdentitySet);
        assertTrue(objectIdentitySet.isEmpty());
        assertFalse(objectIdentitySet.iterator().hasNext());
    }

    @Test
    public void testNewListOrderedSet()
    {
        final IdentitySet<Object> objectIdentitySet = IdentitySet.newListOrderedSet();
        assertNotNull(objectIdentitySet);
        assertTrue(objectIdentitySet.isEmpty());
        assertFalse(objectIdentitySet.iterator().hasNext());

        final List<Object> expectedObjects = CollectionBuilder.newBuilder(new Object(), new Object(), 1).asList();

        for (final Object object : expectedObjects)
        {
            assertTrue(objectIdentitySet.add(object));
        }

        assertEquals(expectedObjects.size(), objectIdentitySet.size());
        final Iterator<Object> actualIterator = objectIdentitySet.iterator();
        for (final Object object : expectedObjects)
        {
            assertSame(object, actualIterator.next());
        }
    }

    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new IdentitySet<Object>(null);
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testClear()
    {
        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);
        mockSet.clear();
        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);
        identitySet.clear();

        mockController.verify();
    }

    @Test
    public void testAdd()
    {
        final Object obj = new Object();

        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);
        mockSet.add(new IdentitySet.IdentityReference<Object>(obj));
        mockController.setReturnValue(true);
        mockSet.add(new IdentitySet.IdentityReference<Object>(null));
        mockController.setReturnValue(false);
        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);

        assertTrue(identitySet.add(obj));
        assertFalse(identitySet.add(null));

        mockController.verify();
    }

    @Test
    public void testRemove()
    {
        final Object obj = new Object();

        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);
        mockSet.remove(new IdentitySet.IdentityReference<Object>(obj));
        mockController.setReturnValue(true);
        mockSet.remove(new IdentitySet.IdentityReference<Object>(null));
        mockController.setReturnValue(false);
        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);

        assertTrue(identitySet.remove(obj));
        assertFalse(identitySet.remove(null));

        mockController.verify();
    }

    @Test
    public void testContainsAll()
    {
        final List<Integer> integers = CollectionBuilder.newBuilder(10, 12, 11, 23, 11).asList();
        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);

        mockSet.containsAll(convert(integers));
        mockController.setReturnValue(true);
        mockSet.containsAll(null);
        mockController.setReturnValue(false);

        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);

        assertTrue(identitySet.containsAll(integers));
        assertFalse(identitySet.containsAll(null));

        mockController.verify();
    }

    @Test
    public void testAddAll()
    {
        final List<Double> doubles = CollectionBuilder.newBuilder(10.0, 12.1, 11.24, 23.23484, 11.3726423).asList();
        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);

        mockSet.addAll(TestIdentitySet.<Object> convert(doubles));
        mockController.setReturnValue(false);
        mockSet.addAll(null);
        mockController.setReturnValue(true);

        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);

        assertFalse(identitySet.addAll(doubles));
        assertTrue(identitySet.addAll(null));

        mockController.verify();
    }

    @Test
    public void testRetainAll()
    {
        final List<Long> longs = asList(10L);
        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);

        mockSet.retainAll(TestIdentitySet.<Object> convert(longs));
        mockController.setReturnValue(false);
        mockSet.retainAll(null);
        mockController.setReturnValue(true);

        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);

        assertFalse(identitySet.retainAll(longs));
        assertTrue(identitySet.retainAll(null));

        mockController.verify();
    }

    @Test
    public void testRemoveAll()
    {
        final List<Object> emptyObjects = Collections.emptyList();
        final List<Object> objects = asList(new Object(), new Object());
        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);

        mockSet.removeAll(TestIdentitySet.<Object> convert(emptyObjects));
        mockController.setReturnValue(false);
        mockSet.removeAll(null);
        mockController.setReturnValue(true);
        mockSet.removeAll(TestIdentitySet.<Object> convert(objects));
        mockController.setReturnValue(false);

        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);

        assertFalse(identitySet.removeAll(emptyObjects));
        assertTrue(identitySet.removeAll(null));
        assertFalse(identitySet.removeAll(objects));

        mockController.verify();
    }

    @Test
    public void testSize()
    {
        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);
        mockSet.size();
        mockController.setReturnValue(10);
        mockSet.size();
        mockController.setReturnValue(12);
        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);
        assertEquals(10, identitySet.size());
        assertEquals(12, identitySet.size());

        mockController.verify();
    }

    @Test
    public void testIsEmpty()
    {
        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);
        mockSet.isEmpty();
        mockController.setReturnValue(false);
        mockSet.isEmpty();
        mockController.setReturnValue(true);
        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);
        assertFalse(identitySet.isEmpty());
        assertTrue(identitySet.isEmpty());

        mockController.verify();
    }

    @Test
    public void testContains()
    {
        final Object obj = new Object();

        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Object>> mockSet = mockController.getMock(Set.class);
        mockSet.contains(new IdentitySet.IdentityReference<Object>(obj));
        mockController.setReturnValue(true);
        mockSet.contains(new IdentitySet.IdentityReference<Object>(null));
        mockController.setReturnValue(false);
        mockController.replay();

        final IdentitySet<Object> identitySet = new IdentitySet<Object>(mockSet);

        assertTrue(identitySet.contains(obj));
        assertFalse(identitySet.contains(null));

        mockController.verify();
    }

    @SuppressWarnings( { "UnnecessaryBoxing" })
    @Test
    public void testIterator() throws Exception
    {
        final Double value = new Double(10.0d);
        @SuppressWarnings( { "unchecked" })
        final Iterator<IdentitySet.IdentityReference<Double>> mockIterator = mockController.getMock(Iterator.class);
        mockIterator.hasNext();
        mockController.setReturnValue(true);
        mockIterator.next();
        mockController.setReturnValue(IdentitySet.IdentityReference.wrap(value));
        mockIterator.remove();
        mockIterator.hasNext();
        mockController.setReturnValue(true);

        @SuppressWarnings( { "unchecked" })
        final Set<IdentitySet.IdentityReference<Double>> mockSet = mockController.getMock(Set.class);
        mockSet.iterator();
        mockController.setReturnValue(mockIterator);

        mockController.replay();

        final IdentitySet<Double> identitySet = new IdentitySet<Double>(mockSet);
        final Iterator<Double> doubleIter = identitySet.iterator();
        assertTrue(doubleIter.hasNext());
        assertEquals(value, doubleIter.next());
        doubleIter.remove();
        assertTrue(doubleIter.hasNext());

        mockController.verify();
    }

    @SuppressWarnings( { "UnnecessaryBoxing" })
    @Test
    public void testRealTest() throws Exception
    {
        final Integer one1 = new Integer(1);
        final Integer one2 = new Integer(1);
        final Integer one3 = new Integer(1);
        final Integer one4 = new Integer(1);

        //check the initial state of the set
        final IdentitySet<Integer> testSet = IdentitySet.newListOrderedSet();
        assertNotNull(testSet);
        assertTrue(testSet.isEmpty());
        assertEquals(0, testSet.size());
        assertFalse(testSet.iterator().hasNext());
        assertFalse(testSet.contains(one1));
        assertFalse(testSet.containsAll(Collections.singletonList(one2)));
        assertTrue(testSet.containsAll(Collections.emptyList()));
        assertFalse(testSet.remove(one1));
        assertFalse(testSet.removeAll(asList(one1, one2, one3)));
        assertFalse(testSet.removeAll(Collections.emptyList()));
        assertFalse(testSet.retainAll(asList(one1, one2)));
        assertFalse(testSet.retainAll(Collections.emptyList()));
        assertFalse(testSet.remove(one2));

        //The set should now contain {one1}
        assertTrue(testSet.add(one1));
        assertFalse(testSet.isEmpty());
        assertEquals(1, testSet.size());
        assertTrue(testSet.iterator().hasNext());
        assertSame(one1, testSet.iterator().next());
        assertTrue(testSet.contains(one1));
        assertFalse(testSet.contains(one2));
        assertFalse(testSet.contains(null));
        assertTrue(testSet.containsAll(Collections.singletonList(one1)));
        assertFalse(testSet.containsAll(asList(one1, null)));
        assertFalse(testSet.removeAll(asList(one2, one3)));
        assertFalse(testSet.retainAll(asList(one1, null)));
        assertFalse(testSet.remove(one3));

        assertFalse(testSet.add(one1));

        //The set should now contain {one1, one2, one4}
        assertTrue(testSet.addAll(asList(one1, one2, one4)));
        assertFalse(testSet.addAll(asList(one2, one4)));
        assertFalse(testSet.add(one2));
        assertFalse(testSet.add(one4));
        assertFalse(testSet.isEmpty());
        assertEquals(3, testSet.size());

        List<Integer> expectedContent = asList(one1, one2, one4);
        Iterator<Integer> actualIterator = testSet.iterator();
        for (final Integer expectedInteger : expectedContent)
        {
            assertTrue(testSet.contains(expectedInteger));
            assertTrue(testSet.containsAll(Collections.singletonList(expectedInteger)));
            assertTrue(actualIterator.hasNext());
            assertSame(expectedInteger, actualIterator.next());
        }
        assertFalse(actualIterator.hasNext());
        assertTrue(testSet.containsAll(Collections.singletonList(one1)));
        assertTrue(testSet.containsAll(asList(one2, one1)));
        assertFalse(testSet.containsAll(asList(one3, one1, one2, one4)));
        assertFalse(testSet.removeAll(asList(one3, null, new Integer(1))));
        assertFalse(testSet.retainAll(asList(one1, one2, one3, one4)));
        assertFalse(testSet.remove(one3));

        //The set should not contain {one2, one4}.
        assertTrue(testSet.remove(one1));
        assertFalse(testSet.remove(one1));

        expectedContent = asList(one2, one4);
        actualIterator = testSet.iterator();
        for (final Integer expectedInteger : expectedContent)
        {
            assertTrue(testSet.contains(expectedInteger));
            assertTrue(testSet.containsAll(Collections.singletonList(expectedInteger)));
            assertTrue(actualIterator.hasNext());
            assertSame(expectedInteger, actualIterator.next());
        }

        //The set should not be empty.
        assertTrue(testSet.removeAll(asList(one1, one2, one3, one4)));
        assertTrue(testSet.isEmpty());
        assertFalse(testSet.iterator().hasNext());

        assertTrue(testSet.addAll(asList(one2, one3, one4)));

        expectedContent = asList(one2, one3);
        //The set should now be {one2, one3}.
        assertTrue(testSet.retainAll(asList(one1, one2, one3)));
        assertFalse(testSet.retainAll(asList(one1, one2, one3, new Double(20), new ArrayList<String>())));
        assertEquals(2, testSet.size());

        actualIterator = testSet.iterator();
        for (final Integer expectedInteger : expectedContent)
        {
            assertTrue(testSet.contains(expectedInteger));
            assertTrue(testSet.containsAll(Collections.singletonList(expectedInteger)));
            assertTrue(actualIterator.hasNext());
            assertSame(expectedInteger, actualIterator.next());
        }

        try
        {
            actualIterator.next();
            fail("Should not be able to iterate past the end of the set.");
        }
        catch (final NoSuchElementException ignored)
        {}

        actualIterator = testSet.iterator();
        try
        {
            actualIterator.remove();
            fail("Should not be able to remove.");
        }
        catch (final IllegalStateException ignored)
        {}

        final Iterator<Integer> expectedIterator = expectedContent.iterator();
        while (expectedIterator.hasNext())
        {
            final Integer expectedRemovedElement = expectedIterator.next();
            expectedIterator.remove();

            assertTrue(actualIterator.hasNext());
            final Integer actualRemovedElement = actualIterator.next();
            actualIterator.remove();
            assertSame(expectedRemovedElement, actualRemovedElement);

            assertEquals(expectedContent.size(), testSet.size());
            assertTrue(testSet.containsAll(expectedContent));
        }

        assertTrue(testSet.isEmpty());
    }

    private static <C> Collection<IdentitySet.IdentityReference<C>> convert(final Collection<? extends C> collection)
    {
        final List<IdentitySet.IdentityReference<C>> list = new ArrayList<IdentitySet.IdentityReference<C>>(collection.size());
        for (final C c : collection)
        {
            list.add(new IdentitySet.IdentityReference<C>(c));
        }
        return list;
    }

    private static <T> List<T> asList(final T... values)
    {
        return new ArrayList<T>(Arrays.asList(values));
    }
}
