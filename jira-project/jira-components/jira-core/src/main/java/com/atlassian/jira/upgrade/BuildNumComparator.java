/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import java.util.Comparator;

public class BuildNumComparator implements Comparator<String>
{
    /**
     * @see Comparator
     */
    public int compare(final String o1, final String o2)
    {
        return getBuildNumber(o1).compareTo(getBuildNumber(o2));
    }

    /**
     * For a version, loop through & pull out numbers.  For the first '.' leave as is,
     * but for subsequent '.', add a '0' in its place. Parse the result into a Double.
     * <p>
     * This should handle '1.10' > '1.1.1' & '1.2beta3' == '1.2'
     * @param version
     */
    private Double getBuildNumber(final Object version)
    {
        final String versionString = (String) version;
        final StringBuilder sb = new StringBuilder(versionString.length()); // this will always be larger than what we need.
        boolean decSeen = false;
        forloop : for (int i = 0; i < versionString.toCharArray().length; i++)
        {
            final char c = versionString.toCharArray()[i];
            if (Character.isDigit(c))
            {
                sb.append(c);
            }
            else if (c == '.')
            {
                if (!decSeen)
                {
                    decSeen = true;
                    sb.append('.');
                }
                else
                {
                    sb.append('0');
                }
            }
            else
            {
                break forloop;
            }
        }

        final Double returnValue = Double.valueOf(sb.toString());
        return returnValue;
    }
}
