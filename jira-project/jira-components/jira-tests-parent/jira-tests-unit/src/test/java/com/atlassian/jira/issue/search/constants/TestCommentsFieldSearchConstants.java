package com.atlassian.jira.issue.search.constants;

import java.util.EnumSet;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link com.atlassian.jira.issue.search.constants.CommentsFieldSearchConstants}.
 *
 * @since v4.0
 */
public class TestCommentsFieldSearchConstants
{
    @Test
    public void testConstants() throws Exception
    {
        CommentsFieldSearchConstants constants = CommentsFieldSearchConstants.getInstance();
        assertEquals(new ClauseNames("comment"), constants.getJqlClauseNames());
        assertEquals("body", constants.getUrlParameter());
    }

    @Test
    public void testSupportedOperators() throws Exception
    {
        assertEquals(EnumSet.of(Operator.LIKE, Operator.NOT_LIKE), CommentsFieldSearchConstants.getInstance().getSupportedOperators());
    }
}
