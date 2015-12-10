package com.atlassian.jira.util.index;

import com.atlassian.annotations.PublicApi;

/**
 * IndexingCounter provides access to a persistent 'count' of full re-indexes. This may be used by upgraded plugins
 * to determine whether the index is up to date for whatever custom field changes they have made. For example, an
 * upgrade task can record the current index value and a plugin can display a 're-index required' message until a full
 * re-index has occurred and the value has changed.
 *
 * @since v6.0
 */
@PublicApi
public interface IndexingCounter
{
    /**
     * This method returns the current re-indexing count.
     *
     * @return the value of the most recent re-index run
     */
    public long getCurrentValue();
}
