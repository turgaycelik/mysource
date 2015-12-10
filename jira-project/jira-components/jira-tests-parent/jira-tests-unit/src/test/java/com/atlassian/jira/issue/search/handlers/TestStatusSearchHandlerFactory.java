package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.StatusSearcher;
import com.atlassian.jira.jql.query.StatusClauseQueryFactory;
import com.atlassian.jira.jql.validator.StatusValidator;

import org.junit.Test;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.StatusSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestStatusSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateFactory() throws Exception
    {
        _testSystemSearcherHandler(StatusSearchHandlerFactory.class,
                StatusClauseQueryFactory.class,
                StatusValidator.class,
                SystemSearchConstants.forStatus(),
                StatusSearcher.class, null);
    }
}
