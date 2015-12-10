package com.atlassian.jira.chartpopup;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardService;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.dashboard.spi.GadgetStateFactory;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.View;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.chartpopup.model.Gadget;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderByImpl;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static com.atlassian.jira.template.TemplateSources.file;

/**
 * REST endpoint to retrieve the URL to display a legacy portlet.
 *
 * @since v4.0
 */
@Path ("/chart")
@Produces (MediaType.TEXT_HTML)
public class ChartPopupResource
{
    private static final Logger log = Logger.getLogger(ChartPopupResource.class);

    private static final String TEMPLATE_DIRECTORY_PATH = "templates/";
    private static final String PREF_PROJECT_OR_FILTER_ID = "projectOrFilterId";
    private static final String PREF_FILTER_ID = "filterId";
    private static final String PREF_JQL = "jql";
    private static final String PREF_IS_POPUP = "isPopup";
    private static final String PREF_IS_CONFIGURED = "isConfigured";

    private static final String FILTER_PREFIX = "filter-";
    private static final String JQL_PREFIX = "jql-";

    private final JiraAuthenticationContext authenticationContext;
    private final GadgetViewFactory gadgetViewFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final PortalPageService portalPageService;
    private final SearchRequestService searchRequestService;
    private final SearchService searchService;
    private final DashboardService dashboardService;
    private final GadgetStateFactory gadgetStateFactory;
    private final UserUtil userUtil;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private final VelocityTemplatingEngine templatingEngine;

    public ChartPopupResource(final JiraAuthenticationContext authenticationContext, final GadgetViewFactory gadgetViewFactory,
            final VelocityRequestContextFactory velocityRequestContextFactory, final PortalPageService portalPageService,
            final SearchRequestService searchRequestService, final SearchService searchService,
            final DashboardService dashboardService, final GadgetStateFactory gadgetStateFactory,
            final UserUtil userUtil, final GadgetRequestContextFactory gadgetRequestContextFactory,
            final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory, final VelocityTemplatingEngine templatingEngine)
    {
        this.authenticationContext = authenticationContext;
        this.gadgetViewFactory = gadgetViewFactory;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.portalPageService = portalPageService;
        this.searchRequestService = searchRequestService;
        this.searchService = searchService;
        this.dashboardService = dashboardService;
        this.gadgetStateFactory = gadgetStateFactory;
        this.userUtil = userUtil;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
        this.templatingEngine = templatingEngine;
    }

    /**
     * Renders the contents for a particular chart needed to display this chart in the issue navigator 'Charts' popup.
     *
     * @param gadgetId The id of the gadget being rendered.  Really just a bogus identifier associated with the popup
     * panel being displayed.
     * @param gadgetUri The gadget spec URI for the chart being displayed.
     * @param filterId The saved filter id to use for this chart. May be null if this is an anonymous filter
     * @param jql The query entered by the user. May be null if the user was using a saved filter
     * @param request The httpServletRequest
     * @return html with the chart gadget iframe as well as the save gadget to dashboard form
     */
    @GET
    @Path ("/render")
    public Response getChartContents(@QueryParam ("id") Long gadgetId, @QueryParam ("gadgetUri") String gadgetUri,
            @QueryParam ("filterId") Long filterId, @QueryParam (PREF_JQL) String jql, @Context HttpServletRequest request)
    {
        Assertions.notNull("gadgetId", gadgetId);
        Assertions.notBlank("gadgetUri", gadgetUri);

        final MapBuilder<String, String> prefsBuilder = MapBuilder.newBuilder();

        prefsBuilder.add(PREF_IS_POPUP, Boolean.TRUE.toString());
        prefsBuilder.add(PREF_IS_CONFIGURED, Boolean.TRUE.toString());

        if (filterId != null)
        {
            prefsBuilder.add(PREF_PROJECT_OR_FILTER_ID, FILTER_PREFIX + filterId);
            // the Filter Results gadget expects the filterId as a raw number
            prefsBuilder.add(PREF_FILTER_ID, filterId.toString());
        }
        else
        {
            final String filterJql = JQL_PREFIX + jql;
            prefsBuilder.add(PREF_FILTER_ID, filterJql);
            prefsBuilder.add(PREF_PROJECT_OR_FILTER_ID, filterJql);
        }

        final Map<String, String> prefs = prefsBuilder.toMutableMap();

        final GadgetState gadget = GadgetState.gadget(GadgetId.valueOf(gadgetId.toString())).specUri(URI.create(gadgetUri)).userPrefs(prefs).build();
        final Writer out = new StringWriter();
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Writer gadgetWriter = new OutputStreamWriter(baos);
            final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);
            gadgetViewFactory.createGadgetView(gadget, ModuleId.valueOf(gadgetId), View.DEFAULT, requestContext).writeTo(gadgetWriter);
            gadgetWriter.flush();

