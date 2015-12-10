package com.atlassian.jira.issue.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.search.IndexSearcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator} that is meant for
 * {@link com.atlassian.jira.issue.DocumentIssueImpl} usage.
 * <br>
 * NOTE: This will not work for {@link com.atlassian.jira.issue.IssueImpl}.
 *
 * @since v3.11
 */
public class DocumentIssueAggregateTimeTrackingCalculator implements AggregateTimeTrackingCalculator
{
    private final JiraAuthenticationContext context;
    private final SearchProviderFactory searchProviderFactory;
    private final SearchProvider searchProvider;
    private final LuceneFieldSorter remainingEstimateSorter;
    private final LuceneFieldSorter originalEstimateSorter;
    private final LuceneFieldSorter timeSpentSorter;

    public DocumentIssueAggregateTimeTrackingCalculator(JiraAuthenticationContext context, SearchProviderFactory searchProviderFactory, SearchProvider searchProvider, FieldManager fieldManager)
    {
        this.context = context;
        this.searchProviderFactory = searchProviderFactory;
        this.searchProvider = searchProvider;
        this.originalEstimateSorter = getSorter(fieldManager, DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG);
        this.timeSpentSorter = getSorter(fieldManager, DocumentConstants.ISSUE_TIME_SPENT);
        this.remainingEstimateSorter = getSorter(fieldManager, DocumentConstants.ISSUE_TIME_ESTIMATE_CURR);
    }

    LuceneFieldSorter getSorter(FieldManager fieldManager, String field)
    {
        return fieldManager.getNavigableField(field).getSorter();
    }

    public AggregateTimeTrackingBean getAggregates(final Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("The issue must not be null");
        }

        final AggregateTimeTrackingBean aggregateBean = new AggregateTimeTrackingBean(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent(), 0);
        if (issue.isSubTask())
        {
            return aggregateBean;
        }

        Query query = getSubTaskClause(issue.getId()).buildQuery();

        IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        AggregateHitCollector collector = new AggregateHitCollector(searcher, aggregateBean, remainingEstimateSorter, originalEstimateSorter, timeSpentSorter);

        try
        {
            searchProvider.search(query, getUser(), collector);
            aggregateBean.setSubTaskCount(collector.getInvocationCount());
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
        return aggregateBean;
    }

    User getUser()
    {
        return context.getLoggedInUser();
    }

    JqlClauseBuilder getSubTaskClause(final Long issueId)
    {
        return JqlQueryBuilder.newBuilder().where().issueParent().eq(issueId);
    }

    static class AggregateHitCollector extends FieldableDocumentHitCollector
    {
        private final AggregateTimeTrackingBean aggregateBean;
        final LuceneFieldSorter originalEstimateSorter;
        final LuceneFieldSorter timeSpentSorter;
        final LuceneFieldSorter remainingEstimateSorter;
        final FieldSelector fieldSelector;
        int invocationCount = 0;

        public AggregateHitCollector(IndexSearcher searchable, AggregateTimeTrackingBean bean, LuceneFieldSorter remainingEstimateSorter, LuceneFieldSorter originalEstimateSorter, LuceneFieldSorter timeSpentSorter)
        {
            super(searchable);
            this.aggregateBean = bean;
            this.originalEstimateSorter = originalEstimateSorter;
            this.timeSpentSorter = timeSpentSorter;
            this.remainingEstimateSorter = remainingEstimateSorter;
            final Set fieldsToLoad = new HashSet();
            fieldsToLoad.add(timeSpentSorter.getDocumentConstant());
            fieldsToLoad.add(originalEstimateSorter.getDocumentConstant());
            fieldsToLoad.add(remainingEstimateSorter.getDocumentConstant());
            this.fieldSelector = new SetBasedFieldSelector(fieldsToLoad, Collections.EMPTY_SET);
        }

        protected FieldSelector getFieldSelector()
        {
            return fieldSelector;
        }

        /**
         * Called each a sub task has been found giving us a chance to add ups its time tracking estimates
         *
         * @param d the Lucene document
         */
        public void collect(Document d)
        {
            Long timeSpent = getValueFromDocument(d, timeSpentSorter);
            Long remainingEstimate = getValueFromDocument(d, remainingEstimateSorter);
            Long originalEstimate = getValueFromDocument(d, originalEstimateSorter);
            aggregateBean.setTimeSpent(AggregateTimeTrackingBean.addAndPreserveNull(aggregateBean.getTimeSpent(), timeSpent));
            aggregateBean.setRemainingEstimate(AggregateTimeTrackingBean.addAndPreserveNull(aggregateBean.getRemainingEstimate(), remainingEstimate));
            aggregateBean.setOriginalEstimate(AggregateTimeTrackingBean.addAndPreserveNull(aggregateBean.getOriginalEstimate(), originalEstimate));

            aggregateBean.bumpGreatestSubTaskEstimate(originalEstimate, remainingEstimate, originalEstimate);
            invocationCount++;
        }

        Long getValueFromDocument(Document d, LuceneFieldSorter sorter)
        {
            return (Long) sorter.getValueFromLuceneField(getRawDocumentValue(d, sorter.getDocumentConstant()));
        }

        String getRawDocumentValue(Document d, String documentConstant)
        {
            return d.get(documentConstant);
        }

        public int getInvocationCount()
        {
            return invocationCount;
        }
    }
}
