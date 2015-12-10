package com.atlassian.query.lucene.parsing;

import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.index.JiraAnalyzer;
import com.atlassian.jira.issue.index.indexers.phrase.PhraseQuerySupportField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.CharStream;
import org.apache.lucene.queryParser.ExtendedQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParserTokenManager;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class DefaultLuceneQueryParserFactory implements LuceneQueryParserFactory
{
    @Override
    public QueryParser createParserFor(final String fieldName)
    {
        return new LuceneQueryParser(LuceneVersion.get(), fieldName, JiraAnalyzer.ANALYZER_FOR_SEARCHING);
    }

    /**
     * Customises the default query parser provided by Lucene with behaviour specific to JIRA.
     *
     * <ul>
     *     <li>
     *         When quotes appear in a the query string, the text analysed so that an &quot;exact&quot;
     *         {@link org.apache.lucene.search.PhraseQuery} is constructed by the parser.
     *         <br/>
     *         The current definition of &quot;exact&quot; is that no stemming will be performed during analysis.
     *     </li>
     * </ul>
     *
     * To create an instance, please use the {@link com.atlassian.query.lucene.parsing.LuceneQueryParserFactory}
     *
     * @since v6.0.7
     */
    static class LuceneQueryParser extends ExtendedQueryParser
    {
        public LuceneQueryParser(final Version matchVersion, final String fieldName, final Analyzer analyzer)
        {
            super(matchVersion, fieldName, analyzer);
        }

        protected LuceneQueryParser(final CharStream stream)
        {
            super(stream);
        }

        protected LuceneQueryParser(final QueryParserTokenManager tm)
        {
            super(tm);
        }

        @Override
        protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException
        {
            if (quoted)
            {
                return newFieldQuery(JiraAnalyzer.ANALYZER_FOR_EXACT_SEARCHING, PhraseQuerySupportField.forIndexField(field), queryText, quoted);
            }

            return super.getFieldQuery(field, queryText, quoted);
        }

    }
}
