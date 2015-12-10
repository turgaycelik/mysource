package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.LongComparator;
import org.apache.lucene.document.NumberTools;

import java.util.Comparator;

public class VotesStatisticsMapper implements StatisticsMapper
{
    public static final StatisticsMapper MAPPER = new VotesStatisticsMapper();


    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_VOTES;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        return NumberTools.stringToLong(documentValue);
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

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final VotesStatisticsMapper that = (VotesStatisticsMapper) o;

        return DocumentConstants.ISSUE_VOTES.equals(that.getDocumentConstant());
    }

    public int hashCode()
    {
        return DocumentConstants.ISSUE_VOTES.hashCode();
    }
}
