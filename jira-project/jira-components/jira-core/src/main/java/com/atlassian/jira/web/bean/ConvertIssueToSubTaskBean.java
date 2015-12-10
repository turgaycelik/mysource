package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperationImpl;

import javax.servlet.http.HttpSession;

/**
 * Convert issue to sub-task bean
 */
public class ConvertIssueToSubTaskBean extends ConvertIssueBean
{
    public static final String SESSION_KEY = "com.atlassian.jira.web.bean.ConvertIssueToSubTaskBean";

    private static final long serialVersionUID = 3493349530029824743L;

    protected String parentIssueKey;

    /**
     * Creates a new blank instance
     */
    protected ConvertIssueToSubTaskBean()
    {
        super();
    }

    /**
     * Retrieves itself from the session. If not found in the session, a new
     * instance is created and stored in the session.
     *
     * @param session session
     * @param issueId issue id
     * @return bean
     */
    public static ConvertIssueToSubTaskBean getConvertIssueToSubTaskBean(HttpSession session, String issueId)
    {
        return (ConvertIssueToSubTaskBean) getBeanFromSession(ConvertIssueToSubTaskBean.class, SESSION_KEY, session, issueId);
    }


    /**
     * Returns the issue operation of this bean
     *
     * @return the issue operation of this bean
     */
    public IssueOperation getIssueOperation()
    {
        return new IssueOperationImpl("Issue To Sub-task Operation", "Converts an issue to a sub-task");
    }

    public void clearBean()
    {
        super.clearBean();
        parentIssueKey = null;
    }

    public String extraFieldsToString()
    {
        String parent = null;
        if (parentIssueKey != null)
        {
            parent = "Parent: " + parentIssueKey;
        }
        return parent;
    }

    /**
     * Returns parent issue key
     *
     * @return parent issue key
     */
    public String getParentIssueKey()
    {
        return parentIssueKey;
    }

    /**
     * Sets parent issue key
     *
     * @param parentIssueKey parent issue key
     */
    public void setParentIssueKey(String parentIssueKey)
    {
        this.parentIssueKey = parentIssueKey;
    }

}
