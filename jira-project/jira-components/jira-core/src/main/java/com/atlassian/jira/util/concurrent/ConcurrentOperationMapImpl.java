package com.atlassian.jira.util.concurrent;

/**
 * @deprecated since v4.0 use {@link com.atlassian.util.concurrent.ConcurrentOperationMapImpl} directly
 */
@Deprecated
public class ConcurrentOperationMapImpl<K, R> extends com.atlassian.util.concurrent.ConcurrentOperationMapImpl<K, R> implements ConcurrentOperationMap<K, R>
{}
