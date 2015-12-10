package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.populator.BackupOverviewPopulator;
import com.atlassian.jira.imports.project.populator.CustomFieldPopulator;
import com.atlassian.jira.imports.project.populator.IssueIdPopulator;
import com.atlassian.jira.imports.project.populator.PluginVersionPopulator;
import com.atlassian.jira.imports.project.populator.ProjectComponentPopulator;
import com.atlassian.jira.imports.project.populator.ProjectPopulator;
import com.atlassian.jira.imports.project.populator.ProjectVersionPopulator;
import com.atlassian.jira.imports.project.populator.SystemInfoPopulator;
import com.atlassian.jira.plugin.PluginVersion;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v3.13
 */
public class BackupOverviewBuilderImpl implements BackupOverviewBuilder
{

    private static final Logger log = Logger.getLogger(BackupOverviewBuilderImpl.class);

    private final List projects;
    private final List pluginVersions;
    private final Map issueIds;
    private final Map issueIdToKeyMap;
    private final Map versions;
    private final Map components;
    private final List overviewPopulators;
    private final List customFields;
    private final MultiMap configurationContexts;
    private final MultiMap fieldConfigSchemeIssueTypes;
    private String buildNumber;
    private String edition;
    private boolean unassignedIssuesAllowed;
    private int entityCount = 0;

    public BackupOverviewBuilderImpl()
    {
        projects = new ArrayList();
        pluginVersions = new ArrayList();
        versions = new MultiValueMap();
        components = new MultiValueMap();
        issueIds = new MultiValueMap();
        issueIdToKeyMap = new HashMap();
        customFields = new ArrayList();
        configurationContexts = new MultiValueMap();
        fieldConfigSchemeIssueTypes = new MultiValueMap();

        // Register all the populators that know how to handle
        overviewPopulators = new ArrayList();
        registerOverviewPopulators();
    }

    public void addProject(final ExternalProject project)
    {
        projects.add(project);
    }

    public void addPluginVersion(final PluginVersion pluginVersion)
    {
        pluginVersions.add(pluginVersion);
    }

    public void addVersion(final ExternalVersion version)
    {
        versions.put(version.getProjectId(), version);
    }

    public void addComponent(final ExternalComponent component)
    {
        components.put(component.getProjectId(), component);
    }

    public void addIssue(final ExternalIssue issue)
    {
        // We are storing, ONLY the issue id for each project
        issueIds.put(issue.getProject(), new Long(issue.getId()));
        // We are storing all issue/key information from the backup project. This is used by the issue links
        // to keep links to/from external projects when importing.
        issueIdToKeyMap.put(issue.getId(), issue.getKey());
    }

