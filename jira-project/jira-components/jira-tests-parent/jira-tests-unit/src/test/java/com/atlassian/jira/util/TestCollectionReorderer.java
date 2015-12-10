/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestCollectionReorderer
{
    final String string1 = "item_1";
    final String string2 = "item_2";
    final String string3 = "item_3";
    final String string4 = "item_4";

    final static int MOVE_TO_START = 1;
    final static int MOVE_TO_END = 2;
    final static int DECREASE_POSITION = 3;
    final static int INCREASE_POSITION = 4;

    public void _testMove(final List<String> objects, final String toMove, final List<String> expected, final int moveType)
    {
        final CollectionReorderer<String> collectionReorderer = new CollectionReorderer<String>();
        switch (moveType)
        {
            case MOVE_TO_START:
                collectionReorderer.moveToStart(objects, toMove);
                break;

            case MOVE_TO_END:
                collectionReorderer.moveToEnd(objects, toMove);
                break;

            case DECREASE_POSITION:
                collectionReorderer.decreasePosition(objects, toMove);
                break;

            case INCREASE_POSITION:
                collectionReorderer.increasePosition(objects, toMove);
                break;

            default:
                fail("Unknown operation type.");
                break;
        }

        assertEquals(4, objects.size());
        assertEquals(expected.get(0), objects.get(0));
        assertEquals(expected.get(1), objects.get(1));
        assertEquals(expected.get(2), objects.get(2));
        assertEquals(expected.get(3), objects.get(3));
    }

    @Test
    public void testMoveToStart()
    {
        List<String> strings = Lists.newArrayList(string1, string2, string3, string4);
        List<String> expected = Lists.newArrayList(string1, string2, string3, string4);
        _testMove(strings, string1, expected, MOVE_TO_START);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string2, string1, string3, string4);
        _testMove(strings, string2, expected, MOVE_TO_START);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string3, string1, string2, string4);
        _testMove(strings, string3, expected, MOVE_TO_START);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string4, string1, string2, string3);
        _testMove(strings, string4, expected, MOVE_TO_START);
    }

    @Test
    public void testMoveToEnd()
    {
        List<String> strings = Lists.newArrayList(string1, string2, string3, string4);
        List<String> expected = Lists.newArrayList(string2, string3, string4, string1);
        _testMove(strings, string1, expected, MOVE_TO_END);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string3, string4, string2);
        _testMove(strings, string2, expected, MOVE_TO_END);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string2, string4, string3);
        _testMove(strings, string3, expected, MOVE_TO_END);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string2, string3, string4);
        _testMove(strings, string4, expected, MOVE_TO_END);
    }

    @Test
    public void testDecreasePosition()
    {
        List<String> strings = Lists.newArrayList(string1, string2, string3, string4);
        List<String> expected = Lists.newArrayList(string2, string1, string3, string4);
        _testMove(strings, string1, expected, DECREASE_POSITION);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string3, string2, string4);
        _testMove(strings, string2, expected, DECREASE_POSITION);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string2, string4, string3);
        _testMove(strings, string3, expected, DECREASE_POSITION);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string2, string3, string4);
        _testMove(strings, string4, expected, DECREASE_POSITION);

    }

    @Test
    public void testIncreasePosition()
    {
        List<String> strings = Lists.newArrayList(string1, string2, string3, string4);
        List<String> expected = Lists.newArrayList(string1, string2, string3, string4);
        _testMove(strings, string1, expected, INCREASE_POSITION);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string2, string1, string3, string4);
        _testMove(strings, string2, expected, INCREASE_POSITION);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string3, string2, string4);
        _testMove(strings, string3, expected, INCREASE_POSITION);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string2, string4, string3);
        _testMove(strings, string4, expected, INCREASE_POSITION);

    }

    @Test
    public void testMoveToCollection()
    {
        final CollectionReorderer<String> collectionReorderer = new CollectionReorderer<String>();

        // Test nothing to do
        List<String> strings = Lists.newArrayList(string1, string2, string3, string4);
        List<String> expected = Lists.newArrayList(string1, string2, string3, string4);
        collectionReorderer.moveToPosition(strings, 1, 1);
        assertEquals(expected, strings);

        // Test moving to position
        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string2, string3, string1, string4);
        collectionReorderer.moveToPosition(strings, 0, 2);
        assertEquals(expected, strings);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string3, string2, string4);
        collectionReorderer.moveToPosition(strings, 1, 2);
        assertEquals(expected, strings);

        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string2, string3, string4, string1);
        collectionReorderer.moveToPosition(strings, 0, 3);
        assertEquals(expected, strings);

        // Reverse direction
        strings = Lists.newArrayList(string1, string2, string3, string4);
        expected = Lists.newArrayList(string1, string4, string2, string3);
        collectionReorderer.moveToPosition(strings, 3, 1);
        assertEquals(expected, strings);
    }
}
