package com.atlassian.jira.bc.dataimport;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Option;

/**
 * Raised after a JIRA XML import has finished.
 *
 * @since v5.1
 */
@PublicApi
public final class ImportCompletedEvent implements DataImportEvent
{
    private final boolean importSuccessful;
    private final Option<Long> xmlExportTime;

    @Deprecated
    ImportCompletedEvent(boolean importSuccessful)
    {
        this(importSuccessful, Option.<Long>none());
    }

    public ImportCompletedEvent(final boolean importSuccessful, final Option<Long> xmlExportTime)
    {
        this.importSuccessful = importSuccessful;
        this.xmlExportTime = xmlExportTime;
    }

    /**
     * @return a boolean indicating whether the XML import was successful
     */
    public boolean isImportSuccessful()
    {
        return importSuccessful;
    }

    @Override
    public Option<Long> getXmlExportTime()
    {
        return xmlExportTime;
    }
}
