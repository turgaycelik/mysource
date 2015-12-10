package com.atlassian.jira.auditing;

import java.util.List;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v6.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAuditingRetentionPeriod
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private FeatureManager featureManager;

    @Test
    public void shouldAllowForUnlimitedAndDefaultToUnlimitedIfNotOnDemand()
    {
        when(featureManager.isOnDemand()).thenReturn(false);

        final AuditingRetentionPeriod defaultPeriod = AuditingRetentionPeriod.getDefault();
        List<AuditingRetentionPeriod> validPeriods = AuditingRetentionPeriod.getValidValues();

        assertThat(defaultPeriod, sameInstance(AuditingRetentionPeriod.UNLIMITED));
        assertThat(validPeriods.toArray(new AuditingRetentionPeriod[validPeriods.size()]),
                arrayContaining(
                        AuditingRetentionPeriod.ONE_MONTH,
                        AuditingRetentionPeriod.THREE_MONTHS,
                        AuditingRetentionPeriod.SIX_MONTHS,
                        AuditingRetentionPeriod.UNLIMITED));
    }

    @Test
    public void shouldNotAllowForUnlimitedAndDefaultToThreeMonthsIfOnDemand()
    {
        when(featureManager.isOnDemand()).thenReturn(true);

        final AuditingRetentionPeriod defaultPeriod = AuditingRetentionPeriod.getDefault();
        List<AuditingRetentionPeriod> validPeriods = AuditingRetentionPeriod.getValidValues();

        assertThat(defaultPeriod, sameInstance(AuditingRetentionPeriod.THREE_MONTHS));
        assertThat(validPeriods.toArray(new AuditingRetentionPeriod[validPeriods.size()]),
                arrayContaining(
                        AuditingRetentionPeriod.ONE_MONTH,
                        AuditingRetentionPeriod.THREE_MONTHS,
                        AuditingRetentionPeriod.SIX_MONTHS));
    }
}
