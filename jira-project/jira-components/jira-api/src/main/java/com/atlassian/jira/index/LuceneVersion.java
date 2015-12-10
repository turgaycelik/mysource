package com.atlassian.jira.index;

import com.atlassian.annotations.Internal;
import org.apache.lucene.util.Version;

/**
 * This is the value used by JIRA when it interacts with Apache Lucene classes.
 *
 * @see Version
 * @since v6.0.5
 */
@Internal
public class LuceneVersion
{
    /*
        Keep the private field and the public accessor method. Otherwise, this will be inlined as it will be considered
        to be a compile time constant.

        See JLS 13.4.9
    */
    private static Version value = Version.LUCENE_30;

    /**
     * Gets the value used by JIRA when it interacts with Apache Lucene classes.
     * @return A Version instance.
     */
    public static Version get()
    {
        return value;
    }
}
