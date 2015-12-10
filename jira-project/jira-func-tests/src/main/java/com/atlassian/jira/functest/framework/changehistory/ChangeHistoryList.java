package com.atlassian.jira.functest.framework.changehistory;

import junit.framework.AssertionFailedError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This represents a simple object model for testing change history.  A ChangeHistoryList
 * is a list of {@link ChangeHistorySet} objects.
 * <p/>
 * The order of change history field items is database dependant and hence the order of the items
 * can be gauranteed.  Hence the name {@link ChangeHistorySet}
 * <p/>
 * However the order of the each set of changes is time dependant and hence can be represented by a list
 * hence the name {ChangeHistoryList}.
 * <p/>
 * You can use this class using a fluent object notation like this :
 * <pre>
 *      ChangeHistoryList expectedList = new ChangeHistoryList();
 *      expectedList.addChangeSet("Administrator")
 *          .add("Time Spent", "1 hour [ 3600 ]")
 *          .add("Remaining Estimate", "1 week [ 604800 ]");
 *      expectedList.addChangeSet("Administrator")
 *          .add("Time Spent", "2 hours [ 7200 ]")
 *          .add("Remaining Estimate", "3 days [ 259200 ]");
 *       expectedList.addChangeSet("Administrator")
 *          .add("Time Spent", "1 day, 2 hours [ 93600 ] ")
 *          .add("Remaining Estimate", "2 days [ 172800 ]");
 *      expectedList.addChangeSet("Administrator")
 *          .add("Time Spent", "2 days, 2 hours [ 180000 ] ");
 *      try
 *      {
 *          ChangeHistoryList actualList = ChangeHistoryParser.getChangeHistory(getDialog());
 *          actualList.assertContainsChangeHistory(expectedList);
 *      }
 *      catch (Error e)
 *      {
 *          dumpResponse(e);
 *          throw e;
 *      }
 * </pre>
 *
 * @since v3.13
 */
public class ChangeHistoryList implements List
{
    private final List<ChangeHistorySet> changeHistoryList = new ArrayList<ChangeHistorySet>();

    public ChangeHistorySet addChangeSet(String changedBy)
    {
        ChangeHistorySet set = new ChangeHistorySet(this, changedBy);
        changeHistoryList.add(set);
        return set;
    }


    /**
     * Asserts that this {@link ChangeHistoryList} contains the list of {@link ChangeHistorySet ) objects in
     * expectedChangeHistoryList. Note that this is a "contains" test and hence it may have more change history than
     * specified in expectedChangeHistoryList  but not less.
     *
     * @param expectedChangeHistoryList the list of expected ChangeHistoryEntry items
     *
     * @throws AssertionFailedError if this {@link ChangeHistoryList } does not contain all of the expected change
     *                              history
     */
    public void assertContainsChangeHistory(ChangeHistoryList expectedChangeHistoryList)
    {
        for (final Object o : expectedChangeHistoryList)
        {
            ChangeHistorySet set = (ChangeHistorySet) o;
            if (!changeHistoryList.contains(set))
            {
                ChangeHistorySet setWithHeader = addHeaderRow(set);


                if (!changeHistoryList.contains(setWithHeader))
                {
                    throw new AssertionFailedError("Expected change history : \n" + expectedChangeHistoryList.toString() +
                            "\nActual change history : \n" + this.toString());
                }
            }
        }
    }

    private ChangeHistorySet addHeaderRow(final ChangeHistorySet set)
    {
        // this could have failed because the caller did not include the "header" row that will appear only in the first set.
        // try adding the header row and see if this helps
        ChangeHistorySet setWithHeader = new ChangeHistorySet(set);
        setWithHeader.add("Field", "Original Value", "New Value");
        return setWithHeader;
    }


    public void assertContainsSomeOf(ChangeHistoryList expectedChangeHistoryList)
    {
        for (Object o : expectedChangeHistoryList)
        {
            ChangeHistorySet expectedChangeHistorySet = (ChangeHistorySet) o;            
            if (!this.containsSomeOf(expectedChangeHistorySet))
            {
                ChangeHistorySet setWithHeader = addHeaderRow(expectedChangeHistorySet);

                if (!this.containsSomeOf(setWithHeader))
                {
                    throw new AssertionFailedError("Expected partial change history : \n\n" + expectedChangeHistoryList.toString() +
                            "\n\nActual change history : \n" + this.toString());
                }
            }
        }
    }

    /**
     * Returns true if this ChangeHistoryList contains some of the provided ChangeHistorySet
     *
     * @param changeHistorySet the ChangeHistorySet to see if we contain some of it
     * @return true if we contain some of the ChangeHistorySet 
     */
    public boolean containsSomeOf(ChangeHistorySet changeHistorySet)
    {
        for (final Object o : changeHistoryList)
        {
            ChangeHistorySet set = (ChangeHistorySet) o;
            if (set.isSuperSetOf(changeHistorySet))
            {
                return true;
            }
        }
        return false;
    }

    public int size()
    {
        return changeHistoryList.size();
    }

    public boolean isEmpty()
    {
        return changeHistoryList.isEmpty();
    }

    public boolean contains(Object o)
    {
        return changeHistoryList.contains(o);
    }

    public Iterator iterator()
    {
        return changeHistoryList.iterator();
    }

    public Object[] toArray()
    {
        return changeHistoryList.toArray();
    }

    public Object[] toArray(Object[] a)
    {
        return changeHistoryList.toArray(a);
    }

    public boolean add(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection c)
    {
        return changeHistoryList.containsAll(c);
    }

    public boolean addAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o)
    {
        return changeHistoryList.equals(o);
    }

    public int hashCode()
    {
        return changeHistoryList.hashCode();
    }

    public Object get(int index)
    {
        return changeHistoryList.get(index);
    }

    public Object set(int index, Object element)
    {
        throw new UnsupportedOperationException();
    }

    public void add(int index, Object element)
    {
        throw new UnsupportedOperationException();
    }

    public Object remove(int index)
    {
        throw new UnsupportedOperationException();
    }

    public int indexOf(Object o)
    {
        return changeHistoryList.indexOf(o);
    }

    public int lastIndexOf(Object o)
    {
        return changeHistoryList.lastIndexOf(o);
    }

    public ListIterator listIterator()
    {
        return changeHistoryList.listIterator();
    }

    public ListIterator listIterator(int index)
    {
        return changeHistoryList.listIterator(index);
    }

    public List subList(int fromIndex, int toIndex)
    {
        return changeHistoryList.subList(fromIndex, toIndex);
    }


    public String toString()
    {
        StringBuilder sb = new StringBuilder("ChangeHistoryList [").append(changeHistoryList.size()).append("]");
        for (final ChangeHistorySet changeHistorySet : changeHistoryList)
        {
            sb.append("\n\t");
            sb.append(changeHistorySet.toString());
        }
        return sb.toString();
    }
}
