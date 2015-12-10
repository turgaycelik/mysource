package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.Collection;
import java.util.List;

/**
 * Manages option values for fields (eg. custom field select lists).
 */
@PublicApi
public interface OptionsManager
{
    /**
     * Get options for this particular custom field type.  As a custom field may have multiple types of options
     * (eg cascading drop downs), the type can be used to signify this
     * @param fieldConfig For example, retrieved from {@link CustomField#getRelevantConfig(com.atlassian.jira.issue.Issue)}
     * @return  An option Object of Options
     */
    Options getOptions(FieldConfig fieldConfig);

    void setRootOptions(FieldConfig fieldConfig, Options options);

    void removeCustomFieldOptions(CustomField customField);

    void removeCustomFieldConfigOptions(FieldConfig fieldConfig);

    /**
     * Update a set of options. After doing this, any existing {@link Options} objects may be stale, and should be
     * re-fetched with {@link #getOptions(com.atlassian.jira.issue.fields.config.FieldConfig)}.
     *
     * @param options Usually an {@link Options} implementation.
     */
    void updateOptions(Collection<Option> options);

    Option createOption(FieldConfig fieldConfig, Long parentOptionId, Long sequence, String value);

    void deleteOptionAndChildren(Option option);

    Option findByOptionId(Long optionId);

    /**
     * Retreives all {@link Option} in the system.
     *
     * @return a list of all options in the system
     */
    List<Option> getAllOptions();

    /**
     * Set an option to enabled.
     * @param option The Option to enable.
     */
    void enableOption(Option option);

    /**
     * Set an option to disabled.
     * A disabled option will is not available to be assigned to this associated custom field, It remains
     * valid historically and for searching with.
     * @param option The option to be disabled.
     */
    void disableOption(Option option);

    void setValue(Option option, String value);

    /**
     * Finds all options with the given value. Returns and empty list if no options are found.
     * 
     * @param value the value of the options to find (case insensitive). Must not be null.
     * @return the list of found options, empty if non found.
     */
    List<Option> findByOptionValue(String value);

    List<Option> findByParentId(Long parentOptionId);
}
