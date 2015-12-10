package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.dbc.Assertions;

public class DefaultSearchRequestAdminManager implements SearchRequestAdminManager
{
    private final ShareManager shareManager;
    private final SearchRequestStore searchRequestStore;
    private final SearchRequestManager searchRequestManager;

    public DefaultSearchRequestAdminManager(final SearchRequestStore searchRequestStore, final SearchRequestManager searchRequestManager, final ShareManager shareManager)
    {
        this.searchRequestStore = searchRequestStore;
        this.searchRequestManager = searchRequestManager;
        this.shareManager = shareManager;
    }

    /**
     * Called from the admin section. Should not perform a Search (which would seem to make sense otherwise) as we may not be indexed yet.
     * Does not need permissions.
     */
    public EnclosedIterable /* <SearchRequest> */getSearchRequests(final Project project)
    {
        Assertions.notNull("project", project);
        return searchRequestStore.getSearchRequests(project);
    }

    /**
     * Called from the admin section. Should not perform a Search (which would seem to make sense otherwise) as we may not be indexed yet.
     * Does not need permissions.
     */
    public EnclosedIterable /* <SearchRequest> */getSearchRequests(final Group group)
    {
        Assertions.notNull("group", group);
        return searchRequestStore.getSearchRequests(group);
    }

    /**
     * Called from upgrade task 321. Needs permissions.
     */
    public SearchRequest getSearchRequestById(final Long id)
    {
        Assertions.notNull("id", id);
        return setSharePermissions(searchRequestStore.getSearchRequest(id));
    }

    public SearchRequest update(final SearchRequest request)
    {
        return searchRequestManager.update(request);
    }

    public void delete(final Long searchRequestId)
    {
        searchRequestManager.delete(searchRequestId);
    }

    private SearchRequest setSharePermissions(final SearchRequest filter)
    {
        if (filter != null)
        {
            filter.setPermissions(shareManager.getSharePermissions(filter));
        }
        return filter;
    }
}
