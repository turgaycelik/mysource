package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.rest.v2.issue.project.ProjectBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.project.ProjectRoleBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectRoleBeanFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypePermissionChecker;
import com.atlassian.jira.user.util.UserManager;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A builder utility to create a {@link FilterBean}
 *
 * @since v5.2
 */
public class FilterBeanBuilder
{
    private final JiraAuthenticationContext authContext;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final ProjectRoleManager projectRoleManager;
    private final ProjectBeanFactory projectBeanFactory;
    private final ShareTypeFactory shareTypeFactory;
    private final UserManager userManager;
    private final JqlStringSupport jqlStringSupport;
    private final GroupManager groupManager;
    private final SchemeManager schemeManager;
    private final FilterSubscriptionService filterSubscriptionService;
    private final JiraBaseUrls jiraBaseUrls;
    private final ProjectRoleBeanFactory projectRoleBeanFactory;

    private SearchRequest filter;
    private UriInfo context;
    private String canoncialBaseUrl;
    private User owner = null;
    private boolean favourite = false;

    public FilterBeanBuilder(JiraAuthenticationContext authContext, ProjectManager projectManager, PermissionManager permissionManager,
            ProjectRoleManager projectRoleManager, ProjectBeanFactory projectBeanFactory,
            ShareTypeFactory shareTypeFactory, UserManager userManager, JqlStringSupport jqlStringSupport,
            GroupManager groupManager, SchemeManager schemeManager, FilterSubscriptionService filterSubscriptionService,
            JiraBaseUrls jiraBaseUrls, ProjectRoleBeanFactory projectRoleBeanFactory)
    {
        this.authContext = authContext;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.projectRoleManager = projectRoleManager;
        this.projectBeanFactory = projectBeanFactory;
        this.shareTypeFactory = shareTypeFactory;
        this.userManager = userManager;
        this.jqlStringSupport = jqlStringSupport;
        this.groupManager = groupManager;
        this.schemeManager = schemeManager;
        this.filterSubscriptionService = filterSubscriptionService;
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectRoleBeanFactory = projectRoleBeanFactory;
    }

    /**
     * Sets the filter
     *
     * @param filter a filter
     * @return this
     */
    public FilterBeanBuilder filter(final SearchRequest filter)
    {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the context.
     *
     * @param context a UriInfo
     * @param canoncialBaseUrl the baseurl of this instance
     * @return this
     */
    public FilterBeanBuilder context(UriInfo context, final String canoncialBaseUrl)
    {
        this.context = context;
        this.canoncialBaseUrl = canoncialBaseUrl;
        return this;
    }


    public FilterBeanBuilder owner(User owner)
    {
        this.owner = owner;
        return this;
    }

    public FilterBeanBuilder favourite(boolean favourite)
    {
        this.favourite = favourite;
        return this;
    }


    public FilterBean build()
    {
        if (filter != null)
        {
            if (context == null || canoncialBaseUrl == null)
            {
                throw new IllegalStateException("No context set.");
            }

            final UserBean owner = new UserBeanBuilder(jiraBaseUrls).user(this.owner).buildShort();
            final URI issueNavUri = URI.create(canoncialBaseUrl +
                    "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filter.getId());

            final URI self = context.getBaseUriBuilder().path(FilterResource.class).
                    path(Long.toString(filter.getId())).build();

            String JQL = jqlStringSupport.generateJqlString(filter.getQuery());
            URI searchUri = context.getBaseUriBuilder().path(SearchResource.class)
                    .queryParam("jql", "{0}").build(JQL);

            Collection<FilterPermissionBean> sharePermissions = new ArrayList<FilterPermissionBean>();
            for (SharePermission sharePermission : filter.getPermissions())
            {
                final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
                if (type != null)
                {
                    final ShareTypePermissionChecker permissionChecker = type.getPermissionsChecker();
                    if (permissionChecker.hasPermission(authContext.getLoggedInUser(), sharePermission))
                    {
                        sharePermissions.add(buildSharePermission(sharePermission));
                    }
                }
            }

            final FilterSubscriptionBeanListWrapper filterSubscribtionBeanListWrapper = new FilterSubscriptionBeanListWrapper(
                    filterSubscriptionService, userManager, authContext.getUser(), filter, jiraBaseUrls);

            final UserListResolver userListResolver = new UserListResolver(authContext, userManager,
                                                                           groupManager, projectManager, permissionManager,
                                                                           projectRoleManager, schemeManager, sharePermissions);

            final UserBeanListWrapper userBeanListWrapper = new UserBeanListWrapper(jiraBaseUrls, userListResolver.getShareUsers(), FilterBean.MAX_USER_LIMIT);

            return new FilterBean(self, Long.toString(this.filter.getId()),
                    this.filter.getName(), this.filter.getDescription(), owner, JQL,
                    issueNavUri, searchUri, favourite, sharePermissions,
                    filterSubscribtionBeanListWrapper, userBeanListWrapper);
        }
        return null;
    }

    private FilterPermissionBean buildSharePermission(SharePermission input)
    {
        FilterPermissionBeanBuilder builder = new FilterPermissionBeanBuilder().sharePermission(input);
        ShareType.Name type = input.getType();
        if (ShareType.Name.GLOBAL.equals(type))
        {
            // ignore
        }
        else if (ShareType.Name.PROJECT.equals(type))
        {
            Long projectId = Long.valueOf(input.getParam1());
            Project project = projectManager.getProjectObj(projectId);
            ProjectBean projectBean = projectBeanFactory.shortProject(project);
            builder.project(projectBean);

            String roleKey = input.getParam2();
            if (null != roleKey)
            {
                Long id = Long.valueOf(roleKey);
                final ProjectRole projectRole = projectRoleManager.getProjectRole(id);
                final ProjectRoleBean projectRoleBean = projectRoleBeanFactory.projectRole(project, projectRole);

                builder.role(projectRoleBean);
            }
        }
        else if (ShareType.Name.GROUP.equals(type))
        {
            String groupKey = input.getParam1();
            builder.group(new GroupJsonBeanBuilder(jiraBaseUrls).name(groupKey).build());
        }
        return builder.build();
    }

    static class FilterPermissionBeanBuilder
    {
        private SharePermission sharePermission;
        private ProjectBean project;
        private ProjectRoleBean role;
        private GroupJsonBean group;

        public FilterPermissionBeanBuilder sharePermission(SharePermission sharePermission)
        {
            this.sharePermission = sharePermission;
            return this;
        }

        public FilterPermissionBeanBuilder project(ProjectBean project)
        {
            this.project = project;
            return this;
        }

        public FilterPermissionBeanBuilder role(ProjectRoleBean role)
        {
            this.role = role;
            return this;
        }

        public FilterPermissionBeanBuilder group(GroupJsonBean group)
        {
            this.group = group;
            return this;
        }

        public FilterPermissionBean build()
        {
            return new FilterPermissionBean(sharePermission.getId(), sharePermission.getType().get(), project, role, group);
        }
    }
}
