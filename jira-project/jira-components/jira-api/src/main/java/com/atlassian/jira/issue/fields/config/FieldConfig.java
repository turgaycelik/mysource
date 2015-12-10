package com.atlassian.jira.issue.fields.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Collections;
import java.util.List;

/**
 * Represents a particular configuration of a{@link ConfigurableField}. A configuration can have many facets, such as default value
 * and options. These are customizable through registering {@link FieldConfigItem} objects in the {@link #getConfigItems()} method.
 * <p />
 * This object is generally used in conjunction as part {@link FieldConfigScheme} objects where a config can be attached
 * per {@link IssueType}
 * This is used in <a href="http://www.atlassian.com/software/jira/docs/latest/customfields/configcustomfield.html">configuring custom fields.</a>.
 *
 * <h3>Sample code</h3>
 * Retrieving a FieldConfig and using it to retrieve a set of custom field options:
 * <pre>
 * {@link com.atlassian.jira.issue.context.IssueContextImpl} issueContext = new {@link com.atlassian.jira.issue.context.IssueContextImpl#IssueContextImpl(Long, String)}  IssueContextImpl(projectId, issueTypeId)};
    {@link FieldConfig} fieldConfig = {@link CustomField#getRelevantConfig(com.atlassian.jira.issue.context.IssueContext) cf.getRelevantConfig(issueContext)};
    {@link com.atlassian.jira.issue.customfields.option.Options} options = {@link com.atlassian.jira.issue.customfields.manager.OptionsManager#getOptions(FieldConfig) optionsManager.getOptions(fieldConfig)};
 * </pre>
 * @see FieldConfigScheme
 * @see FieldConfigItem
 * @see ConfigurableField
 */
@PublicApi
public interface FieldConfig
{
    Long getId();

    String getName();

    String getDescription();

    /**
     * Returns a list of FieldConfigItems.
     * @return A list of {@link FieldConfigItem}s. {@link Collections#EMPTY_LIST} if nothing
     */
    List <FieldConfigItem> getConfigItems();

    String getFieldId();

    /**
     * Returns the related custom field. This is very much a legacy method as {@link FieldConfig} was originally only
     * used for customfields. Should eventually return a {@link ConfigurableField}
     * @return Related {@link CustomField}
     * @throws DataAccessException if the fieldId does not refer to a valid custom field
     */
    CustomField getCustomField();
}
