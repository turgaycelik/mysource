package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.util.InjectableComponent;

import java.util.List;

/**
 * Link Fields to their (JQL) NameResolver
 * @since v4.2
 */
@InjectableComponent
public interface ResolverManager
{
    /**
     * @param field the name of the field (from IssueFieldConstants)
     * @return true if the Resolver Manager knows about the field in question
     */
    boolean handles(final String field);

    /**
     * Given a name and a field-name return the list of all possible values for it
     * @param name the "name" value (e.g. 'New Component 5')
     * @param field which field the value belongs to; must match IssueFieldConstants (e.g. 'component')
     * @return a list of Strings consisting of the IDs corresponding to the name.
     */
    List<String> getIdsFromName(final String name, final String field);

    /**
     * As above, but ensures that there is only a single value being returned. A warning will be issued --
     * but no exceptions thrown -- if there is more than one ID for the value.
     * @param name the "name" value (e.g. 'New Component 5')
     * @param field which field the value belongs to; must match IssueFieldConstants (e.g. 'component')
     * @return the ID for the value provided
     */
    String getSingleIdFromName(final String name, final String field);
}
