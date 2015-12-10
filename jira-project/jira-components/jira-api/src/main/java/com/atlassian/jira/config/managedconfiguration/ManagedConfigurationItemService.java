package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * This provides an API for plugin developers and for JIRA internally to restrict the administration of certain configuration
 * items. Often, a plugin developer will want to configure JIRA programmatically and rely on that configuration being
 * unchanged. Using this API, the developer can register their desired items as "managed" or "locked" configuration
 * items. This will prevent administrators from changing the configuration and invalidating the plugin's needs.
 * <p/>
 * There are a few different states a {@link ManagedConfigurationItem} can be in. These are determined by the properties
 * {@link ManagedConfigurationItem#isManaged()} and {@link ManagedConfigurationItem#getConfigurationItemAccessLevel()}:
 * <ul>
 *     <li><code>isManaged() == true</code> - this item is being managed by JIRA or a plugin</li>
 *     <li><code>getConfigurationItemAccessLevel()</code> - if an item is managed, this property determines who can edit the
 *     item. See {@link ConfigurationItemAccessLevel} for more information.</li>
 * </ul>
 * <p/>
 * For a list of which JIRA configuration items can be managed, see {@link ManagedConfigurationItemType}.
 * <p/>
 * Each configuration item can only have one {@link ManagedConfigurationItem} describing it.
 *
 * @see ManagedConfigurationItem
 * @see ManagedConfigurationItemBuilder
 * @see ManagedConfigurationItemType
 * @see ConfigurationItemAccessLevel
 * @since v5.2
 */
@PublicApi
public interface ManagedConfigurationItemService
{
    /**
     * Denotes the prefix used in the {@link ManagedConfigurationItem#getSourceId()}
     * field when items are managed by a plugin.
     */
    public String SOURCE_PREFIX_PLUGIN = "plugin:";

    /**
     * Given the {@link CustomField}, attempt to retrieve the {@link ManagedConfigurationItem} that describes it. If it
     * does not exist, a default implementation will be returned.
     * <p/>
     * If the manager of this item is no longer available (e.g. if a plugin manages a JIRA custom field and the plugin
     * is disabled) then the item can not be considered managed.
     *
     * @param customField the custom field
     * @return the item's representation
     */
    @Nonnull
    public ManagedConfigurationItem getManagedCustomField(@Nonnull CustomField customField);

    /**
     * Given the {@link JiraWorkflow}, attempt to retrieve the {@link ManagedConfigurationItem} that describes it. If it
     * does not exist, a default implementation will be returned.
     * <p/>
     * If the manager of this item is no longer available (e.g. if a plugin manages a workflow and the plugin
     * is disabled) then the item can not be considered managed.
     *
     * @param workflow the workflow
     * @return the item's representation
     */
    @Nonnull
    public ManagedConfigurationItem getManagedWorkflow(@Nonnull JiraWorkflow workflow);

    /**
     * Given the {@link WorkflowScheme}, attempt to retrieve the {@link ManagedConfigurationItem} that describes it. If it
     * does not exist, a default implementation will be returned.
     * <p/>
     * If the manager of this item is no longer available (e.g. if a plugin manages a workflow scheme and the plugin
     * is disabled) then the item can not be considered managed.
     *
     * @see #getManagedWorkflowScheme(Long)
     * @param workflowScheme the workflow scheme
     * @return the item's representation
     */
    @Nonnull
    public ManagedConfigurationItem getManagedWorkflowScheme(@Nonnull WorkflowScheme workflowScheme);

    /**
     * Given the workflow scheme ID, attempt to retrieve the {@link ManagedConfigurationItem} that describes it. If it
     * does not exist, a default implementation will be returned.
     * <p/>
     * If the manager of this item is no longer available (e.g. if a plugin manages a workflow scheme and the plugin
     * is disabled) then the item can not be considered managed.
     *
     * @see #getManagedWorkflowScheme(WorkflowScheme)
     * @param schemeId the workflow scheme ID
     * @return the item's representation
     */
    @Nonnull
    public ManagedConfigurationItem getManagedWorkflowScheme(@Nonnull Long schemeId);

    /**
     * Update the registration of this {@link ManagedConfigurationItem}. If the item was not previously managed, it
     * will now be managed.
     *
     * @param item the item to manage
     * @return the result; errors if saving the item failed
     */
    @Nonnull
    public ServiceOutcome<ManagedConfigurationItem> updateManagedConfigurationItem(@Nonnull ManagedConfigurationItem item);

    /**
     * Remove the registration of this {@link ManagedConfigurationItem}. If the item was not previously managed, an
     * error will be returned.
     *
     * @param item the item to stop managing
     * @return the result; errors if saving the item failed
     */
    @Nonnull
    public ServiceOutcome<Void> removeManagedConfigurationItem(@Nonnull ManagedConfigurationItem item);

    /**
     * Retrieves all of the {@link ManagedConfigurationItem}s of the specified type which are currently "available"
     * (meaning their owner is currently available).
     *
     * @param type the type to retrieve
     * @return the items
     */
    @Nonnull
    public Collection<ManagedConfigurationItem> getManagedConfigurationItems(@Nonnull ManagedConfigurationItemType type);

    /**
     * Determine if the specified {@link User} would have permission to edit the {@link ManagedConfigurationItem}.
     *
     * @param user the user
     * @param item the item
     * @return the result
     */
    public boolean doesUserHavePermission(User user, @Nonnull ManagedConfigurationItem item);

    /**
     * Determine if the specified {@link User} would have permission to edit an {@link ManagedConfigurationItem} with the
     * specified level.
     *
     * @param user the user
     * @param configurationItemAccessLevel the level
     * @return the result
     */
    public boolean doesUserHavePermission(User user, @Nonnull ConfigurationItemAccessLevel configurationItemAccessLevel);
}
