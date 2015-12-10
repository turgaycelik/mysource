package com.atlassian.jira.issue.fields.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

/**
 * Represents a single dimension of configuration for a custom field. If
 * a custom field needs to be configured in many ways, each way would have
 * an implementation of this type. Methods defined include those necessary to
 * render the current configured state and a name for the type of configuration
 * that the implementation embodies. Also included are a URL link for directing
 * the browser to the configuration screen and a value for the current
 * configuration as well as a key unique to this configuration type.
 */
@PublicApi
public interface FieldConfigItemType
{
    /**
     * The user interface name for this dimension of configuration.
     * @return the name.
     */
    String getDisplayName();

    /**
     * The i18n key for the user interface name for this dimension of configuration.
     * @return the i18n key.
     */
    String getDisplayNameKey();

    /**
     * Renders a view of the current configuration as html.
     * @param fieldConfig
     * @param fieldLayoutItem
     * @return the view html.
     */
    String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem);

    /**
     * Returns a key unique among FieldConfigItemType implementations for the
     * configuration value so it can be retrieved from a key-value pair store.
     * @return the configuration key.
     */
    String getObjectKey();

    /**
     * Returns an Object that holds the the configuration.
     * @param issue
     * @param config
     * @return the configuration value.
     */
    Object getConfigurationObject(Issue issue, FieldConfig config);

    /**
     * Creates the base of the URL that links to the configuration screen for
     * this type of configuration.
     * @return the URL.
     */
    String getBaseEditUrl();
}
