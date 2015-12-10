package com.atlassian.jira.memoryinspector;

import org.apache.log4j.Logger;

/**
 * @since v6.3
 */
public interface InspectionReport
{
    void printReport(Logger log);
    boolean inspectionPassed();
}
