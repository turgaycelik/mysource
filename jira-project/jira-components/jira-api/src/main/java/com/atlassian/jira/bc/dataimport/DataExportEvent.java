package com.atlassian.jira.bc.dataimport;

/**
 * The interface implemented by events related to JIRA data export.
 *
 * @since v6.2
 */
public interface DataExportEvent
{
    /**
     * @return the time in milliseconds when the export was started.
     */
    Long getXmlExportTime();
}
