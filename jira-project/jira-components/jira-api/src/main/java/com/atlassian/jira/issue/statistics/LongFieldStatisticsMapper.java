package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.LongComparator;

import java.util.Comparator;

public class LongFieldStatisticsMapper implements StatisticsMapper
{
    public static final StatisticsMapper WORK_RATIO = new LongFieldStatisticsMapper(DocumentConstants.ISSUE_WORKRATIO);
    public static final StatisticsMapper PROGRESS = new LongFieldStatisticsMapper(DocumentConstants.ISSUE_PROGRESS);

    private final String documentConstant;

    public LongFieldStatisticsMapper(String documentConstant)
    {
        this.documentConstant = documentConstant;
    }

    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        if ("-1".equals(documentValue))
            return null;
        else
            return new Long(documentValue);
    }

    public Comparator getComparator()
    {
        return LongComparator.COMPARATOR;
    }

    public boolean isValidValue(Object value)
    {
        return true;
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
    }

    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        return null;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof LongFieldStatisticsMapper))
        {
            return false;
        }

        final LongFieldStatisticsMapper that = (LongFieldStatisticsMapper) o;

        if (documentConstant != null ? !documentConstant.equals(that.documentConstant) : that.documentConstant != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
    }
}
