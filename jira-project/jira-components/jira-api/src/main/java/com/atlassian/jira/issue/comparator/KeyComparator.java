package com.atlassian.jira.issue.comparator;

import java.util.Comparator;

public class KeyComparator implements Comparator<String>
{
    public static final Comparator<String> COMPARATOR = new KeyComparator();

    private KeyComparator()
    {}

    public int compare(final String key1, final String key2)
    {
        if (key1 == key2)
        {
            return 0;
        }
        else if (key1 == null)
        {
            return 1;
        }
        else if (key2 == null)
        {
            return -1;
        }

        final int index1 = key1.lastIndexOf('-');
        final int index2 = key2.lastIndexOf('-');

        // issue key may not have project key
        // data imported from Bugzilla may not comply with atlassian issue key format
        // this added to make jira more fault tolerant
        if ((index1 == -1) && (index2 == -1))
        {
            return 0;
        }
        else if (index1 == -1)
        {
            return 1;
        }
        else if (index2 == -1)
        {
            return -1;
        }

        // compare the project part (up until the first '-')
        for (int i = 0; i < Math.min(index1, index2); i++)
        {
            final char c1 = key1.charAt(i);
            final char c2 = key2.charAt(i);
            if (c1 != c2)
            {
                // This is a different project, do unicode char comparison
                return (c1 < c2) ? -1 : 1;
            }
        }

        // if one of the project parts is shorter than the other, that will be less than
        if (index1 != index2)
        {
            return (index1 < index2) ? -1 : 1;
        }

        // Same project, compare numbers
        return compareNumPart(key1, key2);
    }

    private int compareNumPart(final String key1, final String key2)
    {

        // As we know project part is the same, we can just compare using a
        // string comparator if they are the same length
        if (key1.length() == key2.length())
        {
            return key1.compareTo(key2);
        }
        // Else the longer one will be the bigger number
        return (key1.length() > key2.length()) ? 1 : -1;
    }
}
