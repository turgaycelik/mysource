package com.atlassian.jira.index;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Supplier;

/**
 * Implementations determine how we do multi-threading for reindex-all.
 */
public interface IndexingStrategy extends Function<Supplier<Result>, Result>, Closeable
{}