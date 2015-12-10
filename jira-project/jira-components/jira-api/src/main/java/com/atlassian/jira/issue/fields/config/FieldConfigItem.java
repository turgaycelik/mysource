package com.atlassian.jira.issue.fields.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

/**
 * Models a row in a <a href="http://www.atlassian.com/software/jira/docs/latest/issuefield_configuration.html">field configuration</a>.
 *
 */
@PublicApi
public interface FieldConfigItem
{
    /**
     * Note: use {@link #getDisplayNameKey()} where possible.
     * @return the display name for this config item in hard-coded English
     */
    String getDisplayName();

    /**
     * @return the display name for this config item as an i18n key
     */
    String getDisplayNameKey();
    
    String getViewHtml(FieldLayoutItem fieldLayoutItem);
    FieldConfig getFieldConfig();
    FieldConfigItemType getType();

    String getObjectKey();
    Object getConfigurationObject(Issue issue);
    String getBaseEditUrl();


}
