package com.atlassian.jira.rest.v2.issue;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImageResolver;
import com.atlassian.jira.avatar.TypeAvatarService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v2.avatar.AvatarUrls;
import com.atlassian.jira.avatar.SystemAndCustomAvatars;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.rest.v2.avatar.TemporaryAvatarHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Path ("universal_avatar")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class UniversalAvatarResource
{
    public static final String SYSTEM_AVATARS_FIELD = "system";
    public static final String CUSTOM_AVATARS_FIELD = "custom";

    private final JiraAuthenticationContext authContext;
    private final UniversalAvatarsService universalAvatars;
    private final AvatarUrls avatarUrls;
    private final AvatarResourceHelper avatarResourceHelper;
    private final TemporaryAvatarHelper avatarTemporaryHelper;

    public UniversalAvatarResource(final JiraAuthenticationContext authContext,
            final UniversalAvatarsService universalAvatars, final AvatarUrls avatarUrls, AvatarResourceHelper avatarResourceHelper, final TemporaryAvatarHelper avatarTemporaryHelper)
    {
        this.authContext = authContext;
        this.universalAvatars = universalAvatars;
        this.avatarUrls = avatarUrls;
        this.avatarResourceHelper = avatarResourceHelper;
        this.avatarTemporaryHelper = avatarTemporaryHelper;
    }

    /**
     * @since v6.3
     */
    @GET
    @Path ("type/{type}/owner/{owningObjectId}")
    public Response getAvatars(final @PathParam ("type") String avatarType, final @PathParam ("owningObjectId") String owningObjectId)
    {
        final Avatar.Type avatarsType = Avatar.Type.getByName(avatarType);
        final TypeAvatarService avatarsForType = universalAvatars.getAvatars(avatarsType);
        if (null == avatarsForType)
        {
            return Response.status(Response.Status.NOT_FOUND).entity("avatarType").build();
        }
        final AvatarImageResolver uriResolver = universalAvatars.getImages(avatarsType);
        if (null == uriResolver)
        {
            return Response.status(Response.Status.NOT_FOUND).entity("avatarType").build();
        }

        final SystemAndCustomAvatars allAvatars = avatarsForType.getAvatars(authContext.getUser(), owningObjectId);

        Map<String, List<AvatarBean>> result = sytemAndCustomAvatarsToBeanMap(uriResolver, allAvatars);

        return Response.ok(result).build();
    }

    /**
     * Creates temporary avatar
     *
     * @param avatarType Type of entity where to change avatar
     * @param owningObjectId Entity id where to change avatar
     * @param filename name of file being uploaded
     * @param size size of file
     * @param request servlet request
     * @return temporary avatar cropping instructions
     * @since v5.0
     */
    @POST
    @Consumes (MediaType.WILDCARD)
    @RequiresXsrfCheck
    @Path ("type/{type}/owner/{owningObjectId}/temp")
    public Response storeTemporaryAvatar(final @PathParam ("type") String avatarType, final @PathParam ("owningObjectId") String owningObjectId, final @QueryParam ("filename") String filename,
            final @QueryParam ("size") Long size, final @Context HttpServletRequest request)
    {
        try
        {
            final Avatar.Type avatarsType = Avatar.Type.getByName(avatarType);
            if (null == avatarsType)
            {
                throw new NoSuchElementException("avatarType");
            }

            final ApplicationUser remoteUser = authContext.getUser();

            // get from paramter!!
            Avatar.Size avatarTargetSize = Avatar.Size.LARGE;
            final Response storeTemporaryAvatarResponse = avatarTemporaryHelper.storeTemporaryAvatar(remoteUser, avatarsType, owningObjectId, avatarTargetSize, filename, size, request);

            return storeTemporaryAvatarResponse;
        }
        catch (NoSuchElementException x)
        {
            return Response.status(Response.Status.NOT_FOUND).entity(x.getMessage()).build();
        }
    }

    @POST
    @Consumes (MediaType.MULTIPART_FORM_DATA)
    @RequiresXsrfCheck
    @Path ("type/{type}/owner/{owningObjectId}/temp")
    @Produces ({ MediaType.TEXT_HTML })
    public Response storeTemporaryAvatarUsingMultiPart(final @PathParam ("type") String avatarType, final @PathParam ("owningObjectId") String owningObjectId,
            final @MultipartFormParam ("avatar") FilePart filePart, final @Context HttpServletRequest request)
    {
        try
        {
            try
            {
                final Avatar.Type avatarsType = Avatar.Type.getByName(avatarType);
                if (null == avatarsType)
                {
                    throw new NoSuchElementException("avatarType");
                }

                final ApplicationUser remoteUser = authContext.getUser();

                // get from paramter!!
                Avatar.Size avatarTargetSize = Avatar.Size.LARGE;
                final Response storeTemporaryAvatarResponse = avatarTemporaryHelper.storeTemporaryAvatar(remoteUser, avatarsType, owningObjectId, avatarTargetSize, filePart, request);

                return storeTemporaryAvatarResponse;
            }
            catch (NoSuchElementException x)
            {
                return Response.status(Response.Status.NOT_FOUND).entity(x.getMessage()).build();
            }
        }
        catch (RESTException x)
        {
            String errorMsgs = x.toString();
            String sep = "";

            return Response.status(Response.Status.OK)
                    .entity("<html><body>"
                            + "<textarea>"
                            + "{"
                            + "\"errorMessages\": [" + errorMsgs + "]"
                            + "}"
                            + "</textarea>"
                            + "</body></html>")
                    .cacheControl(never()).build();
        }
    }

    @POST
    @Path ("type/{type}/owner/{owningObjectId}/avatar")
    public Response createAvatarFromTemporary(final @PathParam ("type") String avatarType, final @PathParam ("owningObjectId") String owningObjectId, final AvatarCroppingBean croppingInstructions)
    {
        try
        {
            final Avatar.Type avatarsType = Avatar.Type.getByName(avatarType);
            if (null == avatarsType)
            {
                throw new NoSuchElementException("avatarType");
            }
            final ApplicationUser remoteUser = authContext.getUser();

            final AvatarBean avatarFromTemporary =
                    avatarTemporaryHelper.createAvatarFromTemporary(
                            remoteUser,
                            avatarsType,
                            owningObjectId,
                            croppingInstructions);

            return Response.status(Response.Status.CREATED).entity(avatarFromTemporary).cacheControl(never()).build();
        }
        catch (NoSuchElementException x)
        {
            return Response.status(Response.Status.NOT_FOUND).entity(x.getMessage()).build();
        }
    }


    /**
     * Deletes avatar
     *
     * @param avatarType Project id or project key
     * @param id database id for avatar
     * @return temporary avatar cropping instructions
     * @since v5.0
     */
    @DELETE
    @Path ("type/{type}/owner/{owningObjectId}/avatar/{id}")
    public Response deleteAvatar(final @PathParam ("type") String avatarType, final @PathParam ("owningObjectId") String owningObjectId, @PathParam ("id") final Long id)
    {
        final Avatar.Type avatarsType = Avatar.Type.getByName(avatarType);
        final TypeAvatarService avatarsForType = universalAvatars.getAvatars(avatarsType);

        if (null == avatarsForType)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!avatarsForType.canUserCreateAvatar(authContext.getUser(), owningObjectId))
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        final Response avatarFromTemporaryResponse = avatarResourceHelper.deleteAvatar(id);
        return avatarFromTemporaryResponse;
    }


    private Map<String, List<AvatarBean>> sytemAndCustomAvatarsToBeanMap(final AvatarImageResolver uriResolver, final SystemAndCustomAvatars allAvatars)
    {
        final ApplicationUser remoteUser = authContext.getUser();

        final List<AvatarBean> systemAvatarBeans = createAvatarBeans(allAvatars.getSystemAvatars(), remoteUser, uriResolver);
        final List<AvatarBean> customAvatarBeans = createAvatarBeans(allAvatars.getCustomAvatars(), remoteUser, uriResolver);

        return ImmutableMap.<String, List<AvatarBean>>builder().
                put(SYSTEM_AVATARS_FIELD, systemAvatarBeans).
                put(CUSTOM_AVATARS_FIELD, customAvatarBeans).
                build();
    }

    private List<AvatarBean> createAvatarBeans(final Iterable<Avatar> avatars, final ApplicationUser remoteUser, final AvatarImageResolver uriResolver)
    {
        List<AvatarBean> beans = Lists.newArrayList();
        for (Avatar avatar : avatars)
        {
            beans.add(createAvatarBean(remoteUser, avatar, uriResolver));
        }

        return beans;
    }

    private AvatarBean createAvatarBean(final ApplicationUser remoteUser, final Avatar avatar, final AvatarImageResolver uriResolver)
    {
        return new AvatarBean(
                avatar.getId().toString(),
                avatar.getOwner(),
                avatar.isSystemAvatar(),
                avatarUrls.getAvatarURLs(remoteUser, avatar, uriResolver)
        );
    }

}
