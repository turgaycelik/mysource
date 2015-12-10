package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.OSPropertyParser;

import java.util.Map;

/**
 * This BackupOverviewPopulator reads System Info from OSProperty values and adds it to the BackupOverviewBuilder.
 * Namely, it adds the Build Number and the Edition.
 * Utilizes the fact that OSPropertyEntry will always be processed before OSPropertyString so that we can find the build number.
 *
 * @since v3.13
 */
public class SystemInfoPopulator extends OSPropertyParser implements BackupOverviewPopulator
{
    private static final String JIRA_PROPERTIES_KEY = "jira.properties";
    private String buildNumberId = null;
    private String editionId = null;
    private String allowUnassignedId = null;

    public void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException
    {
        if (elementName == null)
        {
            throw new IllegalArgumentException("'elementName' cannot be null.");
        }

        if (elementName.equals(OSPROPERTY_ENTRY))
        {
            // <OSPropertyEntry id="10142" entityName="jira.properties" entityId="1" propertyKey="jira.edition" type="5"/>
            final String id = getID(attributes);
            final String entityName = getEntityName(attributes);

            if (JIRA_PROPERTIES_KEY.equals(entityName))
            {
                final String propertyKey = getPropertyKey(attributes);
                // Check if this is one of the OSProperties that we are interested in:
                if (APKeys.JIRA_PATCHED_VERSION.equals(propertyKey))
                {
                    // Build Number: Remember the ID of the OSProperty so we can retrieve the value later.
                    buildNumberId = id;
                }
                if (APKeys.JIRA_EDITION.equals(propertyKey))
                {
                    // Edition: Remember the ID of the OSProperty so we can retrieve the value later.
                    editionId = id;
                }
                if (APKeys.JIRA_OPTION_ALLOWUNASSIGNED.equals(propertyKey))
                {
                    // Remember the ID of the OSProperty so we can retrieve the value later.
                    allowUnassignedId = id;
                }
            }
        }

        if (elementName.equals(OSPROPERTY_STRING))
        {
            //  <OSPropertyString id="10142" value="enterprise"/>
            final String id = getID(attributes);
            final String value = getValue(attributes);
            if (id != null)
            {
                if (id.equals(buildNumberId))
                {
                    backupOverviewBuilder.setBuildNumber(value);
                }
                if (id.equals(editionId))
                {
                    backupOverviewBuilder.setEdition(value);
                }
            }
        }
        if (elementName.equals(OSPROPERTY_NUMBER))
        {
            // <OSPropertyNumber id="10017" value="1"/>
            final String id = getID(attributes);
            final String value = getValue(attributes);
            if (id != null)
            {
                if (id.equals(allowUnassignedId))
                {
                    backupOverviewBuilder.setUnassignedIssuesAllowed(parseNumberAsBoolean(value));
                }
            }
        }
    }
}
