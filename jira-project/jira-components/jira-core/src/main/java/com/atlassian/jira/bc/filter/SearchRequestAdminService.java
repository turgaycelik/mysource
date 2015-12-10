package com.atlassian.jira.bc.filter;

import com.atlassian.crowd.embedded.api.Group;

import java.util.Collection;

public interface SearchRequestAdminService
{
    /**
     * Returns a collection of SimpleSearchRequestDisplay objects that are resticted
     * to owner name, request name and request id.
     *
     * {@link com.atlassian.jira.web.action.util.SimpleSearchRequestDisplay}
     * 
     * @param group the group in play
     * @return a collection of SimpleSearchRequestDisplay objects
     */
    Collection /*<SimpleSearchRequestDisplay>*/ getFiltersSharedWithGroup(final Group group);
}
