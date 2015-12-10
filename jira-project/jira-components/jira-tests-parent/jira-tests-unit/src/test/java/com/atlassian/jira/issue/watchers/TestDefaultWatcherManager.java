/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.watchers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueWatcherAddedEvent;
import com.atlassian.jira.event.issue.IssueWatcherDeletedEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultWatcherManager
{
    @Rule
    public RuleChain mockAllTheThings = MockitoMocksInContainer.forTest(this);

    @Mock @AvailableInContainer
    public UserAssociationStore userAssociationStore;

    @Mock @AvailableInContainer
    public ApplicationProperties applicationProperties;

    @Mock @AvailableInContainer
    public IssueIndexManager indexManager;

    @Mock @AvailableInContainer
    public UserManager userManager;

    @Mock @AvailableInContainer
    public IssueFactory issueFactory;

    @Mock @AvailableInContainer
    public EventPublisher eventPublisher;

    @Mock @AvailableInContainer
    public IssueManager issueManager;

    private DefaultWatcherManager watcherManager;

    private final Map<Long, List<ApplicationUser>> watchers = new HashMap<Long, List<ApplicationUser>>();
    private final MockIssue issue = new MockIssue(1, "DEMO-1");
    private final ApplicationUser user = new MockApplicationUser("bob");

    @Before
    public void setUp() throws Exception
    {
        when(issueManager.getIssueObject(1l)).thenReturn(issue);

        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final ApplicationUser user = (ApplicationUser) invocation.getArguments()[1];
                final Issue issue = (Issue) invocation.getArguments()[2];
                watchers.put(issue.getId(), Lists.newArrayList(user));
                return null;
            }
        }).when(userAssociationStore).createAssociation(eq(DefaultWatcherManager.ASSOCIATION_TYPE), any(ApplicationUser.class), any(Issue.class));


        when(userAssociationStore.getUsersFromSink(eq(DefaultWatcherManager.ASSOCIATION_TYPE), any(GenericValue.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                final GenericValue gv = (GenericValue) invocation.getArguments()[1];
                final List<ApplicationUser> users = watchers.get(gv.getLong("id"));
                return users != null ? users : ImmutableList.<ApplicationUser>of();
            }
        });

        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final ApplicationUser user = (ApplicationUser) invocation.getArguments()[1];
                for (List<ApplicationUser> list : watchers.values())
                {
                    list.remove(user);
                }
                return null;
            }
        }).when(userAssociationStore).removeUserAssociationsFromUser(eq(DefaultWatcherManager.ASSOCIATION_TYPE), any(ApplicationUser.class), eq("Issue"));

        this.watcherManager = new DefaultWatcherManager(userAssociationStore, applicationProperties, indexManager,
                userManager, issueFactory, eventPublisher, issueManager);
    }

    @Test
    public void testAddUserToWatchList()
    {
        watcherManager.startWatching(user, issue);

        assertThat(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH), hasSize(1));
        assertThat(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH), hasItems(user.getDirectoryUser()));
    }

    @Test
    public void testRemoveAllWatchesForUser()
    {
        watcherManager.startWatching(user, issue);

        MockIssue issue2 = new MockIssue(2, "DEMO-2");
        watcherManager.startWatching(user, issue2);

        assertThat(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH), hasItems(user.getDirectoryUser()));
        assertThat(watcherManager.getCurrentWatchList(issue2, Locale.ENGLISH), hasItems(user.getDirectoryUser()));

        verify(eventPublisher, times(2)).publish(any(IssueWatcherAddedEvent.class));

        watcherManager.removeAllWatchesForUser(user);
        assertThat(watcherManager.getCurrentWatchList(issue, Locale.ENGLISH), hasSize(0));
        assertThat(watcherManager.getCurrentWatchList(issue2, Locale.ENGLISH), hasSize(0));

        verify(eventPublisher, times(2)).publish(any(IssueWatcherDeletedEvent.class));
    }
}
