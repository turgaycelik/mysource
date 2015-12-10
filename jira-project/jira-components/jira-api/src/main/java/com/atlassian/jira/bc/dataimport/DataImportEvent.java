package com.atlassian.jira.bc.dataimport;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Option;

/**
 * The interface implemented by events related to JIRA data import.
 *
 * @since v6.2
 */
@PublicApi
public interface DataImportEvent
{
    /**
     * @return time in milliseconds when the imported data was exported.
     */
    Option<Long> getXmlExportTime();
}
