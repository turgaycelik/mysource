package com.atlassian.jira.webtests.ztests.plugin.reloadable;

/**
 * Base class for all custom field module testers
 *
 * @since v4.3
 */
public abstract class AbstractCustomFieldPluginTest extends AbstractReloadablePluginsTest
{
    protected String customFieldId;
    protected String customFieldNumericId;

    protected void setUpCustomFieldInstance(final String customFieldTypeKey, final String fieldName, final String defaultValue, final String... fieldValues)
    {
        administration.plugins().referencePlugin().enable();
        customFieldId = administration.customFields()
                .addCustomField(customFieldTypeKey, fieldName);
        customFieldNumericId = customFieldId.replace("customfield_", "");
        if (fieldValues != null && fieldValues.length > 0)
        {
            administration.customFields().addOptions(customFieldNumericId, fieldValues);
        }
        if (defaultValue != null)
        {
            administration.customFields().setDefaultValue(customFieldNumericId, defaultValue);
        }
    }

}
