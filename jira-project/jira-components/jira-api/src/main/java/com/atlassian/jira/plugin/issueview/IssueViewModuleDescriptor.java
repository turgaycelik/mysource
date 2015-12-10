package com.atlassian.jira.plugin.issueview;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.plugin.web.descriptors.ConditionalDescriptor;

/**
 * An issue view allows you to view an issue in different ways  (eg XML, Word, PDF)
 *
 * @see IssueView
 */
public interface IssueViewModuleDescriptor extends JiraResourcedModuleDescriptor<IssueView>, OrderableModuleDescriptor, ConditionalDescriptor
{
    IssueView getIssueView();

    String getFileExtension();

    String getContentType();

    String getURLWithoutContextPath(String issueKey);
}
