package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.SummaryQuerySearcher;
import com.atlassian.jira.jql.query.SummaryClauseQueryFactory;
import com.atlassian.jira.jql.validator.SummaryValidator;

import org.junit.Test;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.SummarySearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestSummarySearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateHandler() throws Exception
    {
        _testSystemSearcherHandler(SummarySearchHandlerFactory.class,
                SummaryClauseQueryFactory.class,
                SummaryValidator.class,
                SystemSearchConstants.forSummary(),
                SummaryQuerySearcher.class, null);
    }
}
