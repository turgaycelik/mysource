package com.atlassian.jira.rest.v1.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.AbstractOption;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import org.apache.commons.collections.CollectionUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint for getting the Issue Type Scheme info.  It returns a list of projects the user can create issues in,
 * a list of issue types for those projects and a mapping between the two. 
 *
 * @since v4.0
 */
@CorsAllowed
public class IssueTypeSchemeResource
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final OptionSetManager optionSetManager;
    private final UserProjectHistoryManager projectHistoryManager;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory requestContextFactory;

    public IssueTypeSchemeResource(PermissionManager permissionManager, JiraAuthenticationContext authenticationContext,
                                   IssueTypeSchemeManager issueTypeSchemeManager, OptionSetManager optionSetManager,
                                   UserProjectHistoryManager projectHistoryManager, ApplicationProperties applicationProperties, VelocityRequestContextFactory requestContextFactory)
    {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.optionSetManager = optionSetManager;
        this.projectHistoryManager = projectHistoryManager;
        this.applicationProperties = applicationProperties;
        this.requestContextFactory = requestContextFactory;
    }

    @GET
    @AnonymousAllowed
    public Response getSchemeInfoResponse(@QueryParam("includeRecent")  @DefaultValue("false") boolean includeRecent)
    {
        return Response.ok(getSchemeInfo(includeRecent)).cacheControl(NO_CACHE).build();
    }

    /**
     * Returns all projects the user can create an issue in, as well as all available issue types for those projects and a mapping between them.
     *
     * @return all projects the user can create an issue in, as well as all available issue types for those projects and a mapping between them
     *         {@link com.atlassian.jira.rest.v1.admin.IssueTypeSchemeResource.IssueTypeSchemeInfo}
     */
    public IssueTypeSchemeInfo getSchemeInfo(boolean includeRecent)
    {
        final User user = authenticationContext.getLoggedInUser();
        final String currentUser = user == null ? null : user.getName();

        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.CREATE_ISSUE, user);
        final Collection<ProjectBean> projectBeans = new ArrayList<ProjectBean>(projects.size());
        final Set<FieldConfig> relevantConfigs = new HashSet<FieldConfig>();

        final List<Project> recentProjects = includeRecent ? projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user) : Collections.<Project>emptyList();
        List<ProjectBean> recentProjectBeans = new ArrayList<ProjectBean>(recentProjects.size());
        for (Project project : recentProjects)
        {
            final FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(project);
            final FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();
            final Avatar avatar = project.getAvatar();
            final ProjectBean projectBean = new ProjectBean(project.getId(), project.getName(), project.getKey(), relevantConfig.getId(), avatar == null ? null : avatar.getId());
            recentProjectBeans.add(projectBean);
        }
        for (Project project : projects)
        {
            final FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(project);
            final FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();
            relevantConfigs.add(relevantConfig);
            final Avatar avatar = project.getAvatar();
            final ProjectBean projectBean = new ProjectBean(project.getId(), project.getName(), project.getKey(), relevantConfig.getId(), avatar == null ? null : avatar.getId());
            projectBeans.add(projectBean);
        }


        final Collection<IssueTypeScheme> schemes = new ArrayList<IssueTypeScheme>(relevantConfigs.size());
        final Set<Option> allOptions = new HashSet<Option>();
        for (FieldConfig config : relevantConfigs)
        {
            final OptionSet optionsForConfig = optionSetManager.getOptionsForConfig(config);
            allOptions.addAll(optionsForConfig.getOptions());
            final IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(config);
            final String defaultId = defaultValue != null ? defaultValue.getId() : null;
            schemes.add(new IssueTypeScheme(config.getId(), defaultId, optionsForConfig.getOptionIds()));
        }
        final List<AbstractOption> optionsList = new ArrayList<AbstractOption>(CollectionUtils.select(allOptions, IssueConstantOption.STANDARD_OPTIONS_PREDICATE));
        Collections.sort(optionsList);

        final Collection<IssueTypeBean> issueTypeBeans = new ArrayList<IssueTypeBean>(optionsList.size());
        for (AbstractOption option : optionsList)
        {
            issueTypeBeans.add(new IssueTypeBean(option.getId(), option.getName(), option.getImagePath()));
        }

        String defaultType = getSelectedIssueType();

        if(recentProjectBeans.isEmpty())
        {
            return new IssueTypeSchemeInfo(schemes, projectBeans, issueTypeBeans, null, defaultType, currentUser);
        }
        else if (recentProjectBeans.containsAll(projectBeans))
        {
            return new IssueTypeSchemeInfo(schemes, recentProjectBeans, issueTypeBeans, null, defaultType, currentUser);
        }

        return new IssueTypeSchemeInfo(schemes, projectBeans, issueTypeBeans, recentProjectBeans, defaultType, currentUser);
    }


    private String getSelectedIssueType()
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();
        String issuetype = (String) session.getAttribute(SessionKeys.USER_HISTORY_ISSUETYPE);

        if (issuetype == null)
        {
            // fall back to the default issue type
            issuetype = applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_ISSUE_TYPE);
        }

        return issuetype;
    }

    @XmlRootElement
    public static class IssueTypeSchemeInfo
    {
        @XmlElement
        private Collection<IssueTypeScheme> schemes;
        @XmlElement
        private Collection<ProjectBean> projects;
        @XmlElement
        private Collection<ProjectBean> recentProjects;
        @XmlElement
        private Collection<IssueTypeBean> types;
        @XmlElement
        private String defaultType;
        @XmlElement
        private String currentUser;

        @XmlElement
        private boolean isEmpty;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private IssueTypeSchemeInfo() {
        }

        IssueTypeSchemeInfo(Collection<IssueTypeScheme> schemes, Collection<ProjectBean> projects, Collection<IssueTypeBean> issueTypes,
                            Collection<ProjectBean> recentProjects, String defaultType, String currentUser)
        {
            this.schemes = schemes;
            this.projects = projects;
            this.types = issueTypes;
            this.recentProjects = recentProjects;
            this.defaultType = defaultType;
            isEmpty = projects.isEmpty() || issueTypes.isEmpty() || schemes.isEmpty();
            this.currentUser = currentUser;
        }
    }

    @XmlRootElement
    public static class IssueTypeScheme
    {
        @XmlElement
        private Long id;
        @XmlElement
        private String defaultId;
        @XmlElement
        private Collection<String> types;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private IssueTypeScheme() {}

        IssueTypeScheme(Long schemeId, String defaultId, Collection<String> issueTypes)
        {
            this.id = schemeId;
            this.defaultId = defaultId;
            this.types = issueTypes;
        }
    }

    @XmlRootElement
    public static class ProjectBean
    {
        @XmlElement
        private Long id;
        @XmlElement
        private String name;
        @XmlElement
        private String key;
        @XmlElement
        private Long scheme;
        @XmlElement
        private Long img;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private ProjectBean() {}

        ProjectBean(Long id, String name, String key, Long issueTypeSchemeId, Long avatarId)
        {
            this.id = id;
            this.name = name;
            this.key = key;
            this.scheme = issueTypeSchemeId;
            this.img = avatarId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ProjectBean that = (ProjectBean) o;

            if (!id.equals(that.id))
            {
                return false;
            }
            if (!img.equals(that.img))
            {
                return false;
            }
            if (!key.equals(that.key))
            {
                return false;
            }
            if (!name.equals(that.name))
            {
                return false;
            }
            if (!scheme.equals(that.scheme))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + key.hashCode();
            result = 31 * result + scheme.hashCode();
            result = 31 * result + img.hashCode();
            return result;
        }
    }

    @XmlRootElement
    public static class IssueTypeBean
    {
        @XmlElement
        private String id;
        @XmlElement
        private String name;
        @XmlElement
        private String url;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private IssueTypeBean() {}

        IssueTypeBean(String id, String name, String url)
        {
            this.id = id;
            this.name = name;
            this.url = url;
        }
    }
}
