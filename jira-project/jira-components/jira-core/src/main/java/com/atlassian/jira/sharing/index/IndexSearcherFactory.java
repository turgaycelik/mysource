package com.atlassian.jira.sharing.index;

import org.apache.lucene.search.IndexSearcher;

/**
 * Responsible for getting a {@link IndexSearcher}. These should be closed after use in a finally block.
 */
interface IndexSearcherFactory
{
    IndexSearcher get();
}
