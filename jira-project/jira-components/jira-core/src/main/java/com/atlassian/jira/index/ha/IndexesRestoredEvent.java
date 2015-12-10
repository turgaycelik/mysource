package com.atlassian.jira.index.ha;

/**
 * The lucene indexes have been restored.
 *
 * @since v6.3
 */
public class IndexesRestoredEvent
{
    public static final IndexesRestoredEvent INSTANCE = new IndexesRestoredEvent();
}
