package com.atlassian.jira.bc.dataimport;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Option;

/**
 * Raised before a JIRA XML import is performed.
 *
 * @since v5.1
 */
@PublicApi
public final class ImportStartedEvent implements DataImportEvent
{
    private Option<Long> xmlExportTime;

    @Deprecated
    ImportStartedEvent()
    {
        this(Option.<Long>none());
    }

    ImportStartedEvent(Option<Long> xmlExportTime)
    {
        this.xmlExportTime = xmlExportTime;
    }

    @Override
    public Option<Long> getXmlExportTime()
    {
        return xmlExportTime;
    }
}
