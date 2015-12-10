package com.atlassian.jira.appconsistency.integrity.check;

import java.util.List;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;

public class BaseFilterSubscriptionsCheckTest
{
    private static class ForTestingBaseFilterSubscriptionsCheckTest extends BaseFilterSubscriptionsCheck
    {
        private Iterable<DeleteEntityAmendment> stubMessages;

        protected ForTestingBaseFilterSubscriptionsCheckTest(final OfBizDelegator ofBizDelegator, final int id)
        {
            super(ofBizDelegator, id);
        }

        @Override
        protected void doRealCheck(final boolean correct, final GenericValue subscription, final List messages)
                throws IntegrityException
        {
            if(stubMessages != null) {
                messages.addAll(Lists.newArrayList(stubMessages));
                stubMessages = null;
            }
        }

        @Override
        public String getDescription()
        {
            return "DummyString";
        }

        public void setReturnMessages(Iterable<DeleteEntityAmendment> messages)
        {
            this.stubMessages = messages;
        }
    }

    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private OfBizDelegator delegator = new MockOfBizDelegator();

    @Mock
    @AvailableInContainer
    private SubscriptionManager subscriptionManager;

    private ForTestingBaseFilterSubscriptionsCheckTest subscriptionsCheck;
    private GenericValue filterSubscription1;
    private GenericValue filterSubscription2;

    @Before
    public void setUp() throws Exception
    {
        filterSubscription1 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", new Long(1), "filterID", new Long(1), "username", "nick"));
        filterSubscription2 = UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", new Long(2), "filterID", new Long(2), "username", "nick"));
        UtilsForTests.getTestEntity("FilterSubscription", ImmutableMap.of("id", new Long(3), "filterID", new Long(3), "username", "nick"));

        subscriptionsCheck = new ForTestingBaseFilterSubscriptionsCheckTest(delegator, 1);
    }

    @Test
    public void testPreviewShouldNoRemoveAnyValues() throws Exception
    {
        final DeleteEntityAmendment message = new DeleteEntityAmendment(1, "", filterSubscription1);
        subscriptionsCheck.setReturnMessages(ImmutableList.of(message));

        final List<DeleteEntityAmendment> previewResult = subscriptionsCheck.preview();

        Assert.assertThat(previewResult, Matchers.contains(message));
        Mockito.verify(subscriptionManager, never()).deleteSubscription(anyLong());
    }

    @Test
    public void testCorrectShouldRemoveAllValuesReturnedByChild() throws Exception
    {
        final DeleteEntityAmendment messageOne = new DeleteEntityAmendment(1, "someString", filterSubscription1);
        final DeleteEntityAmendment messageTwo = new DeleteEntityAmendment(2, "someString2", filterSubscription2);
        subscriptionsCheck.setReturnMessages(ImmutableList.of(messageOne, messageTwo));

        final List<DeleteEntityAmendment> correctResult = subscriptionsCheck.correct();

        Assert.assertThat(correctResult, Matchers.containsInAnyOrder(messageOne, messageTwo));
        Mockito.verify(subscriptionManager).deleteSubscription(filterSubscription1.getLong("id"));
        Mockito.verify(subscriptionManager).deleteSubscription(filterSubscription2.getLong("id"));
    }
}
