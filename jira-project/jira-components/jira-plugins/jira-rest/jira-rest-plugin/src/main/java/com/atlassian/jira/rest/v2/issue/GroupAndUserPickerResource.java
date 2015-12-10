package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.groups.GroupPickerResourceHelper;
import com.atlassian.jira.rest.v2.issue.users.UserPickerResourceHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Path ("groupuserpicker")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@AnonymousAllowed
public class GroupAndUserPickerResource
{
    private final JiraAuthenticationContext authContext;
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final GroupPickerResourceHelper groupPickerHelper;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final UserFilterManager userFilterManager;
    private final UserPickerResourceHelper userPickerHelper;

    public GroupAndUserPickerResource(final JiraAuthenticationContext authContext,
            final ConstantsManager constantsManager,
            final CustomFieldManager customFieldManager,
            final FieldConfigSchemeManager fieldConfigSchemeManager,
            final GroupPickerResourceHelper groupPickerHelper,
            final PermissionManager permissionManager,
            final ProjectManager projectManager,
            final UserFilterManager userFilterManager,
            final UserPickerResourceHelper userPickerHelper)
    {
        this.authContext = authContext;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.groupPickerHelper = groupPickerHelper;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.userFilterManager = userFilterManager;
        this.userPickerHelper = userPickerHelper;
    }


    /**
     * Returns a list of users and groups matching query with highlighting. This resource cannot be accessed
     * anonymously.
     *
     * @param query A string used to search username, Name or e-mail address
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000. If
     * you specify a value that is higher than this number, your search results will be truncated.
     * @param showAvatar
     * @param fieldId The custom field id, if this request comes from a custom field, such as a user picker. Optional.
     * @param projectId The list of project ids to further restrict the search
     * This parameter can occur multiple times to pass in multiple project ids.
     * Comma separated value is not supported.
     * This parameter is only used when fieldId is present.
     * @param issueTypeId The list of issue type ids to further restrict the search.
     * This parameter can occur multiple times to pass in multiple issue type ids.
     * Comma separated value is not supported.
     * Special values such as -1 (all standard issue types), -2 (all subtask issue types) are supported.
     * This parameter is only used when fieldId is present.
     * @return An object containing list of matched user objects, with html representing highlighting.
     */
    @GET
    public Response findUsersAndGroups(@QueryParam ("query") final String query, @QueryParam ("maxResults")
    final Integer maxResults, @QueryParam ("showAvatar") final Boolean showAvatar,
            @QueryParam("fieldId") final String fieldId, @QueryParam("projectId") final List<String> projectId,
            @QueryParam("issueTypeId") final List<String> issueTypeId)
    {
        final UserSearchParams.Builder paramBuilder = UserSearchParams.builder().allowEmptyQuery(false).includeActive(true).includeInactive(false);
        boolean returnEmptyUsers = false;
        if (StringUtils.isNotBlank(fieldId))
        {
            final List<Long> projectIdList = getProjectIdList(projectId);
            final SearchContext searchContext = new SearchContextImpl(null, projectIdList, getIssueTypeIdList(issueTypeId));

            if (!updateParamWithUserFilter(paramBuilder, fieldId, searchContext))
            {
                returnEmptyUsers = true;
            }
        }
        final UserSearchParams searchParams = paramBuilder.build();
        // pass in empty query to return empty result
        final UserPickerResultsBean userResultsBean = userPickerHelper.findUsersAsBean(
                returnEmptyUsers ? "" : query, maxResults, showAvatar, null, searchParams);
        final GroupSuggestionsBean groupsAsBean = groupPickerHelper.findGroupsAsBean(query, null, maxResults);
        return Response.ok(new UsersAndGroupsBean(userResultsBean, groupsAsBean))
                .cacheControl(never())
                .build();
    }

