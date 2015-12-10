package com.atlassian.jira.issue.subscription;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.component.ComponentAccessor.getMailQueue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mail.SubscriptionMailQueueItem;
import com.atlassian.jira.mail.SubscriptionMailQueueItemFactory;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.status.JobDetails;

public class TestDefaultSubscriptionManager
{
    @Rule
    public MockitoContainer container = new MockitoContainer(this);

    private final long runAt = new Date().getTime();
    private GenericValue subscrip;
    private ApplicationUser u;
    private final Long filterId = 1000L;

    @Mock
    @AvailableInContainer
    private GroupManager groupManager;

    @Mock
    @AvailableInContainer
    private SchedulerService schedulerService;

    @Mock
    @AvailableInContainer
    private MailQueue mailQueue;

    @Mock
    @AvailableInContainer
    private SubscriptionMailQueueItemFactory factory;

    @AvailableInContainer
    private final UserManager userManager = new MockUserManager();

    @AvailableInContainer
    private final CrowdService crowdService = new MockCrowdService();

    @AvailableInContainer
    private final OfBizDelegator ofbiz = new MockOfBizDelegator();


    public TestDefaultSubscriptionManager()
    {
    }

    private ApplicationUser createMockApplicationUser(final String userName, final String name, final String email)
        throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        createMockUser(userName, name, email);
        return ApplicationUsers.from(ComponentAccessor.getCrowdService().getUser(userName));
    }

    private User createMockUser(final String userName, final String name, final String email) throws OperationNotPermittedException,
        InvalidUserException, InvalidCredentialException
    {
        final User user = new MockUser(userName, name, email);
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addUser(user, "password");
        return user;
    }

    private User createMockUser(final String userName) throws Exception
    {
        return createMockUser(userName, userName, "");
    }

    @Before
    public void setUp() throws Exception
    {
        u = createMockApplicationUser("owen", "owen", "owen@atlassian.com");

        final FieldMap columns = FieldMap.build("filterID", filterId, "username", "owen", "group", "group", "lastRun",
            new Timestamp(runAt));
        columns.put("emailOnEmpty", Boolean.TRUE.toString());
        subscrip = UtilsForTests.getTestEntity("FilterSubscription", columns);
    }

    @Test
    public void testHasSubscription() throws GenericEntityException
    {
        when(groupManager.getGroupNamesForUser(u.getDirectoryUser())).thenReturn(Collections.<String>emptyList());

        final SubscriptionManager sm = getSubscriptionManager(null, null, null);
        assertThat(sm.hasSubscription(u, filterId), is(true));
    }

    @Test
    public void testGetSubscription() throws GenericEntityException
    {
        final SubscriptionManager sm = getSubscriptionManager(null, null, null);
        final GenericValue subscription = sm.getSubscription(u, filterId);
        assertNotNull(subscription);
        assertEquals(subscrip, subscription);
    }

    @Test
    public void testGetSubscriptions() throws Exception
    {
        final List<String> groupNames = new ArrayList<String>();

        when(groupManager.getGroupNamesForUser(u.getDirectoryUser())).thenReturn(groupNames);

        // Test retrieving group subscriptions for a filter given a user
        final SubscriptionManager sm = getSubscriptionManager(null, null, null);

        // Test if the subscription owned by the user is returned
        List<GenericValue> subscriptions = sm.getSubscriptions(u, filterId);
        assertThat(subscriptions, contains(subscrip));

        // Create a subscription that is shared with a group that user does not
        // belong to
        final User testUser = createMockUser("test");
        final Group anotherGroup = createMockGroup("anothergroup");
        final GenericValue anotherSubscription = UtilsForTests.getTestEntity("FilterSubscription", FieldMap.build(
            "filterID", filterId, "username", testUser.getName(), "group", anotherGroup.getName(), "lastRun",
            new Timestamp(runAt)));

        // Ensure that only the old subscription is returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertThat(subscriptions, contains(subscrip));

        // Put the user into the group
        groupNames.add("anothergroup");
        when(groupManager.getGroupNamesForUser(u.getDirectoryUser())).thenReturn(groupNames);

        // Now test that both subscriptions come back
        subscriptions = sm.getSubscriptions(u, filterId);
        assertThat(subscriptions, containsInAnyOrder(subscrip, anotherSubscription));

        // Create a subscriptioned owned by the user by now shared with a group
        final GenericValue ownedSubscription = UtilsForTests.getTestEntity("FilterSubscription", FieldMap.build(
            "filterID", filterId, "username", u.getName(), "group", null, "lastRun", new Timestamp(runAt)));

        // Ensure it is returned as the user owns it
        subscriptions = sm.getSubscriptions(u, filterId);
        assertThat(subscriptions, containsInAnyOrder(subscrip, anotherSubscription, ownedSubscription));

        // Create another group
        when(groupManager.getGroupNamesForUser(u.getDirectoryUser())).thenReturn(groupNames);

        // And another subscription
        final GenericValue yetAnotherSubscription = UtilsForTests.getTestEntity("FilterSubscription",
            FieldMap.build("filterID", filterId, "username", testUser.getName(), "group", "testGroup", "lastRun",
                new Timestamp(runAt)));

        // Ensure that the subscription is not returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertThat(subscriptions, containsInAnyOrder(subscrip, anotherSubscription, ownedSubscription));

        // Make the user member fo the group
        groupNames.add("testGroup");
        when(groupManager.getGroupNamesForUser(u.getDirectoryUser())).thenReturn(groupNames);

        // Tets that the subscription is returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertThat(subscriptions,
            containsInAnyOrder(subscrip, anotherSubscription, ownedSubscription, yetAnotherSubscription));

        // Now remove the user from the groups
        groupNames.clear();
        when(groupManager.getGroupNamesForUser(u.getDirectoryUser())).thenReturn(groupNames);

        // Test that only owned filters are returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertThat(subscriptions, containsInAnyOrder(subscrip, ownedSubscription));
    }

    protected Group createMockGroup(final String groupName) throws OperationNotPermittedException,
        InvalidGroupException
    {
        final Group group = new MockGroup(groupName);
        final CrowdService crowdService = ComponentAccessor.getCrowdService();
        crowdService.addGroup(group);
        return group;
    }

    @Test
    public void testCreateSubscriptions() throws Exception
    {
        final SubscriptionManager sm = getSubscriptionManager(null, null, null);
        final GenericValue subscription = sm.createSubscription(u, filterId, null, 3600L, false);
        final List<GenericValue> subs = ComponentAccessor.getOfBizDelegator().findAll("FilterSubscription");
        assertThat(subscription, isIn(subs));

        verify(schedulerService).scheduleJob(any(JobId.class), any(JobConfig.class));
    }

    @Test
    public void testUpdateSubscription() throws Exception
    {
        final JobId jobId = JobId.of(DefaultSubscriptionManager.SUBSCRIPTION_PREFIX + ':' + subscrip.getLong("id"));

        final SubscriptionManager sm = getSubscriptionManager(null, null, null);

        sm.updateSubscription(u, subscrip.getLong("id"), "newgroup", "", true);

        final List subs = ComponentAccessor.getOfBizDelegator().findAll("FilterSubscription");
        assertEquals(1, subs.size());

        verify(schedulerService).unscheduleJob(eq(jobId));
        verify(schedulerService).scheduleJob(any(JobId.class), any(JobConfig.class));
    }

    @Test
    public void testDeleteSubscription() throws Exception
    {
        final JobId jobId = JobId.of(DefaultSubscriptionManager.SUBSCRIPTION_PREFIX + ':' + subscrip.getLong("id"));

        when(schedulerService.getJobDetails(eq(jobId))).thenReturn(Mockito.mock(JobDetails.class));

        final SubscriptionManager sm = getSubscriptionManager(null, null, null);

        sm.deleteSubscription(subscrip.getLong("id"));

        final List<GenericValue> subs = ComponentAccessor.getOfBizDelegator().findAll("FilterSubscription");
        assertThat(subs, Matchers.<GenericValue> empty());

        verify(schedulerService).unscheduleJob(eq(jobId));
    }

    @Test
    public void testRunSubscription() throws Exception
    {
        final Timestamp ts = new Timestamp(new Date().getTime());
        final SubscriptionManager sm = getSubscriptionManager(mailQueue, null, null);
        GenericValue sub = sm.getSubscription(u, subscrip.getLong("id"));
        assertThat(sub.getTimestamp("lastRun").getTime(), lessThanOrEqualTo(ts.getTime()));

        sm.runSubscription(subscrip);
        sub = sm.getSubscription(u, subscrip.getLong("id"));
        assertThat(sub.getTimestamp("lastRun").getTime(), greaterThanOrEqualTo(ts.getTime()));

        verify(mailQueue).addItem(any(SubscriptionMailQueueItem.class));
    }

    private SubscriptionManager getSubscriptionManager(MailQueue mailQueue, TemplateManager templateManager,
        SubscriptionMailQueueItemFactory subscriptionMailQueueItemFactory)
    {
        if (mailQueue == null)
        {
            mailQueue = getMailQueue();
        }
        if (templateManager == null)
        {
            templateManager = getComponent(TemplateManager.class);
        }
        if (subscriptionMailQueueItemFactory == null)
        {
            subscriptionMailQueueItemFactory = getComponent(SubscriptionMailQueueItemFactory.class);
        }

        return new DefaultSubscriptionManager(ofbiz, mailQueue, templateManager, subscriptionMailQueueItemFactory,
            null, groupManager, schedulerService);
    }
}