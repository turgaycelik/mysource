package com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.jira.plugin.headernav.customcontentlinks.CustomContentLinkServiceFactory;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.CustomContentLink;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.NoAdminPermissionException;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * REST endpoint for the restful table used to administer custom content links.
 *
 * This could be moved to the navlinks plugin to allow use by other applications.
 */
@Produces ("application/json")
@Consumes ("application/json")
@Path("/customcontentlinks")
public class CustomContentLinksRestResource
{
    private final CustomContentLinkServiceFactory customContentLinkServiceFactory;
    private final I18nResolver i18nResolver;

    public CustomContentLinksRestResource(final CustomContentLinkServiceFactory customContentLinkServiceFactory, I18nResolver i18nResolver) {
        this.customContentLinkServiceFactory = customContentLinkServiceFactory;
        this.i18nResolver = i18nResolver;
    }

    @Path ("{projectKey}/list")
    @GET
    public Response list(@PathParam("projectKey") String projectKey) {
        Iterable<CustomContentLinkData> l = Iterables.transform(customContentLinkServiceFactory.getCustomContentLinkService().getCustomContentLinks(projectKey), converter());
        return Response.ok(l).build();
    }

    private Function<CustomContentLink, CustomContentLinkData> converter() {
        return new Function<CustomContentLink, CustomContentLinkData>() {
            @Override
            public CustomContentLinkData apply(@Nullable CustomContentLink c) {
                return new CustomContentLinkData(Integer.toString(c.getId()), c.getContentKey(), c.getLinkLabel(), c.getLinkUrl());
            }
        };
    }

    @Path("{projectKey}/{id}")
    @GET
    public Response get(@PathParam("projectKey") String projectKey, @PathParam ("id") String id, @Context HttpServletRequest request) {
        try {
            CustomContentLink link = customContentLinkServiceFactory.getCustomContentLinkService().getById(Integer.parseInt(id));
            if (link == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                return Response.ok(converter().apply(link)).build();
            }
        } catch (NumberFormatException e) {
            return reportBadId(id);
        }
    }

    private Response reportBadId(String id) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Bad id '" + id + "'").build();
    }

    private Response handleNoPermission() {
        return Response.status(403).build();
    }

    @Path("{projectKey}/{id}")
    @DELETE
    public Response delete(@PathParam("projectKey") String projectKey, @PathParam("id") String id, @Context HttpServletRequest request) {
        try {
            customContentLinkServiceFactory.getCustomContentLinkService().removeById(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            return reportBadId(id);
        } catch (NoAdminPermissionException e) {
            return handleNoPermission();
        }
        return Response.ok().build();
    }

    // the data instance contains only the url and label -- id and key will be ignored
    @Path("{projectKey}")
    @POST
    public Response create(@PathParam("projectKey") String projectKey, CustomContentLinkData data) {
        try {
            CustomContentLink link = dataToLink(projectKey, data).build();
            return Response.ok(converter().apply(customContentLinkServiceFactory.getCustomContentLinkService().addCustomContentLink(link))).build();
        } catch (NoAdminPermissionException e) {
            return handleNoPermission();
        } catch (ValidationException ve) {
            return validationErrorResponse(ve);
        }
    }

    private CustomContentLink.Builder dataToLink(String projectKey, CustomContentLinkData data)
            throws ValidationException
    {
        if (StringUtils.isBlank(data.linkUrl)) {
            throw new ValidationException("linkUrl", i18nResolver.getText("must.not.be.empty"));
        }
        if (StringUtils.isBlank(data.linkLabel)) {
            throw new ValidationException("linkLabel", i18nResolver.getText("must.not.be.empty"));
        }
        return CustomContentLink.builder().key(projectKey).url(data.linkUrl).label(data.linkLabel);
    }

    // the data instance contains only the url and label -- id and key will be ignored
    @Path("{projectKey}/{id}")
    @PUT
    public Response update(@PathParam("projectKey") String projectKey, @PathParam("id") String idString, CustomContentLinkData data, @Context HttpServletRequest request) {
        try {
            int id = Integer.parseInt(idString);
            CustomContentLink currentLink = customContentLinkServiceFactory.getCustomContentLinkService().getById(id);
            if (currentLink == null) {
                return Response.status(404).build();
            } else {
                if (data.linkLabel == null) {
                    data.linkLabel = currentLink.getLinkLabel();
                }
                if (data.linkUrl == null) {
                    data.linkUrl = currentLink.getLinkUrl();
                }
                CustomContentLink link = dataToLink(projectKey, data).id(id).build();
                customContentLinkServiceFactory.getCustomContentLinkService().update(link);
                return Response.ok(converter().apply(link)).build();
            }
        } catch (NumberFormatException e) {
            return reportBadId(idString);
        } catch (NoAdminPermissionException e) {
            return handleNoPermission();
        } catch (ValidationException e) {
            return validationErrorResponse(e);
        }
    }

    /**
     * Move the custom content link at {projectKey}/{id} as specified by the MoveBean in the request
     */
    @POST
    @Path ("{projectKey}/{id}/move")
    public Response movePosition(
            @PathParam("projectKey") String projectKey,
            @PathParam ("id") final Integer id,
            @Context HttpServletRequest request,
            MoveBean bean)
    {
        try {
            if (bean.after != null) {
                int idToMoveAfter = extractIdFromLink(bean.after.getPath());
                customContentLinkServiceFactory.getCustomContentLinkService().moveAfter(id, idToMoveAfter);
            } else {
                switch (bean.position) {
                    case Earlier:
                    case Later:
                    case Last:
                        throw new IllegalArgumentException("Unexpected position '" + bean.position + "'");
                    case First:
                        customContentLinkServiceFactory.getCustomContentLinkService().moveToStart(id);
                }
            }
            return Response.ok().build();
        }
        catch (NoAdminPermissionException e)
        {
            return handleNoPermission();
        }

    }

    private int extractIdFromLink(String path)
    {
        String idString = path.substring(path.lastIndexOf('/') + 1);
        try {
            return Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse id from path '" + path + "'");
        }
    }

    private Response validationErrorResponse(ValidationException e) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("{\"errors\": {\"" + e.getField() + "\": \"" + e.getValidationError() + "\"}}")
                .build();
    }

    private static class ValidationException extends Exception
    {
        private final String field;
        private final String message;

        public ValidationException(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getValidationError() {
            return message;
        }
    }
}
