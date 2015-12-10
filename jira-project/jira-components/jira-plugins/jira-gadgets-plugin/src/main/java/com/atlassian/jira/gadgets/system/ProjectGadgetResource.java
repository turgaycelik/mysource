package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.gadgets.system.util.DefaultProjectsAndCategoriesHelper;
import com.atlassian.jira.gadgets.system.util.ProjectsAndCategoriesHelper;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.ProjectHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static java.util.Arrays.asList;

/**
 * REST resource for the project gadget.
 *
 * @since v4.0
 */
@Path("/project")
@AnonymousAllowed
@Produces({MediaType.APPLICATION_JSON})
public class ProjectGadgetResource extends AbstractResource
{
    private static final Logger log = Logger.getLogger(ProjectGadgetResource.class);
    private static final String PROJECT_OR_CATEGORY_IDS = "projectsOrCategories";

    private ProjectsAndCategoriesHelper helper;
    private final ProjectManager projectManager;
    private final PluginAccessor pluginAccessor;
    private final JiraAuthenticationContext authenticationContext;
    private final ConstantsManager constantsManager;
    private final PermissionManager permissionManager;
    private final SearchService searchService;
    private final SimpleLinkManager simpleLinkManager;
    private final ProjectDescriptionRenderer projectDescriptionRenderer;
    private final FieldVisibilityManager fieldVisibilityManager;

    public ProjectGadgetResource(final JiraAuthenticationContext authenticationContext,
            final PermissionManager permissionManager, final ProjectManager projectManager,
            final PluginAccessor pluginAccessor, final ConstantsManager constantsManager,
            final SearchService searchService, final SimpleLinkManager simpleLinkManager,
            final ProjectDescriptionRenderer projectDescriptionRenderer,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        this.projectManager = projectManager;
        this.pluginAccessor = pluginAccessor;
        this.authenticationContext = authenticationContext;
        this.constantsManager = constantsManager;
        this.permissionManager = permissionManager;
        this.searchService = searchService;
        this.simpleLinkManager = simpleLinkManager;
        this.projectDescriptionRenderer = projectDescriptionRenderer;
        this.fieldVisibilityManager = fieldVisibilityManager;
        helper = new DefaultProjectsAndCategoriesHelper(projectManager, permissionManager, authenticationContext);
    }

    @GET
    @Path("validate")
    public Response validate(@QueryParam(PROJECT_OR_CATEGORY_IDS) final String projectsOrCategories)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        validateProjectsAndCategories(errors, projectsOrCategories, true);

