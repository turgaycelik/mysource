package com.atlassian.jira.issue.fields.option;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;

/**
 * A set of options representing the root set of any options
 */
@PublicApi
public interface OptionSet
{
    /**
     * List of options for this set
     * @return List of {@link Option} objects
     */
    Collection<Option> getOptions();

    /**
     * Get the list of options ids
     * @return List of {@link String} objects
     */
    Collection<String> getOptionIds();

    /**
     * Adds the option to the underlying list
     * @param constantType
     * @param constantId
     */
    void addOption(String constantType, String constantId);
}
