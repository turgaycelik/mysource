/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.type.GlobalShareQueryFactory;
import com.atlassian.jira.sharing.type.GroupShareQueryFactory;
import com.atlassian.jira.sharing.type.PrivateShareQueryFactory;
import com.atlassian.jira.sharing.type.ProjectShareQueryFactory;
import com.atlassian.jira.user.ApplicationUsers;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

/**
 * Create a permission query.
 *
 * @since v3.13
 */
public class PermissionQueryFactory implements QueryFactory
{
    private final PrivateShareQueryFactory privateShareQueryFactory = new PrivateShareQueryFactory();
    private final GlobalShareQueryFactory globalShareQueryFactory = new GlobalShareQueryFactory();
    private final ProjectShareQueryFactory projectShareQueryFactory;
    private final GroupShareQueryFactory groupShareQueryFactory;

    public PermissionQueryFactory(final ProjectShareQueryFactory projectShareQueryFactory, final GroupManager groupManager)
    {
        this.projectShareQueryFactory = projectShareQueryFactory;
        this.groupShareQueryFactory = new GroupShareQueryFactory(groupManager);
    }

    public Query create(final SharedEntitySearchParameters searchParameters, final User user)
    {
        final QueryBuilder builder = new QueryBuilder();
        builder.add(privateShareQueryFactory.getTerms(ApplicationUsers.from(user)), BooleanClause.Occur.SHOULD);
        builder.add(globalShareQueryFactory.getTerms(ApplicationUsers.from(user)), BooleanClause.Occur.SHOULD);
        builder.add(groupShareQueryFactory.getTerms(ApplicationUsers.from(user)), BooleanClause.Occur.SHOULD);
        builder.add(projectShareQueryFactory.getTerms(ApplicationUsers.from(user)), BooleanClause.Occur.SHOULD);
        return builder.toQuery();
    }

    /**
     * specifically unsupported here as this is designed for non-permission queries.
     */
    public Query create(final SharedEntitySearchParameters searchParameters)
    {
        throw new UnsupportedOperationException();
    }
}
