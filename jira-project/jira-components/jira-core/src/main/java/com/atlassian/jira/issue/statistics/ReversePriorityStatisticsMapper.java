package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.Comparator;

/**
 * This StatisticsMapper works exactly the same as PriorityStatisticsMapper, except that it returns a reverse-order comparator.
 *
 * <p> This class needs to minimise its outgoing references so as not to cause memory leaks.
 * See comments in {@link PriorityStatisticsMapper}.
 */
public class ReversePriorityStatisticsMapper extends PriorityStatisticsMapper implements StatisticsMapper<IssueConstant>
{
    private final Comparator<IssueConstant> comparator;

    public ReversePriorityStatisticsMapper(final ConstantsManager constantsManager)
    {
        super(constantsManager);
        comparator = Collections.reverseOrder(super.getComparator());
    }

    @Override
    public Comparator<IssueConstant> getComparator()
    {
        return comparator;
    }
}
