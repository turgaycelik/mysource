/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @since v3.13
 */
public class ProjectShareQueryFactory implements ShareQueryFactory<ProjectShareTypeSearchParameter>
{
    private static final class Name
    {
        static final String FIELD = "shareTypeProject";
    }

    private final Visibility visibility;

    public ProjectShareQueryFactory(final ProjectManager projectManager, final ProjectRoleManager projectRoleManager, final PermissionManager permissionManager, final ProjectFactory projectFactory)
    {
        this(new VisibilityImpl(projectManager, projectRoleManager, permissionManager, projectFactory));
    }

    public ProjectShareQueryFactory(final Visibility visibility)
    {
        this.visibility = visibility;
    }

    public Query getQuery(final ShareTypeSearchParameter searchParameter, final ApplicationUser user)
    {
        final ProjectShareTypeSearchParameter projectParameter = (ProjectShareTypeSearchParameter) searchParameter;
        visibility.check(user, projectParameter);

        // specific project and maybe role
        final TermQuery projectQuery = new TermQuery(new Term(Name.FIELD, ProjectSharePermission.indexValue(projectParameter.getProjectId(),
            projectParameter.getRoleId())));
        if (projectParameter.hasRole())
        {
            return projectQuery;
        }
        final BooleanQuery result = new BooleanQuery();
        result.add(projectQuery, Occur.SHOULD);
        for (final ProjectRole role : visibility.getRoles(user, projectParameter.getProjectId()))
        {
            result.add(new TermQuery(new Term(Name.FIELD, ProjectSharePermission.indexValue(projectParameter.getProjectId(), role.getId()))),
                Occur.SHOULD);
        }
        return result;
    }

    @Override
    public Query getQuery(ShareTypeSearchParameter parameter, User user)
    {
        return getQuery(parameter, ApplicationUsers.from(user));
    }

    public Query getQuery(final ShareTypeSearchParameter searchParameter)
    {
        final ProjectShareTypeSearchParameter projectParameter = (ProjectShareTypeSearchParameter) searchParameter;

        // specific project and maybe role
        final TermQuery projectQuery = new TermQuery(new Term(Name.FIELD, ProjectSharePermission.indexValue(projectParameter.getProjectId(),
            projectParameter.getRoleId())));
        if (projectParameter.hasRole())
        {
            return projectQuery;
        }
        final BooleanQuery result = new BooleanQuery();
        result.add(projectQuery, Occur.SHOULD);
        result.add(new PrefixQuery(new Term(Name.FIELD, ProjectSharePermission.searchAllRolesValue(projectParameter.getProjectId()))), Occur.SHOULD);
        return result;
    }

    public Term[] getTerms(final ApplicationUser user)
    {
        // All Case, need to find the user's Projects and Roles. Could be expensive.
        final List<ProjectAndRole> projectsAndRoles = visibility.getProjects(user);
        final List<Term> result = new ArrayList<Term>(projectsAndRoles.size());
        for (final ProjectAndRole projectAndRole : projectsAndRoles)
        {
            result.add(new Term(Name.FIELD, ProjectSharePermission.indexValue(projectAndRole.getProjectId(), projectAndRole.getRoleId())));
        }
        return result.toArray(new Term[result.size()]);
    }

    @Override
    public Term[] getTerms(User user)
    {
        return getTerms(ApplicationUsers.from(user));
    }

    public Field getField(final SharedEntity entity, final SharePermission permission)
    {
        return new Field(Name.FIELD, new ProjectSharePermission(permission).getIndexValue(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    /**
     * Responsible for checking and finding visible projects and roles for a particular parameter.
     */
    static interface Visibility
    {
        /**
         * Throw an unchecked exception if not visible.
         */
        void check(ApplicationUser user, ProjectShareTypeSearchParameter parameter) throws IllegalStateException;

        /**
         * Get the list of projects a user can see.
         */
        List<ProjectAndRole> getProjects(ApplicationUser user);

        /**
         * Get the list of roles a user can see for a project.
         */
        List<ProjectRole> getRoles(final ApplicationUser user, final Long projectId);
    }

    static class VisibilityImpl implements Visibility
    {
        private final ProjectManager projectManager;
        private final ProjectRoleManager projectRoleManager;
        private final PermissionManager permissionManager;
        private final ProjectFactory projectFactory;

        VisibilityImpl(final ProjectManager projectManager, final ProjectRoleManager projectRoleManager, final PermissionManager permissionManager, final ProjectFactory projectFactory)
        {
            Assertions.notNull("projectManager", projectManager);
            Assertions.notNull("projectRoleManager", projectRoleManager);
            Assertions.notNull("permissionManager", permissionManager);
            Assertions.notNull("projectFactory", projectFactory);

            this.projectManager = projectManager;
            this.projectRoleManager = projectRoleManager;
            this.permissionManager = permissionManager;
            this.projectFactory = projectFactory;
        }

        public void check(final ApplicationUser user, final ProjectShareTypeSearchParameter projectParameter)
        {
            // Not necessary if the permission query works
            final Project project = projectManager.getProjectObj(projectParameter.getProjectId());
            if (!permissionManager.hasPermission(Permissions.BROWSE, project, user))
            {
                throw new IllegalStateException("Cannot search for a Project you cannot see: " + project);
            }
            final Long roleId = projectParameter.getRoleId();
            if (roleId != null)
            {
                final ProjectRole role = projectRoleManager.getProjectRole(roleId);
                if (!projectRoleManager.isUserInProjectRole(user, role, project))
                {
                    throw new IllegalStateException("Cannot search for a ProjectRole you not a member of: " + role + " project:" + project);
                }
            }
        }

        public List<ProjectAndRole> getProjects(final ApplicationUser user)
        {
            final Collection<GenericValue> projectGvs = projectManager.getProjects();
            final Collection<Project> projects = (projectGvs == null) ? Collections.<Project>emptyList() : projectFactory.getProjects(projectGvs);
            final List<ProjectAndRole> result = new ArrayList<ProjectAndRole>();
            for (final Project project : projects)
            {
                final Collection<ProjectRole> roles = projectRoleManager.getProjectRoles(user, project);
                for (final ProjectRole role : roles)
                {
                    result.add(new ProjectAndRole(project.getId(), role.getId()));
                }
            }

            for (final Project project : CollectionUtil.filter(projects, new Predicate<Project>()
            {
                public boolean evaluate(final Project o)
                {
                    return permissionManager.hasPermission(Permissions.BROWSE, o, user);
                }
            }))
            {
                result.add(new ProjectAndRole(project.getId()));
            }
            return Collections.unmodifiableList(result);
        }

        public List<ProjectRole> getRoles(final ApplicationUser user, final Long projectId)
        {
            final Project project = projectManager.getProjectObj(projectId);
            return Collections.unmodifiableList(new ArrayList<ProjectRole>(projectRoleManager.getProjectRoles(user, project)));
        }
    }

    static class ProjectAndRole
    {
        private final Long projectId;
        private final Long projectRoleId;

        public ProjectAndRole(final Long projectId)
        {
            this(projectId, null);
        }

        public ProjectAndRole(final Long projectId, final Long projectRoleId)
        {
            Assertions.notNull("projectId", projectId);
            this.projectId = projectId;
            this.projectRoleId = projectRoleId;
        }

        Long getProjectId()
        {
            return projectId;
        }

        Long getRoleId()
        {
            return projectRoleId;

        }
    }
}
