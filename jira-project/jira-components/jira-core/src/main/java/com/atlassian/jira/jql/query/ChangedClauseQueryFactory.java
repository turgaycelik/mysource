package com.atlassian.jira.jql.query;

import java.io.IOException;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.util.lucene.ConstantScorePrefixQuery;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.operator.Operator;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

/**
 * Factory class for validating and building the Lucene Changed query.
 *
 * @since v5.0
 */
public class ChangedClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(ChangedClauseQueryFactory.class);

    private final SearchProviderFactory searchProviderFactory;
    private final HistoryPredicateQueryFactory changedPredicateQueryFactory;


    /**
     * @param searchProviderFactory factory for retrieving the history search provider
     * @param changedPredicateQueryFactory returns queries for the predicates
     */
    public ChangedClauseQueryFactory(final SearchProviderFactory searchProviderFactory,
            final HistoryPredicateQueryFactory changedPredicateQueryFactory)
    {
        this.searchProviderFactory = searchProviderFactory;
        this.changedPredicateQueryFactory = changedPredicateQueryFactory;
    }


    /**
     * @param searcher the {@link com.atlassian.crowd.embedded.api.User} representing the current searcher
     * @param clause the search cluase , for instance "Status was Open"
     * @return {@link com.atlassian.jira.jql.query.QueryFactoryResult} that wraps the  Lucene Query
     */
    public QueryFactoryResult create(final User searcher, final ChangedClause clause)
    {
        ConstantScoreQuery issueQuery;
        Query changedQuery = makeQuery(searcher, clause);
        IndexSearcher historySearcher = searchProviderFactory.getSearcher(SearchProviderFactory.CHANGE_HISTORY_INDEX);
        Set<String> issueIds;
        final IssueIdCollector collector = new IssueIdCollector(historySearcher.getIndexReader());
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Running Changed query (" + clause + "): " + changedQuery);
            }

            historySearcher.search(changedQuery, collector);
            final Set<String> queryIds = collector.getIssueIds();
            if (clause.getOperator() == Operator.CHANGED)
            {
                issueIds = queryIds;
            }
            else
            {
                final Set<String> allIssueIds = collector.getAllIssueIds();
                allIssueIds.removeAll(queryIds);
                issueIds = allIssueIds;
            }

            if (log.isDebugEnabled())
            {
                log.debug("History query returned: " + issueIds);
            }

            issueQuery = new ConstantScoreQuery(new IssueIdFilter(issueIds));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return new QueryFactoryResult(issueQuery);

    }

    private Query makeQuery(final User searcher, final ChangedClause clause)
    {
        final BooleanQuery outerQuery = new BooleanQuery();
        final BooleanQuery changedQuery = new BooleanQuery();
        final Query toQuery = createQuery(clause, DocumentConstants.CHANGE_FROM);
        if (clause.getPredicate() == null)
        {
            changedQuery.add(toQuery, BooleanClause.Occur.SHOULD);
        }
        else
        {
            BooleanQuery predicateQuery = changedPredicateQueryFactory.makePredicateQuery(searcher, clause.getField().toLowerCase(), clause.getPredicate(), true);
            changedQuery.add(predicateQuery, BooleanClause.Occur.MUST);
            changedQuery.add(toQuery, BooleanClause.Occur.MUST);
        }
        outerQuery.add(changedQuery, BooleanClause.Occur.SHOULD);
        return outerQuery;
    }


    private PrefixQuery createQuery(ChangedClause clause, String documentField)
    {
        return ConstantScorePrefixQuery.build(new Term(clause.getField().toLowerCase() + "." + documentField, "ch-"));
    }
}



