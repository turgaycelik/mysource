package com.atlassian.jira.issue.transport;

/**
 * Field params where all values are collections of Strings
 */
public interface StringParams extends CollectionParams
{
    String getFirstValueForNullKey();
    String getFirstValueForKey(String key);
}
