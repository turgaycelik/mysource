package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comments.util.CommentIterator;
import com.atlassian.jira.issue.comments.util.LuceneCommentIterator;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.issue.statistics.util.FieldHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.query.ChangedClauseQueryFactory;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.DateEqualityQueryFactory;
import com.atlassian.jira.jql.query.DateRelationalQueryFactory;
import com.atlassian.jira.jql.query.DefaultLuceneQueryBuilder;
import com.atlassian.jira.jql.query.GenericClauseQueryFactory;
import com.atlassian.jira.jql.query.LuceneQueryBuilder;
import com.atlassian.jira.jql.query.OperatorSpecificQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.query.QueryRegistry;
import com.atlassian.jira.jql.query.WasClauseQueryFactory;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultRecentCommentManager implements RecentCommentManager
{
    private static final Logger log = Logger.getLogger(DefaultRecentCommentManager.class);

    private final CommentService commentService;
    private final SearchProviderFactory searchProviderFactory;
    private final SearchProvider searchProvider;
    private final SearchService searchService;
    private final LuceneQueryBuilder luceneQueryBuilder;

    public DefaultRecentCommentManager(final CommentService commentService, final SearchProviderFactory searchProviderFactory,
            final SearchProvider searchProvider, final JqlDateSupport jqlDateSupport, final JqlOperandResolver operandResolver,
            final SearchService searchService, final LuceneQueryModifier luceneQueryModifier,
            final WasClauseQueryFactory wasClauseQueryFactory, final ChangedClauseQueryFactory changedClauseQueryFactory)
    {
        this.commentService = notNull("commentService", commentService);
        this.searchProviderFactory = notNull("searchProviderFactory", searchProviderFactory);
        this.searchProvider = notNull("searchProvider", searchProvider);
        this.searchService = notNull("searchService", searchService);
        final QueryRegistry queryRegistry = createSingletonRegistry(createClauseQueryFactory(notNull("jqlDateSupport", jqlDateSupport), notNull("operandResolver", operandResolver)));
        this.luceneQueryBuilder = new DefaultLuceneQueryBuilder(queryRegistry, luceneQueryModifier, wasClauseQueryFactory, changedClauseQueryFactory);
    }

    @Override
    public CommentIterator getRecentComments(SearchRequest searchRequest, User user) throws SearchException
    {
        return getRecentComments(searchRequest, ApplicationUsers.from(user));
    }

    @Override
    public CommentIterator getRecentComments(SearchRequest searchRequest, ApplicationUser user) throws SearchException
    {
        notNull("searchRequest", searchRequest);
        final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);
        return new LuceneCommentIterator(user, commentService, getCommentsHits(searchRequest, user, searcher), searcher);
    }

    private TopFieldDocs getCommentsHits(final SearchRequest request, final ApplicationUser user, IndexSearcher searcher) throws SearchException
    {
        final BooleanQuery commentQuery = new BooleanQuery();

        commentQuery.add(createIssueIdQuery(request, user), BooleanClause.Occur.MUST);

        final org.apache.lucene.search.Query dateQuery = getDateClauses(request, user);
        if (dateQuery != null)
        {
            commentQuery.add(dateQuery, BooleanClause.Occur.MUST);
        }

        try
        {

            return searcher.search(commentQuery, Integer.MAX_VALUE,
                new Sort(new SortField[] { new SortField(DocumentConstants.COMMENT_UPDATED, SortField.STRING, true) }));
        }

        catch (final IOException e)
        {
            log.error("Failed to create LuceneCommentIterator", e);
            return null;
        }
    }

    private BooleanQuery createIssueIdQuery(final SearchRequest request, final ApplicationUser user)
    {
        final Collection<String> issueIds;
        try
        {
            issueIds = getIssueIds(request, user);
        }
        catch (Exception e)
        {
            throw new DataAccessException(e);
        }

        // Create a 'boolean' comment query
        final BooleanQuery commentIdQuery = new BooleanQuery();
        for (final String issueId : issueIds)
        {
            final Term term = new Term(DocumentConstants.ISSUE_ID, issueId);
            commentIdQuery.add(new TermQuery(term), BooleanClause.Occur.SHOULD);
        }
        return commentIdQuery;
    }

    private Collection<String> getIssueIds(SearchRequest searchRequest, final ApplicationUser user) throws IOException, SearchException
    {
        final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        final FieldHitCollector hitCollector = new FieldHitCollector(searcher, DocumentConstants.ISSUE_ID);
        searchProvider.search((searchRequest != null) ? searchRequest.getQuery() : null, user, hitCollector);
        return new LinkedHashSet<String>(hitCollector.getValues());
    }

    private org.apache.lucene.search.Query getDateClauses(final SearchRequest searchRequest, final ApplicationUser user) throws SearchException
    {
        final Query query = searchRequest.getQuery();
        if (!searchService.doesQueryFitFilterForm(user == null ? null : user.getDirectoryUser(), query))
        {
            return null;
        }

        if (query.getWhereClause() == null)
        {
            return null;
        }

        //look for all the updated date clauses.
        final SimpleNavigatorCollectorVisitor collectingVisitor = new SimpleNavigatorCollectorVisitor(SystemSearchConstants.forUpdatedDate().getJqlClauseNames().getJqlFieldNames());
        query.getWhereClause().accept(collectingVisitor);
        if (collectingVisitor.isValid())
        {
            //lets group all the "updated date" conditions to the query.
            final List<TerminalClause> list = collectingVisitor.getClauses();
            if (!list.isEmpty())
            {
                //create a comment based query.
                return luceneQueryBuilder.createLuceneQuery(new QueryCreationContextImpl(user), createCommentQuery(list));
            }
        }
        else
        {
            log.debug("Unable to add updated date to comment query.");
        }

        return null;
    }

    private static Clause createCommentQuery(final List<TerminalClause> list)
    {
        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        for (TerminalClause clause : list)
        {
            builder.addCondition(DocumentConstants.COMMENT_UPDATED, clause.getOperator(), clause.getOperand());
        }
        return builder.buildClause();
    }

    private static GenericClauseQueryFactory createClauseQueryFactory(final JqlDateSupport dateSupport, final JqlOperandResolver operandResolver)
    {
        // Use the DateQueryFactory but create a Lucene Query for the Comment Updated field instead of the regular
        // Updated Date field. This is a bit of a hack but allows massive reuse of the DateQueryFactory parsing of
        // JQL date clauses.

        final List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>(2);
        operatorFactories.add(new DateEqualityQueryFactory(dateSupport));
        operatorFactories.add(new DateRelationalQueryFactory(dateSupport));
        return new GenericClauseQueryFactory(DocumentConstants.COMMENT_UPDATED, operatorFactories, operandResolver);
    }

    private static QueryRegistry createSingletonRegistry(final ClauseQueryFactory factory)
    {
        notNull("factory", factory);
        final List<ClauseQueryFactory> clauseQueryFactory = Collections.singletonList(factory);
        return new QueryRegistry()
        {
            public Collection<ClauseQueryFactory> getClauseQueryFactory(final QueryCreationContext queryCreationContext, final TerminalClause clause)
            {
                if (DocumentConstants.COMMENT_UPDATED.equalsIgnoreCase(clause.getName()))
                {
                    return clauseQueryFactory;
                }
                else
                {
                    return null;
                }
            }
        };
    }
}
