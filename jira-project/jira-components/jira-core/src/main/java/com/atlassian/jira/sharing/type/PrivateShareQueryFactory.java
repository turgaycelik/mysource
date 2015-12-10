/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Special ShareQueryFactory for the implied Private share type.
 *
 * @since v3.13
 */
public class PrivateShareQueryFactory implements ShareQueryFactory<PrivateShareTypeSearchParameter>
{
    private static final class Name
    {
        static final String FIELD = "owner";
    }

    public Field getField(final SharedEntity entity, final SharePermission permission)
    {
        Assertions.not("entity must be private", !entity.getPermissions().isPrivate());
        final String ownerUserKey = entity.getOwner() == null ? null : entity.getOwner().getKey();
        return new Field(Name.FIELD, (ownerUserKey == null) ? "" : ownerUserKey, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    @Override
    public Term[] getTerms(final ApplicationUser user)
    {
        final Term term = getTerm(user);
        return (term == null) ? new Term[0] : new Term[] { term };
    }

    @Override
    public Term[] getTerms(User user)
    {
        return getTerms(ApplicationUsers.from(user));
    }

    @Override
    public Query getQuery(final ShareTypeSearchParameter parameter, final ApplicationUser user)
    {
        final Term term = getTerm(user);
        return (term == null) ? null : new TermQuery(term);
    }

    @Override
    public Query getQuery(ShareTypeSearchParameter parameter, User user)
    {
        return getQuery(parameter, ApplicationUsers.from(user));
    }

    public Query getQuery(final ShareTypeSearchParameter parameter)
    {
        throw new UnsupportedOperationException("Can't query for Private Shares");
    }

    private Term getTerm(final ApplicationUser user)
    {
        return (user == null) ? null : new Term(Name.FIELD, user.getKey());
    }
}
