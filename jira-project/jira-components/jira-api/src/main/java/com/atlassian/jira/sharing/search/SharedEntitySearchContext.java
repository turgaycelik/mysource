package com.atlassian.jira.sharing.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.security.Permissions;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

/**
 * Indicates the context of a shared entity search, shared entities can be searched in &quot;USER&quot; or
 * &quot;ADMINISTRATION&quot; context.
 *
 * <p>When searching within a USER context, the search is being performed with the objective of using the results in
 * jira as a user, e.g. favourite the result, use it in a dashboard, see its content in the issue navigator...</p>
 *
 * <p>Conversely, a search in &quot;ADMINISTRATION&quot; context is being done with the objective of performing
 * administration tasks on the returned search entities, e.g. Delete the entity, change its owner, change its sharing
 * permissions.</p>
 *
 * <p><em>Only JIRA Administrators are able to perform &quot;ADMINISTRATION&quot; context queries.</em><p/>
 *
 * @since v4.4
 */
@PublicApi
public enum SharedEntitySearchContext
{
    USE()
            {
                @Override
                List<Integer> requiredPermissions()
                {
                    return Collections.emptyList();
                }
            },

    ADMINISTER()
            {
                @Override
                List<Integer> requiredPermissions()
                {
                    return ImmutableList.of(Permissions.ADMINISTER);
                }
            };

    /**
     * Gets the permissions required to perform a search in this context.
     * @return A List of the permissions required to perform a search in this context.
     */
    abstract List<Integer> requiredPermissions();
}
