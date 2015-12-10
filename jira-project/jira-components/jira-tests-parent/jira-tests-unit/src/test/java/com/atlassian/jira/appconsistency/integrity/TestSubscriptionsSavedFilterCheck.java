/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsSavedFilterCheck;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSubscriptionsSavedFilterCheck
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private OfBizDelegator delegator = new MockOfBizDelegator();

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    @AvailableInContainer
    private SearchRequestService searchRequestService;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @Mock
    @AvailableInContainer
    private SubscriptionManager subscriptionManager;

    private final Function<DeleteEntityAmendment,GenericValue> AMENDMENT_TO_GENERIC_VALUE = new Function<DeleteEntityAmendment, GenericValue>()
    {
        @Override
        public GenericValue apply(@Nullable final DeleteEntityAmendment input)
        {
            return input.getEntity();
        }
    };

    private FilterSubscriptionsSavedFilterCheck filterCheck;
    private GenericValue filterSubscription1;
    private GenericValue filterSubscription2;
    private GenericValue filterSubscription3;

    @Before
    public void setUp() throws Exception
    {
        when(authenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());

        filterSubscription1 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", new Long(1), "filterID", new Long(1), "username", "nick"));
        filterSubscription2 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", new Long(2), "filterID", new Long(2), "username", "nick"));
        filterSubscription3 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", new Long(3), "filterID", new Long(3), "username", "nick"));
        filterCheck = new FilterSubscriptionsSavedFilterCheck(delegator, 1);

    }

    @Test
    public void testShouldReturnSubscriptionsIfUserCannotBeFound() throws Exception
    {
        final List<DeleteEntityAmendment> correct = filterCheck.correct();
        final Iterable<GenericValue> subscriptions = Iterables.transform(correct, AMENDMENT_TO_GENERIC_VALUE);
        assertThat(subscriptions, Matchers.containsInAnyOrder(filterSubscription1, filterSubscription2, filterSubscription3));
    }

    @Test
    public void testShouldReturnSubscriptionWhenNoFilterWasFound() throws Exception
    {
        when(userManager.getUserByKey("nick")).thenReturn(new MockApplicationUser("nick"));
        final SearchRequest searchRequest = mock(SearchRequest.class);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(1L))).thenReturn(searchRequest);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(2L))).thenReturn(searchRequest);

        final List<DeleteEntityAmendment> correctResult = filterCheck.correct();

        final Iterable<GenericValue> subscriptions = Iterables.transform(correctResult, AMENDMENT_TO_GENERIC_VALUE);
        assertThat(subscriptions, Matchers.contains(filterSubscription3));
    }

    @Test
    public void testDoRealCheckAddsMessagesWithAmendmentCorrectionInCorrectMethod() throws Exception
    {
        final List<DeleteEntityAmendment> correctResult = filterCheck.correct();
        final boolean isAllCorrection = Iterables.all(correctResult, new Predicate<DeleteEntityAmendment>()
        {
            @Override
            public boolean apply(final DeleteEntityAmendment input)
            {
                return input.isCorrection();
            }
        });
        assertTrue("Each returned amendment should to be set to CORRECTION", isAllCorrection);
    }

    @Test
    public void testDoRealCheckAddMessagesWithAmendmentErrorInPreview() throws Exception
    {
        final List<DeleteEntityAmendment> correctResult = filterCheck.preview();
        final boolean isAllError = Iterables.all(correctResult, new Predicate<DeleteEntityAmendment>()
        {
            @Override
            public boolean apply(final DeleteEntityAmendment input)
            {
                return input.isError();
            }
        });
        assertTrue("Each returned amendment should to be set to ERROR", isAllError);
    }
}
