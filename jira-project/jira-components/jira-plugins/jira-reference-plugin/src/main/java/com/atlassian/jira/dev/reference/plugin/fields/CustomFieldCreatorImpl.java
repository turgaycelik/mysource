package com.atlassian.jira.dev.reference.plugin.fields;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gzipfilter.org.apache.commons.lang.builder.ToStringBuilder;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * Will create instances of certain custom fields onStart and register as managed configuration items.
 *
 * @see com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService
 * @since v5.2
 */
public class CustomFieldCreatorImpl implements CustomFieldCreator
{
    private static final Logger log = Logger.getLogger(CustomFieldCreatorImpl.class);

    private static final String PLUGIN_KEY = "com.atlassian.jira.dev.reference-plugin";
    private static final CustomFieldMetaData SELECT_MANAGED = new CustomFieldMetaData(
            "Reference Select Managed",
            "This was programmatically created by the Reference Plugin",
            PLUGIN_KEY + ":reference-select-managed-admin",
            "com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher",
            ConfigurationItemAccessLevel.ADMIN
    );

    private static final CustomFieldMetaData SELECT_MANAGED_SYSADMIN = new CustomFieldMetaData(
            "Reference Select Managed Sys Admin",
            "This was programmatically created by the Reference Plugin",
            PLUGIN_KEY + ":reference-select-managed-sysadmin",
            "com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher",
            ConfigurationItemAccessLevel.SYS_ADMIN
    );

    private static final CustomFieldMetaData SELECT_LOCKED = new CustomFieldMetaData(
            "Reference Select Locked",
            "This was programmatically created by the Reference Plugin",
            PLUGIN_KEY + ":reference-select-locked",
            "com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher",
            ConfigurationItemAccessLevel.LOCKED
    );

    private static final CustomFieldMetaData JIRA_SELECT_LOCKED = new CustomFieldMetaData(
            "JIRA Select Locked",
            "This was programmatically created by the Reference Plugin",
            "com.atlassian.jira.plugin.system.customfieldtypes:select",
            "com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher",
            ConfigurationItemAccessLevel.LOCKED
    );

    private final ManagedConfigurationItemService managedConfigurationItemService;
    private final CustomFieldManager customFieldManager;
    private final ConstantsManager constantsManager;
    private final EventPublisher eventPublisher;
    private final PluginAccessor pluginAccessor;
    private JiraContextTreeManager jiraContextTreeManager;

    public CustomFieldCreatorImpl(ManagedConfigurationItemService managedConfigurationItemService, CustomFieldManager customFieldManager, ConstantsManager constantsManager, EventPublisher eventPublisher, PluginAccessor pluginAccessor)
    {
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.customFieldManager = customFieldManager;
        this.constantsManager = constantsManager;
        this.eventPublisher = eventPublisher;
        this.pluginAccessor = pluginAccessor;
    }

    @PostConstruct
    public void onSpringContextStarted()
    {
        this.jiraContextTreeManager = ComponentManager.getComponentInstanceOfType(JiraContextTreeManager.class);

        this.eventPublisher.register(this);
    }

    @PreDestroy
    public void onSpringContextStopped()
    {
        this.eventPublisher.unregister(this);
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event)
    {
        if (event.getPlugin().getKey().equals(PLUGIN_KEY))
        {
//            registerManagedFields();
        }
    }

    public void registerManagedFields()
    {
        registerManagedField(SELECT_MANAGED);
        registerManagedField(SELECT_MANAGED_SYSADMIN);
        registerManagedField(SELECT_LOCKED);
        registerManagedField(JIRA_SELECT_LOCKED);
    }

    private void registerManagedField(CustomFieldMetaData customFieldMetaData)
    {
        CustomField customField = getOrCreateCustomField(customFieldMetaData);
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedCustomField(customField);
        item = item.newBuilder()
                .setManaged(true)
                .setConfigurationItemAccessLevel(customFieldMetaData.getConfigurationItemAccessLevel())
                .setSource(getPlugin())
                .setDescriptionI18nKey(customFieldMetaData.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED ? "module.customfieldtype.select.locked.desc" : "module.customfieldtype.select.managed.desc")
                .build();

        ServiceOutcome<ManagedConfigurationItem> outcome = managedConfigurationItemService.updateManagedConfigurationItem(item);
        if (!outcome.isValid())
        {
            log.warn("Could not successfully register managed custom field from Reference Plugin");
        }
    }

    private Plugin getPlugin()
    {
        return pluginAccessor.getPlugin(PLUGIN_KEY);
    }

    private CustomField getOrCreateCustomField(CustomFieldMetaData customFieldMetaData)
    {
        CustomField customField = getCustomField(customFieldMetaData);
        if (customField == null)
        {
            customField = createCustomField(customFieldMetaData);
        }
        return customField;
    }

    private CustomField getCustomField(CustomFieldMetaData customFieldMetaData)
    {
        return customFieldManager.getCustomFieldObjectByName(customFieldMetaData.getName());
    }

    private CustomField createCustomField(CustomFieldMetaData customFieldMetaData)
    {
        CustomFieldType type = customFieldManager.getCustomFieldType(customFieldMetaData.getCustomFieldTypeKey());
        CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(customFieldMetaData.getCustomFieldSearcherKey());

        // global project and issue type context
        List<GenericValue> issueTypes = CustomFieldUtils.buildIssueTypes(constantsManager, new String[] {"-1"});
        List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, null, null, jiraContextTreeManager);

        try
        {
            return customFieldManager.createCustomField(customFieldMetaData.getName(), customFieldMetaData.getDescription(), type, searcher, contexts, issueTypes);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException("Exception while trying to create a customField: " + customFieldMetaData.toString(), e);
        }
    }

    private static class CustomFieldMetaData
    {
        private String name;
        private String description;
        private String customFieldTypeKey;
        private String customFieldSearcherKey;
        private ConfigurationItemAccessLevel configurationItemAccessLevel;

        private CustomFieldMetaData(String name, String description, String customFieldTypeKey, String customFieldSearcherKey, ConfigurationItemAccessLevel configurationItemAccessLevel)
        {
            this.name = name;
            this.description = description;
            this.customFieldTypeKey = customFieldTypeKey;
            this.customFieldSearcherKey = customFieldSearcherKey;
            this.configurationItemAccessLevel = configurationItemAccessLevel;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getCustomFieldTypeKey()
        {
            return customFieldTypeKey;
        }

        public String getCustomFieldSearcherKey()
        {
            return customFieldSearcherKey;
        }

        public ConfigurationItemAccessLevel getConfigurationItemAccessLevel()
        {
            return configurationItemAccessLevel;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
