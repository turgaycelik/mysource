package com.atlassian.jira.upgrade.tasks;

import java.util.Collections;

import com.atlassian.core.ofbiz.AbstractOFBizTestCase;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test {@link UpgradeTask_Build6207}, which sets the base sequence number for issue constants.
 *
 * @since v6.1.2
 */

@RunWith(ListeningMockitoRunner.class)
public class TestUpgradeTask_Build6207 extends AbstractOFBizTestCase
{
    @Mock
    @AvailableInContainer
    OfBizDelegator mockOfBizDelegator;

    @Mock
    @AvailableInContainer
    DelegatorInterface mockDelegatorInterface;

    @Test
    public void testUpgrade() throws Exception
    {
        UpgradeTask_Build6207 upgradeTask_build6207 = new UpgradeTask_Build6207(mockOfBizDelegator);

        GenericValue bigId = mock(GenericValue.class);
        when(bigId.getString("id")).thenReturn("10000");

        GenericValue nonNumberId = mock(GenericValue.class);
        when(nonNumberId.getString("id")).thenReturn("thisisnotanumber");

        GenericValue nullId = mock(GenericValue.class);
        when(nonNumberId.getString("id")).thenReturn(null);

        GenericValue moreReasonableIssueConstant = mock(GenericValue.class);
        when(moreReasonableIssueConstant.getString("id")).thenReturn("1");

        // Happy path
        when(mockOfBizDelegator.findAll("Resolution")).thenReturn(ImmutableList.of(moreReasonableIssueConstant));
        GenericValue sequenceItemResolution = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("SequenceValueItem", ImmutableMap.of("seqName", "Resolution"))).thenReturn(ImmutableList.of(sequenceItemResolution));

        // This should trigger the store new id > 10000 case
        when(mockOfBizDelegator.findAll("IssueType")).thenReturn(ImmutableList.of(bigId, nonNumberId));
        GenericValue sequenceItemIssueType = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("SequenceValueItem", ImmutableMap.of("seqName", "IssueType"))).thenReturn(ImmutableList.of(sequenceItemIssueType));

        // Edge case - no issue constants
        when(mockOfBizDelegator.findAll("Priority")).thenReturn(Collections.<GenericValue>emptyList());
        GenericValue sequenceItemPriority = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("SequenceValueItem", ImmutableMap.of("seqName", "Priority"))).thenReturn(ImmutableList.of(sequenceItemPriority));

        // Edge case - issue constant returns null when asked for id
        when(mockOfBizDelegator.findAll("Status")).thenReturn(ImmutableList.of(nullId));
        GenericValue sequenceItemStatus = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("SequenceValueItem", ImmutableMap.of("seqName", "Status"))).thenReturn(ImmutableList.of(sequenceItemStatus));

        when(mockOfBizDelegator.getDelegatorInterface()).thenReturn(mockDelegatorInterface);

        upgradeTask_build6207.doUpgrade(false);

        for (String entityName : UpgradeTask_Build6207.ISSUE_CONSTANT_ENTITIES)
        {
            verify(mockDelegatorInterface).getNextSeqId(entityName);
        }

        verify(sequenceItemResolution).set("seqId", 10000L);
        verify(mockOfBizDelegator).store(sequenceItemResolution);

        verify(sequenceItemIssueType).set("seqId", 10001L);
        verify(mockOfBizDelegator).store(sequenceItemIssueType);

        verify(sequenceItemPriority).set("seqId", 10000L);
        verify(mockOfBizDelegator).store(sequenceItemPriority);

        verify(sequenceItemStatus).set("seqId", 10000L);
        verify(mockOfBizDelegator).store(sequenceItemStatus);

        verify(mockOfBizDelegator, times(4)).refreshSequencer();
    }
}
