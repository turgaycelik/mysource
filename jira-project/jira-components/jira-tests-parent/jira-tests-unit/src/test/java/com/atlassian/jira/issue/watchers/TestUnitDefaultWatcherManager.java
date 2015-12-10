package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertFalse;

/**
 * @since v6.0
 */

public class TestUnitDefaultWatcherManager
{
    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);
    @Mock
    private UserAssociationStore userAssociationStore;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private IssueIndexManager indexManager;
    @Mock
    private UserManager userManager;
    @Mock
    private IssueFactory issueFactory;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private DefaultWatcherManager watcherManager;
    @Mock
    private IssueManager issueManager;

    @Before
    public void setUp() throws Exception
    {
        watcherManager = new DefaultWatcherManager(
                userAssociationStore, applicationProperties, indexManager, userManager, issueFactory, eventPublisher, issueManager);
    }

    @Test
    public void shouldNotThrowNPEWhenNoWatchersDeffined()
    {
        GenericValue issue = new MockGenericValue("issue");
        User user = new MockUser("test");

        final boolean watching = watcherManager.isWatching(user, issue);
        assertFalse("User should not be watching this issue",watching);
    }
}
