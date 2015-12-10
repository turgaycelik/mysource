package com.atlassian.jira.bc.dataimport;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;

/**
 * Event fired when an export is complete.
 *
 * @since v5.0
 */
@PublicApi
public class ExportCompletedEvent implements DataExportEvent
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
     * The outcome of the export. Will contain success or failure and possibly some error messages.
     */
    public final ServiceOutcome<Void> outcome;


    /**
     * The time in milliseconds when the export was started.
     */
    public final Long xmlExportTime;

    @Deprecated
    public ExportCompletedEvent(final User user, final String filename, final ServiceOutcome<Void> outcome)
    {
        this(user, filename, outcome, null);
    }

    public ExportCompletedEvent(final User user, final String filename, final ServiceOutcome<Void> outcome, final Long xmlExportTime)
    {
        this.loggedInUser = user;
        this.filename = filename;
        this.outcome = outcome;
        this.xmlExportTime = xmlExportTime;
    }

    @Override
    public Long getXmlExportTime()
    {
        return xmlExportTime;
    }
}
