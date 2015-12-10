package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.annotations.PublicApi;

/**
 * The types of items which can be "managed". This may be expanded in future.
 *
 * @since v5.2
 */
@PublicApi
public enum ManagedConfigurationItemType
{
    /**
     * Instances of custom fields.
     */
    CUSTOM_FIELD,

    /**
     * Instances of workflows.
     */
    WORKFLOW,

    /**
     * Instances of workflow schemes.
     */
    WORKFLOW_SCHEME
}
