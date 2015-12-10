package com.atlassian.jira.rest.v2.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.issue.fields.ColumnService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.search.ColumnOptions;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.UrlValidator;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * REST resource for changing the JIRA system settings
 *
 * @since v5.0.3
 */
@Path ("settings")
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
public class SettingsResource
{
    private final ApplicationProperties applicationProperties;
    private final ColumnService columnService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final GlobalPermissionManager globalPermissionManager;
    private final I18nHelper i18n;

    public SettingsResource(
            ApplicationProperties applicationProperties, ColumnService columnService,
            JiraAuthenticationContext jiraAuthenticationContext, GlobalPermissionManager globalPermissionManager,
            I18nHelper i18n)
    {
        this.applicationProperties = applicationProperties;
        this.columnService = columnService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.globalPermissionManager = globalPermissionManager;
        this.i18n = i18n;
    }

    /**
     * Returns the default system columns for issue navigator. Admin permission will be required.
     *
     * @since v6.1
     * @return column configuration
     *
     * @response.representation.200.qname
     *      columns
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of columns for configured for the given user
     *
     * @response.representation.403.doc
     *      Returned if the user does not have admin permission
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the column configuration.
     */
    @GET
    @Path ("columns")
    public Response getIssueNavigatorDefaultColumns()
    {
        final ApplicationUser currentUser = jiraAuthenticationContext.getUser();

        final ServiceOutcome<ColumnLayout> outcome = columnService.getDefaultColumnLayout(currentUser);
        if (outcome.isValid())
        {
            final List<ColumnLayoutItem> columnLayoutItems = outcome.getReturnedValue().getColumnLayoutItems();
            return Response.ok(ColumnOptions.toColumnOptions(columnLayoutItems)).cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Sets the default system columns for issue navigator. Admin permission will be required.
     *
     * @since v6.1
     * @param fields list of column ids
     * @return javax.ws.rs.core.Response containing basic message and http return code
     *
     * @response.representation.200.doc
     *      Returned when the columns is saved successfully
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the column configuration.
     */
    @PUT
    @Path ("columns")
    @Consumes (MediaType.WILDCARD)
    public Response setIssueNavigatorDefaultColumns(@FormParam("columns") List<String> fields)
    {
        final ApplicationUser currentUser = jiraAuthenticationContext.getUser();

        final ServiceResult outcome = columnService.setDefaultColumns(currentUser, fields);
        if (outcome.isValid())
        {
            return Response.ok().cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Sets the base URL that is configured for this JIRA instance.
     *
     * @param baseURL a String containing the base URL that will be set for this JIRA instance
     *
     * @request.representation.mediaType application/json
     *
     * @request.representation.doc
     *      A string containing the base URL that will be set for this JIRA instance.
     *
     * @request.representation.example
     *      http://jira.atlassian.com/
     *
     * @response.representation.400.doc
     *      Returned if the specified base URL is not valid.
     *
     */
    @PUT
    @AnonymousAllowed
    @Path ("baseUrl")
    public void setBaseURL(String baseURL)
    {
        if (!isSysAdmin(jiraAuthenticationContext.getLoggedInUser()))
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.settings.baseurl.permission.denied")));
        }

        if (!UrlValidator.isValid(baseURL))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("rest.settings.baseurl.invalid")));
        }

        applicationProperties.setString(APKeys.JIRA_BASEURL, baseURL);
    }

    private boolean isSysAdmin(User currentUser)
    {
        return currentUser != null && globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser);
    }
}
