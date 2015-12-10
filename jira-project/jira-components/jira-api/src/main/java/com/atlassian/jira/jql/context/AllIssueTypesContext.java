package com.atlassian.jira.jql.context;

/**
 * Represents the special case of all issue types in an unenumerated form.
 *
 * @since v4.0
 */
public class  AllIssueTypesContext implements IssueTypeContext
{
    public static final AllIssueTypesContext INSTANCE = new AllIssueTypesContext();

    public static AllIssueTypesContext getInstance()
    {
        return INSTANCE;
    }
    
    private AllIssueTypesContext()
    {
        //Don't create me.
    }

    public String getIssueTypeId()
    {
        return null;
    }

    public boolean isAll()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "AllIssue Types Context";
    }
}
