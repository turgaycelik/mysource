package com.atlassian.jira.jql.permission;

import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestVotePermissionChecker extends MockControllerTestCase
{
    private VoteManager voteManager;

    @Before
    public void setUp() throws Exception
    {
        voteManager = mockController.getMock(VoteManager.class);
    }

    @Test
    public void testHasPermissionToUseClauseVotingDisabled() throws Exception
    {
        voteManager.isVotingEnabled();
        mockController.setReturnValue(false);
        mockController.replay();

        final VotePermissionChecker checker = new VotePermissionChecker(voteManager);
        assertFalse(checker.hasPermissionToUseClause(null));
        mockController.verify();
    }

    @Test
    public void testHasPermissionToUseClauseVotingEnabled() throws Exception
    {
        voteManager.isVotingEnabled();
        mockController.setReturnValue(true);
        mockController.replay();

        final VotePermissionChecker checker = new VotePermissionChecker(voteManager);
        assertTrue(checker.hasPermissionToUseClause(null));
        mockController.verify();
    }
}
