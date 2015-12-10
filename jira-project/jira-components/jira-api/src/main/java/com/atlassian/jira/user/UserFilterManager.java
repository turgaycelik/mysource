package com.atlassian.jira.user;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.config.FieldConfig;

/**
 * provide read/write functionality for user filtering settings.
 *
 * @since v6.2
 */
@PublicApi
public interface UserFilterManager
{
    /**
     * Retrieve the user filter options of a custom field configuration.
     * <p/>
     * Create an empty selection with {@link UserFilter#DISABLED} if not found.
     *
     * @param fieldConfig the custom field configuration of the user filter
     * @return the user filter associated with the given custom field configuration
     */
    UserFilter getFilter(FieldConfig fieldConfig);

    /**
     * Update the user filter options of a custom field configuration.
     * @param fieldConfig the custom field configuration of which the user filter is to be updated
     * @param filter contains the new user filter values to be updated
     */
    void updateFilter(FieldConfig fieldConfig, UserFilter filter);

    /**
     * Remove all the user filter options of a custom field.
     * @param customFieldId the id of the custom field
     */
    void removeFilter(Long customFieldId);
}
