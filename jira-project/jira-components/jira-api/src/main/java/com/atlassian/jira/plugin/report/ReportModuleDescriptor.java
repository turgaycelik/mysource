/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.report;

import com.atlassian.jira.plugin.ConfigurableModuleDescriptor;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.project.Project;

/**
 * The report plugin allows end users to write pluggable reports for JIRA.
 *
 * @see Report
 */

public interface ReportModuleDescriptor extends JiraResourcedModuleDescriptor<Report>, ConfigurableModuleDescriptor
{
    public Report getModule();

    public String getLabel();

    public String getLabelKey();

    /**
     * Returns url for first page of this report. Project parameter is provided to insert context into the URL
     *
     * @param project project in which context this report is opened
     * @return url for the first page of this report
     * @since  6.2
     */
    public String getUrl(Project project);
}
