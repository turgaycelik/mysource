package com.atlassian.jira.notification;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.PluginModuleTracker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class NotificationFilterManagerImplTest
{

    private List<NotificationFilter> filterList;
    private NotificationRecipient elvis;
    private NotificationRecipient madge;
    private NotificationRecipient justinBieber;

    private NotificationFilterContext context;

    private NotificationFilterManagerImpl notificationFilterManager;

    private NotificationFilter alwaysAddsElvis;
    private NotificationFilter alwaysRemovesElvis;
    private NotificationFilter noopFilter;
    private NotificationFilter alwaysRemovesBieber;
    private NotificationFilter returnsNullOnAdd;
    private NotificationFilter throwsARuntimeException;

    @Before
    public void setUp() throws Exception
    {
        elvis = new MockNotificationRecipient(new MockApplicationUser("Elvis", "Elvis The Pelvis", "elvis@local.cosco.com"));
        madge = new MockNotificationRecipient(new MockApplicationUser("MAdge", "Madonna", "madge@past.it.com"));
        justinBieber = new MockNotificationRecipient(new MockApplicationUser("Justin Bieber", "Bacon Bacon Bacon...Ohhhh", "hairspray@tenpaces.com"));

        filterList = newArrayList();

        notificationFilterManager = new NotificationFilterManagerImpl(null, null)
        {

            @Override
            PluginModuleTracker<NotificationFilter, NotificationFilterModuleDescriptor> createTracker(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
            {
                return null;
            }

            @Override
            Iterable<NotificationFilter> enabledNotificationFilters()
            {
                return filterList;
            }
        };

        context = notificationFilterManager.makeContextFrom(JiraNotificationReason.ADHOC_NOTIFICATION);

        alwaysAddsElvis = new NotificationFilter()
        {
            @Override
            public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
            {
                return newArrayList(elvis);
            }

            @Override
            public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
            {
                return false;
            }
        };
        alwaysRemovesElvis = new NotificationFilter()
        {
            @Override
            public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
            {
                return Collections.emptyList();
            }

            @Override
            public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
            {
                return recipient.getUserRecipient().getName().equals(elvis.getUserRecipient().getName());
            }
        };
        alwaysRemovesBieber = new NotificationFilter()
        {
            @Override
            public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
            {
                return Collections.emptyList();
            }

            @Override
            public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
            {
                return recipient.getUserRecipient().getName().equals(justinBieber.getUserRecipient().getName());
            }
        };
        noopFilter = new NotificationFilter()
        {
            @Override
            public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
            {
                return Collections.emptyList();
            }

            @Override
            public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
            {
                return false;
            }
        };
        returnsNullOnAdd = new NotificationFilter()
        {
            @Override
            public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
            {
                return null;
            }

            @Override
            public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
            {
                return false;
            }
        };
        throwsARuntimeException = new NotificationFilter()
        {
            @Override
            public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
            {
                throw new RuntimeException("Unsocial plugin");
            }

            @Override
            public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
            {
                throw new RuntimeException("Unsocial plugin");
            }
        };
    }

    @org.junit.Test
    public void testSimpleAdd() throws Exception
    {
        filterList = newArrayList(alwaysAddsElvis, noopFilter);

        assertThat("Everyone loves the king", notificationFilterManager.recomputeRecipients(
                newArrayList(madge, justinBieber), context),
                containsInAnyOrder(madge, justinBieber, elvis)
        );

        assertThat("No Bieber allowed", notificationFilterManager.recomputeRecipients(
                newArrayList(madge), context),
                not(containsInAnyOrder(madge, justinBieber, elvis))
        );
    }


    @org.junit.Test
    public void testSimpleRemoval() throws Exception
    {
        filterList = newArrayList(alwaysAddsElvis, alwaysRemovesBieber);

        assertThat("No Bieber allowed now or ever", notificationFilterManager.recomputeRecipients(
                newArrayList(madge, justinBieber), context),
                containsInAnyOrder(madge, elvis)
        );

        // test that adds are always done before removes
        filterList = newArrayList(alwaysRemovesElvis, alwaysAddsElvis);

        assertThat("The King has left the building", notificationFilterManager.recomputeRecipients(
                newArrayList(madge, elvis), context),
                containsInAnyOrder(madge)
        );

    }

    @org.junit.Test
    public void testOrdering() throws Exception
    {
        // doesnt matter the order of the filter.  adds are always first and removes ar allways second
        filterList = newArrayList(alwaysAddsElvis, alwaysRemovesElvis);

        assertThat("The King has left the building still.", notificationFilterManager.recomputeRecipients(
                newArrayList(madge, elvis), context),
                containsInAnyOrder(madge)
        );
    }

    @org.junit.Test
    public void testStability() throws Exception
    {
        // we ignore nulls
        filterList = newArrayList(alwaysAddsElvis, returnsNullOnAdd);

        assertThat("Nulls on adds are ignored", notificationFilterManager.recomputeRecipients(
                newArrayList(madge, justinBieber), context),
                containsInAnyOrder(madge, justinBieber, elvis)
        );

        // we ignore runtime exceptions
        filterList = newArrayList(alwaysAddsElvis, returnsNullOnAdd, throwsARuntimeException);

        assertThat("Runtime exceptions are ignored", notificationFilterManager.recomputeRecipients(
                newArrayList(madge, justinBieber), context),
                containsInAnyOrder(madge, justinBieber, elvis)
        );

        assertThat("Runtime exceptions are ignored", notificationFilterManager.filtered(justinBieber, context), is(false));
    }

    @org.junit.Test
    public void testWeHaveSetSemantics() throws Exception
    {
        filterList = newArrayList(alwaysAddsElvis, alwaysAddsElvis, alwaysAddsElvis);

        assertThat("Set semantics", notificationFilterManager.recomputeRecipients(
                newHashSet(madge, justinBieber), context),
                containsInAnyOrder(madge, justinBieber, elvis)
        );
    }

    @org.junit.Test
    public void testContextState() throws Exception
    {
        NotificationFilter statefulFilter = new NotificationFilter()
        {
            @Override
            public Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients)
            {
                incrementState(context);
                return null;
            }

            private void incrementState(NotificationFilterContext context)
            {
                Long l = (Long) context.get("myState");
                if (l == null)
                {
                    l = 666L;
                }
                context.put("myState", l);

            }

            @Override
            public boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context)
            {
                Assert.assertEquals(666L, context.get("myState"));
                return false;
            }
        };
        filterList = newArrayList(statefulFilter);

        assertThat("Set semantics", notificationFilterManager.recomputeRecipients(
                newHashSet(madge, justinBieber, elvis), context),
                containsInAnyOrder(madge, justinBieber, elvis)
        );
    }

    @Test
    public void testFilterOnly() throws Exception
    {
        filterList = newArrayList(alwaysRemovesBieber);

        assertThat("Seriously...non Bieber...ever!", notificationFilterManager.filtered(
                justinBieber, context),
                is(true)
        );
    }
}
