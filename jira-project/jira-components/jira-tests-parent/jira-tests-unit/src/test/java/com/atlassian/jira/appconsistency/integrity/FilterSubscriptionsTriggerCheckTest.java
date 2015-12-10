/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import java.util.List;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsScheduleCheck;
import com.atlassian.jira.issue.subscription.DefaultSubscriptionManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.status.JobDetails;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class FilterSubscriptionsTriggerCheckTest
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private OfBizDelegator delegator = new MockOfBizDelegator();

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @Mock
    @AvailableInContainer
    private SchedulerService schedulerService;

    @Mock
    @AvailableInContainer
    private SubscriptionManager subscriptionManager;

    private final Function<DeleteEntityAmendment,GenericValue> AMENDMENT_TO_GENERIC_VALUE = new Function<DeleteEntityAmendment, GenericValue>()
    {
        @Override
        public GenericValue apply(final DeleteEntityAmendment input)
        {
            return input.getEntity();
        }
    };

    private FilterSubscriptionsScheduleCheck triggerCheck;
    private GenericValue filterSubscription2;
    private GenericValue filterSubscription3;


    @Before
    public void setUp() throws Exception
    {
        when(authenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());

        when(schedulerService.getJobDetails(toJobId(1000L))).thenReturn(Mockito.mock(JobDetails.class));

        final GenericValue filterSubscription1 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", 1000L));
        filterSubscription2 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", 1001L));
        filterSubscription3 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", 1002L));

        triggerCheck = new FilterSubscriptionsScheduleCheck(delegator, schedulerService, 1);
    }

    @Test
    public void testDoRealCheckReturnsOnlySubscriptionNotConnectedToTrigger() throws Exception
    {
        final List<DeleteEntityAmendment> correctResult = triggerCheck.correct();
        assertThat(correctResult, hasSize(2));

        final Iterable<GenericValue> transform = Iterables.transform(correctResult, AMENDMENT_TO_GENERIC_VALUE);
        assertThat(transform, containsInAnyOrder(filterSubscription2, filterSubscription3));
    }

    @Test
    public void testDoRealCheckAddsMessagesWithAmendmentCorrectionInCorrectMethod() throws Exception
    {
        final List<DeleteEntityAmendment> correctResult = triggerCheck.correct();
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
        final List<DeleteEntityAmendment> correctResult = triggerCheck.preview();
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

    private JobId toJobId(final Long subId)
    {
        return JobId.of(DefaultSubscriptionManager.SUBSCRIPTION_PREFIX + ":" + subId);
    }

}
