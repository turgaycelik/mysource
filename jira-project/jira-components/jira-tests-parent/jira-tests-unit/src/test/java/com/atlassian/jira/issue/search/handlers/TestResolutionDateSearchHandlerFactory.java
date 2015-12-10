package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.ResolutionDateSearcher;
import com.atlassian.jira.jql.query.ResolutionDateClauseQueryFactory;
import com.atlassian.jira.jql.validator.ResolutionDateValidator;

import org.junit.Test;

/**
 * A test for {@link ResolutionDateSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestResolutionDateSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateHandler() throws Exception
    {
        _testSystemSearcherHandler(ResolutionDateSearchHandlerFactory.class,
                ResolutionDateClauseQueryFactory.class,
                ResolutionDateValidator.class,
                SystemSearchConstants.forResolutionDate(),
                ResolutionDateSearcher.class, null);
    }
}
