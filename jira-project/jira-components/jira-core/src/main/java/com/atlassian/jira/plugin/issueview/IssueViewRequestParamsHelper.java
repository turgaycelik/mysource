package com.atlassian.jira.plugin.issueview;

import java.util.Map;

/**
 * The purpose of this interface is to provide field information for @{link IssueView} interface implementations.
 * Field definition in build based on url "field" parameters
 */
public interface IssueViewRequestParamsHelper
{
    /**
     * Based on url request parameters build list of requested field ids. 
     *
     * @param requestParameters map of url parameters
     * @return @{link IssueViewFieldParams} object containing requested field description
     */
    IssueViewFieldParams getIssueViewFieldParams(Map requestParameters);
}
