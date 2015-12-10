package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import org.apache.log4j.Logger;

import java.util.Comparator;

public class SubTaskStatisticsMapper implements LuceneFieldSorter
{
    private static final Logger log = Logger.getLogger(SubTaskStatisticsMapper.class);
    private final IssueManager issueManager;

    public SubTaskStatisticsMapper(IssueManager issueManager)
    {
        this.issueManager = issueManager;
    }

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_SUBTASKS;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        return issueManager.getIssue(new Long(documentValue));
    }

    public Comparator getComparator()
    {
        return IssueKeyComparator.COMPARATOR;
    }

    public int hashCode()
    {
        return (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        final SubTaskStatisticsMapper that = (SubTaskStatisticsMapper) obj;

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);
    }
}
