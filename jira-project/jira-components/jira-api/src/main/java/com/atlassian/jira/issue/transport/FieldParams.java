package com.atlassian.jira.issue.transport;

import com.atlassian.annotations.PublicApi;

import java.util.Map;
import java.util.Set;

/**
 * A parent interface for transport objects in JIRA. All FieldParams share the logic of a String key with a
 * multi-dimensional value. e.g. array, lists, FieldParams. Keys may be null. The interface does not mandate what objects
 * the multi-dimensional value contain. This is up to the implementers and sub-interfaces to mandate
 */
@PublicApi
public interface FieldParams
{
    Set<String> getAllKeys();

    Map<String, ?> getKeysAndValues();

    boolean containsKey(String key);

    boolean isEmpty();
}
