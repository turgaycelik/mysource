package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeType;
import org.apache.lucene.search.Query;

import java.util.Set;

public interface SecurityType extends SchemeType
{
    Query getQuery(User searcher, Project project, String parameter);

    /**
     * Returns a query based on security level and the project passed in.  This may be required in particular
     * for role based issue security levels.  (see JRA-12739)
     *
     * @param searcher The searcher conducting the search
     * @param project The project for which we're constructing a query
     * @param securityLevel The security level for which we are constructing the query
     * @param parameter Parameter identifying user related field (reporter, assignee, etc)
     * @return A lucene permissions query
     */
    Query getQuery(User searcher, Project project, IssueSecurityLevel securityLevel, String parameter);

    String getArgumentDisplay(String argument);

    /**
     * Returns a list of {@link User}s represented by a security type instance.  The collection must
     * not contain any nulls.
     *
     * @param permissionContext The current issue and project
     * @param argument Instance value, eg. a group name, user name, custom field id
     * @return A set of {@link User}s.
     * @throws IllegalArgumentException if argument is invalid for this type.
     */
    Set<User> getUsers(PermissionContext permissionContext, String argument);
}
