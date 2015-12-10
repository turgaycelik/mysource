package com.atlassian.jira.auditing;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

/**
 * Possible values for auditing logs retention period.
 *
 * @since v6.3
 */
@ExperimentalApi
public enum AuditingRetentionPeriod
{
    ONE_MONTH("1", "jira.auditing.retention.period.one.month"),
    THREE_MONTHS("3", "jira.auditing.retention.period.three.months"),
    SIX_MONTHS("6", "jira.auditing.retention.period.six.months"),
    UNLIMITED("-1", "jira.auditing.retention.period.unlimited");

    private final String value;
    private final String nameI18nKey;

    private final static Map<String, AuditingRetentionPeriod> periodsByValue;

    static {
        periodsByValue = ImmutableMap.of(
                ONE_MONTH.getValue(), ONE_MONTH,
                THREE_MONTHS.getValue(), THREE_MONTHS,
                SIX_MONTHS.getValue(), SIX_MONTHS,
                UNLIMITED.getValue(), UNLIMITED
        );
    }

    private AuditingRetentionPeriod(final String value, final String nameI18nKey)
    {
        this.value = value;
        this.nameI18nKey = nameI18nKey;
    }

    public String getValue()
    {
        return value;
    }

    public String getNameI18nKey()
    {
        return nameI18nKey;
    }

    @Nullable
    public static AuditingRetentionPeriod getByValue(final String value)
    {
        if (!periodsByValue.containsKey(value))
        {
            return null;
        }

        return periodsByValue.get(value);
    }

    public static List<AuditingRetentionPeriod> getValidValues()
    {
        final FeatureManager featureManager = ComponentAccessor.getComponent(FeatureManager.class);

        if (featureManager.isOnDemand())
        {
            return ImmutableList.of(ONE_MONTH, THREE_MONTHS, SIX_MONTHS);
        }

        return ImmutableList.of(ONE_MONTH, THREE_MONTHS, SIX_MONTHS, UNLIMITED);
    }

    public static AuditingRetentionPeriod getDefault()
    {
        final FeatureManager featureManager = ComponentAccessor.getComponent(FeatureManager.class);

        if (featureManager.isOnDemand())
        {
            return THREE_MONTHS;
        }

        return UNLIMITED;
    }

    public boolean isShorterThan(final AuditingRetentionPeriod that)
    {
        if (this.isUnlimited())
        {
            return false;
        }

        if (that.isUnlimited())
        {
            return true;
        }

        return this.getNumberOfMonths() < that.getNumberOfMonths();
    }

    public boolean isUnlimited() {
        return this == UNLIMITED;
    }

    public DateTime monthsIntoPast()
    {
        return DateTime.now().minusMonths(getNumberOfMonths());
    }

    private int getNumberOfMonths()
    {
        if (this == UNLIMITED)
        {
            throw new RuntimeException("There is no number of months value for UNLIMITED");
        }
        return Integer.parseInt(getValue());
    }
}
