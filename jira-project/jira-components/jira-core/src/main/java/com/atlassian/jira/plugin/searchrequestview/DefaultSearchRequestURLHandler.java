package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.core.util.XMLUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.search.ClauseTooComplexSearchException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestFactory;
import com.atlassian.jira.issue.search.SearchRequestInfo;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.views.util.RssViewUtils;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.plugin.issueview.IssueViewFieldParams;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParamsHelper;
import com.atlassian.jira.plugin.searchrequestview.auth.Authorizer;
import com.atlassian.jira.plugin.searchrequestview.auth.Authorizer.Result;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.RequestParameterKeys;
import com.atlassian.jira.web.action.issue.IssueNavigatorConstants;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultSearchRequestURLHandler implements SearchRequestURLHandler
{
    private static final String SAMPLE_URL = "/sr/jira.issueviews:searchrequest-xml/10010/SearchRequest-10010.xml OR /sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?param1=abc&param2=xyz";
    private static final String XML_MODULE_NAME = "XML";
    private static final String VALIDATE_PARAM = "validateQuery";

    private final PluginAccessor pluginAccessor;
    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory i18n;
    private final Authorizer requestAuthorizer;
    private final SearchProvider searchProvider;
    private final IssueViewRequestParamsHelper issueViewRequestParamsHelper;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final SearchService searchService;
    private final JqlStringSupport jqlStringSupport;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final BuildUtilsInfo buildUtilsInfo;

    // NOTE: this needs to be injected with the SearchRequestManager once we have properly fixed JRA-10978. The
    // problem with injecting this object is that eventually one of its dependancies is a CustomFieldManager which
    // eagerly instantiates its custom field cache at object construction. This is bad since the plugin manager has
    // not yet had a chance to let all plugins register themselves.
    public DefaultSearchRequestURLHandler(
            final PluginAccessor pluginAccessor,
            final JiraAuthenticationContext authenticationContext,
            final ApplicationProperties applicationProperties,
            final I18nHelper.BeanFactory i18nBean,
            final Authorizer requestAuthorizer,
            final SearchProvider searchProvider,
            final IssueViewRequestParamsHelper issueViewRequestParamsHelper,
            final SearchService searchService,
            final JqlStringSupport jqlStringSupport,
            final BuildUtilsInfo buildUtilsInfo,
            final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.pluginAccessor = pluginAccessor;
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.i18n = i18nBean;
        this.requestAuthorizer = requestAuthorizer;
        this.searchProvider = searchProvider;
        this.issueViewRequestParamsHelper = issueViewRequestParamsHelper;
        this.searchService = searchService;
        this.jqlStringSupport = jqlStringSupport;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        velocityRequestContextFactory = new DefaultVelocityRequestContextFactory(applicationProperties);
    }

    public String getURLWithoutContextPath(final SearchRequestViewModuleDescriptor moduleDescriptor, final SearchRequestInfo searchRequestInfo)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("/sr/");
        sb.append(moduleDescriptor.getCompleteKey());

        final Long searchRequestId = searchRequestInfo.getId();
        // generate the link for saved search requests: (eg. /sr/jira.issueviews:searchrequest-xml/10010/SearchRequest-10010.xml)
        if ((searchRequestId != null) && !searchRequestInfo.isModified())
        {
            sb.append("/");
            sb.append(searchRequestId);
            sb.append("/SearchRequest-");
            sb.append(searchRequestId);
            sb.append(".");
            sb.append(moduleDescriptor.getFileExtension());
        }
        else
        // generate link for temporary search request (eg. /sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?param1=abc&param2=xyz)
        {
            sb.append("/temp/SearchRequest.");
            sb.append(moduleDescriptor.getFileExtension());
            sb.append("?");
            //JRA-13936: Strip leading '&' to avoid tomcat errors.
            String queryString = getQueryString(searchRequestInfo);
            if (queryString.indexOf("&") == 0)
            {
                queryString = queryString.substring(1);
            }
            sb.append(queryString);
        }
        return sb.toString();
    }

    private String getQueryString(final SearchRequestInfo searchRequest)
    {
        return searchService.getQueryString(authenticationContext.getLoggedInUser(), (searchRequest == null) ? new QueryImpl() : searchRequest.getQuery());
    }

    /**
     * A sample URL to be used for used for error messages
     *
     * @return A sample SearchRequest URL
     */
    private static String getSampleURL()
    {
        return SAMPLE_URL;
    }

    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        String pathInfo = request.getPathInfo();
        // JRA-15847: check for null path
        if (StringUtils.isBlank(pathInfo))
        {
            response.sendError(400, "Invalid path format. Path should be of format " + getSampleURL());
            return;
        }

        //trim any leading slash
        if (pathInfo.startsWith("/"))
        {
            pathInfo = pathInfo.substring(1);
        }

        final int firstSlashLocation = pathInfo.indexOf("/");
        if (firstSlashLocation == -1)
        {
            response.sendError(400, "Invalid path format. Path should be of format " + getSampleURL());
            return;
        }
        final String pluginKey = pathInfo.substring(0, firstSlashLocation);

        final int secondSlashLocation = pathInfo.indexOf("/", firstSlashLocation + 1);
        if (secondSlashLocation == -1)
        {
            response.sendError(400, "Invalid path format. Path should be of format " + getSampleURL());
            return;
        }

        SearchRequestViewModuleDescriptor moduleDescriptor = null;
        try
        {
            moduleDescriptor = (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule(pluginKey);
        }
        catch (IllegalArgumentException e)
        {
            // Invalid complete key specified - respond with 400
        }
        
        if (moduleDescriptor == null)
        {
            response.sendError(400, "Could not find any enabled plugin with key " + pluginKey);
            return;
        }
        final SearchRequestView view = moduleDescriptor.getSearchRequestView();

        SearchRequest searchRequest;

        final User loggedInUser = authenticationContext.getLoggedInUser();
        final String betweenSlashes = pathInfo.substring(firstSlashLocation + 1, secondSlashLocation);
        final IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(request.getParameterMap());
        if (betweenSlashes.startsWith("temp"))
        {
            // temporary search request
            @SuppressWarnings("unchecked")
            final Map<String, String[]> parameters = request.getParameterMap();

            final String jqlQueryString = getJqlQueryString(parameters);
            if (jqlQueryString != null)
            {
                final SearchService.ParseResult jqlQueryResult = searchService.parseQuery(loggedInUser, jqlQueryString);
                Query query = null;
                if (jqlQueryResult != null)
                {
                    if (!isValidateJql(request) || jqlQueryResult.isValid())
                    {
                        query = jqlQueryResult.getQuery();
                    }
                    else
                    {
                        response.sendError(400, jqlQueryResult.getErrors().getErrorMessages().iterator().next());
                        return;
                    }
                }

                // Lets build the request from the JQL
                searchRequest = getSearchRequestFactory().createFromQuery(null, loggedInUser, query);
            }
            else
            {
                // Lets build the request from the Parameters
                searchRequest = getSearchRequestFactory().createFromParameters(null, loggedInUser, new ActionParamsImpl(parameters));
            }

            if (searchRequest == null)
            {
                response.sendError(500, i18n.getInstance(authenticationContext.getLocale()).getText("search.request.invalid"));
                return;
            }

            if (issueViewFieldParams.isCustomViewRequested() && !issueViewFieldParams.isAnyFieldDefined())
            {
                response.sendError(400, "No valid field defined for issue custom view");
                return;
            }
        }
        else
        {
            try
            {
                final Long searchRequestId = new Long(betweenSlashes);
                searchRequest = getSearchRequestService().getFilter(new JiraServiceContextImpl(loggedInUser), searchRequestId);
            }
            catch (final NumberFormatException searchRequestIdIsNotALongValue)
            {
               searchRequest = null;
            }
            // The searchrequest was null.  This could have 3 different reasons:
            //  1.) The user is not logged in and the searchrequest requires authentication
            //  2.) The user is logged in but does not have permission to access the saved filter
            //  3.) The user is logged in but the search request doesn't exist.
            if (searchRequest == null)
            {
                //special case for the XML view to stay backwards compatible.  Need to output XML without any issues.
                if (XML_MODULE_NAME.equals(moduleDescriptor.getName()))
                {
                    //special case for the XML view to stay backwards compatible.  Need to output XML without any issues.
                    writeDummyXMLResponse(response);
                    return;
                }

                //not logged in.  Attempt to redirect to a URL with os_authType=basic set to force the client to login if
                //we are trying to view a SR with requiresAuthentication set to true.
                if (isAnonymous(loggedInUser) && moduleDescriptor.isBasicAuthenticationRequired())
                {
                    redirectToBasicAuthentication(request, response);
                    return;
                }

                //not logged in and trying to view something other than RSS: open up a permissionviolation page telling the user
                //that they need to be logged in.
                if (isAnonymous(loggedInUser))
                {
                    if (view instanceof SearchRequestViewAccessErrorHandler)
                    {
                        final SearchRequestViewAccessErrorHandler searchRequestViewAccessErrorHandler = ((SearchRequestViewAccessErrorHandler) view);
                        searchRequestViewAccessErrorHandler.writeErrorHeaders(new HttpRequestHeaders(response));
                        final BufferedWriter writer = new BufferedWriter(response.getWriter());
                        searchRequestViewAccessErrorHandler.writePermissionViolationError(writer);
                        writer.flush();
                    }
                    else
                    {
                        response.sendRedirect(RedirectUtils.getLoginUrl(request));
                    }
                    return;
                }

                //Logged in but the search request doesn't exist and we're trying to view a SR that requires Authentication.
                // This means that the searchrequest either doesn't exist, or the user doesn't have permissions to view it.
                if ((loggedInUser != null) && moduleDescriptor.isBasicAuthenticationRequired())
                {
                    //In the case of the newer RSS feeds, we return a 403 (Forbidden) error. (and pray to god that RSS readers handle it in some sensible way)
                    response.sendError(403, i18n.getInstance(authenticationContext.getLocale()).getText("search.request.invalid.permission"));
                    return;
                }

                //Finally, the Searchrequest is null even though were logged in. This means that the searchrequest either doesn't
                //exist, or the user doesn't have permissions to view it.
                if (view instanceof SearchRequestViewAccessErrorHandler)
                {
                    final SearchRequestViewAccessErrorHandler searchRequestViewAccessErrorHandler = ((SearchRequestViewAccessErrorHandler) view);
                    searchRequestViewAccessErrorHandler.writeErrorHeaders(new HttpRequestHeaders(response));
                    final BufferedWriter writer = new BufferedWriter(response.getWriter());
                    searchRequestViewAccessErrorHandler.writeSearchRequestDoesNotExistError(writer);
                    writer.flush();
                }
                else
                {
                    loadJsp(request, response, "/secure/views/searchrequesterror.jsp");
                }
                return;
            }
            // allow for the sort order to be overridden - JRA-15313
            final SearchSortUtil searchSortUtil = ComponentAccessor.getComponentOfType(SearchSortUtil.class);
            final OrderBy orderByFromParams = searchSortUtil.getOrderByClause(request.getParameterMap());

            // JRA-19531 - we were dropping the order by secified in the saved filter.
            final OrderBy orderBy;
            if (searchRequest.getQuery().getOrderByClause() != null)
            {
                // Lets merge the existing sorts in the query with any that were specified in the request params
                orderBy = new OrderByImpl(searchSortUtil.mergeSearchSorts(loggedInUser,
                        orderByFromParams.getSearchSorts(),
                        searchRequest.getQuery().getOrderByClause().getSearchSorts(),
                        Integer.MAX_VALUE));
            }
            else
            {
                // Just use the request params, which might be empty
                orderBy = orderByFromParams;
            }

            searchRequest.setQuery(new QueryImpl(searchRequest.getQuery().getWhereClause(), orderBy, searchRequest.getQuery().getQueryString()));
        }

        // validate the search request
        if (isValidateJql(request))
        {
			final MessageSet messageSet = searchService.validateQuery(loggedInUser, searchRequest.getQuery(), searchRequest.getId());
	        if (messageSet.hasAnyErrors())
    	    {
	            response.sendError(400, messageSet.getErrorMessages().iterator().next());
    	        return;
        	}
        }

        // calculate the search count
        long resultCount;
        try
        {
            resultCount = searchProvider.searchCount(searchRequest.getQuery(), loggedInUser);
        }
        catch (ClauseTooComplexSearchException e)
        {
            response.sendError(400, createTooComplexError(e.getClause()));
            return;
        }
        catch (final SearchException e)
        {
            throw new RuntimeException(e);
        }
        final Map<String, String> searchCountParam = new HashMap<String, String>();
        searchCountParam.put(Parameter.SEARCH_COUNT, String.valueOf(resultCount));

        final PagerFilter pagerFilter = getPagerFilter(request);
        final SearchRequestParams searchRequestParams = new SearchRequestParamsImpl(request.getSession(true), pagerFilter,
            searchCountParam, issueViewFieldParams, request.getHeader(BrowserUtils.USER_AGENT_HEADER));

        if (Boolean.valueOf(request.getParameter(RequestParameterKeys.JIRA_SEARCH_REQUEST_RETURN_MAX)))
        {
            searchRequestParams.setReturnMax(true);
            pagerFilter.setMax(Math.min(pagerFilter.getMax(), getMaxAllowed()));
        }

        // check that we allow them to run the SearchRequest (ie. its not too big).
        if (!moduleDescriptor.isExcludeFromLimitFilter())
        {
            final Result result = requestAuthorizer.isSearchRequestAuthorized(loggedInUser, searchRequest, searchRequestParams);
            if (!result.isOK())
            {
                response.sendError(403, result.getReason());
                return;
            }
        }

        if (!"true".equalsIgnoreCase(request.getParameter(Parameter.NO_HEADERS)))
        {
            response.setContentType(moduleDescriptor.getContentType() + ";charset=" + ComponentAccessor.getApplicationProperties().getEncoding());
            view.writeHeaders(searchRequest, new HttpRequestHeaders(response), searchRequestParams);
        }
        final Writer writer = new BufferedWriter(response.getWriter());
        try
        {
            view.writeSearchResults(searchRequest, searchRequestParams, writer);
        }
        catch (ClauseTooComplexSearchException e)
        {
            response.sendError(400, createTooComplexError(e.getClause()));
            return;
        }
        catch (final SearchException e)
        {
            throw new RuntimeException(e);
        }
        writer.flush();

    }

    protected int getMaxAllowed()
    {
        String defaultMax = applicationProperties.getDefaultBackedString(APKeys.JIRA_SEARCH_VIEWS_MAX_LIMIT);
        int retVal = Integer.MAX_VALUE;
        try
        {
            if (StringUtils.isNotBlank(defaultMax))
            {
                retVal = Integer.valueOf(defaultMax);
            }
        }
        catch (NumberFormatException e)
        {
            // Will only get called on startup after admin has manually added a BAD limit. Might as well fail fast!
            throw new IllegalArgumentException("Cannot get search result restriction limit for: '" + defaultMax + "' key=" + APKeys.JIRA_SEARCH_VIEWS_MAX_LIMIT);
        }
        return retVal;
    }

    protected SearchRequestService getSearchRequestService()
    {
        return ComponentAccessor.getComponent(SearchRequestService.class);
    }

    private String createTooComplexError(Clause clause)
            throws IOException
    {
        if (clause instanceof TerminalClause)
        {
            return i18n.getInstance(authenticationContext.getLocale()).getText("search.request.clause.too.complex", jqlStringSupport.generateJqlString(clause));
        }
        else
        {
            return i18n.getInstance(authenticationContext.getLocale()).getText("search.request.clause.query.complex");
        }
    }

    SearchRequestFactory getSearchRequestFactory()
    {
        return ComponentAccessor.getComponent(SearchRequestFactory.class);
    }

    private String getJqlQueryString(final Map<String, String[]> parameters)
    {
        final String[] jqlArr = parameters.get(IssueNavigatorConstants.JQL_QUERY_PARAMETER);
        if ((jqlArr != null) && (jqlArr.length == 1))
        {
            return jqlArr[0];
        }
        return null;
    }

    private void writeDummyXMLResponse(final HttpServletResponse response) throws IOException
    {
        response.setContentType("text/xml");

        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();

        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n");
        sb.append("<!--  RSS generated by JIRA ").append(buildUtilsInfo.getVersion()).append(" at ").append(dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).withSystemZone().format(new Date())).append(
            " -->\n");
        sb.append("<rss version=\"0.92\">\n");
        sb.append("<channel>\n");
        sb.append("\t<title>").append(XMLUtils.escape(applicationProperties.getString(APKeys.JIRA_TITLE))).append("</title>\n");
        sb.append("\t<link>").append(XMLUtils.escape(baseUrl)).append("</link>\n");
        sb.append("\t<description>").append(XMLUtils.escape(i18n.getInstance(authenticationContext.getLocale()).getText("issue.views.xml.some.desc"))).append("</description>\n");
        final String rssLocale = RssViewUtils.getRssLocale(authenticationContext.getLocale());
        if (rssLocale != null)
        {
            sb.append("\t<language>").append(rssLocale).append("</language>\n");
        }
        sb.append("<build-info>");
        sb.append("\t<version>").append(XMLUtils.escape(buildUtilsInfo.getVersion())).append("</version>");
        sb.append("\t<build-number>").append(XMLUtils.escape(buildUtilsInfo.getCurrentBuildNumber())).append("</build-number>");
        sb.append("\t<build-date>").append(XMLUtils.escape(new SimpleDateFormat("dd-MM-yyyy").format(buildUtilsInfo.getCurrentBuildDate()))).append("</build-date>");
        sb.append("</build-info>");
        sb.append("</channel>\n");
        sb.append("</rss>\n");

        final PrintWriter out = response.getWriter();
        out.write(sb.toString());
        out.flush();
    }

    private void loadJsp(final HttpServletRequest request, final HttpServletResponse response, final String jspPage) throws IOException
    {
        try
        {
            request.getRequestDispatcher(jspPage).forward(request, response);
        }
        catch (final ServletException e)
        {
            throw new RuntimeException("Could not load java server page", e);
        }
    }

    private void redirectToBasicAuthentication(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        final CharSequence requestUrl = request.getRequestURL();
        if ((requestUrl != null) && StringUtils.isNotEmpty(requestUrl.toString()))
        {
            String requestUrlString = requestUrl.toString();
            if (requestUrlString.indexOf('?') == -1)
            {
                requestUrlString = requestUrlString + "?os_authType=basic";
            }
            else
            {
                requestUrlString = requestUrlString + "&os_authType=basic";
            }
            response.setHeader("Location", requestUrlString);
            response.setStatus(301);
        }
        else
        {
            //could not find a request URL.  Should never happen but we'll handle it anyway
            response.sendError(500, i18n.getInstance(authenticationContext.getLocale()).getText("search.request.invalid.permission"));
        }
    }

    /**
     * Returns a PagerFilter initialised using URL parameters from the request
     *
     * @param request the request
     * @return the PagerFilter
     */
    protected PagerFilter getPagerFilter(final HttpServletRequest request)
    {
        //Max records returned defaults to unlimited unless specified as a URL param ('tempMax') - JRA-8899
        final String tempMax = request.getParameter(RequestParameterKeys.JIRA_SEARCH_REQUEST_VIEW_TEMP_MAX);
        PagerFilter pager = null;
        if (StringUtils.isNotEmpty(tempMax))
        {
            try
            {
                final int max = Integer.parseInt(tempMax);
                pager = new PagerFilter(max);
            }
            catch (final NumberFormatException e)
            {
                //invalid tempMax param, default to unlimited
            }
        }
        if (pager == null)
        {
            pager = PagerFilter.getUnlimitedFilter();
        }

        final String startParam = request.getParameter(RequestParameterKeys.JIRA_SEARCH_REQUEST_VIEW_PAGER_START);
        int start = 0;
        if (StringUtils.isNotEmpty(startParam))
        {
            try
            {
                start = Integer.parseInt(startParam);
            }
            catch (final NumberFormatException e)
            {
                // Lets just start at 0
            }
        }
        pager.setStart(start);
        return pager;
    }

    /**
     * Whether we should validate the query before running it. Defaults to true if <code>{@value #VALIDATE_PARAM}</code>
     * is not in the query params.
     *
     *
     * @param request the HttpServletRequest
     * @return a boolean indicating whether to validate the JQL
     */
    private boolean isValidateJql(HttpServletRequest request)
    {
        @SuppressWarnings("unchecked")
        Map<String, String[]> requestParameters = request.getParameterMap();
        if (requestParameters.containsKey(VALIDATE_PARAM))
        {
            String[] validateParams = requestParameters.get(VALIDATE_PARAM);
            if (validateParams != null && validateParams.length > 0)
            {
                // we don't support multiple values for validate. just take the first one.
                return Boolean.valueOf(validateParams[0]);
            }
        }

        // default to true
        return true;
    }
}
