package com.atlassian.jira.issue.label;

import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import org.apache.commons.lang.StringUtils;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper to group labels into buckets for displaying them in alphabetical blocks.
 *
 * @since 4.2
 */
public class AlphabeticalLabelGroupingSupport
{
    private final Set<String> data;
    private MultiMap<String, String, Set<String>> alphabetBuckets = MultiMaps.create(new Supplier<Set<String>>()
    {
        public Set<String> get()
        {
            return new TreeSet<String>();
        }
    });
    public static final String NUMERIC = "0-9";
    private static final String[] KEYS = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
    private static final String LAST_KEY = KEYS[KEYS.length - 1];
    private static final int MIN_GROUP_SIZE = 10;

    private Collator collator = Collator.getInstance();

    public AlphabeticalLabelGroupingSupport(Set<String> labels)
    {
        this.data = labels;
        fillBuckets();
        mergeBuckets();
    }

    public Set<String> getContents(String key)
    {
        return alphabetBuckets.get(key);
    }

    public Collection<String> getKeys()
    {
        TreeSet<String> keys = new TreeSet<String>(new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                if (o1.equals(o2))
                {
                    return 0;
                }
                if (NUMERIC.equals(o1))
                {
                    return 1;
                }
                else if (NUMERIC.equals(o2))
                {
                    return -1;
                }
                else
                {
                    return collator.compare(o1, o2);
                }
            }
        });
        keys.addAll(alphabetBuckets.keySet());
        return keys;
    }

    private void fillBuckets()
    {
        for (String label : data)
        {
            char firstCharacter = extractFirstCharacter(label);
            if (Character.isLetter(firstCharacter))
            {
                alphabetBuckets.putSingle(Character.toUpperCase(firstCharacter) + "", label);
            }
            else if (Character.isDigit(firstCharacter))
            {
                alphabetBuckets.putSingle(NUMERIC, label);
            }
        }
    }

    // Merge content of buckets together if the number of items in one bucket is small.
    private void mergeBuckets()
    {
        int minGroupSize = alphabetBuckets.size() / KEYS.length + MIN_GROUP_SIZE;
        String lastBucketKey = KEYS[0];
        boolean flipLastBucketKey = true;
        Set<String> currentBucket = new TreeSet<String>();

        for (String currentKey : KEYS)
        {
            if (flipLastBucketKey)
            {
                lastBucketKey = currentKey;
                flipLastBucketKey = false;
            }
            final Set<String> currentLabels = alphabetBuckets.remove(currentKey + "");
            if (currentLabels != null)
            {
                currentBucket.addAll(currentLabels);
                if (currentBucket.size() >= minGroupSize)
                {
                    alphabetBuckets.put(getBucketKey(lastBucketKey, currentKey), currentBucket);
                    currentBucket = new TreeSet<String>();
                    flipLastBucketKey = true;
                }
            }
        }
        if (!currentBucket.isEmpty())
        {
            alphabetBuckets.put(getBucketKey(lastBucketKey, LAST_KEY), currentBucket);
        }
    }

    private String getBucketKey(final String lastBucketKey, final String currentKey)
    {
        if (lastBucketKey.equals(currentKey))
        {
            return lastBucketKey;
        }
        return lastBucketKey + "-" + currentKey;
    }


    private char extractFirstCharacter(String label)
    {
        char firstCharacter = '\u0000';
        if (StringUtils.isNotBlank(label))
        {
            firstCharacter = label.charAt(0);
        }
        return firstCharacter;
    }
}
