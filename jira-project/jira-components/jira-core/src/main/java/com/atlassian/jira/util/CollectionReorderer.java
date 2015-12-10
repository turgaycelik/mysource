/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CollectionReorderer<T>
{
    public void moveToStart(final List<T> objects, final T toMove)
    {
        assertContains(objects, toMove);
        objects.remove(toMove);
        objects.add(0, toMove);
    }

    public void moveToEnd(final List<T> objects, final T toMove)
    {
        assertContains(objects, toMove);
        objects.remove(toMove);
        objects.add(objects.size(), toMove);
    }

    public void decreasePosition(final List<T> objects, final T toMove)
    {
        assertContains(objects, toMove);
        final int initialPosition = objects.indexOf(toMove);
        if ((initialPosition < 0) || (initialPosition == (objects.size() - 1)))
        {
            return;
        }
        moveToPosition(objects, initialPosition, initialPosition + 1);
    }

    public void increasePosition(final List<T> objects, final T toMove)
    {
        assertContains(objects, toMove);
        final int initialPosition = objects.indexOf(toMove);
        if (initialPosition < 1)
        {
            return;
        }
        moveToPosition(objects, initialPosition, initialPosition - 1);
    }

    /**
     * Move the 'toMove' to the position after the 'target'. To insert to the head of the list set afterThis to null
     * @param objects list of objects to look up and order in
     * @param toMove the object to move
     * @param target the position to move to
     */
    public void moveToPositionAfter(final List<T> objects, final T toMove, final T target)
    {
        assertContains(objects, toMove);
        final int initialPosition = objects.indexOf(toMove);

        //if null move the object to the first position
        int targetPosition;
        if (target == null)
        {
            targetPosition = -1;
        }
        else
        //move the object to where the target object is
        {
            assertContains(objects, target);
            targetPosition = objects.indexOf(target);
        }

        //if target position is before the initial position, add 1 so that it is added after the target object
        if (targetPosition < initialPosition)
        {
            targetPosition++;
        }
        moveToPosition(objects, initialPosition, targetPosition);
    }

    /**
     * Moves multiple objects in the objects list to given destination indexes
     *
     * @param objects           the list of objects
     * @param positionToObjects a naturally sorted map with destination indexes as keys
     *                          and the objects to move as values
     */
    public void moveToPosition(final List<T> objects, final Map<Integer, T> positionToObjects)
    {
        for (final T o : positionToObjects.values())
        {
            objects.remove(o);
        }

        for (final Map.Entry<Integer, T> entry : positionToObjects.entrySet())
        {
            objects.add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * If moving more than one object at the same time please use {@link #moveToPosition(java.util.List, java.util.Map)}
     * <p/>
     * Moves an object at initialPosition index in the objects list to the targetPosition index
     * </p>
     *
     * @param objects         the list of objects to modify
     * @param initialPosition the current index of the object that should be moved in the objects list
     * @param targetPosition  the destination index
     */
    public void moveToPosition(final List<T> objects, final int initialPosition, final int targetPosition)
    {
        final int objectsSize = objects.size();

        // If target position is outside the collection
        final boolean outsideTarget = (targetPosition < 0) || (targetPosition >= objectsSize);

        // If initial position is outside the collection
        final boolean outsideInitial = (initialPosition < 0) || (initialPosition >= objectsSize);

        if (!outsideTarget && !outsideInitial)
        {
            objects.add(targetPosition, objects.remove(initialPosition));
        }
    }

    private void assertContains(final Collection<T> objects, final T o)
    {
        if (!objects.contains(o))
        {
            throw new IllegalArgumentException("Object " + o + " not contained in Collection " + objects);
        }
    }
}
