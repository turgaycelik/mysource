package com.atlassian.jira.issue.vote;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.association.UserAssociationStoreImpl;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.config.util.MockIndexingConfiguration;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.BulkOnlyIndexManager;
import com.atlassian.jira.issue.index.MemoryIssueIndexer;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;

public class TestDefaultVoteManager
{
    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @AvailableInContainer
    private final CrowdService crowdService = new MockCrowdService();

    @Mock
    @AvailableInContainer
    private ApplicationProperties ap;

    @Mock
    @AvailableInContainer
    private ReindexMessageManager reindexIssueManager;

    @Mock
    @AvailableInContainer
    private EventPublisher eventPublisher;

    @Mock
    @AvailableInContainer
    private IssueManager issueManager;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    @AvailableInContainer
    private SearchExtractorRegistrationManager registrationManager;

    @AvailableInContainer
    private final OfBizDelegator ofbiz = new MockOfBizDelegator();

    private VoteHistoryStore historyStore;
    private JiraAuthenticationContext authContext;
    private DefaultVoteManager voteManager;
    private MockIssue issue;
    private UserAssociationStore assocStore;
    private User bob;
    private GenericValue issueGV;

    @Before
    public void setUp() throws Throwable
    {
        bob = new MockUser("bob");
        crowdService.addUser(bob, "password");

        when(userManager.getUserByKeyEvenWhenUnknown(anyString())).thenReturn(new MockApplicationUser("bob"));

        authContext = new MockAuthenticationContext(bob);
        mockitoContainer.getMockWorker().addMock(JiraAuthenticationContext.class, authContext);
        assocStore = new UserAssociationStoreImpl(ofbiz, userManager);
        mockitoContainer.getMockWorker().addMock(UserAssociationStore.class, assocStore);
        historyStore = new OfbizVoteHistoryStore(ofbiz);
        mockitoContainer.getMockWorker().addMock(VoteHistoryStore.class, historyStore);

        issueGV = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("id", new Long(1), "key", "JRA-52", "project", new Long(1), "votes", new Long(1)));
        issue = new MockIssue();
        issue.setGenericValue(issueGV);

        when(ap.getOption(APKeys.JIRA_OPTION_VOTING)).thenReturn(true);

        voteManager = new DefaultVoteManager(ap, assocStore, new BulkOnlyIndexManager(
            new MockIndexingConfiguration(), new MemoryIssueIndexer(), new MockIndexPathManager(), reindexIssueManager,
                eventPublisher, null, null, null, null, null, issueManager), historyStore, issueManager);
    }

    @Test
    public void testAddRemoveVote()
    {
        assertThat(voteManager.getVoters(issue, Locale.ENGLISH), is(Matchers.<User>empty()));
        assertThat(voteManager.getVoters(issue, Locale.ENGLISH), not(hasItems(bob)));
        assertFalse("voteManager.hasVoted()", voteManager.hasVoted(bob, issueGV));

        voteManager.addVote(bob, issueGV);

        assertThat(voteManager.getVoters(issue, Locale.ENGLISH), is(not(Matchers.<User>empty())));
        assertThat(voteManager.getVoters(issue, Locale.ENGLISH), hasItems(bob));
        assertTrue("voteManager.hasVoted()", voteManager.hasVoted(bob, issueGV));
        assertEquals(1, voteManager.getVoteHistory(new MockIssue(1L)).size());

        voteManager.removeVote(bob, issueGV);

        assertThat(voteManager.getVoters(issue, Locale.ENGLISH), is(Matchers.<User>empty()));
        assertThat(voteManager.getVoters(issue, Locale.ENGLISH), not(hasItems(bob)));
        assertFalse("voteManager.hasVoted()", voteManager.hasVoted(bob, issueGV));

        assertThat(voteManager.getVoteHistory(new MockIssue(1L)), Matchers.<VoteHistoryEntry>hasSize(2));
    }

    @Test
    public void testRemoveVotesForUser()
    {
        final GenericValue issueGV1 = UtilsForTests.getTestEntity("Issue",
            ImmutableMap.of("id", new Long(1111), "key", "JRA-1", "project", new Long(1), "votes", new Long(1)));
        final MockIssue issue1 = new MockIssue(1111);
        issue1.setGenericValue(issueGV1);
        final GenericValue issueGV2 = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("id", new Long(222), "key", "JRA-2", "project", new Long(1), "votes", new Long(1)));
        final MockIssue issue2 = new MockIssue(222);
        issue2.setGenericValue(issueGV2);
        final GenericValue issueGV3 = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("id", new Long(333), "key", "JRA-3", "project", new Long(1), "votes", new Long(1)));
        final MockIssue issue3 = new MockIssue(333);
        issue3.setGenericValue(issueGV3);

        voteManager.addVote(bob, issueGV1);
        voteManager.addVote(bob, issueGV2);

        assertThat(voteManager.getVoters(issue1, Locale.ENGLISH), hasItems(bob));
        assertThat(voteManager.getVoters(issue2, Locale.ENGLISH), hasItems(bob));
        assertThat(voteManager.getVoters(issue3, Locale.ENGLISH), is(Matchers.<User>empty()));
        assertThat(voteManager.getVoteHistory(issue1), Matchers.<VoteHistoryEntry>hasSize(1));
        assertThat(voteManager.getVoteHistory(issue2), Matchers.<VoteHistoryEntry>hasSize(1));
        assertThat(voteManager.getVoteHistory(issue3), Matchers.<VoteHistoryEntry>hasSize(0));

        voteManager.removeVotesForUser(bob);

        assertThat(voteManager.getVoters(issue1, Locale.ENGLISH), is(Matchers.<User>empty()));
        assertThat(voteManager.getVoters(issue2, Locale.ENGLISH), is(Matchers.<User>empty()));
        assertThat(voteManager.getVoters(issue3, Locale.ENGLISH), is(Matchers.<User>empty()));

        assertThat(voteManager.getVoteHistory(issue1), Matchers.<VoteHistoryEntry>hasSize(2));
        assertThat(voteManager.getVoteHistory(issue2), Matchers.<VoteHistoryEntry>hasSize(2));
        assertThat(voteManager.getVoteHistory(issue3), Matchers.<VoteHistoryEntry>hasSize(0));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testRemoveVotesForUserNullParam()
    {
        voteManager.removeVotesForUser((User) null);
    }
}