package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchRequestAppender;

import java.util.Comparator;

public class IssueTypeStatisticsMapper extends AbstractConstantStatisticsMapper
        implements SearchRequestAppender.Factory<IssueConstant>
{
    public IssueTypeStatisticsMapper(ConstantsManager constantsManager)
    {
        super(constantsManager);
    }

    protected String getConstantType()
    {
        return ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE;
    }

    protected String getIssueFieldConstant()
    {
        return IssueFieldConstants.ISSUE_TYPE;
    }

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_TYPE;
    }

    public Comparator<IssueConstant> getComparator()
    {
        return new Comparator<IssueConstant>()
        {
            @Override
            public int compare(IssueConstant o1, IssueConstant o2)
            {
                if (o1 == null && o2 == null)
                { return 0; }
                else if (o2 == null) // any value is less than null
                { return -1; }
                else if (o1 == null) // null is greater than any value
                { return 1; }

                String name1 = o1.getName();
                String name2 = o2.getName();
                if (name1 == null && name2 == null)
                { return 0; }
                else if (name2 == null) // any value is less than null
                { return -1; }
                else if (name1 == null) // null is greater than any value
                { return 1; }

                return name1.compareToIgnoreCase(name2);
            }
        };
    }

    /**
     * @since v6.0
     */
    @Override
    public SearchRequestAppender<IssueConstant> getSearchRequestAppender()
    {
        return new IssueConstantSearchRequestAppender(getIssueFieldConstant());
    }
}
