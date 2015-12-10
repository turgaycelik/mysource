/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 2, 2004
 * Time: 8:03:57 PM
 */
package com.atlassian.jira.plugin.report;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.web.action.ProjectActionSupport;

import java.util.Map;

/**
 * The interface for pluggable reports within JIRA.
 * 
 * @see com.atlassian.jira.plugin.report.impl.AbstractReport
 * @see ReportModuleDescriptor
 */
@PublicSpi
public interface Report
{
    /**
     * Initialise this report, given the report's module descriptor.
     * <p>
     * This method is run immediately after the report is constructed.
     */
    void init(ReportModuleDescriptor reportModuleDescriptor);

    /**
     * Validate the parameters passed to this report from the UI.
     * <p>
     * Any errors should be added to the action errors.
     */
    void validate(ProjectActionSupport action, Map params);

    /**
     * Generate the report's HTML - usually from the Velocity resource named "html".
     */
    String generateReportHtml(ProjectActionSupport action, Map params) throws Exception;

    /**
     * Whether or not this report has an Excel view.
     */
    boolean isExcelViewSupported();

    /**
     * Generate the report's Excel HTML - usually from the Velocity resource named "excel".
     */
    String generateReportExcel(ProjectActionSupport action, Map params) throws Exception;

    /**
     * Whether or not to show this report at the current state of the system.
     */
    boolean showReport();
}