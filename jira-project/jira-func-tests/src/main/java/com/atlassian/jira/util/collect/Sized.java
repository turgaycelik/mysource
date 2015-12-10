package com.atlassian.jira.util.collect;

/**
 * Something that contains a number of items.
 * 
 * @since v3.13
 */
public interface Sized
{
    int size();

    boolean isEmpty();
}
