package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.comparator.KeyComparator;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;

import java.util.Comparator;

public class IssueKeyStatisticsMapper implements LuceneFieldSorter
{
    public static final LuceneFieldSorter MAPPER = new IssueKeyStatisticsMapper();

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_KEY;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        return documentValue;
    }

    public Comparator getComparator()
    {
        return KeyComparator.COMPARATOR;
    }

    public int hashCode()
    {
        return DocumentConstants.ISSUE_KEY.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        // Ensure that we test that the other object is an instance of the same class! As we could have a
        // totally different sorter also sort on the Issue Key constant (so comparing constants is not enought).
        // For example, the MultiIssueKeySearcher in the JIRA toolkit also sorts on Issue Key 
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        IssueKeyStatisticsMapper that = (IssueKeyStatisticsMapper) obj;

        return DocumentConstants.ISSUE_KEY.equals(that.getDocumentConstant());
    }
}
