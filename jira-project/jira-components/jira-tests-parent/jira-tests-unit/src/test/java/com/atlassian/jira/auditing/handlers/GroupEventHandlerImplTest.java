package com.atlassian.jira.auditing.handlers;

import java.util.Locale;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.core.FilteredGroupsProvider;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableSet;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

/**
 * This test is a sanity check if we have all possible
 * MembershipTypes covered in our group handler.
 * In tests we loop though all MembershipTypes to check
 * if all of them are expected.
 *
 * @since v6.2
 */
@RunWith (ListeningMockitoRunner.class)
public class GroupEventHandlerImplTest
{
    @Mock
    I18nHelper.BeanFactory beanFactory;

    @Mock
    I18nHelper i18nHelper;

    @Mock
    Directory directory;

    @Mock
    FilteredGroupsProvider filteredGroupsProvider;

    @Before
    public void setUp() throws Exception
    {
        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(I18nHelper.BeanFactory.class, beanFactory);
        ComponentAccessor.initialiseWorker(componentAccessorWorker);

        when(beanFactory.getInstance(Locale.ENGLISH)).thenReturn(i18nHelper);
    }

    @Test
    public void checkIfWeServeAllMembershipTypesWhenCreatingMembership()
    {
        for (MembershipType membershipType : MembershipType.values())
        {
            final GroupMembershipCreatedEvent groupMembershipCreatedEvent = Mockito.mock(GroupMembershipCreatedEvent.class);
            when(groupMembershipCreatedEvent.getMembershipType()).thenReturn(membershipType);
            when(groupMembershipCreatedEvent.getDirectory()).thenReturn(directory);
            new GroupEventHandlerImpl(filteredGroupsProvider).onGroupMembershipCreatedEvent(groupMembershipCreatedEvent);
        }
    }

    @Test
    public void checkIfWeServeAllMembershipTypesWhenDeletingMembership()
    {
        for (MembershipType membershipType : MembershipType.values())
        {
            final GroupMembershipDeletedEvent groupMembershipDeletedEvent = Mockito.mock(GroupMembershipDeletedEvent.class);
            when(groupMembershipDeletedEvent.getMembershipType()).thenReturn(membershipType);
            when(groupMembershipDeletedEvent.getDirectory()).thenReturn(directory);
            new GroupEventHandlerImpl(filteredGroupsProvider).onGroupMembershipDeletedEvent(groupMembershipDeletedEvent);
        }
    }

    @Test
    public void testFilteringWorksForGroupCreatedEvent()
    {
        final GroupCreatedEvent event = Mockito.mock(GroupCreatedEvent.class);
        final Group group = Mockito.mock(Group.class);
        when(event.getGroup()).thenReturn(group);
        when(group.getName()).thenReturn("test");
        when(filteredGroupsProvider.getGroups()).thenReturn(ImmutableSet.of("test"));

        final Option<RecordRequest> recordRequests = new GroupEventHandlerImpl(filteredGroupsProvider).onGroupCreatedEvent(event);

        Assert.assertThat(recordRequests, Matchers.equalTo(Option.none(RecordRequest.class)));
    }

    @Test
    public void testFilteringWorksForGroupDeletedEvent()
    {
        final GroupDeletedEvent event = Mockito.mock(GroupDeletedEvent.class);
        when(event.getGroupName()).thenReturn("test");
        when(filteredGroupsProvider.getGroups()).thenReturn(ImmutableSet.of("test"));

        final Option<RecordRequest> recordRequests = new GroupEventHandlerImpl(filteredGroupsProvider).onGroupDeletedEvent(event);

        Assert.assertThat(recordRequests, Matchers.equalTo(Option.none(RecordRequest.class)));
    }



    @Test
    public void testFilteringWorksForGroupMembershipCreatedEvent()
    {
        final GroupMembershipCreatedEvent event = Mockito.mock(GroupMembershipCreatedEvent.class);
        when(event.getGroupName()).thenReturn("test");
        when(filteredGroupsProvider.getGroups()).thenReturn(ImmutableSet.of("test"));

        final Option<RecordRequest> recordRequests = new GroupEventHandlerImpl(filteredGroupsProvider).onGroupMembershipCreatedEvent(event);

        Assert.assertThat(recordRequests, Matchers.equalTo(Option.none(RecordRequest.class)));
    }

    @Test
    public void testFilteringWorksForGroupMembershipDeletedEvent()
    {
        final GroupMembershipDeletedEvent event = Mockito.mock(GroupMembershipDeletedEvent.class);
        when(event.getGroupName()).thenReturn("test");
        when(filteredGroupsProvider.getGroups()).thenReturn(ImmutableSet.of("test"));

        final Option<RecordRequest> recordRequests = new GroupEventHandlerImpl(filteredGroupsProvider).onGroupMembershipDeletedEvent(event);

        Assert.assertThat(recordRequests, Matchers.equalTo(Option.none(RecordRequest.class)));
    }

}
