package com.atlassian.jira.event.config;

import com.atlassian.jira.event.AbstractEvent;

import java.util.Map;

/**
 * Represents the consequence of an ApplicationProperty being set to a new value.
 *
 * @since v4.4
 */
public class ApplicationPropertyChangeEvent extends AbstractEvent
{
    public static final String KEY_METADATA = "metadata";
    public static final String KEY_OLD_VALUE = "oldValue";
    public static final String KEY_NEW_VALUE = "newValue";


    /**
     * Expects parameters to include:
     * metadata: the instance of ApplicationPropertyMetadata that represents the property being changed
     * oldValue: the string value before the change
     * newValue: the string value after the change (this is not guaranteed to be different to oldValue by the publisher)
     * @param params the params as described above.
     */
    public ApplicationPropertyChangeEvent(Map params)
    {
        super(params);
    }

}
