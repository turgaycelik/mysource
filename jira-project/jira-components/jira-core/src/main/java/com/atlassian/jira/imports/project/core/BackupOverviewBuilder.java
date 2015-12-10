package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.plugin.PluginVersion;

import java.util.Map;

/**
 * This is the intermediate object that collects information from the SAX parser and is able to produce a
 * {@link com.atlassian.jira.imports.project.core.BackupOverview}.
 * <br/>
 * This object is used to build up data gleaned from an XML backup. This contains the JIRA information (build number,
 * edition, plugins, etc.) and all the project information (components, versions, etc.) about the data stored in an
 * XML backup.
 * <br/>
 * NOTE: This object only collates all its project data when the {@link #getBackupOverview()} method is called. This
 * object is not meant to be used as a transfer object, instead it is an incrementally populated builder.
 *
 * @since v3.13
 */
public interface BackupOverviewBuilder
{
    /**
     * Returns a {@link com.atlassian.jira.imports.project.core.BackupOverview} that is populated by the current
     * state of the builder.
     *
     * @return backupOverview that contains the {@link com.atlassian.jira.imports.project.core.BackupProject}'s and
     * JIRA system information that has been registered with the builder.
     */
    BackupOverview getBackupOverview();

    /**
     * Allows you to add a reference to an {@link com.atlassian.jira.external.beans.ExternalProject}.
     *
     * @param project the project to add.
     */
    void addProject(ExternalProject project);

    /**
     * Allows you to add a reference to a {@link com.atlassian.jira.plugin.PluginVersion}.
     *
     * @param pluginVersion the plugin version to add.
     */
    void addPluginVersion(PluginVersion pluginVersion);

    /**
     * Allows you to add a reference to an {@link com.atlassian.jira.external.beans.ExternalVersion}.
     *
     * @param version   the version information to associate with the project.
     */
    void addVersion(ExternalVersion version);

    /**
     * Allows you to add a reference to an {@link com.atlassian.jira.external.beans.ExternalComponent}.
     *
     * @param component the component information to associate with the project.
     */
    void addComponent(ExternalComponent component);

    /**
     * Allows you to register an issue against a project. The issue id's are made available via the
     * {@link BackupProject#getIssueIds()} method.
     *
     * @param issue which has its id and project set. We should be able to convert the issue id to a long.
     */
    void addIssue(ExternalIssue issue);

    /**
     * Registers the JIRA build number with this object.
     *
     * @param buildNumber the build number as contained in the JIRA backup.
     */
    void setBuildNumber(String buildNumber);

    /**
     * Registers the JIRA edition with this object.
     *
     * @param edition the edition as contained in the JIRA backup.
     */
    void setEdition(String edition);

    /**
     * Registers the value of the "UnassignedIssuesAllowed" setting.
     *
     * @param unassignedIssuesAllowed The value of the "UnassignedIssuesAllowed" setting.
     */
    void setUnassignedIssuesAllowed(final boolean unassignedIssuesAllowed);

    /**
     * Regiserters a portion of a projects custom field configuration so that the builder can collate this with the
     * {@link #addFieldConfigSchemeIssueType(com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.FieldConfigSchemeIssueType)}
     * and {@link #addExternalCustomField(com.atlassian.jira.external.beans.ExternalCustomField)} data to produce a
     * project relevant custom field configuration.
     *
     * @param configuration is the configuration represented in the backup XML.
     */
    void addConfigurationContext(final BackupOverviewBuilderImpl.ConfigurationContext configuration);

    /**
     * Regiserters a portion of a projects custom field configuration so that the builder can collate this with the
     * {@link #addConfigurationContext(com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.ConfigurationContext)}
     * and {@link #addExternalCustomField(com.atlassian.jira.external.beans.ExternalCustomField)} data to produce a
     * project relevant custom field configuration.
     *
     * @param fieldConfigSchemeIssueType is the linked issue type information represented in the backup XML.
     */
    void addFieldConfigSchemeIssueType(final BackupOverviewBuilderImpl.FieldConfigSchemeIssueType fieldConfigSchemeIssueType);

    /**
     * Registers the custom field information portion of a projects custom field configuration so that the builder can collate this with the
     * {@link #addConfigurationContext(com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.ConfigurationContext)}
     * and {@link #addFieldConfigSchemeIssueType(com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.FieldConfigSchemeIssueType)} data to produce a
     * project relevant custom field configuration.
     * @param externalCustomField is the custom field information represented in the backup XML.
     */
    void addExternalCustomField(final ExternalCustomField externalCustomField);

    /**
     * Used to handle populating the builder from a SAX parser. This method will delegate the actual handling
     * of the element and attributes to an instance of a {@link com.atlassian.jira.imports.project.populator.BackupOverviewPopulator}.
     * This will then populate this object with the information it gains from the XML information.
     *
     * @param elementName identifies the XML element.
     * @param attributes  identifies the attributes associated with the XML element.
     * @throws ParseException if the attributes are invalid for this element.
     */
    void populateInformationFromElement(String elementName, Map attributes) throws ParseException;
}
