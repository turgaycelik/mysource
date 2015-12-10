package com.atlassian.jira.bc.issue.search;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.lucene.ConstantScorePrefixQuery;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.emptyList;

/**
 * Abstract convienience class with common methods for IssuePickerSearchProviers
 */
public abstract class AbstractIssuePickerSearchProvider implements IssuePickerSearchProvider
{
    private static final Logger log = Logger.getLogger(AbstractIssuePickerSearchProvider.class);

    private final SearchProvider searchProvider;
    private final ConstantsManager constantsManager;
    private final LuceneQueryModifier modifier;

    final Analyzer keyAnalyzer = new WhitespaceAnalyzer();
    Analyzer summaryAnalyzer = DefaultIndexManager.ANALYZER_FOR_SEARCHING;

    protected AbstractIssuePickerSearchProvider(final SearchProvider searchProvider, final ConstantsManager constantsManager,
            final LuceneQueryModifier modifier)
    {
        this.searchProvider = searchProvider;
        this.constantsManager = constantsManager;
        this.modifier = modifier;
    }

    /**
     * @see IssuePickerSearchProvider
     */
    public IssuePickerResults getResults(final JiraServiceContext context, final IssuePickerSearchService.IssuePickerParameters issuePickerParams, final int maxIssueCount)
    {
        Collection<Issue> results = emptyList();
        Collection<String> keyTerms = emptyList();
        Collection<String> summaryTerms = emptyList();

        final SearchRequest currentSearch = getRequest(issuePickerParams);
        int total = -1;

        if (currentSearch == null)
        {
            return new IssuePickerResults(results, 0, keyTerms, summaryTerms, getLabelKey(), getId());
        }

        try
        {
            final String query = StringUtils.trimToNull(issuePickerParams.getQuery());

            keyTerms = new LinkedHashSet<String>();
            summaryTerms = new LinkedHashSet<String>();

            //Create the filter to find the issues.
            org.apache.lucene.search.Query filterQuery = createQuery(query, keyTerms, summaryTerms);
            //Extend the filter to limit the issues based on the the picker parameters (e.g. must be in a particular project; must be a subtask;...)
            filterQuery = addFilterToQuery(issuePickerParams, filterQuery);
            //Rewrite the query to ensure that NOTs work correctly. Should probably move this into the search provider but I am too chicken.
            if (filterQuery != null)
            {
                filterQuery = modifier.getModifiedQuery(filterQuery);
            }

            final PagerFilter pagerFilter = new PagerFilter(maxIssueCount);
            final SearchResults searchResults = searchProvider.search(currentSearch.getQuery(), context.getLoggedInUser(), pagerFilter, filterQuery);
            results = searchResults.getIssues();
            total = searchResults.getTotal();
        }
        catch (final Exception e)
        {
            log.error("Error while executing search request", e);
        }
        return new IssuePickerResults(results, total, Collections.unmodifiableCollection(keyTerms),
                Collections.unmodifiableCollection(summaryTerms), getLabelKey(), getId());
    }

