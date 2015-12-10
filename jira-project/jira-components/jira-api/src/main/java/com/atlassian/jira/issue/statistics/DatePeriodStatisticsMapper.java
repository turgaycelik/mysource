package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.dbc.Assertions;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;

import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;

/**
 * A StatsMapper that takes the document constant name (lucene) and a JFreeChart TimePeriod class, and rounds the dates
 * to the appropriate time period
 *
 * @see org.jfree.data.time.TimePeriod
 */
public class DatePeriodStatisticsMapper
        implements StatisticsMapper<TimePeriod>, SearchRequestAppender.Factory<TimePeriod>
{
    private final Class timePeriodClass;
    private final TimeZone periodTimeZone;
    private final String documentConstant;

    /**
     * Creates a new DatePeriodStatisticsMapper using the given time zone.
     */
    public DatePeriodStatisticsMapper(Class timePeriodClass, String documentConstant, TimeZone periodTimeZone)
    {
        this.documentConstant = documentConstant;
        this.timePeriodClass = timePeriodClass;
        this.periodTimeZone = periodTimeZone;
    }

    /**
     * @deprecated Use #getSearchRequestAppender().appendInclusiveSingleValueClause()
     */
    @Override
    @Deprecated
    public SearchRequest getSearchUrlSuffix(TimePeriod timePeriod, SearchRequest searchRequest)
    {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(timePeriod, searchRequest);
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender<TimePeriod> getSearchRequestAppender()
    {
        return new TimePeriodSearchRequestAppender(documentConstant);
    }

    @Override
    public String getDocumentConstant()
    {
        return documentConstant;
    }

    @Override
    public TimePeriod getValueFromLuceneField(String documentValue)
    {
        Date date = LuceneUtils.stringToDate(documentValue);
        if (date == null)
        {
            return null;
        }
        return RegularTimePeriod.createInstance(timePeriodClass, date, periodTimeZone);
    }

    @Override
    public Comparator<TimePeriod> getComparator()
    {
        return new Comparator<TimePeriod>()
        {
            public int compare(TimePeriod timePeriod1, TimePeriod timePeriod2)
            {
                if (timePeriod1 == null)
                {
                    return -1;
                }

                return timePeriod1.compareTo(timePeriod2);
            }
        };
    }


    @Override
    public boolean isValidValue(TimePeriod value)
    {
        return value != null;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    static class TimePeriodSearchRequestAppender
            implements SearchRequestAddendumBuilder.AddendumCallback<TimePeriod>, SearchRequestAppender<TimePeriod>
    {

        private final String documentConstant;

        public TimePeriodSearchRequestAppender(String documentConstant)
        {
            this.documentConstant = Assertions.notNull(documentConstant);
        }

        @Override
        public void appendNonNullItem(TimePeriod timePeriod, JqlClauseBuilder clauseBuilder)
        {
            Date startDate = timePeriod.getStart();
            Date endDate = new Date(timePeriod.getEnd().getTime());

            clauseBuilder.addDateRangeCondition(documentConstant, startDate, endDate);
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder)
        {
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(TimePeriod value, SearchRequest searchRequest)
        {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable<? extends TimePeriod> values, SearchRequest searchRequest)
        {
            // doesn't really make sense for this implementation
            return null;
        }
    }
}