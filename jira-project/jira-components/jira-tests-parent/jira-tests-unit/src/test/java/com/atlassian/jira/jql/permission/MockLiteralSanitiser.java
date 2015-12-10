package com.atlassian.jira.jql.permission;

import java.util.List;

import com.atlassian.jira.jql.operand.QueryLiteral;

import org.junit.Assert;

/**
 * A simple mock for {@link com.atlassian.jira.jql.permission.LiteralSanitiser}s that asserts the expected literals were
 * passed in for sanitising, and returns the predefined result once complete.
 *
 * @since v4.0
 */
public class MockLiteralSanitiser implements LiteralSanitiser
{
    private final Result result;
    private final QueryLiteral[] expectedLiterals;

    public MockLiteralSanitiser(final Result result, final QueryLiteral... expectedLiterals)
    {
        this.result = result;
        this.expectedLiterals = expectedLiterals;
    }

    public Result sanitiseLiterals(final List<QueryLiteral> literals)
    {
        Assert.assertEquals(literals.size(), expectedLiterals.length);
        for (QueryLiteral expectedLiteral : expectedLiterals)
        {
            Assert.assertTrue(literals.contains(expectedLiteral));
        }
        return result;
    }
}
