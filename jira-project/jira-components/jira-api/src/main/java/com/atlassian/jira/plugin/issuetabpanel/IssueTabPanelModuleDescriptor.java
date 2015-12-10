/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;

import java.util.List;

/**
 * An issue tab panel plugin adds extra panel tabs to JIRA's View Issue page.
 */
public interface IssueTabPanelModuleDescriptor extends JiraResourcedModuleDescriptor<IssueTabPanel3>, OrderableModuleDescriptor
{
    public String getLabel();

    public int getOrder();

    public boolean isDefault();

    public boolean isSortable();

    /**
     * @return a boolean indicating whether the issue tab panel can be loaded using an AJAX request
     */
    boolean isSupportsAjaxLoad();
}