    private boolean updateParamWithUserFilter(final UserSearchParams.Builder paramBuilder, final String fieldId, final SearchContext searchContext)
    {
        final CustomField customField = customFieldManager.getCustomFieldObject(fieldId);
        if (customField != null)
        {
            final FieldConfig fieldConfig = customField.getReleventConfig(searchContext);
            if (fieldConfig != null)
            {
                final UserFilter userFilter = userFilterManager.getFilter(fieldConfig);
                if (userFilter != null)
                {
                    paramBuilder.filter(userFilter);
                    if (CollectionUtils.isNotEmpty(userFilter.getRoleIds()))
                    {
                        // add project ids if we need to filter by roles
                        final Collection<Long> projectIds = getProjectIdsFromFieldConfig(searchContext.getProjectIds(), fieldConfig);
                        final Collection<Project> projectObjects = getProjectObjects(projectIds);
                            final Collection<Project> browsableProjects = Collections2.filter(projectObjects, new Predicate<Project>()
                            {
                                @Override
                                public boolean apply(@Nullable final Project project)
                                {
                                    return (project != null && permissionManager.hasPermission(Permissions.BROWSE, project, authContext.getUser()));
                                }
                            });

                        paramBuilder.filterByProjectIds(Collections2.transform(browsableProjects, new Function<Project, Long>()
                        {
                            @Override
                            public Long apply(final Project input)
                            {
                                return input.getId();
                            }
                        }));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private Collection<Long> getProjectIdsFromFieldConfig(Collection<Long> existingProjectIds, FieldConfig fieldConfig)
    {
        if (CollectionUtils.isNotEmpty(existingProjectIds))
        {
            return existingProjectIds;
        }
        return CustomFieldUtils.getProjectIdsFromProjectOrFieldConfig(null, fieldConfig, fieldConfigSchemeManager, projectManager);
    }

    /**
     * Retrieve the list of project objects based on a set of project id's with some smartness
     * to avoid too many db round trips.
     * @param projectIds the ids of the projects to be retrieved
     * @return the project objects
     */
    private Collection<Project> getProjectObjects(final Collection<Long> projectIds)
    {
        if (CollectionUtils.isEmpty(projectIds))
        {
            // when used in a context where specific projects could be found, the caller is responsible for
            // making sure that all browsable project ids by the current user is passed in as <code>projectIds</code>
            // because only the caller knows the calling user.
            return ImmutableList.of();
        }
        else if (projectIds.size() == 1)
        {
            final Project project = projectManager.getProjectObj(projectIds.iterator().next());
            return project == null ? ImmutableList.<Project>of() : ImmutableList.of(project);
        }
        else
        {
            // try to retrieve all projects at one go, and return those whose ID is in the set
            final List<Project> projects = projectManager.getProjectObjects();
            return Collections2.filter(projects, new Predicate<Project>()
            {
                @Override
                public boolean apply(@Nullable final Project input)
                {
                    return input != null && projectIds.contains(input.getId());
                }
            });
        }
    }

    private List<Long> getProjectIdList(List<String> projectIdList)
    {
        final Set<Long> projectIds = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(projectIdList))
        {
            for (String projectIdStr : projectIdList)
            {
                final long projectId = NumberUtils.toLong(projectIdStr, -1);
                if (projectId > 0)
                {
                    projectIds.add(projectId);
                }
            }
        }
        return ImmutableList.copyOf(projectIds);
    }

    private List<String> getIssueTypeIdList(List<String> issueTypeIdList)
    {
        final Set<String> issueTypeIdSet = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(issueTypeIdList))
        {
            for (String issueTypeIdStr : issueTypeIdList)
            {
                // try to parse it to be sure that it's valid
                if (NumberUtils.toLong(issueTypeIdStr, -1) > 0)
                {
                    // reusing expandIssueTypeIds such that we don't need to be aware of the logic of the special values like -2,-3
                    issueTypeIdSet.addAll(constantsManager.expandIssueTypeIds(ImmutableList.of(issueTypeIdStr)));
                }
            }
        }
        return ImmutableList.copyOf(issueTypeIdSet);
    }
}
