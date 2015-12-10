package com.atlassian.jira.bc.group.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.Group;

import java.util.List;

/**
 * Service that retrieves a collection of {@link Group} objects based on a partial query string
 *
 * @since v4.4
 */
@PublicApi
public interface GroupPickerSearchService
{
    /**
     * Get groups based on a query string. Will be unique and sorted.

     * Results are sorted according to the {@link com.atlassian.crowd.embedded.api.GroupComparator}.
     *
     * @param query              String to search for.
     * @return List of {@link Group} objects that match criteria.
     */
    public List<Group> findGroups(String query);

    /**
     * Returns a group by name.
     *
     * @param groupName The group name
     * @return The group (if any)
     */
    public Group getGroupByName(String groupName);
}
