package com.atlassian.jira.bc.dataimport;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Event raised when a data export begins.
 *
 * @since v5.0
 */
@PublicApi
public class ExportStartedEvent implements DataExportEvent
{
    /**
     * The user that instigated the export. May be null if, for instance, it is
     * triggered by a scheduled job and not a user.
     */
    public final User loggedInUser;

    /**
     * The filename the data is being saved to.
     */
    public final String filename;

    /**
     * The time in milliseconds when the export was started..
     */
    public final Long xmlExportTime;

    @Deprecated
    public ExportStartedEvent(final User user, final String filename)
    {
        this(user, filename, null);
    }

    public ExportStartedEvent(final User user, final String filename, final Long xmlExportTime)
    {
        this.loggedInUser = user;
        this.filename = filename;
        this.xmlExportTime = xmlExportTime;
    }

    @Override
    public Long getXmlExportTime()
    {
        return xmlExportTime;
    }
}
