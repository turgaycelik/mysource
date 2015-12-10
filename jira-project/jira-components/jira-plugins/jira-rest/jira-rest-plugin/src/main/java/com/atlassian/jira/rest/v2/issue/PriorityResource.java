package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.PriorityJsonBean;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 4.2
 */
@Path ("priority")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class PriorityResource
{
    private final ConstantsManager constantsManager;
    private final I18nHelper i18n;
    private final JiraBaseUrls baseUrls;

    public PriorityResource(final ConstantsManager constantsManager, I18nHelper i18n, JiraBaseUrls baseUrls)
    {
        this.constantsManager = constantsManager;
        this.baseUrls = baseUrls;
        this.i18n = i18n;
    }

    /**
     * Returns a list of all issue priorities.
     *
     * @param uriInfo a UriInfo
     *
     * @return a list of Jira Priorities
     *
     * @response.representation.200.qname
     *      list of priorities
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the priorities exists and the user has permission to view it. Contains a full representation of
     *      the priority in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.PriorityBean#DOC_EXAMPLE_LIST}
     *
     */
    @GET
    public Response getPriorities(@Context UriInfo uriInfo)
    {
        Collection<Priority> priorities = constantsManager.getPriorityObjects();

        final List<PriorityJsonBean> beans = new ArrayList<PriorityJsonBean>();
        for (Priority priority : priorities)
        {
            beans.add(PriorityJsonBean.fullBean(priority, baseUrls));
        }
        return Response.ok(beans).cacheControl(never()).build();
    }

    /**
     * Returns an issue priority.
     *
     * @param id a String containing the priority id
     *
     * @return a response containing the requested issue priority
     *
     * @response.representation.200.qname
     *      issuePriority
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the issue priority exists and is visible by the calling user. Contains a full representation of
     *      the issue priority in JSON.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.PriorityBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the issue priority does not exist or is not visible to the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getPriority(@PathParam ("id") final String id)
    {
        final Priority priority = constantsManager.getPriorityObject(id);
        if (priority == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.priority.error.not.found", id)));
        }

        return Response.ok(PriorityJsonBean.fullBean(priority, baseUrls)).cacheControl(never()).build();
    }
}