package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.annotations.PublicApi;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Nonnull;

/**
 * Represents a configuration item in JIRA (an instance of a {@link com.atlassian.jira.issue.fields.CustomField}, a
 * {@link com.atlassian.jira.workflow.JiraWorkflow}, etc.) which is being managed by a plugin or JIRA itself.
 *
 * @since v5.2
 */
@PublicApi
public class ManagedConfigurationItem
{
    private final Long id;
    private final String itemId;
    private final ManagedConfigurationItemType itemType;
    private final boolean isManaged;
    private final ConfigurationItemAccessLevel configurationItemAccessLevel;
    private final String sourceId;
    private final String descriptionI18nKey;

    ManagedConfigurationItem(Long id, String itemId, ManagedConfigurationItemType itemType, boolean managed,
            @Nonnull ConfigurationItemAccessLevel configurationItemAccessLevel, String sourceId, String descriptionI18nKey)
    {
        this.id = id;
        this.itemId = itemId;
        this.itemType = itemType;
        isManaged = managed;
        this.configurationItemAccessLevel = configurationItemAccessLevel;
        this.sourceId = sourceId;
        this.descriptionI18nKey = descriptionI18nKey;
    }

    /**
     * @return The unique ID of this {@link ManagedConfigurationItem}
     */
    public Long getId()
    {
        return id;
    }

    /**
     * @return the ID of the item that is being managed. For different {@link ManagedConfigurationItemType} this can
     * be different things (e.g. workflow name, custom field ID, etc.)
     */
    public String getItemId()
    {
        return itemId;
    }

    /**
     * @return the type of item which is being managed
     */
    public ManagedConfigurationItemType getItemType()
    {
        return itemType;
    }

    /**
     * @return is this item currently being managed?
     */
    public boolean isManaged()
    {
        return isManaged;
    }

    /**
     * @return which users are allowed to edit this managed item. If {@link #isManaged()} is <tt>false</tt>, this will
     * return {@link ConfigurationItemAccessLevel#ADMIN}.
     */
    @Nonnull
    public ConfigurationItemAccessLevel getConfigurationItemAccessLevel()
    {
        return configurationItemAccessLevel;
    }

    /**
     * @return who owns/manages this item
     */
    public String getSourceId()
    {
        return sourceId;
    }

    /**
     * @return the I18n key which describes why this item is being managed
     */
    public String getDescriptionI18nKey()
    {
        return descriptionI18nKey;
    }

    /**
     * Use this method to get a {@link ManagedConfigurationItemBuilder} instance, prepopulated with this item's properties.
     * From there you can make modifications as necessary to alter the configuration.
     *
     * @return the builder instance
     */
    @Nonnull
    public ManagedConfigurationItemBuilder newBuilder()
    {
        return ManagedConfigurationItemBuilder.builder(this);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
