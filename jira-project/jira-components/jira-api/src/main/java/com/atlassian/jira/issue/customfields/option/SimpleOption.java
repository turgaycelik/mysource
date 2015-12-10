package com.atlassian.jira.issue.customfields.option;

import com.atlassian.annotations.ExperimentalApi;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the basic properties of a custom field option in JIRA. The {@link Option} is more heavy weight interface
 * that can be used when the options actually exist in the database.
 *
 * @since v6.2
 */
@ExperimentalApi
public interface SimpleOption<T extends SimpleOption<T>>
{
    /**
     * The ID of the option or null if it currently does not have one.
     *
     * @return the ID of the option or null if it does not have one.
     */
    @Nullable
    Long getOptionId();

    /**
     * The current value of the option. This is the option displayed to the user.
     *
     * @return the value of the option.
     */
    String getValue();

    /**
     * Return a list of options that exist under this option (i.e. its children).
     *
     * @return the list of options that exist under this option. An empty list is returned when such options exist.
     */
    @Nonnull
    List<T> getChildOptions();
}
