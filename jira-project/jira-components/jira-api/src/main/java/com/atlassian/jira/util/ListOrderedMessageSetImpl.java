package com.atlassian.jira.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Is a message set that keeps the messages and warnings in the order in which they were added.
 *
 * @since v3.13
 */
public class ListOrderedMessageSetImpl extends AbstractMessageSet
{
    public ListOrderedMessageSetImpl()
    {
        super(new LinkedHashMap<String, MessageLink>(), new LinkedHashMap<String, MessageLink>(), new LinkedHashSet<String>(), new LinkedHashSet<String>());
    }
}
