package com.atlassian.jira.bc.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteHistoryEntry;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultVoteService
{
    @Mock
    private VoteManager voteManager;
    @Mock
    private I18nHelper.BeanFactory beanFactory;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private PermissionManager permissionManager;

    private MockI18nHelper mockI18nHelper = new MockI18nHelper();

    private DefaultVoteService voteService;

    private User user;
    private User voter;

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();

        voteService = new DefaultVoteService(voteManager, beanFactory, applicationProperties, permissionManager, beanFactory);

        user = new MockUser("Admin");
        voter = new MockUser("Voter");

        when(beanFactory.getInstance(any(User.class))).thenReturn(mockI18nHelper);
    }

    @Test
    public void testValidateAddVote()
    {
        MockIssue issue = new MockIssue(10000L);
        issue.setReporterId("voter");
        issue.setResolution(new MockGenericValue("blah"));

        when(permissionManager.hasPermission(Permissions.BROWSE, issue, voter)).thenReturn(false);
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING)).thenReturn(false);

        VoteService.VoteValidationResult validationResult = voteService.validateAddVote(user, voter, issue);

        assertFalse(validationResult.isValid());
        Collection<String> errors = validationResult.getErrorCollection().getErrorMessages();
        assertTrue(errors.contains("issue.operations.error.vote.issue.permission"));
        assertTrue(errors.contains("issue.operations.voting.resolved"));
        assertTrue(errors.contains("issue.operations.voting.disabled"));
        assertTrue(errors.contains("issue.operations.novote"));
        assertEquals(4, errors.size());
    }
    
    @Test
    public void testValidateAddVoteDoesNotCheckWhetherTheUserHasAlreadyVoted()
    {
        MockIssue issue = new MockIssue(10000L);

        voteService.validateAddVote(user, voter, issue);

        verify(voteManager, never()).hasVoted(voter, issue);
    }

    @Test
    public void testValidateRemoveVote()
    {
        MockIssue issue = new MockIssue(10000L);
        issue.setReporterId("voter");
        issue.setResolution(new MockGenericValue("blah"));

        when(permissionManager.hasPermission(Permissions.BROWSE, issue, voter)).thenReturn(false);
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING)).thenReturn(false);

        VoteService.VoteValidationResult validationResult = voteService.validateRemoveVote(user, voter, issue);

        assertFalse(validationResult.isValid());
        Collection<String> errors = validationResult.getErrorCollection().getErrorMessages();
        assertTrue(errors.contains("issue.operations.error.vote.issue.permission"));
        assertTrue(errors.contains("issue.operations.voting.resolved"));
        assertTrue(errors.contains("issue.operations.voting.disabled"));
        assertTrue(errors.contains("issue.operations.novote"));
        assertEquals(4, errors.size());
    }

    @Test
    public void testValidateRemoveVoteDoesNotCheckWhetherTheUserHasAlreadyVoted()
    {
        MockIssue issue = new MockIssue(10000L);

        voteService.validateRemoveVote(user, voter, issue);

        verify(voteManager, never()).hasVoted(voter, issue);
    }

    @Test
    public void testAddVoted()
    {
        int expectedVotes = 5;
        User voter = mock(User.class);
        Issue issue = new MockIssue(10000L);
        VoteService.VoteValidationResult validationResult = validationResultWith(voter, issue);

        when(voteManager.getVoteCount(issue)).thenReturn(expectedVotes);

        int votes = voteService.addVote(voter, validationResult);

        assertThat(votes, is(expectedVotes));
        verify(voteManager).addVote(voter, issue);
    }

    @Test
    public void testRemoveVote()
    {
        int expectedVotes = 5;
        User voter = mock(User.class);
        Issue issue = new MockIssue(10000L);
        VoteService.VoteValidationResult validationResult = validationResultWith(voter, issue);

        when(voteManager.getVoteCount(issue)).thenReturn(expectedVotes);

        int votes = voteService.removeVote(voter, validationResult);

        assertThat(votes, is(expectedVotes));
        verify(voteManager).removeVote(voter, issue);
    }

    @Test
    public void testViewVoters_noPermission() throws Exception
    {
        MockIssue issue = new MockIssue(10000L);

        when(permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), user)).thenReturn(false);

        ServiceOutcome<Collection<User>> outcome = voteService.viewVoters(issue, user);

        assertFalse(outcome.isValid());
        assertEquals(null, outcome.getReturnedValue());
    }

    @Test
    public void testViewVoters_votingDisabled() throws Exception
    {
        MockIssue issue = new MockIssue(10000L);

        when(permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), user)).thenReturn(true);
        when(voteManager.isVotingEnabled()).thenReturn(false);

        ServiceOutcome<Collection<User>> outcome = voteService.viewVoters(issue, user);

        assertTrue(!outcome.isValid());
        assertEquals(null, outcome.getReturnedValue());
    }

    @Test
    public void testViewVoters() throws Exception
    {
        MockIssue issue = new MockIssue(10000L);

        User user = new MockUser("bob");
        when(permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, user)).thenReturn(true);
        when(voteManager.isVotingEnabled()).thenReturn(true);
        when(voteManager.getVoters(issue, mockI18nHelper.getLocale())).thenReturn(Arrays.asList(user));

        ServiceOutcome<Collection<User>> outcome = voteService.viewVoters(issue, user);

        assertTrue(outcome.isValid());
        assertEquals(1, outcome.getReturnedValue().size());
        assertEquals(user, outcome.getReturnedValue().iterator().next());
    }

    @Test
    public void testGetVoterHistoryChecksPermissionsOverTheIssueAndNotJustTheProject()
    {
        Issue issue = mock(Issue.class);
        when(permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, user)).thenReturn(false);

        ServiceOutcome<List<VoteHistoryEntry>> voterHistory = voteService.getVoterHistory(issue, user);

        verify(permissionManager).hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, user);
        assertThat(voterHistory.getReturnedValue(), is(nullValue()));
    }

    private VoteService.VoteValidationResult validationResultWith(@Nonnull final User voter, @Nonnull final Issue issue)
    {
        VoteService.VoteValidationResult validationResult = mock(VoteService.VoteValidationResult.class);
        when(validationResult.getVoter()).thenReturn(voter);
        when(validationResult.getIssue()).thenReturn(issue);
        return validationResult;
    }
}
