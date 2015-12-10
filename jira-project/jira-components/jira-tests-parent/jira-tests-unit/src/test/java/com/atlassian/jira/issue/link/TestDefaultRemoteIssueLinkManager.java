package com.atlassian.jira.issue.link;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkCreateEvent;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkDeleteEvent;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkUpdateEvent;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.GetException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link DefaultRemoteIssueLinkManager}.
 *
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultRemoteIssueLinkManager
{
    private static final Long ISSUE_THAT_EXISTS = 10000L;
    private static final Long ISSUE_THAT_DOES_NOT_EXIST = 10001L;
    private static final String BLANK = "    ";
    private static final String INVALID_URI = "this is not a valid URI";

    private DefaultRemoteIssueLinkManager remoteIssueLinkManager;
    private RemoteIssueLinkStore issueLinkStore;
    @Mock private IssueManager issueManager;
    @Mock private IssueUpdater issueUpdater;
    @Mock private I18nHelper.BeanFactory i18nBeanFactory;
    @Mock private EventPublisher eventPublisher;
    @Mock private ApplicationUser user;

    @Before
    public void setUp()
    {
        issueLinkStore = new MemoryRemoteIssueLinkStore();

        remoteIssueLinkManager = new DefaultRemoteIssueLinkManager(issueLinkStore, issueManager, issueUpdater, i18nBeanFactory, eventPublisher);

        when(issueManager.getIssueObject(ISSUE_THAT_EXISTS)).thenReturn(new MockIssue(ISSUE_THAT_EXISTS));
        when(issueManager.getIssueObject(ISSUE_THAT_DOES_NOT_EXIST)).thenReturn(null);
        final I18nHelper i18nHelper = mock(I18nHelper.class);
        when(i18nBeanFactory.getInstance(user)).thenReturn(i18nHelper);
    }

    @Test
    public void testGetRemoteIssueLinkByGlobalId()
    {
        final RemoteIssueLink expectedLink = populatedBuilder().globalId("example").build();

        issueLinkStore.createRemoteIssueLink(expectedLink);
        issueLinkStore.createRemoteIssueLink(populatedBuilder().globalId("another example").build());

        final RemoteIssueLink link = remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example");

        assertEquals(expectedLink.getGlobalId(), link.getGlobalId());
    }

    @Test
    public void testGetRemoteIssueLinkByGlobalIdReturnsFirstWhenDuplicates()
    {
        final RemoteIssueLink link = populatedBuilder().globalId("example").build();

        final RemoteIssueLink firstLink = issueLinkStore.createRemoteIssueLink(link);
        final RemoteIssueLink secondLink = issueLinkStore.createRemoteIssueLink(link);
        assertThat(firstLink.getId(), is(not(secondLink.getId())));

        final RemoteIssueLink returnedLink = remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example");

        assertEquals(firstLink.getId(), returnedLink.getId());
    }

    @Test
    public void testGetRemoteIssueLinksByGlobalIdOnly() throws GetException
    {
        final List<RemoteIssueLink> expectedLinks = Lists.transform(ImmutableList.<Long>of(10001L, 10002L, 10003L), new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return populatedBuilder().globalId("example").issueId(issueId).build();
            }
        });

        final List<RemoteIssueLink> nonExpectedLinks = Lists.transform(ImmutableList.<Long>of(10003L, 10004L), new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return populatedBuilder().globalId("example1").issueId(issueId).build();
            }
        });

        for (RemoteIssueLink remoteIssueLink : Iterables.concat(expectedLinks, nonExpectedLinks))
        {
            issueLinkStore.createRemoteIssueLink(remoteIssueLink);
        }

        issueLinkStore.createRemoteIssueLink(populatedBuilder().globalId("another example").build());

        final List<RemoteIssueLink> links = remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(ImmutableList.of("example"));

        assertThat(links, hasSize(3));
        for (int i = 0; i < links.size(); i++)
        {
            assertThat(links.get(i).getIssueId(), equalTo(expectedLinks.get(i).getIssueId()));
            assertThat(links.get(i).getGlobalId(), equalTo(expectedLinks.get(i).getGlobalId()));
        }
    }

    @Test
    public void testGetRemoteIssueLinksByGlobalIdOnlyTooManyGlobalIds() throws GetException
    {
        final List<RemoteIssueLink> expectedLinks = Lists.transform(ImmutableList.<Long>of(10001L, 10002L, 10003L), new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return populatedBuilder().globalId("example").issueId(issueId).build();
            }
        });

        final List<RemoteIssueLink> nonExpectedLinks = Lists.transform(ImmutableList.<Long>of(10003L, 10004L), new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return populatedBuilder().globalId("example1").issueId(issueId).build();
            }
        });

        for (RemoteIssueLink remoteIssueLink : Iterables.concat(expectedLinks, nonExpectedLinks))
        {
            issueLinkStore.createRemoteIssueLink(remoteIssueLink);
        }

        issueLinkStore.createRemoteIssueLink(populatedBuilder().globalId("another example").build());

        final List<RemoteIssueLink> links = remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(ImmutableList.of("example"));

        assertThat(links, hasSize(3));
        for (int i = 0; i < links.size(); i++)
        {
            assertThat(links.get(i).getIssueId(), equalTo(expectedLinks.get(i).getIssueId()));
            assertThat(links.get(i).getGlobalId(), equalTo(expectedLinks.get(i).getGlobalId()));
        }
    }

    @Test
    public void testGetRemoteIssueLinksByGlobalIdOnlyEmptyResult() throws GetException
    {
        final List<RemoteIssueLink> expectedLinks = Lists.transform(ImmutableList.<Long>of(), new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return populatedBuilder().globalId("example").issueId(issueId).build();
            }
        });

        final List<RemoteIssueLink> nonExpectedLinks = Lists.transform(ImmutableList.<Long>of(10003L, 10004L), new Function<Long, RemoteIssueLink>()
        {
            @Override
            public RemoteIssueLink apply(@Nullable final Long issueId)
            {
                return populatedBuilder().globalId("example1").issueId(issueId).build();
            }
        });

        for (RemoteIssueLink remoteIssueLink : Iterables.concat(expectedLinks, nonExpectedLinks))
        {
            issueLinkStore.createRemoteIssueLink(remoteIssueLink);
        }

        issueLinkStore.createRemoteIssueLink(populatedBuilder().globalId("another example").build());

        final List<RemoteIssueLink> links = remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(ImmutableList.of("example"));

        assertThat(links, hasSize(0));
    }

    @Test
    public void testCreate()
    {
        createExpectingSuccess(populatedBuilder().build());
    }

    @Test
    public void testCreateCreatesChangeItem() throws JiraException
    {
        createExpectingSuccess(populatedBuilder().build());

        verify(issueUpdater).doUpdate(Matchers.<IssueUpdateBean>any(), anyBoolean());
    }

    @Test
    public void testCreateWithDuplicateGlobalId()
    {
        final RemoteIssueLink existing = createExpectingSuccess(populatedBuilder().build());
        createExpectingFailure(populatedBuilder().globalId(existing.getGlobalId()).build());
    }

    @Test
    public void testCreateWithGlobalIdTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().globalId(tooLongValue).build();
        createExpectingFailure(remoteIssueLink);
    }

    @Test
    public void testCreateWithTitleTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().title(tooLongValue).build();
        createExpectingFailure(remoteIssueLink);
    }

    @Test
    public void testCreatePublishesEventWhenSuccess()
    {
        final RemoteIssueLink remoteIssueLink = populatedBuilder().build();
        createExpectingSuccess(remoteIssueLink);

        verify(eventPublisher).publish(isA(RemoteIssueLinkCreateEvent.class));
    }

        @Test
    public void testCreateDoesNotPublishEventWhenFailure()
    {
        createExpectingFailure(populatedBuilder().issueId(null).build());

        verify(eventPublisher, times(0)).publish(any());
    }

    @Test
    public void testCreateWithRelationShipTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().relationship(tooLongValue).build();
        createExpectingFailure(remoteIssueLink);
    }

    @Test
    public void testCreateWithApplicationTypeTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().applicationType(tooLongValue).build();
        createExpectingFailure(remoteIssueLink);
    }

    @Test
    public void testCreateWithApplicationNameTooLong()
    {
        final String tooLongValue = RandomStringUtils.randomAscii(256);
        final RemoteIssueLink remoteIssueLink = populatedBuilder().applicationName(tooLongValue).build();
        createExpectingFailure(remoteIssueLink);
    }

    @Test
    public void testCreateWithoutNonRequiredFields()
    {
        // Set non-required fields to null
        final RemoteIssueLink remoteIssueLink = populatedBuilder()
                .globalId(null)
                .summary(null)
                .iconUrl(null)
                .iconTitle(null)
                .relationship(null)
                .resolved(null)
                .statusIconUrl(null)
                .statusIconTitle(null)
                .statusIconLink(null)
                .applicationType(null)
                .applicationName(null)
                .build();

        createExpectingSuccess(remoteIssueLink);
    }

    @Test
    public void testCreateWithBlankNonRequiredFields()
    {
        // Set non-required fields to blank string
        // Any fields validated as a URI must be empty, otherwise invalid URI
        final RemoteIssueLink remoteIssueLink = populatedBuilder()
                .globalId(BLANK)
                .summary(BLANK)
                .iconUrl("")
                .iconTitle(BLANK)
                .relationship(BLANK)
                .statusIconUrl("")
                .statusIconTitle(BLANK)
                .statusIconLink("")
                .applicationType(BLANK)
                .applicationName(BLANK)
                .build();

        createExpectingSuccess(remoteIssueLink);
    }

    @Test
    public void testCreateWithoutRequiredFields()
    {
        // Set required fields to null
        createExpectingFailure(populatedBuilder().issueId(null).build());
        createExpectingFailure(populatedBuilder().title(null).build());
        createExpectingFailure(populatedBuilder().url(null).build());
    }

    @Test
    public void testCreateWithBlankRequiredFields()
    {
        // Set required string fields to blank string
        createExpectingFailure(populatedBuilder().title(BLANK).build());
        createExpectingFailure(populatedBuilder().url(BLANK).build());
    }

    @Test
    public void testCreateWithInvalidIssue()
    {
        createExpectingFailure(populatedBuilder().issueId(ISSUE_THAT_DOES_NOT_EXIST).build());
    }

    @Test
    public void testCreateWithInvalidUrls()
    {
        createExpectingFailure(populatedBuilder().url(INVALID_URI).build());
        createExpectingFailure(populatedBuilder().iconUrl(INVALID_URI).build());
        createExpectingFailure(populatedBuilder().statusIconUrl(INVALID_URI).build());
        createExpectingFailure(populatedBuilder().statusIconLink(INVALID_URI).build());
    }

    @Test
    public void testUpdate()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());

        // Update some fields
        final RemoteIssueLink updated = populatedBuilder(created)
                .title("An updated URL label")
                .relationship("a different relationship")
                .applicationName("com.different.application.name")
                .build();

        updateExpectingSuccess(updated);
    }

    @Test
    public void testUpdateCreatesChangeItem() throws JiraException
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());

        // Update some fields
        final RemoteIssueLink updated = populatedBuilder(created)
                .title("An updated URL label")
                .relationship("a different relationship")
                .applicationName("com.different.application.name")
                .build();

        updateExpectingSuccess(updated);

        verify(issueUpdater, times(2)).doUpdate(Matchers.<IssueUpdateBean>any(), anyBoolean());
    }

    @Test
    public void testUpdateWhenDoesNotExist()
    {
        updateExpectingFailure(populatedBuilder().id(99L).build());
    }

    @Test
    public void testUpdateWithDuplicateGlobalId()
    {
        final String globalId = "A unique global id";
        createExpectingSuccess(populatedBuilder().globalId(globalId).build());
        final RemoteIssueLink toUpdate = createExpectingSuccess(populatedBuilder().build());
        updateExpectingFailure(populatedBuilder(toUpdate).globalId(globalId).build());
    }

    @Test
    public void testUpdateGlobalIdFromNullToDuplicate()
    {
        final String globalId = "A unique global id";
        createExpectingSuccess(populatedBuilder().globalId(globalId).build());
        final RemoteIssueLink toUpdate = createExpectingSuccess(populatedBuilder().globalId(null).build());
        updateExpectingFailure(populatedBuilder(toUpdate).globalId(globalId).build());
    }

    @Test
    public void testUpdateGlobalIdToNull()
    {
        final RemoteIssueLink toUpdate = createExpectingSuccess(populatedBuilder().build());
        updateExpectingSuccess(populatedBuilder(toUpdate).globalId(null).build());
    }

    @Test
    public void testUpdateGlobalIdFromNull()
    {
        final RemoteIssueLink toUpdate = createExpectingSuccess(populatedBuilder().globalId(null).build());
        updateExpectingSuccess(populatedBuilder(toUpdate).globalId("globalId").build());
    }

    @Test
    public void testUpdateWithoutNonRequiredFields()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());

        // Set non-required fields to null
        final RemoteIssueLink updated = populatedBuilder(created)
                .globalId(null)
                .summary(null)
                .iconUrl(null)
                .iconTitle(null)
                .relationship(null)
                .resolved(null)
                .statusIconUrl(null)
                .statusIconTitle(null)
                .statusIconLink(null)
                .applicationType(null)
                .applicationName(null)
                .build();

        updateExpectingSuccess(updated);
    }

    @Test
    public void testUpdateWithBlankNonRequiredFields()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());

        // Set non-required fields to blank string
        // Any fields validated as a URI must be empty, otherwise invalid URI
        final RemoteIssueLink updated = populatedBuilder(created)
                .globalId(BLANK)
                .summary(BLANK)
                .iconUrl("")
                .iconTitle(BLANK)
                .relationship(BLANK)
                .statusIconUrl("")
                .statusIconTitle(BLANK)
                .statusIconLink("")
                .applicationType(BLANK)
                .applicationName(BLANK)
                .build();

        updateExpectingSuccess(updated);
    }

    @Test
    public void testUpdateWithoutRequiredFields()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());

        // Set required fields to null
        updateExpectingFailure(populatedBuilder(created).id(null).build());
        updateExpectingFailure(populatedBuilder(created).issueId(null).build());
        updateExpectingFailure(populatedBuilder(created).title(null).build());
        updateExpectingFailure(populatedBuilder(created).url(null).build());
    }

    @Test
    public void testUpdateWithBlankRequiredFields()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());

        // Set required string fields to blank string
        updateExpectingFailure(populatedBuilder(created).title(BLANK).build());
        updateExpectingFailure(populatedBuilder(created).url(BLANK).build());
    }

    @Test
    public void testUpdateWithInvalidIssue()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());
        updateExpectingFailure(populatedBuilder(created).issueId(ISSUE_THAT_DOES_NOT_EXIST).build());
    }

    @Test
    public void testUpdateWithInvalidUrls()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());
        updateExpectingFailure(populatedBuilder(created).url(INVALID_URI).build());
        updateExpectingFailure(populatedBuilder(created).iconUrl(INVALID_URI).build());
        updateExpectingFailure(populatedBuilder(created).statusIconUrl(INVALID_URI).build());
        updateExpectingFailure(populatedBuilder(created).statusIconLink(INVALID_URI).build());
    }

    @Test
    public void testUpdatePublishesEventWhenSuccess()
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());

        // Update some fields
        final RemoteIssueLink updated = populatedBuilder(created)
                .title("An updated URL label")
                .relationship("a different relationship")
                .applicationName("com.different.application.name")
                .build();

        updateExpectingSuccess(updated);

        verify(eventPublisher).publish(isA(RemoteIssueLinkCreateEvent.class));
        verify(eventPublisher).publish(isA(RemoteIssueLinkUpdateEvent.class));
    }

    @Test
    public void testUpdateDoesNotPublishEventWhenFailure()
    {
        updateExpectingFailure(populatedBuilder().id(99L).build());

        verify(eventPublisher, times(0)).publish(any());
    }

    @Test
    public void testRemove() throws Exception
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());
        remoteIssueLinkManager.removeRemoteIssueLink(created.getId(), user);

        // Should not find the remote issue link now
        final RemoteIssueLink found = remoteIssueLinkManager.getRemoteIssueLink(created.getId());
        assertNull(found);
    }

    @Test
    public void testRemoveCreatesChangeItem() throws Exception
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());
        remoteIssueLinkManager.removeRemoteIssueLink(created.getId(), user);

        verify(issueUpdater, times(2)).doUpdate(Matchers.<IssueUpdateBean>any(), anyBoolean());
    }

    @Test
    public void testRemoveNullId()
    {
        remoteIssueLinkManager.removeRemoteIssueLink(null, user);
    }

    @Test
    public void testRemovePublishesEventWhenSuccess() throws RemoveException
    {
        final RemoteIssueLink created = createExpectingSuccess(populatedBuilder().build());
        remoteIssueLinkManager.removeRemoteIssueLink(created.getId(), user);

        verify(eventPublisher).publish(isA(RemoteIssueLinkDeleteEvent.class));
    }

    @Test
    public void testRemoveDoesNotPublishEventWhenNoOp()
    {
        // non-existant key
        remoteIssueLinkManager.removeRemoteIssueLink(99L, user);

        verify(eventPublisher, times(0)).publish(any());
    }

    @Test
    public void testRemoveRemoteIssueLinkByGlobalId() throws RemoveException
    {
        issueLinkStore.createRemoteIssueLink(populatedBuilder().globalId("example").build());

        remoteIssueLinkManager.removeRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example", user);

        assertNull(remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example"));
    }

    @Test
    public void testRemoveRemoteIssueLinkByGlobalIdRemovesAllWhenDuplicates() throws RemoveException
    {
        final RemoteIssueLink remoteIssueLink = populatedBuilder().globalId("example").build();

        issueLinkStore.createRemoteIssueLink(remoteIssueLink);
        issueLinkStore.createRemoteIssueLink(remoteIssueLink);

        remoteIssueLinkManager.removeRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example", user);

        assertNull(remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example"));
    }

    @Test
    public void testRemoveRemoteIssueLinkByGlobalIdDoesNotPublishEventWhenDoesNotExist()
    {
        remoteIssueLinkManager.removeRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example", user);

        verify(eventPublisher, times(0)).publish(any());
    }

    @Test
    public void testRemoveByGlobalIdPublishesEventWhenSuccess() throws RemoveException
    {
        issueLinkStore.createRemoteIssueLink(populatedBuilder().globalId("example").build());

        remoteIssueLinkManager.removeRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example", user);

        verify(eventPublisher).publish(isA(RemoteIssueLinkDeleteEvent.class));
    }

    @Test
    public void testRemoveByGlobalIdDoesNotPublishEventWhenFailure() throws RemoveException
    {
        remoteIssueLinkManager.removeRemoteIssueLinkByGlobalId(new MockIssue(ISSUE_THAT_EXISTS), "example", user);

        verify(eventPublisher, times(0)).publish(any());
    }

    private void assertExists(final RemoteIssueLink expected)
    {
        final RemoteIssueLink found = remoteIssueLinkManager.getRemoteIssueLink(expected.getId());
        assertNotNull(found);
        assertRemoteIssueLinksEqual(expected, found);
    }

    private RemoteIssueLink createExpectingSuccess(final RemoteIssueLink remoteIssueLink)
    {
        try
        {
            final RemoteIssueLink created = remoteIssueLinkManager.createRemoteIssueLink(remoteIssueLink, user);
            assertExists(created);
            return created;
        }
        catch (final CreateException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void createExpectingFailure(final RemoteIssueLink remoteIssueLink)
    {
        try
        {
            remoteIssueLinkManager.createRemoteIssueLink(remoteIssueLink, user);
            fail("Expected CreateException to be thrown");
        }
        catch (final CreateException e)
        {
            // Expected
        }
    }

    private void updateExpectingSuccess(final RemoteIssueLink remoteIssueLink)
    {
        try
        {
            remoteIssueLinkManager.updateRemoteIssueLink(remoteIssueLink, user);
            assertExists(remoteIssueLink);
        }
        catch (final UpdateException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void updateExpectingFailure(final RemoteIssueLink remoteIssueLink)
    {
        try
        {
            remoteIssueLinkManager.updateRemoteIssueLink(remoteIssueLink, user);
            fail("Expected UpdateException to be thrown");
        }
        catch (final UpdateException e)
        {
            // Expected
        }
    }

    private RemoteIssueLinkBuilder populatedBuilder()
    {
        return populatedBuilder(ISSUE_THAT_EXISTS);
    }

    private RemoteIssueLinkBuilder populatedBuilder(final RemoteIssueLink remoteIssueLink)
    {
        return new RemoteIssueLinkBuilder(remoteIssueLink);
    }

    public static RemoteIssueLinkBuilder populatedBuilder(final Long issueId)
    {
        return new RemoteIssueLinkBuilder()
                .issueId(issueId)
                .globalId("url=http://www.remoteapplication.com&ticketid=12345")
                .title("Ticket #12345")
                .summary("Summary of the ticket goes here")
                .url("http://www.remoteapplication.com/ticket/12345")
                .iconUrl("http://www.remoteapplication.com/images/ticket.gif")
                .iconTitle("Ticket")
                .relationship("relates to")
                .resolved(false)
                .statusIconUrl("http://www.remoteapplication.com/images/status.gif")
                .statusIconTitle("Status")
                .statusIconLink("http://www.remoteapplication.com/blah")
                .applicationType("com.mycompany.myhelpdesksystem")
                .applicationName("My Company IT Helpdesk");
    }

    public static void assertRemoteIssueLinksEqual(final RemoteIssueLink expected, final RemoteIssueLink actual)
    {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getIssueId(), actual.getIssueId());
        assertEquals(expected.getGlobalId(), actual.getGlobalId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getSummary(), actual.getSummary());
        assertEquals(expected.getUrl(), actual.getUrl());
        assertEquals(expected.getIconUrl(), actual.getIconUrl());
        assertEquals(expected.getIconTitle(), actual.getIconTitle());
        assertEquals(expected.getRelationship(), actual.getRelationship());
        assertEquals(expected.isResolved(), actual.isResolved());
        assertEquals(expected.getStatusIconUrl(), actual.getStatusIconUrl());
        assertEquals(expected.getStatusIconTitle(), actual.getStatusIconTitle());
        assertEquals(expected.getStatusIconLink(), actual.getStatusIconLink());
        assertEquals(expected.getApplicationType(), actual.getApplicationType());
        assertEquals(expected.getApplicationName(), actual.getApplicationName());
    }
}
