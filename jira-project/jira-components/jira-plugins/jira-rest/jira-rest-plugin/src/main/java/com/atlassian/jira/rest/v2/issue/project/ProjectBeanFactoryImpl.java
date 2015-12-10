package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.issue.IssueTypeBeanBuilder;
import com.atlassian.jira.rest.v2.issue.ProjectCategoryResource;
import com.atlassian.jira.rest.v2.issue.ProjectResource;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v4.4
 */
public class ProjectBeanFactoryImpl implements ProjectBeanFactory
{
    private static final String EXPAND_FIELDS = "projectKeys";

    private final VersionBeanFactory versionBeanFactory;
    private final UserManager userManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final ProjectManager projectManager;
    private final ProjectRoleBeanFactory projectRoleBeanFactory;
    private final ResourceUriBuilder uriBuilder;
    private final UriInfo uriInfo;
    private final ProjectRoleService projectRoleService;
    private final JiraAuthenticationContext authenticationContext;

    public ProjectBeanFactoryImpl(VersionBeanFactory versionBeanFactory, UriInfo uriInfo,
            ResourceUriBuilder uriBuilder, ProjectRoleService projectRoleService, JiraAuthenticationContext authenticationContext,
            UserManager userManager, JiraBaseUrls jiraBaseUrls, ProjectManager projectManager,
            ProjectRoleBeanFactory projectRoleBeanFactory)
    {
        this.versionBeanFactory = versionBeanFactory;

        //This is proxied to report the current request URI. Go spring.
        this.uriInfo = uriInfo;
        this.uriBuilder = uriBuilder;
        this.projectRoleService = projectRoleService;
        this.authenticationContext = authenticationContext;
        this.userManager = userManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectManager = projectManager;
        this.projectRoleBeanFactory = projectRoleBeanFactory;
    }

    public ProjectBean fullProject(final Project project, final String expand)
    {
        Preconditions.checkNotNull(project, "project must not be null");
        Preconditions.checkNotNull(expand, "expand must not be null");

        ProjectBeanBuilder builder = shortProjectBuilder(project);

        builder.expand(EXPAND_FIELDS);
        builder.name(project.getName()).description(project.getDescription());
        String leadUserKey  = project.getLeadUserKey();
        final ApplicationUser user = userManager.getUserByKeyEvenWhenUnknown(leadUserKey);
        builder.lead(user);
        builder.components(project.getProjectComponents());
        builder.url(project.getUrl());
        builder.versions(project.getVersions());
        builder.assigneeType(project.getAssigneeType());
        builder.email(project.getEmail());

        if (expand.contains(EXPAND_FIELDS))
        {
            builder.projectKeys(projectManager.getAllProjectKeys(project.getId()));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final Collection<ProjectRole> projectRoles = projectRoleService.getProjectRoles(authenticationContext.getLoggedInUser(), errorCollection);
        if (!errorCollection.hasAnyErrors())
        {
            for (ProjectRole projectRole : projectRoles)
            {
                final URI uri = projectRoleBeanFactory.projectRole(project, projectRole).self;
                builder.role(projectRole.getName(), uri);
            }
        }

        // no lazyness
        builder.issueTypes(Lists.<IssueTypeJsonBean>newArrayList(transform(project.getIssueTypes(), new Function<IssueType, IssueTypeJsonBean>()
        {
            public IssueTypeJsonBean apply(IssueType issueType)
            {
                return new IssueTypeBeanBuilder().jiraBaseUrls(jiraBaseUrls).issueType(issueType).context(uriInfo).buildShort();
            }
        })));

        return builder.build();
    }

    public ProjectBean shortProject(final Project project)
    {
        return shortProjectBuilder(project).build();
    }

    private ProjectBeanBuilder shortProjectBuilder(Project project)
    {
        return new ProjectBeanBuilder()
                .self(createSelfLink(project))
                .key(project.getKey())
                .name(project.getName())
                .id(project.getId())
                .avatarUrls(ProjectJsonBean.getAvatarUrls(project))
                .projectCategory(project.getProjectCategoryObject());
    }

    private URI createSelfLink(Project project)
    {
        return uriBuilder.build(uriInfo, ProjectResource.class, project.getId().toString());
    }

    private class ProjectBeanBuilder
    {
        private String expand;
        private URI self;
        private String id;
        private String key;
        private String name;
        private String description;
        private UserBean lead;
        private Collection<ComponentBean> components;
        private String url;
        private String email;
        private Long assigneeType;
        private Collection<VersionBean> versions;
        private Collection<IssueTypeJsonBean> issueTypes;
        private Map<String, URI> roles = new HashMap<String, URI>();
        private Map<String, String> avatarUrls = new HashMap<String, String>();
        private Collection<String> projectKeys;
        private ProjectCategoryBean projectCategory;

        public ProjectBeanBuilder()
        {
        }

        public ProjectBeanBuilder expand(String expand)
        {
            this.expand = expand;
            return this;
        }

        public ProjectBeanBuilder self(URI self)
        {
            this.self = self;
            return this;
        }

        public ProjectBeanBuilder role(final String name, final URI uri)
        {
            roles.put(name, uri);
            return this;
        }

        public ProjectBeanBuilder id(Long id)
        {
            this.id = id == null ? null : id.toString();
            return this;
        }

        public ProjectBeanBuilder key(String key)
        {
            this.key = key;
            return this;
        }

        public ProjectBeanBuilder issueTypes(Collection<IssueTypeJsonBean> types)
        {
            this.issueTypes = types;
            return this;
        }

        public ProjectBeanBuilder description(String description)
        {
            this.description = description;
            return this;
        }

        public ProjectBeanBuilder lead(User lead)
        {
            this.lead = new UserBeanBuilder(jiraBaseUrls).user(lead).buildShort();
            return this;
        }

        public ProjectBeanBuilder lead(ApplicationUser lead)
        {
            this.lead = new UserBeanBuilder(jiraBaseUrls).user(lead).buildShort();
            return this;
        }

        public ProjectBeanBuilder assigneeType(Long assigneeType)
        {
            this.assigneeType = assigneeType;
            return this;
        }

        public ProjectBeanBuilder components(Collection<? extends ProjectComponent> components)
        {
            this.components = ComponentBean.asBeans(components, jiraBaseUrls);
            return this;
        }

        public ProjectBeanBuilder projectKeys(Collection<String> keys)
        {
            this.projectKeys = keys;
            return this;
        }

        public ProjectBeanBuilder url(String url)
        {
            this.url = StringUtils.stripToNull(url);
            return this;
        }

        public void email(String email)
        {
            this.email = StringUtils.stripToNull(email);
        }

        public ProjectBeanBuilder versions(Collection<? extends Version> versions)
        {
            this.versions = versionBeanFactory.createVersionBeans(versions);
            return this;
        }

        public ProjectBeanBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        public ProjectBeanBuilder avatarUrls(Map<String, String> avatarUrls)
        {
            this.avatarUrls = avatarUrls;
            return this;
        }

        public ProjectBeanBuilder projectCategory(ProjectCategory projectCategory)
        {
            if (projectCategory != null)
            {
                this.projectCategory = new ProjectCategoryBean(projectCategory, uriBuilder.build(uriInfo, ProjectCategoryResource.class, projectCategory.getId().toString()));
            }
            return this;
        }

        public ProjectBean build()
        {
            return new ProjectBean(expand, self, id, key, name, description, lead, assigneeType, url, email, components,
                    versions, issueTypes, roles, avatarUrls, projectKeys, projectCategory);
        }
    }
}
