package com.atlassian.jira.jql;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.mock.jql.MockClauseInformation;
import com.atlassian.jira.mock.jql.context.MockClauseContextFactory;
import com.atlassian.jira.mock.jql.query.MockClauseQueryFactory;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionHandler;
import com.atlassian.jira.mock.jql.validator.MockClauseValidator;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test for {@link DefaultClauseHandler}.
 *
 * @since v4.0
 */
public class TestClauseHandler
{
    @Test
    public void testConstructor()
    {
        final ClauseQueryFactory factory = new MockClauseQueryFactory();
        final ClauseValidator validator = new MockClauseValidator();

        try
        {
            new DefaultClauseHandler(new MockClauseInformation(new ClauseNames("blah")), factory, null, new MockClausePermissionHandler(), new MockClauseContextFactory());
            fail("Expected illegal argument exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DefaultClauseHandler(new MockClauseInformation(new ClauseNames("blah")), null, validator, new MockClausePermissionHandler(), new MockClauseContextFactory());
            fail("Expected illegal argument exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            new DefaultClauseHandler(null, factory, validator, new MockClausePermissionHandler(), new MockClauseContextFactory());
            fail("Expected illegal argument exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testGetters()
    {
        final ClauseQueryFactory factory = new MockClauseQueryFactory();
        final ClauseValidator validator = new MockClauseValidator();

        final ClauseHandler clauseHandler = new DefaultClauseHandler(new MockClauseInformation(new ClauseNames("blah")), factory, validator, new MockClausePermissionHandler(), new MockClauseContextFactory());
        assertSame(factory, clauseHandler.getFactory());
        assertSame(validator, clauseHandler.getValidator());
    }
}
