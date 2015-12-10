package com.atlassian.jira.issue.watchers;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.association.UserAssociationStoreImpl;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Lightweight TestDefaultWatcherManager
 *
 * @since v6.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultWatcherManager2
{
    @Mock
    private IssueIndexManager indexManager;
    @Mock
    private IssueFactory issueFactory;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private IssueManager issueManager;

    @Test
    public void test_startWatching_stopWatching()
    {
        UserAssociationStore userAssociationStore = new UserAssociationStoreImpl(new MockOfBizDelegator(), new MockUserManager());
        WatcherManager watcherManager = new DefaultWatcherManager(userAssociationStore, null, indexManager, null, issueFactory, eventPublisher, issueManager);
        MockIssue issue = new MockIssue(12L);
        ApplicationUser user = new MockApplicationUser("mary");
        assertEquals(0, watcherManager.getWatcherUserKeys(issue).size());

        watcherManager.startWatching(user, issue);

        assertEquals(1, watcherManager.getWatcherUserKeys(issue).size());
        assertEquals("mary", watcherManager.getWatcherUserKeys(issue).iterator().next());

        issue.setWatches(1L);
        watcherManager.stopWatching(user, issue);

        assertEquals(0, watcherManager.getWatcherUserKeys(issue).size());
    }
}
