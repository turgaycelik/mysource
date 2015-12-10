package com.atlassian.jira.rest.v2.issue.groups;

import com.atlassian.jira.rest.v2.issue.GroupSuggestionsBean;

import java.util.List;

/**
 * @since v5.0
 */
public interface GroupPickerResourceHelper
{

    /**
     * Gets group search results as a bean
     *
     * @param query -  a String to match groups agains
     * @param excludeGroups - groups to exclude from search
     * @return bean that represents the search results with meta data
     */
    GroupSuggestionsBean findGroupsAsBean(String query, List<String> excludeGroups, Integer maxResults);
}
