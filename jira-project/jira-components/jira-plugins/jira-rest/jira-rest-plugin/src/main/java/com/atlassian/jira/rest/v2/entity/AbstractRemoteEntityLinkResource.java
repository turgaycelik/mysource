package com.atlassian.jira.rest.v2.entity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.entity.remotelink.RemoteEntityLink;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Common toolkit for implementing remote entity link services.
 *
 * @param <S> the target entity (like {@code Version})
 * @param <T> the entity link type (like {@code RemoteVersionLink})
 * @since JIRA REST v6.5.1  (in JIRA 6.1.1)
 */
public abstract class AbstractRemoteEntityLinkResource<S,T extends RemoteEntityLink<S>>
{
    protected final I18nHelper i18n;
    protected final JiraAuthenticationContext jiraAuthenticationContext;
    protected final JsonEntityPropertyManager jsonEntityPropertyManager;
    protected final ContextUriInfo contextUriInfo;



    protected AbstractRemoteEntityLinkResource(final I18nHelper i18n,
            final JiraAuthenticationContext jiraAuthenticationContext,
            final JsonEntityPropertyManager jsonEntityPropertyManager,
            final ContextUriInfo contextUriInfo)
    {
        this.i18n = i18n;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
        this.contextUriInfo = contextUriInfo;
    }



    /**
     * Creates a JSON bean for the provided remote entity link.
     *
     * @param link the remote entity link to be written out
     * @return the corresponding bean
     */
    protected RemoteEntityLinkJsonBean toBean(final T link)
    {
        return new RemoteEntityLinkJsonBean()
                .self(createSelfLink(link))
                .link(link.getJsonString());
    }

    /**
     * Creates a list of JSON beans for the provided remote entity links.
     *
     * @param links the remote entity links to be written out
     * @return the corresponding bean
     */
    protected RemoteEntityLinksJsonBean toBean(final Collection<? extends T> links)
    {
        final List<RemoteEntityLinkJsonBean> beans = new ArrayList<RemoteEntityLinkJsonBean>(links.size());
        for (T link : links)
        {
            beans.add(toBean(link));
        }
        return new RemoteEntityLinksJsonBean().links(beans);
    }



    /**
     * Responds with a single remote entity link.  This convenience method is equivalent to
     * {@link #toResponse(Object) toResponse}({@link #toBean(RemoteEntityLink) toBean}(link)).
     *
     * @param link the remote entity link to write
     * @return a response containing the link
     */
    protected Response toResponse(final T link)
    {
        return toResponse(toBean(link));
    }

    /**
     * Responds with a list of remote entity links.  This convenience method is equivalent to
     * {@link #toResponse(Object) toResponse}({@link #toBean(Collection) toBean}(links)).
     *
     * @param links the remote entity links to be written out
     * @return the response containing the links
     */
    protected Response toResponse(final Collection<? extends T> links)
    {
        return toResponse(toBean(links));
    }

    /**
     * Turns a bean into an normal response object.
     *
     * @param entity the object to respond with
     * @return the response
     */
    protected Response toResponse(Object entity)
    {
        return Response.ok(entity).cacheControl(never()).build();
    }

    /**
     * Forms a {@code 201} response with a {@code Location:} header pointing to {@code link}'s self URI.
     *
     * @param link the newly created link
     * @return the successful response
     */
    protected Response toSuccessfulPostResponse(final T link)
    {
        return Response.created(createSelfLink(link)).cacheControl(never()).build();
    }

    /**
     * Forms a {@code 204} response with no content to acknowledge the successful deletion.
     *
     * @return the successful response
     */
    protected Response toSuccessfulDeleteResponse()
    {
        return Response.noContent().cacheControl(never()).build();
    }

    /**
     * Forms the {@code self} URI for a link.
     *
     * @param link the link to be rendered as a URI
     * @return the self URI for the link
     */
    protected abstract URI createSelfLink(T link);

    /**
     * Verifies that the provided service result is error-free.  If it is invalid, then we throw
     * the appropriate {@code RESTException} containing the error collection instead of returning
     * the result.
     *
     * @param result the service result to validate
     * @param <T> the inferred service result class
     * @return {@code result} itself
     */
    protected <T extends ServiceResult> T valid(final T result)
    {
        if (!result.isValid())
        {
            throw new RESTException(ErrorCollection.of(result.getErrorCollection()));
        }
        return result;
    }

    /**
     * Returns the currently logged in user.
     * @return the currently logged in user.
     */
    protected ApplicationUser getUser()
    {
        return jiraAuthenticationContext.getUser();
    }
}
