package com.atlassian.jira.bc.filter;

import java.util.Collection;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.FilterCronValidationErrorMappingUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.query.QueryImpl;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestFilterSubscriptionStoring
{
    private FilterSubscriptionService service;
    private JiraServiceContext context;
    private ApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        context = createContext();
        user = new MockApplicationUser("admin");
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(user);
        new MockComponentWorker().addMock(UserManager.class, userManager).init();
    }


    @After
    public void tearDown() throws Exception
    {
        service = null;
        context = null;
        user = null;
    }


    @Test
    public void testSubscriptionStoring()
    {
        Long filterId = (long) 9999;
        String group = "Group";
        boolean emailOnEmpty = true;
        final String cronExpression = "0 0 0 ? 1 MON";

        Mock subManagerMock = new Mock(SubscriptionManager.class);
        Constraint[] args = new Constraint[5];
        args[0] = P.IS_NULL;
        args[1] = P.eq(filterId);
        args[2] = P.eq(group);
        args[3] = P.eq(cronExpression);
        args[4] = P.eq(emailOnEmpty);

        subManagerMock.expectAndReturn("createSubscription", args, null);

        service = createService((SubscriptionManager) subManagerMock.proxy());

        service.storeSubscription(context, filterId, group, cronExpression, emailOnEmpty);

        subManagerMock.verify();

    }

    @Test
    public void testSubscriptionStoringInvalidString()
    {
        Long filterId = (long) 9999;
        String group = "Group";
        boolean emailOnEmpty = true;
        final String cronExpression = "0 0 0 ? 1 NOT";

        Mock subManagerMock = new Mock(SubscriptionManager.class);

        subManagerMock.expectNotCalled("createSubscription");

        service = createService((SubscriptionManager) subManagerMock.proxy());

        service.storeSubscription(context, filterId, group, cronExpression, emailOnEmpty);

        subManagerMock.verify();
        assertTrue(context.getErrorCollection().hasAnyErrors());
        assertTrue(context.getErrorCollection().getErrorMessages().contains("filter.subsription.cron.errormessage.invalid.day.of.week:NOT"));

    }

    @Test
    public void testGetVisibleSubscriptionsForAuthor()
    {
        final Long filterId = (long) 999;
        final SearchRequest filter = new SearchRequest(new QueryImpl(), user, "filter", "filterDesc", filterId, 0L);

        Mock subManagerMock = new Mock(SubscriptionManager.class);

        subManagerMock.expectAndReturn("getAllSubscriptions", P.args(P.eq(filterId)), EasyList.build(filterId));

        service = createService((SubscriptionManager) subManagerMock.proxy());

        Collection result = service.getVisibleSubscriptions(user, filter);

        subManagerMock.verify();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(filterId));

    }

    @Test
    public void testGetVisibleSubscriptionsForNonAuthor()
    {
        final Long filterId = (long) 999;
        final ApplicationUser other = new MockApplicationUser("other");
        final SearchRequest filter = new SearchRequest(new QueryImpl(), other, "filter", "filterDesc", filterId, 0L);

        Mock subManagerMock = new Mock(SubscriptionManager.class);

        subManagerMock.expectAndReturn("getSubscriptions", P.args(P.eq(user), P.eq(filterId)), EasyList.build(filterId));

        service = createService((SubscriptionManager) subManagerMock.proxy());

        Collection result = service.getVisibleSubscriptions(user, filter);

        subManagerMock.verify();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(filterId));

    }
    @Test
    public void testGetVisibleSubscriptionsForError()
    {
        final Long filterId = (long) 999;
        final ApplicationUser other = new MockApplicationUser("other");
        final SearchRequest filter = new SearchRequest(new QueryImpl(), other, "filter", "filterDesc", filterId, 0L);

        Mock subManagerMock = new Mock(SubscriptionManager.class);

        subManagerMock.expectAndThrow("getSubscriptions", P.args(P.eq(user), P.eq(filterId)), new GenericEntityException());

        service = createService((SubscriptionManager) subManagerMock.proxy());

        try
        {
            service.getVisibleSubscriptions(user, filter);
            fail("Should have thrown excepion");
        }
        catch (DataAccessException e)
        {
            // good
        }

        subManagerMock.verify();

    }
    @Test
    public void testGetVisibleSubscriptionsForNullFilterAndUser()
    {
        final Long filterId = (long) 999;
        final SearchRequest filter = new SearchRequest(new QueryImpl(), user, "filter", "filterDesc", filterId, 0L);

        Mock subManagerMock = new Mock(SubscriptionManager.class);

        service = createService((SubscriptionManager) subManagerMock.proxy());

        Collection result = service.getVisibleSubscriptions((ApplicationUser) null, filter);

        subManagerMock.verify();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        result = service.getVisibleSubscriptions(user, null);

        subManagerMock.verify();
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    private DefaultFilterSubscriptionService createService(SubscriptionManager subscriptionManager)
    {
        FilterCronValidationErrorMappingUtil mapper = new FilterCronValidationErrorMappingUtil(null)
        {

            protected String getText(String key)
            {
                return key;
            }

            protected String getText(String key, Object param)
            {
                return key + ':' + param;
            }

        };

        return new DefaultFilterSubscriptionService(mapper, null, subscriptionManager)
        {

            protected String getText(String key, Object param)
            {
                return key + ':' + param;
            }
        };
    }

    private JiraServiceContextImpl createContext()
    {
        return new JiraServiceContextImpl((ApplicationUser)null, new SimpleErrorCollection());
    }
}
