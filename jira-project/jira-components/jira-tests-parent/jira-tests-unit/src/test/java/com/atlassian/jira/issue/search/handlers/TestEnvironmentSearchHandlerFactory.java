package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.EnvironmentQuerySearcher;
import com.atlassian.jira.jql.query.EnvironmentClauseQueryFactory;
import com.atlassian.jira.jql.validator.EnvironmentValidator;

import org.junit.Test;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.EnvironmentSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestEnvironmentSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(EnvironmentSearchHandlerFactory.class,
                EnvironmentClauseQueryFactory.class,
                EnvironmentValidator.class,
                SystemSearchConstants.forEnvironment(),
                EnvironmentQuerySearcher.class, null);
    }

}
