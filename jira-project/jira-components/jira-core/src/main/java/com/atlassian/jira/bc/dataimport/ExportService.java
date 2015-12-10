package com.atlassian.jira.bc.dataimport;

import com.atlassian.jira.action.admin.export.AnonymisingEntityXmlWriter;
import com.atlassian.jira.action.admin.export.DefaultEntityXmlWriter;
import com.atlassian.jira.action.admin.export.EntityXmlWriter;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.crowd.embedded.api.User;

/**
 * A service for the creation of JIRA XML backups.
 *
 * @since v4.4
 */
public interface ExportService
{
    /**
     * Export JIRA to the given filename. It will be a zip file, no matter what suffix you give it. Inside
     * the zip file will be the JIRA XML and the ActiveObjects XML. This will use the NORMAL style.
     *
     * @param filename destination zipfile for the backup
     * @see {#ExportService.Style.NORMAL}
     */
    ServiceOutcome<Void> export(final User loggedInUser, final String filename, final TaskProgressSink taskProgressSink);

    /**
     * Export JIRA to the given filename with the specified backup style.
     * It will be a zip file, no matter what suffix you give it. Inside
     * the zip file will be the JIRA XML and the ActiveObjects XML.
     *
     * @param filename destination zipfile for the backup
     * @param style what time of backup we should perform.
     */
    ServiceOutcome<Void> export(final User loggedInUser, final String filename, final Style style, final TaskProgressSink taskProgressSink);

    /**
     * Export JIRA to the given filename. Only a JIRA XML file will be created,  This will use the NORMAL style.
     *
     * @param xmlFilename destination xmlfile for the backup
     * @see {#ExportService.Style.NORMAL}
     */
    ServiceOutcome<Void> exportForDevelopment(final User loggedInUser, final String xmlFilename, final TaskProgressSink taskProgressSink);

    public enum Style
    {
        ANONYMIZED
                {
                    @Override
                    EntityXmlWriter getEntityXmlWriter()
                    {
                        return new AnonymisingEntityXmlWriter();
                    }
                },
        NORMAL
                {
                    @Override
                    EntityXmlWriter getEntityXmlWriter()
                    {
                        return new DefaultEntityXmlWriter();
                    }
                };

        abstract EntityXmlWriter getEntityXmlWriter();
    }
}