    public void setBuildNumber(final String buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public void setEdition(final String edition)
    {
        this.edition = edition;
    }

    public void setUnassignedIssuesAllowed(final boolean unassignedIssuesAllowed)
    {
        this.unassignedIssuesAllowed = unassignedIssuesAllowed;
    }

    public BackupOverview getBackupOverview()
    {
        final BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl(getBuildNumber(), getEdition(), getPluginVersions(),
            unassignedIssuesAllowed(), issueIdToKeyMap, entityCount);
        return new BackupOverviewImpl(backupSystemInformation, getProjects());
    }

    private boolean unassignedIssuesAllowed()
    {
        return unassignedIssuesAllowed;
    }

    public void populateInformationFromElement(final String elementName, final Map attributes) throws ParseException
    {
        // Keep a count of number of entities.
        entityCount++;
        for (final Object overviewPopulator : overviewPopulators)
        {
            final BackupOverviewPopulator populator = (BackupOverviewPopulator) overviewPopulator;
            populator.populate(this, elementName, attributes);
        }
    }

    public void addExternalCustomField(final ExternalCustomField externalCustomField)
    {
        customFields.add(externalCustomField);
    }

    public void addConfigurationContext(final ConfigurationContext configuration)
    {
        configurationContexts.put(configuration.getCustomFieldId(), configuration);
    }

    public void addFieldConfigSchemeIssueType(final FieldConfigSchemeIssueType fieldConfigSchemeIssueType)
    {
        fieldConfigSchemeIssueTypes.put(fieldConfigSchemeIssueType.getFieldConfigScheme(), fieldConfigSchemeIssueType);
    }

    protected void registerOverviewPopulators()
    {
        registerOverviewPopulator(new ProjectPopulator());
        registerOverviewPopulator(new ProjectComponentPopulator());
        registerOverviewPopulator(new ProjectVersionPopulator());
        registerOverviewPopulator(new CustomFieldPopulator());
        registerOverviewPopulator(new PluginVersionPopulator());
        registerOverviewPopulator(new IssueIdPopulator());
        registerOverviewPopulator(new SystemInfoPopulator());
    }

    protected void registerOverviewPopulator(final BackupOverviewPopulator populator)
    {
        overviewPopulators.add(populator);
    }

    List /*<BackupProject>*/getProjects()
    {
        final List fullProjects = new ArrayList();
        for (final Object project : projects)
        {
            final ExternalProject externalProject = (ExternalProject) project;
            List versions = (List) this.versions.get(externalProject.getId());
            if (versions == null)
            {
                versions = Collections.EMPTY_LIST;
            }
            List components = (List) this.components.get(externalProject.getId());
            if (components == null)
            {
                components = Collections.EMPTY_LIST;
            }
            List issueIds = (List) this.issueIds.get(externalProject.getId());
            if (issueIds == null)
            {
                issueIds = Collections.EMPTY_LIST;
            }

            final List customFieldConfigs = getCustomFieldConfigurations(externalProject.getId());

            fullProjects.add(new BackupProjectImpl(externalProject, versions, components, customFieldConfigs, issueIds));
        }

        return fullProjects;
    }

    /**
     * Gets the List of ExternalCustomFieldConfiguration objects for the given project.
     *
     * @param projectId The project.
     * @return List of ExternalCustomFieldConfiguration objects.
     */
    List getCustomFieldConfigurations(final String projectId)
    {
        final List externalCustomFieldConfigurationList = new ArrayList();
        // We need to inspect every custom field to see if there will be a valid configuration for the project and the
        // custom field.
        for (final Object customField : customFields)
        {
            final ExternalCustomField externalCustomField = (ExternalCustomField) customField;

            // Get the configurationContext that is relevent for the custom field and project that we are looking at.
            final ConfigurationContext configurationContext = getRelevantConfiguratonContext(projectId, externalCustomField.getId());

            // If the context is null then we do not have a custom field configuration for this project or one that is
            // globally applicable.
            if (configurationContext != null)
            {
                // Find the issue types that this custom field configuration is constrained by
                final List fieldConfigIssueTypes = (List) fieldConfigSchemeIssueTypes.get(configurationContext.getConfigSchemeId());
                // if there is no configuration, e.g. broken DB, then custom field will be  ignored
                if (fieldConfigIssueTypes != null)
                {
                    final List issuesTypes = getIssueTypesList(fieldConfigIssueTypes);
                    // We always want to grab the project id from the ConfigurationContext so we can tell if the
                    // config is global or specific to a project.
                    externalCustomFieldConfigurationList.add(new ExternalCustomFieldConfiguration(issuesTypes, configurationContext.getProjectId(),
                            externalCustomField, configurationContext.getConfigSchemeId()));
                }
                else
                {
                    log.warn(String.format("Skipped Custom Field %s (%s), because of missing config scheme, id: %s",
                            externalCustomField.getName(), externalCustomField.getId(), configurationContext.getConfigSchemeId()));
                }
            }
        }
        return externalCustomFieldConfigurationList;
    }

    /**
     * Gets the relevant Configuration Context for the given project and custom field. If a configuration explicitly
     * mentions the projectId then that will be the Configuration that is relevant. If there is no such config but
     * there is a "global" configuration (i.e. a configuration with a null project) then this configuration will be
     * used. There can also be no relevant configuration for the project and custom field combination in which case
     * we return null.
     *
     * @param projectId identifies the project
     * @param customFieldId identifies the custom field
     * @return relevant configuration
     */
    private ConfigurationContext getRelevantConfiguratonContext(final String projectId, final String customFieldId)
    {
        // Grab the ConfigurationContexts associated with this custom field
        final List configContexts = (List) configurationContexts.get(customFieldId);

        // Some custom fields can have no configuration, these will never be relevant to any projects
        if (configContexts == null)
        {
            return null;
        }

        ConfigurationContext globalContext = null;
        for (final Object configContext : configContexts)
        {
            final ConfigurationContext configurationContext = (ConfigurationContext) configContext;
            // Is this explicitly for our Project?
            if (configurationContext.getProjectId() == null)
            {
                // This is global
                globalContext = configurationContext;
            }
            else if (configurationContext.getProjectId().equals(projectId))
            {
                return configurationContext;
            }
        }

        return globalContext;
    }

    /**
     * Builds a list of IssueType Id's (as Strings) from a List of FieldConfigSchemeIssueTypes.
     *
     * @param fieldConfigIssueTypes objects containing the issue type strings
     * @return list of IssueType Id's, null if the custom field config is relevant for all issue types.
     * NOTE: this should never return an empty list, this indicates invalid data.
     */
    private List getIssueTypesList(final List fieldConfigIssueTypes)
    {
        final List ids = new ArrayList();
        for (final Object fieldConfigIssueType : fieldConfigIssueTypes)
        {
            final FieldConfigSchemeIssueType fieldConfigSchemeIssueType = (FieldConfigSchemeIssueType) fieldConfigIssueType;
            final String type = fieldConfigSchemeIssueType.getIssueType();
            // If there is an issue type of null then we are available for all issue types AND the data SHOULD only
            // contain this one FieldConfigSchemeIssueType
            if (type == null)
            {
                return null;
            }
            ids.add(type);
        }
        return ids;
    }

    List /*<PluginVersion>*/getPluginVersions()
    {
        return pluginVersions;
    }

    String getBuildNumber()
    {
        return buildNumber;
    }

    String getEdition()
    {
        return edition;
    }

    /**
     * Represents the CustomField configuration context as stored in the XML Backup. We only include the fields
     * that we need to build {@link com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration}'s
     */
    public static class ConfigurationContext
    {
        private final String projectId;
        private final String customFieldId;
        private final String configSchemeId;

        public ConfigurationContext(final String configSchemeId, final String customFieldId, final String projectId)
        {
            this.configSchemeId = configSchemeId;
            this.customFieldId = customFieldId;
            this.projectId = projectId;
        }

        public String getConfigSchemeId()
        {
            return configSchemeId;
        }

        public String getCustomFieldId()
        {
            return customFieldId;
        }

        public String getProjectId()
        {
            return projectId;
        }
    }

    /**
     * Represents the CustomField field configuration issue type context as stored in the XML Backup. We only include the fields
     * that we need to build {@link com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration}'s
     */
    public static class FieldConfigSchemeIssueType
    {
        private final String fieldConfigScheme;
        private final String issueType;

        public FieldConfigSchemeIssueType(final String fieldConfigScheme, final String issueType)
        {
            this.fieldConfigScheme = fieldConfigScheme;
            this.issueType = issueType;
        }

        public String getFieldConfigScheme()
        {
            return fieldConfigScheme;
        }

        public String getIssueType()
        {
            return issueType;
        }
    }
}
