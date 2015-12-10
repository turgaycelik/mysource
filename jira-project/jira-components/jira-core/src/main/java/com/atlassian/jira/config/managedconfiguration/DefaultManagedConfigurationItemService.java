package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.plugin.PluginAccessor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.atlassian.jira.bc.ServiceOutcomeImpl.error;
import static com.atlassian.jira.bc.ServiceOutcomeImpl.ok;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The default implementation of {@link ManagedConfigurationItemService}.
 *
 * @since v5.2
 */
public class DefaultManagedConfigurationItemService implements ManagedConfigurationItemService
{
    private static final Logger log = Logger.getLogger(DefaultManagedConfigurationItemService.class);

    private final ManagedConfigurationItemStore managedConfigurationItemStore;
    private final PermissionManager permissionManager;
    private final PluginAccessor pluginAccessor;

    public DefaultManagedConfigurationItemService(ManagedConfigurationItemStore managedConfigurationItemStore, PermissionManager permissionManager, PluginAccessor pluginAccessor)
    {
        this.managedConfigurationItemStore = managedConfigurationItemStore;
        this.permissionManager = permissionManager;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    @Nonnull
    public ServiceOutcome<ManagedConfigurationItem> updateManagedConfigurationItem(@Nonnull ManagedConfigurationItem item)
    {
        // set description if one has not already been set
        if (item.isManaged() && StringUtils.isBlank(item.getDescriptionI18nKey()))
        {
            String key = item.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED
                    ? "admin.managed.configuration.items.generic.description.locked" : "admin.managed.configuration.items.generic.description.managed";
            item = item.newBuilder()
                    .setDescriptionI18nKey(key)
                    .build();
        }

        return ok(managedConfigurationItemStore.updateManagedConfigurationItem(item));
    }

    @Override
    @Nonnull
    public ServiceOutcome<Void> removeManagedConfigurationItem(@Nonnull ManagedConfigurationItem item)
    {
        boolean success = managedConfigurationItemStore.deleteManagedConfigurationItem(item);
        if (success)
        {
            return ok(null);
        }
        return error("admin.managed.configuration.items.error.delete");
    }

    @Override
    @Nonnull
    public Collection<ManagedConfigurationItem> getManagedConfigurationItems(@Nonnull ManagedConfigurationItemType type)
    {
        Collection<ManagedConfigurationItem> managedConfigurationItems = managedConfigurationItemStore.getManagedConfigurationItems(type);
        return CollectionUtil.transform(managedConfigurationItems, new Function<ManagedConfigurationItem, ManagedConfigurationItem>()
        {
            @Override
            public ManagedConfigurationItem get(ManagedConfigurationItem input)
            {
                return ensureManagedConfigurationEntitySourceAvailable(input);
            }
        });
    }

    @Override
    @Nonnull
    public ManagedConfigurationItem getManagedCustomField(@Nonnull CustomField customField)
    {
        return getManagedConfigurationEntity(customField.getId(), ManagedConfigurationItemType.CUSTOM_FIELD);
    }

    @Override
    @Nonnull
    public ManagedConfigurationItem getManagedWorkflow(@Nonnull JiraWorkflow workflow)
    {
        return getManagedConfigurationEntity(workflow.getName(), ManagedConfigurationItemType.WORKFLOW);
    }

    @Nonnull
    @Override
    public ManagedConfigurationItem getManagedWorkflowScheme(@Nonnull WorkflowScheme workflowScheme)
    {
        return getManagedWorkflowScheme(workflowScheme.getId());
    }

    @Override
    @Nonnull
    public ManagedConfigurationItem getManagedWorkflowScheme(@Nonnull Long schemeId)
    {
        return getManagedConfigurationEntity(schemeId.toString(), ManagedConfigurationItemType.WORKFLOW_SCHEME);
    }

    @Override
    public boolean doesUserHavePermission(User user, @Nonnull ManagedConfigurationItem item)
    {
        notNull("item", item);

        // anonymous user should never have permission
        if (user == null)
        {
            return false;
        }

        if (!item.isManaged())
        {
            return true;
        }

        return doesUserHavePermission(user, item.getConfigurationItemAccessLevel());
    }

    @Override
    public boolean doesUserHavePermission(User user, @Nonnull ConfigurationItemAccessLevel configurationItemAccessLevel)
    {
        switch (configurationItemAccessLevel)
        {
            case LOCKED:
                return false;
            case SYS_ADMIN:
                return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
            case ADMIN:
                return permissionManager.hasPermission(Permissions.ADMINISTER, user) || permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
            default:
                return false;
        }
    }

    private ManagedConfigurationItem getManagedConfigurationEntity(@Nonnull String itemId, @Nonnull ManagedConfigurationItemType itemType)
    {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemStore.getManagedConfigurationItem(itemId, itemType);
        if (managedConfigurationItem != null)
        {
            return ensureManagedConfigurationEntitySourceAvailable(managedConfigurationItem);
        }

        // item was not in the database -- return a stub with default properties
        return new ManagedConfigurationItemBuilder()
                .setItemId(itemId)
                .setItemType(itemType)
                .setManaged(false)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.ADMIN)
                .build();
    }

    private ManagedConfigurationItem ensureManagedConfigurationEntitySourceAvailable(ManagedConfigurationItem managedConfigurationItem)
    {
        // if we don't have a source then don't do any checks
        String sourceId = managedConfigurationItem.getSourceId();
        if (StringUtils.isBlank(sourceId))
        {
            return managedConfigurationItem;
        }

        // ensure that the source is still valid
        if (sourceId.startsWith(SOURCE_PREFIX_PLUGIN))
        {
            sourceId = sourceId.substring(SOURCE_PREFIX_PLUGIN.length());
            if(!isPluginEnabled(sourceId))
            {
                managedConfigurationItem = managedConfigurationItem.newBuilder()
                        .setManaged(false)
                        .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.ADMIN)
                        .build();
            }
        }

        return managedConfigurationItem;
    }

    private boolean isPluginEnabled(String pluginKey)
    {
        return pluginAccessor.isPluginEnabled(pluginKey);
    }
}
