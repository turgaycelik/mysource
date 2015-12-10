package com.atlassian.jira.scheme;

import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * This is a Comparator for generic value schemes that sorts by name ignoring case. 
 */
public class SchemeGVNameComparator implements Comparator
{
    private static final SchemeGVNameComparator schemeGVNameComparator = new SchemeGVNameComparator();

    public static SchemeGVNameComparator getInstance()
    {
        return schemeGVNameComparator;
    }

    public int compare(Object o1, Object o2)
    {
        if (o1 != null && o2 != null)
        {
            GenericValue scheme1 = (GenericValue) o1;
            GenericValue scheme2 = (GenericValue) o2;

            String lcName1 = scheme1.getString("name").toLowerCase();
            String lcName2 = scheme2.getString("name").toLowerCase();

            return lcName1.compareTo(lcName2);
        }
        return 0;
    }
}
