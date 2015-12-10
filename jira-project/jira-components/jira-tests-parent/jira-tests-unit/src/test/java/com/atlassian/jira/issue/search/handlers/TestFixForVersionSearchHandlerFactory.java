package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.FixForVersionsSearcher;
import com.atlassian.jira.jql.query.FixForVersionClauseQueryFactory;
import com.atlassian.jira.jql.validator.FixForVersionValidator;

import org.junit.Test;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.FixForVersionSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestFixForVersionSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(FixForVersionSearchHandlerFactory.class,
                FixForVersionClauseQueryFactory.class,
                FixForVersionValidator.class,
                SystemSearchConstants.forFixForVersion(),
                FixForVersionsSearcher.class, null);
    }

}
