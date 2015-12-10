package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestVoters extends FuncTestCase
{
    private static final Issue ISSUE_WITH_VOTE = new Issue("HSP-1", 10010);
    private static final Issue ISSUE_WITHOUT_VOTE = new Issue("HSP-2", 10020);
    private static final Issue ISSUE_RESOLVED_WITHOUT_VOTE = new Issue("HSP-3", 10030);
    private static final Issue ISSUE_RESOLVED_WITH_VOTE = new Issue("HSP-4", 10040);
    private static final Issue ISSUE_ADMIN_CAN_VOTE = new Issue("HSP-5", 10050);

    private static final String ID_VOTE_LINK = "vote-toggle";
    private static final String ID_VOTE_SPAN = "vote-label";
    private static final String ID_VOTE_COUNT = "vote-data";
    private static final String ID_VOTE_COUNT_LINK = "view-voter-list";

    private static final String ID_VOTERS_ACTION = "view-voters";
    private static final String ID_VOTE_ACTION = "toggle-vote-issue";

    private static final String XPATH_VOTE_SPAN_TITLE = String.format("//span[@id='%s']/@title", ID_VOTE_SPAN);

    private static final String XPATH_VIEW_VOTERS_LINK = "//a[@id='" + ID_VOTE_COUNT_LINK + "']";
    private static final String XPATH_VIEW_VOTERS_SPAN = "//span[@id='" + ID_VOTE_COUNT  +"']";

    private static final String MSG_CANT_VOTE_ANON = "You have to be logged in to vote for an issue.";
    private static final String MSG_CANT_VOTE_RESOLVED = "You cannot vote or change your vote on resolved issues.";
    private static final String MSG_CANT_REMOVE_VOTE = "Cannot remove a vote for an issue that the user has not already voted for.";
    private static final String MSG_CANT_ADD_VOTE = "Cannot add a vote for an issue that the user has already voted for";
    private static final String MSG_CANT_VOTE_ANON_ERROR = "You must log in to access this page.";
    private static final String MSG_CANT_VOTE_REPORTER_ERROR = "You cannot vote for an issue you have reported.";


    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestVoters.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testAnonmousCannotVote()
    {
        navigation.logout();
        gotoIssue(ISSUE_WITH_VOTE);
        assertCantVote(ISSUE_WITH_VOTE, MSG_CANT_VOTE_ANON, MSG_CANT_VOTE_ANON_ERROR, 1, false);
        navigation.login(ADMIN_USERNAME);
    }

    public void testReporterCannotVote()
    {
        gotoIssue(ISSUE_WITH_VOTE);
        assertCantVote(ISSUE_WITH_VOTE, "", MSG_CANT_VOTE_REPORTER_ERROR, 1, true);
    }

    public void testUserVoting()
    {
        navigation.login(FRED_USERNAME);
        gotoIssue(ISSUE_WITH_VOTE);
        assertUserVoted();
        assertEquals(1, getVoteCount());

        // Click the link which should unvote the issue.
        tester.clickLink(ID_VOTE_LINK);
        assertUserNotVoted();
        assertEquals(0, getVoteCount());

        // Click the vote link, which should vote the issue.
        tester.clickLink(ID_VOTE_LINK);
        assertUserVoted();
        assertEquals(1, getVoteCount());
        navigation.login(ADMIN_USERNAME);
    }

    public void testAnUserCanNotVoteMoreThanOnce()
    {
        navigation.login(FRED_USERNAME);
        gotoIssue(ISSUE_WITH_VOTE);
        assertUserVoted();
        assertEquals(1, getVoteCount());

        // Lets try to vote directly and make sure that it does not add a vote, since we already voted.
        voteDirectlyForIssue(ISSUE_WITH_VOTE);
        gotoIssue(ISSUE_WITH_VOTE);
        assertUserVoted();
        assertEquals(1, getVoteCount());
    }

    public void testAnUserCanNotUnvoteMoreThanOnce()
    {
        navigation.login(FRED_USERNAME);
        gotoIssue(ISSUE_WITH_VOTE);
        assertUserVoted();
        assertEquals(1, getVoteCount());

        // Click the link which should unvote the issue.
        tester.clickLink(ID_VOTE_LINK);
        assertUserNotVoted();
        assertEquals(0, getVoteCount());

        // Lets try to unvote directly and make sure that it does not remove a vote, since we haven't voted.
        unVoteDirectlyForIssue(ISSUE_WITH_VOTE);
        gotoIssue(ISSUE_WITH_VOTE);
        assertUserNotVoted();
        assertEquals(0, getVoteCount());
    }

    private void assertVotingLinks(boolean voted)
    {
        tester.assertLinkPresent(ID_VOTE_LINK);
        tester.assertLinkPresent(ID_VOTE_ACTION);

        if (voted)
        {
            tester.assertTextInElement(ID_VOTE_ACTION, "Remove Vote");
        }
        else
        {
            tester.assertTextInElement(ID_VOTE_ACTION, "Add Vote");
        }
    }

    public void testCannotVoteOnResolved()
    {
        navigation.login(FRED_USERNAME);
        assertCantVote(ISSUE_RESOLVED_WITHOUT_VOTE, MSG_CANT_VOTE_RESOLVED, MSG_CANT_VOTE_RESOLVED, 0, false);

        //Make sure you can't unvote for a resolved issue.
        unVoteDirectlyForIssue(ISSUE_RESOLVED_WITH_VOTE);
        tester.assertTextPresent(MSG_CANT_VOTE_RESOLVED);
        gotoIssue(ISSUE_RESOLVED_WITH_VOTE);
        assertEquals(1, getVoteCount());
    }

    public void testViewVotersWithPermission() throws Exception
    {
        gotoIssue(ISSUE_WITH_VOTE);
        assertCanViewVoters();
        tester.assertLinkPresent("voter_link_fred");

        gotoIssue(ISSUE_ADMIN_CAN_VOTE);
        assertCanViewVoters();
        tester.assertTextPresent("There are no voters for this issue");

        gotoIssue(ISSUE_RESOLVED_WITH_VOTE);
        assertCanViewVoters();
        tester.assertLinkPresent("voter_link_fred");
    }

    public void testViewVotersWithoutPermission() throws Exception
    {
        navigation.login(FRED_USERNAME);
        assertCannotViewVoters(ISSUE_WITH_VOTE, "Access Denied");
        assertCannotViewVoters(ISSUE_RESOLVED_WITH_VOTE, "Access Denied");

        navigation.logout();
        assertCannotViewVoters(ISSUE_WITH_VOTE, "You must log in to access this page.");
        assertCannotViewVoters(ISSUE_RESOLVED_WITH_VOTE, "You must log in to access this page.");
    }

    public void testVoteNoViewPermission() throws Exception
    {
        navigation.login(FRED_USERNAME);
        gotoIssue(ISSUE_WITHOUT_VOTE);
        assertEquals(0, getVoteCount());
        tester.clickLink(ID_VOTE_LINK);
        assertEquals(1, getVoteCount());
    }

    private void assertCanViewVoters()
    {
        assertions.assertNodeExists(XPATH_VIEW_VOTERS_LINK);
        tester.assertLinkPresent(ID_VOTERS_ACTION);
        assertions.assertNodeExists(XPATH_VIEW_VOTERS_SPAN);
        tester.clickLink(ID_VOTE_COUNT_LINK);
        tester.assertTextPresent("Voters");
    }

    private void assertCannotViewVoters(Issue issue, final String expectedErrorMsg)
    {
        gotoIssue(issue);
        assertions.assertNodeDoesNotExist(XPATH_VIEW_VOTERS_LINK);
        tester.assertLinkNotPresent(ID_VOTERS_ACTION);
        assertions.assertNodeExists(XPATH_VIEW_VOTERS_SPAN);
        gotoViewVotersDirectly(issue);
        tester.assertTextPresent(expectedErrorMsg);
    }

    private void assertUserVoted()
    {
        assertVotingLinks(true);
        assertTextPresentInElement(ID_VOTE_LINK, "Remove vote for this issue");
    }

    private void assertUserNotVoted()
    {
        assertVotingLinks(false);
        assertTextPresentInElement(ID_VOTE_LINK, "Vote for this issue");
    }

    private void assertCantVote(final Issue issue, final String cantVoteTitle, final String cantVoteError, final int votes, final boolean isReporter)
    {
        gotoIssue(issue);
        //Should not be able to vote.
        tester.assertLinkNotPresent(ID_VOTE_LINK);
        tester.assertLinkNotPresent(ID_VOTE_ACTION);
        if (isReporter)
        {
            assertTextNotPresentInElement(ID_VOTE_SPAN, "Vote");
        }
        else
        {
            assertTextPresentInElement(ID_VOTE_SPAN, "Vote");
            //Should see this title when we can't vote.
            assertEquals(cantVoteTitle, getXpathText(XPATH_VOTE_SPAN_TITLE));
        }

        //Make sure the vote count is 1
        assertEquals(votes, getVoteCount());

        //Make sure we can't hit the action directly to hack our vote.
        voteDirectlyForIssue(issue);
        assertions.getTextAssertions().assertTextPresent(new WebPageLocator(tester), cantVoteError);

        //Go to the issue page again
        gotoIssue(issue);

        //Make sure the vote count is still 1
        assertEquals(votes, getVoteCount());
    }

    private void assertTextPresentInElement(final String elementId, final String expectedText)
    {
        assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, elementId), expectedText);
    }

    private void assertTextNotPresentInElement(final String elementId, final String expectedText)
    {
        assertions.getTextAssertions().assertTextNotPresent(new IdLocator(tester, elementId), expectedText);
    }

    private void gotoIssue(Issue issue)
    {
        navigation.issue().gotoIssue(issue.getKey());
    }

    private void voteDirectlyForIssue(Issue issue)
    {
        navigation.gotoPage(page.addXsrfToken(String.format("/secure/VoteOrWatchIssue.jspa?id=%d&vote=vote", issue.getId())));
    }

    private void unVoteDirectlyForIssue(Issue issue)
    {
        navigation.gotoPage(page.addXsrfToken(String.format("/secure/VoteOrWatchIssue.jspa?id=%d&vote=unvote", issue.getId())));
    }

    private void gotoViewVotersDirectly(Issue issue)
    {
        navigation.gotoPage(String.format("secure/ViewVoters!default.jspa?id=%d", issue.getId()));
    }

    private int getVoteCount()
    {
        final String s = StringUtils.trimToNull(new IdLocator(tester, ID_VOTE_COUNT).getText());
        if (s != null)
        {
            return Integer.parseInt(s);
        }
        else
        {
            fail("Unable to find voting count.");
            return Integer.MIN_VALUE;
        }
    }

    private String getXpathText(final String xpath)
    {
        return StringUtils.trimToNull(new XPathLocator(tester, xpath).getText());
    }

    private static class Issue
    {
        private final String key;
        private final long id;

        private Issue(final String key, final long id)
        {
            this.key = key;
            this.id = id;
        }

        public String getKey()
        {
            return key;
        }

        public long getId()
        {
            return id;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
