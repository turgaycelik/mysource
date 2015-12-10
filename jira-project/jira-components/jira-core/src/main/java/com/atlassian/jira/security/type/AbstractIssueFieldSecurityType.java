/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;


public abstract class AbstractIssueFieldSecurityType extends AbstractSecurityType
{
    private static final Logger log = Logger.getLogger(AbstractIssueFieldSecurityType.class);

    /**
     * Get the field name for this Issue Field-based Security Type given the parameter in the saved configuration.
     * <p>
     * Some Field based permissions are based on hard-coded fields like assignee and reporter in which case the parameter is not used.
     * Other types use the parameter to name the particular User or Group custom field that is used for the permission.
     *
     * @param parameter the parameter as saved in the config
     * @return the field name for this Issue Field-based Security Type.
     */
    protected abstract String getFieldName(String parameter);

    @Override
    public Query getQuery(User searcher, Project project, String parameter)
    {
        if (project == null)
        {
            return null;
        }

        PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getPermissionSchemeManager();
        try
        {
            final Long browsePermission = (long)Permissions.BROWSE;
            // TODO: This looks stupid and will actually call the DB :( Is it ever actually for anything other than Browse?
            List<GenericValue> schemes = permissionSchemeManager.getSchemes(project.getGenericValue());
            for (GenericValue scheme : schemes)
            {
                if (permissionSchemeManager.getEntities(scheme, getType(), browsePermission).size() > 0)
                {
                    BooleanQuery projectAndUserQuery = getQueryForProject(project, searcher, getFieldName(parameter));
                    if (projectAndUserQuery != null)
                    {
                        BooleanQuery query = new BooleanQuery();
                        query.add(projectAndUserQuery, BooleanClause.Occur.SHOULD);
                        return query;
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Could not retrieve scheme for this project.", e);
        }
        return null;
    }

    @Override
    public Query getQuery(User searcher, Project project, IssueSecurityLevel securityLevel, String parameter)
    {
        BooleanQuery queryForSecurityLevel = getQueryForSecurityLevel(securityLevel, searcher, getFieldName(parameter));
        if (queryForSecurityLevel == null)
        {
            return null;
        }
        else
        {
            BooleanQuery query = new BooleanQuery();
            query.add(queryForSecurityLevel, BooleanClause.Occur.MUST);
            return query;
        }
    }

    /**
     * Gets called to produce the Lucene query for a project
     * @param project The project for which to construct a query
     * @param searcher The user who is searching to add to the query
     * @return A BooleanQuery with the project and searcher terms, or {@code null} if the searcher is either
     *      {@code null} or not a known user
     */
    @Nullable
    protected BooleanQuery getQueryForProject(@Nonnull Project project, @Nullable User searcher, @Nonnull String fieldName)
    {
        BooleanQuery projectAndUserQuery = null;
        final ApplicationUser appSearcher = ApplicationUsers.from(searcher);
        if (appSearcher != null)
        {
            projectAndUserQuery = new BooleanQuery();
            final Query projectQuery = new TermQuery(new Term(DocumentConstants.PROJECT_ID, project.getId().toString()));
            final Query userQuery = new TermQuery(new Term(fieldName, appSearcher.getKey()));
            projectAndUserQuery.add(projectQuery, BooleanClause.Occur.MUST);
            projectAndUserQuery.add(userQuery, BooleanClause.Occur.MUST);
        }
        return projectAndUserQuery;
    }

    /**
     * Produces a Lucene query for a given issue security type such that documents
     * match the query only when the given user is defined for the issue by this
     * custom field in the given security.
     *
     * @param issueSecurity the security defined by this IssueFieldSecurityType instance.
     * @param searcher      the user.
     * @return a query to constrain to the given issue security for the given user or {@code null} if user is either
     *      {@code null} or not a known user
     */
    @Nullable
    protected BooleanQuery getQueryForSecurityLevel(@Nonnull IssueSecurityLevel issueSecurity, @Nullable User searcher,
            @Nonnull String fieldName)
    {
        BooleanQuery issueLevelAndUserQuery = null;
        final ApplicationUser appSearcher = ApplicationUsers.from(searcher);
        if (appSearcher != null)
        {
            issueLevelAndUserQuery = new BooleanQuery();
            // We wish to ensure that the search has the value of the field
            Term securityLevelIsSet = new Term(DocumentConstants.ISSUE_SECURITY_LEVEL, issueSecurity.getId().toString());
            Term customFieldSpecifiesUser = new Term(fieldName, appSearcher.getKey());
            issueLevelAndUserQuery.add(new TermQuery(securityLevelIsSet), BooleanClause.Occur.MUST);
            issueLevelAndUserQuery.add(new TermQuery(customFieldSpecifiesUser), BooleanClause.Occur.MUST);
        }
        return issueLevelAndUserQuery;
    }

    @Override
    public boolean hasPermission(GenericValue entity, String argument)
    {
        return false;
    }

    @Override
    public boolean hasPermission(Issue issue, String argument)
    {
        return false;
    }

    @Override
    public boolean hasPermission(Project project, String argument)
    {
        return false;
    }

    /**
     * Decides if the given User has permission to see the given issue or project.
     * If the user is null they can never have the permission so false is returned.
     * If the entity is a Project the permission is always true as report and
     * assignee have no context in a project only on the issues with the project
     * It the entity is an Issue check if the user is in the relevent field in the issue
     *
     * @param entity        The Generic Value. Should be an Issue (but sometimes it's a Project)
     * @param argument      Not needed for this implementation
     * @param user          User to check the permission on. If it is null then the check is made on the current user
     * @param issueCreation NFI
     * @return true if the user is the current assignee otherwise false
     * @see CurrentReporter#hasPermission
     * @see ProjectLead#hasPermission
     * @see SingleUser#hasPermission
     * @see GroupDropdown#hasPermission
     */
    @Override
    public boolean hasPermission(GenericValue entity, String argument, User user, boolean issueCreation)
    {

        if (user != null && entity != null)
        {
            if ("Issue".equals(entity.getEntityName()))
            {
                return hasIssuePermission(user, issueCreation, entity, argument);
            }
            else if ("Project".equals(entity.getEntityName()))
            {
                return hasProjectPermission(user, issueCreation, entity);
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Issue issue, String parameter, User user, boolean issueCreation)
    {

        if (user == null || issue == null)
        {
            return false;
        }
        else
        {
            return hasIssuePermission(user, issueCreation, issue, parameter);
        }
    }

    @Override
    public boolean hasPermission(Project project, String parameter, User user, boolean issueCreation)
    {

        if (user == null || project == null)
        {
            return false;
        }
        else
        {
             return hasProjectPermission(user, issueCreation, project);
        }
    }

    protected abstract boolean hasIssuePermission(User user, boolean issueCreation, GenericValue issueGv, String argument);

    protected abstract boolean hasIssuePermission(User user, boolean issueCreation, Issue issue, String parameter);

    protected abstract boolean hasProjectPermission(User user, boolean issueCreation, GenericValue project);

    protected abstract boolean hasProjectPermission(User user, boolean issueCreation, Project project);
}
