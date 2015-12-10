package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;

import java.util.Comparator;

public class SecurityLevelStatisticsMapper implements StatisticsMapper
{
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public SecurityLevelStatisticsMapper(IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_SECURITY_LEVEL;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        if ("-1".equals(documentValue))
            return null;
        else
            return issueSecurityLevelManager.getIssueSecurity(new Long(documentValue));
    }

    public Comparator getComparator()
    {
        return OfBizComparators.NAME_COMPARATOR;
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

        final SecurityLevelStatisticsMapper that = (SecurityLevelStatisticsMapper) o;

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);
    }

    public int hashCode()
    {
        return (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
    }
}