            final MapBuilder<String, Object> contextBuilder = MapBuilder.<String, Object>newBuilder().add("gadgetHtml", baos.toString());
            out.write(renderTemplate("chartpopup.vm", contextBuilder.toMap()));
        }
        catch (IOException e)
        {
            log.error("Error rendering gadget '" + gadgetUri + "'", e);
            return Response.serverError().entity("Error rendering gadget '" + gadgetUri + "'").cacheControl(NO_CACHE).build();
        }

        return Response.ok(out.toString()).cacheControl(NO_CACHE).build();
    }

    /**
     * Returns the save to dashboard form.
     *
     * @param filterId FilterId that was used to run the query
     * @param jql JQL that was used to run the query
     * @return A blob of HTML that is the save chart to dashboard form.
     */
    @GET
    @Path ("/add")
    public Response getGadgetToDashboardForm(@QueryParam ("filterId") Long filterId, @QueryParam (PREF_JQL) String jql)
    {
        final Writer out = new StringWriter();
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final MapBuilder<String, Object> contextBuilder = MapBuilder.<String, Object>newBuilder().
                add("portals", portalPageService.getOwnedPortalPages(authenticationContext.getLoggedInUser())).
                add("baseurl", requestContext.getBaseUrl()).
                add("textutils", new TextUtils()).
                add("i18n", authenticationContext.getI18nHelper()).
                add("displayParameters", MapBuilder.singletonMap("theme", "aui"));
        final Map<String, Object> context = filterId != null ? contextBuilder.add("filterId", FILTER_PREFIX + filterId).toMap() : contextBuilder.add(PREF_JQL, jql).toMap();
        try
        {
            out.write(renderTemplate("savetodashboardform.vm", context));
        }
        catch (IOException e)
        {
            log.error("Error rendering save to dashboard form.", e);
            return Response.serverError().entity(authenticationContext.getI18nHelper().getText("gadget.common.error.save.dashboard")).cacheControl(NO_CACHE).build();
        }
        return Response.ok(out.toString()).cacheControl(NO_CACHE).build();
    }

    /**
     * Saves the gadget specified as a parameter to a particular dashboard.
     *
     * @param gadget The gadget to add to a dashboard.
     * @return 200 ok response with the URL to view the dashboard or and error response otherwise
     */
    @POST
    @Path ("/add")
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    public Response addGadgetToDashboard(final Gadget gadget)
    {
        Assertions.notNull("gadget", gadget);

        final ApplicationUser remoteUser = authenticationContext.getUser();
        final JiraServiceContext context = new JiraServiceContextImpl(remoteUser);
        Long filterId = gadget.getFilterId();
        if (filterId == null)
        {
            final SearchRequest searchRequest;
            //remove whitespace from filtername so no 'duplicates' can be created (JRA-18938)
            final String filterName = StringUtils.trim(gadget.getFilterName());
            //create a new filter
            if (StringUtils.isNotBlank(gadget.getJql()))
            {
                final SearchService.ParseResult parseResult = searchService.parseQuery(ApplicationUsers.toDirectoryUser(remoteUser), gadget.getJql());
                searchRequest = new SearchRequest(parseResult.getQuery(), remoteUser, filterName, "");
            }
            else
            {
                searchRequest = new SearchRequest(new QueryImpl(null, new OrderByImpl(), null), remoteUser, filterName, "");
            }
            searchRequestService.validateFilterForCreate(context, searchRequest);
            if (context.getErrorCollection().hasAnyErrors())
            {
                return Response.status(Response.Status.BAD_REQUEST).entity(convertErrorCollectionToJson(context.getErrorCollection())).cacheControl(NO_CACHE).build();
            }
            final SearchRequest request = searchRequestService.createFilter(context, searchRequest);
            filterId = request.getId();

            //JRA-18909: Set the new filter to be the 'current' filter.
            final VelocityRequestSession session = velocityRequestContextFactory.getJiraVelocityRequestContext().getSession();
            sessionSearchObjectManagerFactory.createSearchRequestManager(session).setCurrentObject(request);
        }

        final URI gadgetUri = gadget.getGadgetUri();
        final Map<String, String> userPrefMap = gadget.getUserPrefs();
        userPrefMap.put(PREF_PROJECT_OR_FILTER_ID, FILTER_PREFIX + filterId);
        // the Filter Results gadget expects the filterId as a raw number
        userPrefMap.put(PREF_FILTER_ID, filterId.toString());
        userPrefMap.put(PREF_IS_CONFIGURED, Boolean.TRUE.toString());
        final GadgetState newGadget = GadgetState.gadget(gadgetStateFactory.createGadgetState(gadgetUri)).userPrefs(userPrefMap).build();

        return addGadgetToDashboard(newGadget, gadget.getPortalId());
    }

    private Response addGadgetToDashboard(final GadgetState newGadget, Long portalId)
    {
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        final String remoteUsername = authenticationContext.getLoggedInUser() == null ? null : authenticationContext.getLoggedInUser().getName();
        final ErrorCollection errors = new SimpleErrorCollection();
        //user doesn't have a personal dashboard yet.  Go and create a clone of the default dashboard!
        if (portalId == null)
        {
            portalId = createDefaultDashboardClone();
            if(portalId == null)
            {
                errors.addErrorMessage(i18nHelper.getText("portletSearchRequestView.error.creating.dashboard.clone"));
                return Response.status(Response.Status.BAD_REQUEST).entity(convertErrorCollectionToJson(errors)).cacheControl(NO_CACHE).build();
            }
        }
        DashboardState dashboard;
        try
        {
            dashboard = dashboardService.get(DashboardId.valueOf(portalId.toString()), remoteUsername);
        }
        catch (PermissionException e)
        {
            errors.addErrorMessage(i18nHelper.getText("portletSearchRequestView.dashboard.no.permission.view", portalId));
            return Response.status(Response.Status.BAD_REQUEST).entity(convertErrorCollectionToJson(errors)).cacheControl(NO_CACHE).build();
        }
        if (dashboard != null)
        {
            try
            {
                final DashboardState updatedDashboard = dashboard.prependGadgetToColumn(newGadget, DashboardState.ColumnIndex.ZERO);
                dashboardService.save(updatedDashboard, remoteUsername);
                final String contextPath = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
                return Response.ok(contextPath + "/secure/Dashboard.jspa?selectPageId=" + updatedDashboard.getId()).cacheControl(NO_CACHE).build();
            }
            catch (PermissionException e)
            {
                errors.addErrorMessage(i18nHelper.getText("portletSearchRequestView.dashboard.no.permission", portalId));
                return Response.status(Response.Status.BAD_REQUEST).entity(convertErrorCollectionToJson(errors)).cacheControl(NO_CACHE).build();
            }
        }
        else
        {
            errors.addErrorMessage(i18nHelper.getText("portletSearchRequestView.dashboard.not.found", portalId));
            return Response.status(Response.Status.BAD_REQUEST).entity(convertErrorCollectionToJson(errors)).cacheControl(NO_CACHE).build();
        }
    }

    /**
     * Copies the default dashboard for the user provided and returns the ID of the cloned page.
     *
     * @return the id of the cloned page. Null on errors.
     */
    private Long createDefaultDashboardClone()
    {
        final PortalPage systemDefaultPage = portalPageService.getSystemDefaultPortalPage();
        final Long systemDefaultId = systemDefaultPage.getId();

        String fullName = "";
        final ApplicationUser user = authenticationContext.getUser();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        if (user != null)
        {
            fullName = userUtil.getDisplayableNameSafely(user);
            fullName = fullName == null ? "" : fullName;
        }
        String name = i18nHelper.getText("configureportal.clone.page.name", fullName);
        String desc = systemDefaultPage.getDescription();
        if (StringUtils.isBlank(desc))
        {
            desc = i18nHelper.getText("configureportal.clone.page.desc", systemDefaultPage.getName());
        }

        // When cloning the System Dashboard, always ignore the user's preference for sharing and set the permissions
        // to private by default. This is to stop an unnecessary abundance of System Dashboard clones which are globally shared.
        PortalPage newPage = PortalPage.name(name).description(desc).owner(user).permissions(SharedEntity.SharePermissions.PRIVATE).build();

        final JiraServiceContext serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        if (portalPageService.validateForCreatePortalPageByClone(serviceContext, newPage, systemDefaultId))
        {
            newPage = portalPageService.createPortalPageByClone(serviceContext, newPage, systemDefaultId, true);
            return newPage.getId();
        }
        return null;
    }

    private String convertErrorCollectionToJson(final ErrorCollection errorCollection)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("{generic:[");
        @SuppressWarnings ("unchecked")
        final Collection<String> errorMessages = errorCollection.getErrorMessages();
        for (Iterator<String> it = errorMessages.iterator(); it.hasNext();)
        {
            String errorMessage = it.next();
            ret.append("\"").append(errorMessage).append("\"");
            if (it.hasNext())
            {
                ret.append(",");
            }
        }
        ret.append("], fields:[");
        @SuppressWarnings ("unchecked")
        final Map<String, String> errors = errorCollection.getErrors();
        for (Iterator<Map.Entry<String, String>> it = errors.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, String> errorEntry = it.next();
            ret.append("{field:\"").append(errorEntry.getKey()).append("\",error:\"").append(errorEntry.getValue()).append("\"}");
            if (it.hasNext())
            {
                ret.append(",");
            }
        }
        ret.append("]}");

        return ret.toString();
    }

    private String renderTemplate(final String template, final Map<String, Object> velocityParams)
    {
        try
        {
            return templatingEngine.render(file(TEMPLATE_DIRECTORY_PATH + template)).applying(velocityParams).asHtml();
        }
        catch (final VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + TEMPLATE_DIRECTORY_PATH + "/" + template + "'.", e);
        }

        return "";
    }
}

