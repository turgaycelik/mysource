package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import org.apache.lucene.document.NumberTools;

/**
 * A {@link com.atlassian.jira.issue.statistics.StatisticsMapper} specifically for
 * {@link com.atlassian.jira.issue.fields.OriginalEstimateSystemField},
 * {@link com.atlassian.jira.issue.fields.TimeEstimateSystemField} and
 * {@link com.atlassian.jira.issue.fields.TimeSpentSystemField} as their index values are stored in a specific format.
 *
 * @since v4.0
 */
public class TimeTrackingStatisticsMapper extends LongFieldStatisticsMapper
{
    public static final StatisticsMapper TIME_ESTIMATE_ORIG = new TimeTrackingStatisticsMapper(DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG);
    public static final StatisticsMapper TIME_ESTIMATE_CURR = new TimeTrackingStatisticsMapper(DocumentConstants.ISSUE_TIME_ESTIMATE_CURR);
    public static final StatisticsMapper TIME_SPENT = new TimeTrackingStatisticsMapper(DocumentConstants.ISSUE_TIME_SPENT);

    public TimeTrackingStatisticsMapper(String documentConstant)
    {
        super(documentConstant);
    }

    @Override
    public Object getValueFromLuceneField(final String documentValue)
    {
        if (FieldIndexer.NO_VALUE_INDEX_VALUE.equals(documentValue))
        {
            return null;
        }
        else
        {
            return NumberTools.stringToLong(documentValue);
        }
    }
}
