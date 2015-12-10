package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.DescriptionQuerySearcher;
import com.atlassian.jira.jql.query.DescriptionClauseQueryFactory;
import com.atlassian.jira.jql.validator.DescriptionValidator;

import org.junit.Test;

/**
 * Simple test for {@link com.atlassian.jira.issue.search.handlers.DescriptionSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestDescriptionSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateSearchHandler()
    {
        _testSystemSearcherHandler(DescriptionSearchHandlerFactory.class,
                DescriptionClauseQueryFactory.class,
                DescriptionValidator.class,
                SystemSearchConstants.forDescription(),
                DescriptionQuerySearcher.class, null);
    }
}