    /**
     * Create a Lucene query that will filter out issue according picker prams.  I.e it will remove same issue,
     * parent...
     *
     * @param issuePickerParams params to specify what should be filtered
     * @param filterQuery Current Query to add join
     * @return a new query that has been joind by the filter query
     */
    org.apache.lucene.search.Query addFilterToQuery(final IssuePickerSearchService.IssuePickerParameters issuePickerParams, final org.apache.lucene.search.Query filterQuery)
    {
        final Issue currentIssue = issuePickerParams.getCurrentIssue();
        final Project project = issuePickerParams.getCurrentProject();

        final BooleanQuery newFilterQuery = new BooleanQuery();
        if (filterQuery != null)
        {
            newFilterQuery.add(filterQuery, BooleanClause.Occur.MUST);
        }
        if (currentIssue != null)
        {
            newFilterQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_KEY, currentIssue.getKey())), BooleanClause.Occur.MUST_NOT);
        }
        if (project != null)
        {
            newFilterQuery.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, project.getId().toString())), BooleanClause.Occur.MUST);
        }
        if (!issuePickerParams.showSubTasks())
        {
            final BooleanQuery issueTypeQuery = new BooleanQuery();
            for (final IssueType issueType : constantsManager.getRegularIssueTypeObjects())
            {
                issueTypeQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_TYPE, issueType.getId())), BooleanClause.Occur.SHOULD);
            }
            newFilterQuery.add(issueTypeQuery, BooleanClause.Occur.MUST);
        }
        if ((currentIssue != null) && (currentIssue.getParentObject() != null) && !issuePickerParams.showSubTaskParent())
        {
            newFilterQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_KEY, currentIssue.getParentObject().getKey())), BooleanClause.Occur.MUST_NOT);
        }

        if (!newFilterQuery.clauses().isEmpty())
        {
            return newFilterQuery;
        }
        else
        {
            return null;
        }
    }

    org.apache.lucene.search.Query createQuery(final String query, final Collection<String> keyTerms, final Collection<String> summaryTerms)
    {
        if (query == null)
        {
            return null;
        }

        //  Exact key matchesshould beboosted most, followed by exact number matches, followed by number prefix matches
        final BooleanQuery combinedQuery = new BooleanQuery();
        final org.apache.lucene.search.Query exactKeyQuery = new TermQuery(new Term(DocumentConstants.ISSUE_KEY, query.toUpperCase()));
        exactKeyQuery.setBoost(2.0f);

        final org.apache.lucene.search.Query exactKeyNumQuery = new TermQuery(new Term(DocumentConstants.ISSUE_KEY_NUM_PART, query.toUpperCase()));
        exactKeyNumQuery.setBoost(1.8f);

        final QueryCreator keyQueryCreator = new QueryCreator(query.toUpperCase(), DocumentConstants.ISSUE_KEY, keyAnalyzer,
                new ConstantScorePrefixSubQuery());
        final org.apache.lucene.search.Query keyQueryOR = keyQueryCreator.getQuery();

        keyTerms.clear();
        keyTerms.addAll(keyQueryCreator.getTokens());

        final QueryCreator keyNumQueryCreator = new QueryCreator(query.toUpperCase(), DocumentConstants.ISSUE_KEY_NUM_PART, keyAnalyzer,
                new ConstantScorePrefixSubQuery());
        final org.apache.lucene.search.Query keyNumQueryOR = keyNumQueryCreator.getQuery();
        keyNumQueryOR.setBoost(1.5f);

        final QueryCreator summaryQueryCreator = new QueryCreator(query.toLowerCase(), DocumentConstants.ISSUE_SUMMARY, new WhitespaceAnalyzer(),
                new PrefixSubQuery());
        final org.apache.lucene.search.Query summaryQueryOR = summaryQueryCreator.getQuery();
        summaryTerms.clear();
        summaryTerms.addAll(summaryQueryCreator.getTokens());

        final QueryCreator summaryTermQueryCreator = new QueryCreator(query, DocumentConstants.ISSUE_SUMMARY, summaryAnalyzer, new TermSubQuery());
        final org.apache.lucene.search.Query summaryTermQueryOR = summaryTermQueryCreator.getQuery();
        summaryTerms.addAll(summaryTermQueryCreator.getTokens());

        // combine our queries together and it will be ANDed with the current search query via the searchProvider interface.
        combinedQuery.add(keyQueryOR, BooleanClause.Occur.SHOULD);
        combinedQuery.add(keyNumQueryOR, BooleanClause.Occur.SHOULD);
        combinedQuery.add(summaryQueryOR, BooleanClause.Occur.SHOULD);
        combinedQuery.add(summaryTermQueryOR, BooleanClause.Occur.SHOULD);
        combinedQuery.add(exactKeyQuery, BooleanClause.Occur.SHOULD);
        combinedQuery.add(exactKeyNumQuery, BooleanClause.Occur.SHOULD);
        return combinedQuery;
    }

    /**
     * This returns the label key of the results, eg why type of results are they.
     *
     * @return an i18n key.
     */
    protected abstract String getLabelKey();

    /**
     * This returns a simple ID of the results, used mainly for html tag ids
     *
     * @return an i18n key.
     */
    protected abstract String getId();

    /**
     * Return the search that this provider uses as its view of all issues.
     *
     * @param issuePickerParams the paramaters from the issue picker.
     * @return the search request that returns all issues the provide sees and works with.
     */
    protected abstract SearchRequest getRequest(IssuePickerSearchService.IssuePickerParameters issuePickerParams);

    /**
     * A abstract base class for QueryCreators. All use OR logic.
     */
    static final class QueryCreator
    {
        final String fieldName;
        private final BooleanClause.Occur occurence = BooleanClause.Occur.SHOULD;
        private final Collection<String> tokens = new ArrayList<String>();
        private final SubQuery subQueryCreator;

        /**
         * Default Constructor.
         *
         * @param queryString Query String to analyse
         * @param fieldName Name of field to query
         * @param analyzer Lucene Analyzer that creates tokens/terms
         * @param subQueryCreator subQuery to use to perform the query
         */
        QueryCreator(String queryString, final String fieldName, final Analyzer analyzer, final SubQuery subQueryCreator)
        {
            this.fieldName = notNull("You must provide a field name", fieldName);
            notNull("You must provide a Analyzer", analyzer);
            this.subQueryCreator = notNull("subQueryCreator", subQueryCreator);
            queryString = (queryString == null ? "" : queryString);
            try
            {
                final TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(queryString));
                CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
                while (tokenStream.incrementToken()) {
                    int termLength = termAttribute.length();
                    tokens.add(termAttribute.subSequence(0, termLength).toString());
                }
            }
            catch (final IOException e)
            {
                // wont happen
            }
        }

        public Collection<String> getTokens()
        {
            return tokens;
        }

        public final org.apache.lucene.search.Query getQuery()
        {
            final BooleanQuery query = new BooleanQuery();
            for (final String term : getTokens())
            {
                query.add(subQueryCreator.getSubQuery(fieldName, term), occurence);
            }
            return query;
        }
    }

    interface SubQuery
    {
        org.apache.lucene.search.Query getSubQuery(String field, String term);
    }

    /**
     * A {@link com.atlassian.jira.bc.issue.search.AbstractIssuePickerSearchProvider.SubQuery} that places Wildcards
     * before and after the query. Treats integers by putting a "*-" in front. All others have "*" placed before and
     * after
     */
    static class WildCardSubQuery implements SubQuery
    {
        public org.apache.lucene.search.Query getSubQuery(final String fieldName, final String termText)
        {
            final StringBuilder sb = new StringBuilder();
            org.apache.lucene.search.Query subQuery;
            boolean anyInt = false;
            try
            {
                new Integer(termText);
                sb.append("*-");
                anyInt = true;
            }
            catch (final NumberFormatException e)
            {
                sb.append("*");
            }

            sb.append(termText);

            if (!anyInt)
            {
                sb.append("*");
            }

            subQuery = new WildcardQuery(new Term(fieldName, sb.toString()));
            if (anyInt)
            {
                subQuery.setBoost(1.5f);
            }

            return subQuery;
        }
    }

    /**
     * A {@link com.atlassian.jira.bc.issue.search.AbstractIssuePickerSearchProvider.SubQuery) that creates pure {@link
     * org.apache.lucene.search.PrefixQuery} type queries.
     */
    static class PrefixSubQuery implements SubQuery
    {
        public org.apache.lucene.search.Query getSubQuery(final String fieldName, final String term)
        {
            return new PrefixQuery(new Term(fieldName, term));
        }
    }

    /**
     * A {@link com.atlassian.jira.bc.issue.search.AbstractIssuePickerSearchProvider.SubQuery} that creates pure {@link
     * com.atlassian.jira.util.lucene.ConstantScorePrefixQuery} type queries.
     */
    static class ConstantScorePrefixSubQuery implements SubQuery
    {
        public org.apache.lucene.search.Query getSubQuery(final String fieldName, final String term)
        {
            return ConstantScorePrefixQuery.build(new Term(fieldName, term));
        }
    }

    /**
     * A {@link com.atlassian.jira.bc.issue.search.AbstractIssuePickerSearchProvider.SubQuery} that creates pure {@link
     * org.apache.lucene.search.TermQuery} type queries.
     */
    static class TermSubQuery implements SubQuery
    {
        public org.apache.lucene.search.Query getSubQuery(final String fieldName, final String term)
        {
            return new TermQuery(new Term(fieldName, term));
        }
    }
}
