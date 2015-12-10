package com.atlassian.jira.util.collect;

/*
 *  Copyright 1999-2004,2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.Predicate;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Decorates another {@link Iterator} using a predicate to filter elements.
 * <p>
 * This iterator decorates the underlying iterator, only allowing through
 * those elements that match the specified {@link Predicate Predicate}.
 *
 * @since Commons Collections 1.0
 * @version $Revision: 366576 $ $Date: 2006-01-06 22:07:07 +0000 (Fri, 06 Jan 2006) $
 *
 * @author James Strachan
 * @author Jan Sorensen
 * @author Ralph Wagner
 * @author Stephen Colebourne
 */
class FilteredIterator<T> implements Iterator<T>
{

    /** The iterator being used */
    private final Iterator<T> iterator;
    /** The predicate being used */
    private final Predicate<? super T> predicate;
    /** The next object in the iteration */
    private T nextObject;
    /** Whether the next object has been calculated yet */
    private boolean nextObjectSet = false;

    //-----------------------------------------------------------------------

    /**
     * Constructs a new <code>FilterIterator</code> that will use the
     * given iterator and predicate.
     *
     * @param iterator  the iterator to use
     * @param predicate  the predicate to use
     */
    FilteredIterator(@Nonnull final Iterator<T> iterator, @Nonnull final Predicate<? super T> predicate)
    {
        this.iterator = notNull("iterator", iterator);
        this.predicate = notNull("predicate", predicate);
    }

    //-----------------------------------------------------------------------

    /**
     * Returns true if the underlying iterator contains an object that
     * matches the predicate.
     *
     * @return true if there is another object that matches the predicate
     * @throws NullPointerException if either the iterator or predicate are null
     */
    public boolean hasNext()
    {
        if (nextObjectSet)
        {
            return true;
        }
        else
        {
            return setNextObject();
        }
    }

    /**
     * Returns the next object that matches the predicate.
     *
     * @return the next object which matches the given predicate
     * @throws NullPointerException if either the iterator or predicate are null
     * @throws NoSuchElementException if there are no more elements that
     *  match the predicate
     */
    public T next()
    {
        if (!nextObjectSet)
        {
            if (!setNextObject())
            {
                throw new NoSuchElementException();
            }
        }
        nextObjectSet = false;
        return nextObject;
    }

    /**
     * Removes from the underlying collection of the base iterator the last
     * element returned by this iterator.
     * This method can only be called
     * if <code>next()</code> was called, but not after
     * <code>hasNext()</code>, because the <code>hasNext()</code> call
     * changes the base iterator.
     *
     * @throws IllegalStateException if <code>hasNext()</code> has already
     *  been called.
     */
    public void remove()
    {
        if (nextObjectSet)
        {
            throw new IllegalStateException("remove() cannot be called");
        }
        iterator.remove();
    }

    //-----------------------------------------------------------------------

    /**
     * Set nextObject to the next object. If there are no more
     * objects then return false. Otherwise, return true.
     */
    private boolean setNextObject()
    {
        while (iterator.hasNext())
        {
            final T object = iterator.next();
            if (predicate.evaluate(object))
            {
                nextObject = object;
                nextObjectSet = true;
                return true;
            }
        }
        return false;
    }
}
