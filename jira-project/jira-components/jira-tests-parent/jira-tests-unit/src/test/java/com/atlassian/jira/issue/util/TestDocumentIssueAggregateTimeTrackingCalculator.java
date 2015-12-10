package com.atlassian.jira.issue.util;

import java.util.Comparator;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestDocumentIssueAggregateTimeTrackingCalculator
{
    private static final Long ZERO_ESTIMATE = 0L;
    private static final Long ONE_ESTIMATE = 1L;
    private static final Long BIG_ESTIMATE = (long) 99999;
    private static final int INVOKED_COUNT = 10;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testNullIssue()
    {
        // does this even make sense
        AggregateTimeTrackingCalculator calculator = new DocumentIssueAggregateTimeTrackingCalculator(null, null, null, null)
        {
            LuceneFieldSorter getSorter(FieldManager fieldManager, String field)
            {
                return null;
            }
        };
        try
        {
            calculator.getAggregates(null);
            fail("Should have thrown a IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
        }
    }

    @Test
    public void testSubTaskParentIssue()
    {
        Issue issue = getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, true);
        AggregateTimeTrackingCalculator calculator = new DocumentIssueAggregateTimeTrackingCalculator(null, null, null, null)
        {
            LuceneFieldSorter getSorter(FieldManager fieldManager, String field)
            {
                return null;
            }
        };

        AggregateTimeTrackingBean bean = calculator.getAggregates(issue);
        assertNotNull(bean);
        assertEquals(ZERO_ESTIMATE, bean.getRemainingEstimate());
        assertEquals(ONE_ESTIMATE, bean.getOriginalEstimate());
        assertEquals(BIG_ESTIMATE, bean.getTimeSpent());
    }

    @Test
    public void testHitCollectorAdditionViaCollectCall()
    {
        AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(null, null, null, 0);
        final LuceneFieldSorter remainingEstimateSorter = getLFS(DocumentConstants.ISSUE_TIME_ESTIMATE_CURR, (long) 33);
        final LuceneFieldSorter originalEstimateSorter = getLFS(DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG, (long) 66);
        final LuceneFieldSorter timeSpentSorter = getLFS(DocumentConstants.ISSUE_TIME_SPENT, (long) 99);
        DocumentIssueAggregateTimeTrackingCalculator.AggregateHitCollector hitCollector = new DocumentIssueAggregateTimeTrackingCalculator.AggregateHitCollector(null, bean, remainingEstimateSorter, originalEstimateSorter, timeSpentSorter)
        {

            Long getValueFromDocument(Document d, LuceneFieldSorter sorter)
            {
                return 42L;
            }
        };

        for (int i = 0; i < INVOKED_COUNT; i++)
        {
            hitCollector.collect(null);
        }
        assertEquals(new Long(42 * INVOKED_COUNT), bean.getOriginalEstimate());
        assertEquals(new Long(42 * INVOKED_COUNT), bean.getRemainingEstimate());
        assertEquals(new Long(42 * INVOKED_COUNT), bean.getTimeSpent());
        assertEquals(INVOKED_COUNT, hitCollector.getInvocationCount());
    }

    @Test
    public void testHitCollectorAdditionViaCollectCallWithSorters()
    {
        AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(null, null, null, 0);
        final LuceneFieldSorter remainingEstimateSorter = getLFS(DocumentConstants.ISSUE_TIME_ESTIMATE_CURR, (long) 33);
        final LuceneFieldSorter originalEstimateSorter = getLFS(DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG, (long) 66);
        final LuceneFieldSorter timeSpentSorter = getLFS(DocumentConstants.ISSUE_TIME_SPENT, (long) 99);

        DocumentIssueAggregateTimeTrackingCalculator.AggregateHitCollector hitCollector = new DocumentIssueAggregateTimeTrackingCalculator.AggregateHitCollector(null, bean, remainingEstimateSorter, originalEstimateSorter, timeSpentSorter)
        {
            String getRawDocumentValue(Document d, String documentConstant)
            {
                return null;
            }
        };

        for (int i = 0; i < INVOKED_COUNT; i++)
        {
            hitCollector.collect(null);
        }
        assertEquals(new Long(66 * INVOKED_COUNT), bean.getOriginalEstimate());
        assertEquals(new Long(33 * INVOKED_COUNT), bean.getRemainingEstimate());
        assertEquals(new Long(99 * INVOKED_COUNT), bean.getTimeSpent());
        assertEquals(INVOKED_COUNT, hitCollector.getInvocationCount());
    }

    @Test
    public void testHitCollectorFieldSelectorCorrect()
    {
        AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(null, null, null, 0);
        final LuceneFieldSorter remainingEstimateSorter = getLFS(DocumentConstants.ISSUE_TIME_ESTIMATE_CURR, 33L);
        final LuceneFieldSorter originalEstimateSorter = getLFS(DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG, 66L);
        final LuceneFieldSorter timeSpentSorter = getLFS(DocumentConstants.ISSUE_TIME_SPENT, 99L);

        DocumentIssueAggregateTimeTrackingCalculator.AggregateHitCollector hitCollector = new DocumentIssueAggregateTimeTrackingCalculator.AggregateHitCollector(null, bean, remainingEstimateSorter, originalEstimateSorter, timeSpentSorter);

        assertEquals(FieldSelectorResult.LOAD, hitCollector.getFieldSelector().accept(DocumentConstants.ISSUE_TIME_ESTIMATE_CURR));
        assertEquals(FieldSelectorResult.LOAD, hitCollector.getFieldSelector().accept(DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG));
        assertEquals(FieldSelectorResult.LOAD, hitCollector.getFieldSelector().accept(DocumentConstants.ISSUE_TIME_SPENT));
        assertEquals(FieldSelectorResult.NO_LOAD, hitCollector.getFieldSelector().accept(DocumentConstants.ISSUE_KEY));
    }

    @Test
    public void testSubTaskClause() throws ParseException
    {
        DocumentIssueAggregateTimeTrackingCalculator calculator = new DocumentIssueAggregateTimeTrackingCalculator(null, null, null, null)
        {
            LuceneFieldSorter getSorter(FieldManager fieldManager, String field)
            {
                return null;
            }
        };

        final TerminalClause expectedClause = new TerminalClauseImpl("parent", Operator.EQUALS, new SingleValueOperand(ONE_ESTIMATE));
        final JqlClauseBuilder taskClauseBuilder = calculator.getSubTaskClause(ONE_ESTIMATE);
        final Clause clause = taskClauseBuilder.buildClause();
        assertEquals(expectedClause, clause);
    }

    @Test
    public void testSearchingInvocation()
    {

        Mock mockJAC = new Mock(JiraAuthenticationContext.class);
        mockJAC.expectAndReturn("getLoggedInUser", null);
        JiraAuthenticationContext ctx = (JiraAuthenticationContext) mockJAC.proxy();

        Mock mockSP = new Mock(SearchProvider.class);
        mockSP.expectVoid("search", P.args(P.IS_NOT_NULL, P.IS_NULL, P.IS_NOT_NULL));
        SearchProvider sp = (SearchProvider) mockSP.proxy();

        Mock mockSPF = new Mock(SearchProviderFactory.class);
        mockSPF.expectAndReturn("getSearcher", P.args(P.eq(SearchProviderFactory.ISSUE_INDEX)), null);
        SearchProviderFactory spf = (SearchProviderFactory) mockSPF.proxy();

        DocumentIssueAggregateTimeTrackingCalculator calculator = new DocumentIssueAggregateTimeTrackingCalculator(ctx, spf, sp, null)
        {
            LuceneFieldSorter getSorter(FieldManager fieldManager, String field)
            {
                return new LuceneFieldSorter()
                {
                    public String getDocumentConstant()
                    {
                        return DocumentConstants.ISSUE_TIME_ESTIMATE_CURR;
                    }

                    public Object getValueFromLuceneField(String documentValue)
                    {
                        return 33L;
                    }

                    public Comparator getComparator()
                    {
                        return null;
                    }
                };
            }
        };

        Issue issue = getIssue(null, null, null);
        calculator.getAggregates(issue);

        mockJAC.verify();
        mockSP.verify();
        mockSPF.verify();
    }

    private LuceneFieldSorter getLFS(final String documentConstant, final Object documentValue1)
    {
        return new LuceneFieldSorter()
        {

            public String getDocumentConstant()
            {
                return documentConstant;
            }

            public Object getValueFromLuceneField(String documentValue)
            {
                return documentValue1;
            }

            public Comparator getComparator()
            {
                return null;
            }
        };
    }

    private Issue getIssue(Long remainingEstimate, Long orginalEstimate, Long timeSpent)
    {
        return getIssue(remainingEstimate, orginalEstimate, timeSpent, false);
    }

    private Issue getIssue(Long remainingEstimate, Long orginalEstimate, Long timeSpent, final boolean isSubTask)
    {
        MockIssue mockIssue = new MockIssue(555L)
        {
            public boolean isSubTask()
            {
                return isSubTask;
            }
        };
        mockIssue.setEstimate(remainingEstimate);
        mockIssue.setOriginalEstimate(orginalEstimate);
        mockIssue.setTimeSpent(timeSpent);
        return mockIssue;
    }
}