        return createValidationResponse(errors);
    }

    private void validateProjectsAndCategories(Collection<ValidationError> errors, final String projectsOrCategories, boolean validatePermission)
    {
        if (StringUtils.isBlank(projectsOrCategories))
        {
            errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.projects.and.categories.none.selected"));
            return;
        }

        List<String> projectAndCategoryIds = splitProjectAndCategoryIds(projectsOrCategories);

        if (projectAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_PROJECTS))
        {
            if (projectAndCategoryIds.size() > 1)
            {
                errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.all.projects.and.others"));
            }
        }
        else if (projectAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_CATEGORIES))
        {
            if (projectAndCategoryIds.size() > 1)
            {
                errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.all.categories.and.others"));
            }
        }
        else
        {
            boolean hasProject = false;
            boolean hasCategories = false;

            for (String projectAndCategoryId : projectAndCategoryIds)
            {
                if (projectAndCategoryId.startsWith("cat"))
                {
                    hasCategories = true;
                }
                else
                {
                    hasProject = true;
                }
            }
            if (hasCategories && hasProject)
            {
                errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.projects.and.categories.mixed"));
            }
            else
            {
                if (validatePermission)
                {
                    if (hasCategories)
                    {
                        validateCategories(errors, projectAndCategoryIds);
                    }
                    else if (hasProject)
                    {
                        validateProjects(errors, projectAndCategoryIds);
                    }
                    else
                    {
                        errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.projects.and.categories.none.selected"));
                    }
                }
            }
        }
    }

    private void validateProjects(Collection<ValidationError> errors, List<String> projectAndCategoryIds)
    {
        for (String projectIdStr : projectAndCategoryIds)
        {
            validateProject(errors, projectIdStr);
        }
    }

    private Project validateProject(Collection<ValidationError> errors, String projectIdStr)
    {
        try
        {
            final Long projectId = Long.valueOf(projectIdStr);
            final Project projectObj = projectManager.getProjectObj(projectId);
            if (projectObj == null || !permissionManager.hasPermission(Permissions.BROWSE, projectObj, authenticationContext.getUser()))
            {
                errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.invalid.project"));
            }
            if (errors.isEmpty())
            {
                return projectObj;
            }
        }
        catch (NumberFormatException e)
        {
            errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.invalid.project"));

        }

        return null;
    }

    private void validateCategories(Collection<ValidationError> errors, List<String> projectAndCategoryIds)
    {
        for (String projectId : projectAndCategoryIds)
        {
            GenericValue projectcat = projectManager.getProjectCategory(Long.valueOf(projectId.substring(3)));
            if (projectcat == null || permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getLoggedInUser(), projectcat).isEmpty())
            {
                errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.invalid.projectCategory"));
            }
        }
    }

    @GET
    @Path("filters")
    public Response getFilters (@QueryParam("projectId") final String projectId)
    {
        final User user = authenticationContext.getLoggedInUser();
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        final Project project = validateProject(errors, projectId);

        final List<OptionData> list = getFilterData(project, user);

        return Response.ok(new OptionDataList(list)).cacheControl(CacheControl.NO_CACHE).build();
    }

    @GET
    @Path("reports")
    public Response getReports (@QueryParam("projectId") final String projectId)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        final Project project = validateProject(errors, projectId);

        final List<OptionData> list = getReportData(project);

        return Response.ok(new OptionDataList(list)).cacheControl(CacheControl.NO_CACHE).build();
    }

    @GET
    @Path("generate")
    public Response generate(@QueryParam(PROJECT_OR_CATEGORY_IDS) final String projectsOrCategories,
                             @QueryParam("showStats") @DefaultValue("false") final boolean showStats)
    {
        final User user = authenticationContext.getLoggedInUser();
        DefaultVelocityRequestContextFactory.cacheVelocityRequestContext("", null);

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        validateProjectsAndCategories(errors, projectsOrCategories, false);

        if (!errors.isEmpty())
        {
            return createValidationResponse(errors);
        }

        final List<String> projectsAndCategoryIds = splitProjectAndCategoryIds(projectsOrCategories);

        final List<Category> categories = new ArrayList<Category>();

        if (!projectsAndCategoryIds.isEmpty())
        {
            if (projectsAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_CATEGORIES))
            {
                final Collection<GenericValue> allCategories = projectManager.getProjectCategories();
                for (GenericValue categoryGV : allCategories)
                {
                    Category category = getCategoryData(user, categoryGV, showStats);
                    if (category != null && category.projects.size() > 0)
                    {
                        categories.add(category);
                    }
                }
                Category category = getCategoryData(user, null, showStats);
                if (category != null && category.projects.size() > 0)
                {
                    categories.add(category);
                }

            }
            else if (projectsAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_PROJECTS))
            {
                final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, user);
                final List<DetailedProjectData> projectData = new ArrayList<DetailedProjectData>();
                for (Project project : projects)
                {
                    projectData.add(getProjectDataNoPermissionCheck(user, project, showStats));
                }

                if (!projectData.isEmpty())
                {
                    categories.add(new Category(null, null, projectData));
                }
            }
            else if (projectsAndCategoryIds.get(0).startsWith("cat"))
            {
                for (String projectId : projectsAndCategoryIds)
                {
                    GenericValue projectcat = projectManager.getProjectCategory(Long.valueOf(projectId.substring(3)));
                    if (projectcat != null)
                    {
                        Category category = getCategoryData(user, projectcat, showStats);
                        if (category != null && category.projects.size() > 0)
                        {
                            categories.add(category);
                        }
                    }
                }
            }
            else
            {
                final List<DetailedProjectData> projectData = new ArrayList<DetailedProjectData>();
                for (String projectId : projectsAndCategoryIds)
                {
                    Project project = projectManager.getProjectObj(Long.parseLong(projectId));
                    if (project != null)
                    {
                        final DetailedProjectData detailedProjectData = getProjectData(user, project, showStats);
                        if (detailedProjectData != null)
                        {
                            projectData.add(detailedProjectData);
                        }
                    }
                }

                if (!projectData.isEmpty())
                {
                    categories.add(new Category(null, null, projectData));
                }

            }
        }

        final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, user);
        final boolean doAnyProjectsExist = !projectManager.getProjectObjects().isEmpty();
        return Response.ok(new Projects(categories, isAdmin, doAnyProjectsExist)).cacheControl(CacheControl.NO_CACHE).build();
    }

    private Category getCategoryData(User user, GenericValue projectcat, boolean showStats)
    {
        Category category = null;
        final List<DetailedProjectData> projectData = new ArrayList<DetailedProjectData>();
        final Collection<Project> projects = projectcat == null ? projectManager.getProjectObjectsWithNoCategory() :
                projectManager.getProjectObjectsFromProjectCategory(projectcat.getLong("id"));
        for (Project project : projects)
        {
            DetailedProjectData projectBean = getProjectData(user, project, showStats);
            if (projectBean != null)
            {
                projectData.add(projectBean);
            }
        }
        if (!projects.isEmpty())
        {
            category = new Category(projectcat == null ? null : projectcat.getString("id"), projectcat == null ? null : projectcat.getString("name"), projectData);
        }
        return category;
    }

    private DetailedProjectData getProjectData(User user, Project project, boolean showStats)
    {
        DetailedProjectData projectBean = null;
        if (permissionManager.hasPermission(Permissions.BROWSE, project, user))
        {
            projectBean = getProjectDataNoPermissionCheck(user, project, showStats);
        }
        return projectBean;
    }

    private DetailedProjectData getProjectDataNoPermissionCheck(User user, Project project, boolean showStats)
    {
        List<OpenIssuesData> openIssues = null;
        String projectDescription = null;
        boolean showOpen = false;
        boolean showDescription = false;
        if (showStats && isPriorityFieldVisible(project))
        {
            openIssues = getOpenIssuesData(project);
            showOpen = true;
        }
        if (showStats && StringUtils.isNotBlank(project.getDescription()))
        {
            projectDescription = projectDescriptionRenderer.getViewHtml(project.getDescription());
            showDescription = true;
        }

        final com.atlassian.crowd.embedded.api.User lead = project.getLead();
        return new DetailedProjectData(project.getId(), project.getKey(), project.getName(), project.getLeadUserName(), lead == null ? null : lead.getDisplayName(), openIssues, projectDescription, showOpen, showDescription,
                project.getAvatar());
    }

    private List<OptionData> getReportData(Project project)
    {
        final List<OptionData> tabPanels = new ArrayList<OptionData>();
        for (ProjectTabPanelModuleDescriptor tab : getProjectTabPanels(project))
        {
            tabPanels.add(new OptionData(tab.getLabel(), null, "/browse/" + project.getKey() + "?selectedTab=" + tab.getCompleteKey()));
        }
        return tabPanels;
    }


    private List<String> splitProjectAndCategoryIds(final String projectsOrCategories)
    {
        if (projectsOrCategories == null)
        {
            return Collections.emptyList();
        }
        return asList(projectsOrCategories.split("\\|"));
    }

    private List<OptionData> getFilterData(final Project project, final User user)
    {
        DefaultVelocityRequestContextFactory.cacheVelocityRequestContext("", null);
        final ProjectHelper jiraHelper = getProjectHelper(user, project);

        final List<SimpleLink> simpleLinks = simpleLinkManager.getLinksForSection("system.preset.filters", user, jiraHelper);

        final List<OptionData> filterData = new ArrayList<OptionData>(simpleLinks.size());
        for (SimpleLink simpleLink : simpleLinks)
        {
            filterData.add(new OptionData(simpleLink.getLabel(), simpleLink.getTitle(), simpleLink.getUrl()));
        }
        return filterData;
    }

    private List<ProjectTabPanelModuleDescriptor> getProjectTabPanels(final Project project)
    {
        final List<ProjectTabPanelModuleDescriptor> projectTabPanels = new ArrayList<ProjectTabPanelModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(ProjectTabPanelModuleDescriptor.class));

        final BrowseProjectContext ctx = new BrowseProjectContext(authenticationContext.getLoggedInUser(), project);
        for (final Iterator<ProjectTabPanelModuleDescriptor> iterator = projectTabPanels.iterator(); iterator.hasNext();)
        {
            final ProjectTabPanelModuleDescriptor descriptor = iterator.next();
            if (!descriptor.getModule().showPanel(ctx))
            {
                iterator.remove();
            }
        }
        Collections.sort(projectTabPanels, ModuleDescriptorComparator.COMPARATOR);
        return projectTabPanels;
    }

    private List<OpenIssuesData> getOpenIssuesData(final Project project)
    {
        final StatisticAccessorBean sab = createStatisticsAccessorBean(project);
        try
        {
            final StatisticMapWrapper<Priority, Integer> stats = sab.getAllFilterBy("priorities");
            final List<OpenIssuesData> openIssues = new ArrayList<OpenIssuesData>();
            I18nBean i18nBean = new I18nBean(authenticationContext.getLoggedInUser());
            for (Priority priority : stats.keySet())
            {
                JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
                final JqlClauseBuilder clauseBuilder = builder.where().project(project.getKey()).and().resolution().isEmpty();

                if (priority == null)
                {
                    clauseBuilder.and().priority().isEmpty();
                }
                else
                {
                    clauseBuilder.and().priority(priority.getId());
                }
                String priorityId = priority == null ? "-1" : priority.getId();
                openIssues.add(new OpenIssuesData(
                        priority == null ? "none" : priority.getStatusColor(),
                        stats.getPercentage(priority),
                        constantsManager.getPriorityName(priorityId) + " - " +
                                stats.getPercentage(priority) + "% (" + stats.get(priority) + " " +
                                i18nBean.getText("gadget.common.issues") + ")",
                        priorityId, searchService.getQueryString(authenticationContext.getLoggedInUser(), clauseBuilder.buildQuery())
                ));
            }
            return openIssues;
        }
        catch (final SearchException e)
        {
            return null;
        }
    }

    // protected to be overidden for testing
    private StatisticAccessorBean createStatisticsAccessorBean(Project project)
    {
        return new StatisticAccessorBean(authenticationContext.getLoggedInUser(), project.getId());
    }

    private ProjectsAndCategoriesHelper getHelper()
    {
        return helper;
    }

    // protected so it can be overriden for testing
    private Boolean isPriorityFieldVisible(final Project project)
    {
        return fieldVisibilityManager.isFieldVisible(project.getId(), IssueFieldConstants.PRIORITY, FieldVisibilityManager.ALL_ISSUE_TYPES);
    }

    /**
     * This returns a hackish project helper. I'm sorry.
     *
     * @return A hacked up project helper.
     */
    public ProjectHelper getProjectHelper(final User user, final Project project)
    {
        /**
         * This one ultimately gets used to generate URLs for the filter + reports links in the Project gadget.
         * The JS in the project gadget will prepend the instance's baseUrl, but the thing that generates the URLs
         * will attempt to tack the contextPath on to the front, which results in a double context path.
         * This overriding of the getRequest method hacks around that by returning an empty context path.
         * @see {@link com.atlassian.plugin.web.model.DefaultWebLink#getDisplayableUrl}
         * @see {@linkplain jira-components/jira-plugins/jira-gadgets-plugin/src/main/resources/gadgets/project-gadget.xml} {@literal var linkNode = AJS.$(...)}
         */
        HttpServletRequest request = new HttpServletRequestWrapper(ExecutingHttpRequest.get())
        {
            @Override
            public String getContextPath()
            {
                return "";
            }
        };
        return new ProjectHelper(request, new BrowseProjectContext(user, project));
    }

    ///CLOVER:OFF

    @XmlRootElement
    public static class Projects
    {
        @XmlElement
        private List<Category> categories;

        @XmlElement
        private boolean isAdmin;

        @XmlElement
        private boolean doAnyProjectsExist;

        public Projects()
        {
        }

        public Projects(List<Category> categories, boolean isAdmin, boolean doAnyProjectsExist)
        {
            this.categories = categories;
            this.isAdmin = isAdmin;
            this.doAnyProjectsExist = doAnyProjectsExist;
        }
    }


    @XmlRootElement
    public static class Category
    {

        @XmlElement
        private String id;

        @XmlElement
        private String name;

        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
        @XmlElement
        private List<DetailedProjectData> projects;


        public Category()
        {

        }

        public Category(String id, String name, List<DetailedProjectData> projects)
        {
            this.id = id;
            this.name = name;
            this.projects = projects;
        }
    }

    @XmlRootElement
    public static class DetailedProjectData
    {
        @XmlElement
        long id;
        @XmlElement
        private String key;
        @XmlElement
        private String name;
        @XmlElement
        private boolean showOpen;
        @XmlElement
        private boolean showDescription;
        @XmlElement
        private String leadUserName;
        @XmlElement
        private String leadFullName;
        @XmlElement
        private String projectDescription;
        @XmlElement
        private List<OpenIssuesData> openIssues;
        @XmlElement
        private boolean hasOpenIssuesStats;
        @XmlElement
        private long avatarId;

        DetailedProjectData()
        {
        }

        DetailedProjectData(final long id, String key, String name, String leadUserName, String leadFullName, List<OpenIssuesData> openIssues, String projectDescription, boolean showOpen, boolean showDescription, Avatar avatar)
        {
            this.id = id;
            this.key = key;
            this.name = name;
            this.leadUserName = leadUserName;
            this.leadFullName = leadFullName;
            this.projectDescription = projectDescription;
            this.showOpen = showOpen;
            this.showDescription = showDescription;
            this.hasOpenIssuesStats = openIssues != null;
            this.openIssues = openIssues;
            this.avatarId = avatar.getId();
        }
    }


    @XmlRootElement
    public static class OptionDataList
    {
        @XmlElement
        private List<OptionData> options;

        public OptionDataList()
        {
        }

        public OptionDataList(List<OptionData> options)
        {
            this.options = options;
        }

        public List<OptionData> getOptions()
        {
            return options;
        }
    }

    @XmlRootElement
    public static class OptionData
    {
        @XmlElement
        private String title;
        @XmlElement
        private String description;
        @XmlElement
        private String key;

        public OptionData()
        {
        }

        public OptionData(String title, String description, String key)
        {
            this.title = title;
            this.description = description;
            this.key = key;
        }

        public String getKey()
        {
            return key;
        }
    }

    @XmlRootElement
    public static class OpenIssuesData
    {
        @XmlElement
        private String colour;
        @XmlElement
        private int width;
        @XmlElement
        private String altText;
        @XmlElement
        private String priority;
        @XmlElement
        private String url;

        public OpenIssuesData()
        {
        }

        public OpenIssuesData(String colour, int width, String altText, String priority, String url)
        {
            this.colour = colour;
            this.width = width;
            this.altText = altText;
            this.priority = priority;
            this.url = url;
        }
    }

}
