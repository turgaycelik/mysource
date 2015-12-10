package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.CreatedDateSearcher;
import com.atlassian.jira.jql.query.CreatedDateClauseQueryFactory;
import com.atlassian.jira.jql.validator.CreatedDateValidator;

import org.junit.Test;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.CreatedDateSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestCreatedDateSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(CreatedDateSearchHandlerFactory.class,
                CreatedDateClauseQueryFactory.class,
                CreatedDateValidator.class,
                SystemSearchConstants.forCreatedDate(),
                CreatedDateSearcher.class, null);
    }

}
