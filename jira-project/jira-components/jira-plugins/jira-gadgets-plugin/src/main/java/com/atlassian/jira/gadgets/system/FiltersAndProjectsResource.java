package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.filter.FilterViewHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to retrieve the users favourite filters
 *
 * @since v4.0
 */
@Path ("/filtersAndProjects")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class FiltersAndProjectsResource
{
    private JiraAuthenticationContext authenticationContext;
    private SearchRequestService searchRequestService;
    private FilterViewHelper filterHelper;
    private final PermissionManager permissionManager;
    private I18nHelper.BeanFactory i18nFactory;

    public FiltersAndProjectsResource(final JiraAuthenticationContext authenticationContext, final SearchRequestService searchRequestService,
            final ShareTypeFactory shareTypeFactory, PermissionManager permissionManager,
            I18nHelper.BeanFactory i18nFactory)
    {
        this.authenticationContext = authenticationContext;
        this.searchRequestService = searchRequestService;
        this.permissionManager = permissionManager;
        this.i18nFactory = i18nFactory;
        filterHelper = new FilterViewHelper(shareTypeFactory, authenticationContext, "dummy", "dummy", searchRequestService);
    }

    @GET
    public Response getFilters(@DefaultValue ("true") @QueryParam ("showFilters") boolean showFilters,
            @DefaultValue ("true") @QueryParam ("showProjects") boolean showProjects)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final I18nHelper i18nHelper = i18nFactory.getInstance(user);

        final Collection<GroupWrapper> options = new ArrayList<GroupWrapper>();

        if (showFilters)
        {
            final Collection<SearchRequest> searchRequests;
            if (user != null)
            {
                searchRequests = searchRequestService.getFavouriteFilters(user);
            }
            else
            {
                final ErrorCollection errors = new SimpleErrorCollection();
                final JiraServiceContext ctx = new JiraServiceContextImpl(user, errors);

                searchRequests = filterHelper.getPopularFilters(ctx);
            }
            final Collection<Option> filters = new ArrayList<Option>(searchRequests.size());

            for (SearchRequest searchRequest : searchRequests)
            {
                filters.add(new Option(searchRequest.getName(), "filter-" + searchRequest.getId().toString()));
            }
            final OptionGroup filterGroup = new OptionGroup(i18nHelper.getText("common.concepts.filters"), filters);

            if (!showProjects)
            {
                return Response.ok(filterGroup).cacheControl(NO_CACHE).build();
            }

            options.add(new GroupWrapper(filterGroup));
        }

        if (showProjects)
        {
            final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, ApplicationUsers.toDirectoryUser(user));
            final Collection<Option> projectOptions = new ArrayList<Option>(projects.size());

            for (Project project : projects)
            {
                projectOptions.add(new Option(project.getName(), "project-" + project.getId()));
            }
            final OptionGroup projectGroup = new OptionGroup(i18nHelper.getText("common.concepts.projects"), projectOptions);
            if (!showFilters)
            {
                return Response.ok(projectGroup).cacheControl(NO_CACHE).build();
            }

            options.add(new GroupWrapper(projectGroup));
        }
        return Response.ok(new OptionList(options)).cacheControl(NO_CACHE).build();
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class OptionList
    {
        @XmlElement
        Collection<GroupWrapper> options;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private OptionList()
        {}

        OptionList(Collection<GroupWrapper> options)
        {
            this.options = options;
        }

        public Collection<GroupWrapper> getOptions()
        {
            return options;
        }
    }

    @XmlRootElement
    public static class GroupWrapper
    {
        @XmlElement
        private OptionGroup group;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private GroupWrapper()
        {}

        GroupWrapper(OptionGroup group)
        {

            this.group = group;
        }

        public OptionGroup getGroup()
        {
            return group;
        }
    }

    @XmlRootElement
    public static class OptionGroup
    {
        @XmlElement
        private String label;
        @XmlElement
        private Collection<Option> options;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private OptionGroup()
        { }

        OptionGroup(String label, Collection<Option> options)
        {
            this.label = label;
            this.options = options;
        }

        public String getLabel()
        {
            return label;
        }

        public Collection<Option> getOptions()
        {
            return options;
        }
    }

    @XmlRootElement
    public static class Option
    {
        @XmlElement
        private String label;
        @XmlElement
        private String value;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private Option()
        {}

        Option(String label, String value)
        {
            this.label = label;
            this.value = value;
        }

        public String getLabel()
        {
            return label;
        }

        public String getValue()
        {
            return value;
        }
    }
    ///CLOVER:ON
}
