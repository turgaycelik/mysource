package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;

import java.util.Map;

/**
 * Used to populate the BackupOverview object. Bridges element parsers and the information object.
 * Each populator is in charge of one particular entity type.
 *
 * @since v3.13
 */
public interface BackupOverviewPopulator
{
    /**
     * If the entity referred to in the given elementName is managed by this Populator, then it builds the appropriate
     * object and adds it to the BackupOverviewBuilder.
     *
     * @param backupOverviewBuilder Collects overview information from the import file.
     * @param elementName The name of the entity element in the backup XML.
     * @param attributes The attributes from the backup XML for this entity.
     * @throws ParseException If the attributes for this entity are invalid.
     */
    void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException;
}
