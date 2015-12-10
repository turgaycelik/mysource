package com.atlassian.jira.util;

import com.atlassian.annotations.PublicApi;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Message set implementation that uses the messages natural ordering to sort the messages that are added.
 * @since v3.13
 */
@PublicApi
public class MessageSetImpl extends AbstractMessageSet
{
    public MessageSetImpl()
    {
        super(new TreeMap<String, MessageLink>(), new TreeMap<String, MessageLink>(), new TreeSet<String>(), new TreeSet<String>());
    }
}
