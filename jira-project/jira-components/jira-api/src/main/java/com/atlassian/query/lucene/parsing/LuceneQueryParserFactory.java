package com.atlassian.query.lucene.parsing;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.InjectableComponent;
import org.apache.lucene.queryParser.QueryParser;

/**
 * A factory to obtain a Lucene {@link QueryParser} instance.
 *
 * @since v6.0.7
 */
@Internal
@InjectableComponent
public interface LuceneQueryParserFactory
{
    /**
     * Creates a query parser instance.
     *
     * @param fieldName the default field to be used by the query parser for query terms.
     * @return A query parser instance.
     */
    QueryParser createParserFor(String fieldName);
}
