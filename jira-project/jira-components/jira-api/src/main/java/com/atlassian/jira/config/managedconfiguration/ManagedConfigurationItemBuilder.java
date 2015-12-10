package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.annotations.PublicApi;
import com.atlassian.plugin.Plugin;

/**
 * This builder can be used to modify the properties of an existing {@link ManagedConfigurationItem}.
 *
 * @see ManagedConfigurationItem#newBuilder()
 * @since v5.2
 */
@PublicApi
public class ManagedConfigurationItemBuilder
{
    private Long id;
    private String itemId;
    private ManagedConfigurationItemType itemType;
    private boolean isManaged;
    private ConfigurationItemAccessLevel configurationItemAccessLevel;
    private String source;
    private String descriptionI18nKey;

    public static ManagedConfigurationItemBuilder builder(ManagedConfigurationItem source)
    {
        return new ManagedConfigurationItemBuilder()
                .setId(source.getId())
                .setItemId(source.getItemId())
                .setItemType(source.getItemType())
                .setManaged(source.isManaged())
                .setConfigurationItemAccessLevel(source.getConfigurationItemAccessLevel())
                .setSource(source.getSourceId())
                .setDescriptionI18nKey(source.getDescriptionI18nKey());
    }

    public ManagedConfigurationItemBuilder setId(Long id)
    {
        this.id = id;
        return this;
    }

    public ManagedConfigurationItemBuilder setItemId(String itemId)
    {
        this.itemId = itemId;
        return this;
    }

    public ManagedConfigurationItemBuilder setItemType(ManagedConfigurationItemType itemType)
    {
        this.itemType = itemType;
        return this;
    }

    /**
     * Set whether this item is managed or not
     * @param managed the flag
     * @return the builder
     */
    public ManagedConfigurationItemBuilder setManaged(boolean managed)
    {
        isManaged = managed;
        return this;
    }

    /**
     * Set the {@link ConfigurationItemAccessLevel} for this item
     * @param configurationItemAccessLevel the level
     * @return the builder
     */
    public ManagedConfigurationItemBuilder setConfigurationItemAccessLevel(ConfigurationItemAccessLevel configurationItemAccessLevel)
    {
        this.configurationItemAccessLevel = configurationItemAccessLevel;
        return this;
    }

    /**
     * Set the owner/manager of the item
     * @param plugin the plugin who owns/manages this item
     * @return the builder
     */
    public ManagedConfigurationItemBuilder setSource(Plugin plugin)
    {
        return setSource(ManagedConfigurationItemService.SOURCE_PREFIX_PLUGIN + plugin.getKey());
    }

    public ManagedConfigurationItemBuilder setSource(String source)
    {
        this.source = source;
        return this;
    }

    /**
     * Set the description key
     * @param descriptionI18nKey the key
     * @return the builder
     */
    public ManagedConfigurationItemBuilder setDescriptionI18nKey(String descriptionI18nKey)
    {
        this.descriptionI18nKey = descriptionI18nKey;
        return this;
    }

    /**
     * @return the newly constructed instance of {@link ManagedConfigurationItem}.
     */
    public ManagedConfigurationItem build()
    {
        ConfigurationItemAccessLevel accessLevel = configurationItemAccessLevel == null ? ConfigurationItemAccessLevel.ADMIN : configurationItemAccessLevel;
        return new ManagedConfigurationItem(id, itemId, itemType, isManaged, accessLevel, source, descriptionI18nKey);
    }
}
