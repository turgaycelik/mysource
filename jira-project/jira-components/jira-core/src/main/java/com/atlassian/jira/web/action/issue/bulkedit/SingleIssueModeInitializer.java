package com.atlassian.jira.web.action.issue.bulkedit;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.SingleIssueModeEditBean;
import com.google.common.collect.ImmutableList;

/**
 * Initializes a {@link BulkEditBean} in "single issue modes
 *
 * @see SingleIssueModeEditBean
 */
public class SingleIssueModeInitializer
{

    /**
     * Initializes given <code>editBean</code> with information that is needed
     * for bulk operation (i.e. move).
     *
     * @param editBean bean to be initialized
     * @param singleIssue selected issue
     *
     * @see BulkEditBean#initSelectedIssues(java.util.Collection)
     */
   public static void initialize(@Nullable BulkEditBean editBean, @Nullable Issue singleIssue)
   {
       if(singleIssue != null && editBean != null)
       {
           ImmutableList<Issue> issuesInUse = ImmutableList.of(singleIssue);
           editBean.setMaxIssues(1);
           editBean.setIssuesInUse(issuesInUse);
           editBean.setIssuesFromSearchRequest(issuesInUse);
           editBean.setSingleIssueKey(singleIssue.getKey());
           editBean.initSelectedIssues(issuesInUse);
       }
   }
}
