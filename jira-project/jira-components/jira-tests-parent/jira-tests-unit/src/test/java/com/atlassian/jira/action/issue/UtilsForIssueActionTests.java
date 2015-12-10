/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.issue;

import java.util.Collection;
import java.util.Iterator;

public class UtilsForIssueActionTests
{
    public static void printObjectArray(Collection valueArrays)
    {
        System.out.println("[");
        for (Iterator iterator = valueArrays.iterator(); iterator.hasNext();)
        {
            Object[] values = (Object[]) iterator.next();

            System.out.print("[");
            for (int i = 0; i < values.length; i++)
            {
                Object value = values[i];
                System.out.print(value);
                if ((i + 1) < values.length)
                    System.out.print(",");
            }
            System.out.println("]");
        }
        System.out.println("]");
    }

    public static boolean contains(Collection searchIn, Object[] find)
    {
        for (Iterator iterator = searchIn.iterator(); iterator.hasNext();)
        {
            Object[] objects = (Object[]) iterator.next();
            if (arraysEquals(objects, find))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean arraysEquals(Object[] mThis, Object[] mThat)
    {
        if (mThis.length == mThat.length)
        {
            for (int i = 0; i < mThis.length; i++)
            {
                if (!((mThis[i] == null) && (mThat[i] == null)))
                {
                    if ((mThis[i] == null) || (mThat[i] == null) || (!mThis[i].equals(mThat[i])))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }
}
