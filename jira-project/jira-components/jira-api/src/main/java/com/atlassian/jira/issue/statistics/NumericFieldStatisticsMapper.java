package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.NumericComparator;

import java.util.Comparator;

public class NumericFieldStatisticsMapper implements StatisticsMapper
{
    private final String documentConstant;

    public NumericFieldStatisticsMapper(String documentConstant)
    {
        this.documentConstant = documentConstant;
    }

    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        return new Double(documentValue);
    }

    public Comparator getComparator()
    {
        return NumericComparator.COMPARATOR;
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

        final NumericFieldStatisticsMapper that = (NumericFieldStatisticsMapper) o;

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);
    }

    public int hashCode()
    {
        return (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
    }
}
