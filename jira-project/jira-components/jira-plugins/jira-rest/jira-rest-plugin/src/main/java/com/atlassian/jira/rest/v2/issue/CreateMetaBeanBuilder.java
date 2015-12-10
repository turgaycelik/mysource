package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builder for {@link CreateMetaBean} instances.
 *
 * @since v5.0
 */
public class CreateMetaBeanBuilder
{
    private final JiraAuthenticationContext authContext;
    private final ProjectManager projectManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ContextUriInfo contextUriInfo;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final PermissionManager permissionManager;
    private final VersionBeanFactory versionBeanFactory;
    private final JiraBaseUrls baseUrls;

    private List<StringList> projectIds;
    private List<StringList> projectKeys;
    private List<StringList> issueTypeIds;
    private List<String> issueTypeNames;
    private IssueFactory issueFactory;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldManager fieldManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public CreateMetaBeanBuilder(final JiraAuthenticationContext authContext, final ProjectManager projectManager,
            final FieldLayoutManager fieldLayoutManager,
            final VelocityRequestContextFactory velocityRequestContextFactory, final ContextUriInfo contextUriInfo,
            final IssueTypeSchemeManager issueTypeSchemeManager, final PermissionManager permissionManager,
            final VersionBeanFactory versionBeanFactory, final JiraBaseUrls baseUrls, final IssueFactory issueFactory,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final FieldManager fieldManager,
            final IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.authContext = authContext;
        this.projectManager = projectManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.contextUriInfo = contextUriInfo;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.permissionManager = permissionManager;
        this.versionBeanFactory = versionBeanFactory;
        this.baseUrls = baseUrls;
        this.issueFactory = issueFactory;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldManager = fieldManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    private static <T> List<T> mapNullToEmptyList(List<T> list)
    {
        return (list != null) ? list : Collections.<T>emptyList();
    }

    public CreateMetaBeanBuilder projectIds(final List<StringList> projectIds)
    {
        this.projectIds = mapNullToEmptyList(projectIds);
        return this;
    }

    public CreateMetaBeanBuilder projectKeys(final List<StringList> projectKeys)
    {
        this.projectKeys = mapNullToEmptyList(projectKeys);
        return this;
    }

    public CreateMetaBeanBuilder issueTypeIds(final List<StringList> issueTypeIds)
    {
        this.issueTypeIds = mapNullToEmptyList(issueTypeIds);
        return this;
    }

    public CreateMetaBeanBuilder issueTypeNames(final List<String> issueTypeNames)
    {
        this.issueTypeNames = mapNullToEmptyList(issueTypeNames);
        return this;
    }

    public CreateMetaBean build()
    {
        final Iterable<Project> projects = getProjects(projectIds, projectKeys);
        final Predicate<IssueType> issueTypes = makeIssueTypesPredicate(issueTypeIds, issueTypeNames);

        final List<CreateMetaProjectBean> projectBeans = new ArrayList<CreateMetaProjectBean>();

        for (final Project project : projects)
        {
            projectBeans.add(createProjectBean(project, issueTypes));
        }

        return new CreateMetaBean(projectBeans);
    }

    private CreateMetaProjectBean createProjectBean(final Project project, final Predicate<IssueType> includeIssueType)
    {
        // Get the issue types that this project can see
        final List<IssueType> issueTypesForProject = getIssueTypesForProject(project, includeIssueType);
        final List<CreateMetaIssueTypeBean> issueTypeBeans = new ArrayList<CreateMetaIssueTypeBean>(issueTypesForProject.size());

        for (final IssueType issueType : issueTypesForProject)
        {
            issueTypeBeans.add(createIssueTypeBean(project, issueType));
        }

        final ProjectJsonBean projectBean = ProjectJsonBean.shortBean(project, baseUrls);

        return new CreateMetaProjectBean(
                projectBean.getSelf(),
                projectBean.getId(),
                projectBean.getKey(),
                projectBean.getName(),
                projectBean.getAvatarUrls(),
                issueTypeBeans);
    }

    private List<IssueType> getIssueTypesForProject(final Project project, final Predicate<IssueType> includeIssueType)
    {
        final Collection<IssueType> allIssueTypesForProject = issueTypeSchemeManager.getIssueTypesForProject(project);
        final List<IssueType> result = new ArrayList<IssueType>(allIssueTypesForProject.size());

        for (final IssueType issueType : allIssueTypesForProject)
        {
            // If no issue types given, show all issue types
            // Otherwise, intersect the list of given issue types with the issue types that the project can see
            if (includeIssueType.apply(issueType))
            {
                result.add(issueType);
            }
        }

        return result;
    }

    private CreateMetaIssueTypeBean createIssueTypeBean(final Project project, final IssueType issueType)
    {
        final IssueTypeJsonBean issueTypeBean = new IssueTypeBeanBuilder()
                .jiraBaseUrls(baseUrls)
                .context(contextUriInfo)
                .issueType(issueType)
                .build();

        final MutableIssue nullIssue = issueFactory.getIssue();
        nullIssue.setProjectObject(project);
        nullIssue.setIssueTypeObject(issueType);

        final CreateMetaFieldBeanBuilder fieldsBuilder = new CreateMetaFieldBeanBuilder(fieldLayoutManager, project,
                nullIssue, issueType, authContext.getLoggedInUser(), versionBeanFactory, velocityRequestContextFactory,
                contextUriInfo, baseUrls, permissionManager, fieldScreenRendererFactory, authContext, fieldManager,
                new DefaultFieldMetaBeanHelper(project, issueType, issueSecurityLevelManager));

        return new CreateMetaIssueTypeBean(
                issueTypeBean.getSelf(),
                issueTypeBean.getId(),
                issueTypeBean.getName(),
                issueTypeBean.getDescription(),
                issueTypeBean.isSubtask(),
                issueTypeBean.getIconUrl(),
                fieldsBuilder);
    }

    private Iterable<Project> getProjects(final List<StringList> projectIds, final List<StringList> projectKeys)
    {
        if (projectIds.isEmpty() && projectKeys.isEmpty())
        {
            // If no project params, show all projects in which the user can create issues
            return getAllProjects();
        }
        
        final Map<Long, Project> projects = new LinkedHashMap<Long, Project>();
        addProjectsById(StringList.joinLists(projectIds).asList(), projects);
        addProjectsByKey(StringList.joinLists(projectKeys).asList(), projects);
        return projects.values();
    }

    private boolean hasCreatePerm(Project project)
    {
        return permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, authContext.getLoggedInUser());
    }

