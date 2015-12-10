package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchRequestAppender;

/**
 * PriorityStatisticsMapper.
 * <p/>
 * <p> It is extremely important to minimise the references in this class, because Lucene keeps a global cache of these
 * objects in a WeakHashMap. If this class has lots of outgoing references, then the retained size increases if this
 * class is held onto, and also the risk of a circular reference back to the key of the WeakHashMap increases, which
 * means it can't be garbage collected.
 */
public class PriorityStatisticsMapper extends AbstractConstantStatisticsMapper
        implements SearchRequestAppender.Factory<IssueConstant>
{
    public PriorityStatisticsMapper(ConstantsManager constantsManager)
    {
        super(constantsManager);
    }

    protected String getConstantType()
    {
        return ConstantsManager.PRIORITY_CONSTANT_TYPE;
    }

    protected String getIssueFieldConstant()
    {
        return IssueFieldConstants.PRIORITY;
    }

    public String getDocumentConstant()
    {
        return DocumentConstants.ISSUE_PRIORITY;
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return true;
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
