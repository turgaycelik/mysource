package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.AffectedVersionsSearcher;
import com.atlassian.jira.jql.query.AffectedVersionClauseQueryFactory;
import com.atlassian.jira.jql.validator.AffectedVersionValidator;

import org.junit.Test;

/**
 * Test for {@link com.atlassian.jira.issue.search.handlers.AffectedVersionSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestAffectedVersionSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreate() throws Exception
    {
        _testSystemSearcherHandler(AffectedVersionSearchHandlerFactory.class, AffectedVersionClauseQueryFactory.class,
                AffectedVersionValidator.class, SystemSearchConstants.forAffectedVersion(),
                AffectedVersionsSearcher.class, null);
    }
}