    private List<Project> getAllProjects()
    {
        // Get all projects which the user can create issues in
        return new ArrayList<Project>(permissionManager.getProjectObjects(Permissions.CREATE_ISSUE, authContext.getLoggedInUser()));
    }

    private void addProjectsById(final List<String> projectIds, final Map<Long, Project> projects)
    {
        for (final String projectId : projectIds)
        {
            // Convert projectId to Long
            final Long projectIdLong;
            try
            {
                projectIdLong = Long.parseLong(projectId);
            }
            catch (NumberFormatException e)
            {
                continue;
            }

            final Project project = projectManager.getProjectObj(projectIdLong);

            if (project != null && hasCreatePerm(project))
            {
                // Check if project is already in list, to avoid the chance of duplicates
                if (!projects.containsKey(project.getId()))
                {
                    projects.put(project.getId(), project);
                }
            }
        }
    }

    private void addProjectsByKey(final List<String> projectKeys, final Map<Long, Project> projects)
    {
        for (final String projectKey : projectKeys)
        {
            final Project project = projectManager.getProjectObjByKey(projectKey);

            if (project != null && hasCreatePerm(project))
            {
                // Check if project is already in list, to avoid the chance of duplicates
                if (!projects.containsKey(project.getId()))
                {
                    projects.put(project.getId(), project);
                }
            }
        }
    }

    private Predicate<IssueType> makeIssueTypesPredicate(final List<StringList> issueTypeIdsList, final List<String> issueTypeNames)
    {
        final Set<String> issueTypeIds = new HashSet<String>(StringList.joinLists(issueTypeIdsList).asList());
        final Set<String> issueTypeNameSet = new HashSet<String>(issueTypeNames);
        return new Predicate<IssueType>()
        {
            @Override
            public boolean apply(IssueType input)
            {
                return (issueTypeIds.isEmpty() && issueTypeNameSet.isEmpty()) ||
                        issueTypeIds.contains(input.getId()) ||
                        issueTypeNameSet.contains(input.getName());

            }
        };
    }
}
