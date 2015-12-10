package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.JqlOperandResolver;

import org.apache.lucene.search.Query;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestSummaryClauseQueryFactory
{
    @Test
    public void testGetQueryHappyPath() throws Exception
    {
        final MockControl mockQueryControl = MockClassControl.createControl(Query.class);
        final Query mockQuery = (Query) mockQueryControl.getMock();
        mockQuery.setBoost(SummaryClauseQueryFactory.SUMMARY_BOOST_FACTOR);
        mockQueryControl.replay();

        final QueryFactoryResult queryFactoryResult = new QueryFactoryResult(mockQuery);

        final MockControl mockClauseQueryFactoryControl = MockControl.createStrictControl(ClauseQueryFactory.class);
        final ClauseQueryFactory mockClauseQueryFactory = (ClauseQueryFactory) mockClauseQueryFactoryControl.getMock();
        mockClauseQueryFactory.getQuery(null, null);
        mockClauseQueryFactoryControl.setReturnValue(queryFactoryResult);
        mockClauseQueryFactoryControl.replay();

        SummaryClauseQueryFactory summaryClauseQueryFactory = new SummaryClauseQueryFactory(null)
        {
            @Override
            ClauseQueryFactory getDelegate(final JqlOperandResolver operandSupport)
            {
                return mockClauseQueryFactory;
            }
        };

        summaryClauseQueryFactory.getQuery(null, null);

        mockClauseQueryFactoryControl.verify();
        mockQueryControl.verify();
    }

    @Test
    public void testGetQueryNullQuery() throws Exception
    {
        final MockControl mockClauseQueryFactoryControl = MockControl.createStrictControl(ClauseQueryFactory.class);
        final ClauseQueryFactory mockClauseQueryFactory = (ClauseQueryFactory) mockClauseQueryFactoryControl.getMock();
        mockClauseQueryFactory.getQuery(null, null);
        mockClauseQueryFactoryControl.setReturnValue(null);
        mockClauseQueryFactoryControl.replay();

        SummaryClauseQueryFactory summaryClauseQueryFactory = new SummaryClauseQueryFactory(null)
        {
            @Override
            ClauseQueryFactory getDelegate(final JqlOperandResolver operandSupport)
            {
                return mockClauseQueryFactory;
            }
        };

        assertNull(summaryClauseQueryFactory.getQuery(null, null));

        mockClauseQueryFactoryControl.verify();
    }

}
