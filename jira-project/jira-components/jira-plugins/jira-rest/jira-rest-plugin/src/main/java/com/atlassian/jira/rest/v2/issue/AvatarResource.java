package com.atlassian.jira.rest.v2.issue;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

/**
 * @since v5.0
 */
@Path ("avatar")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class AvatarResource
{
    private final AvatarResourceHelper avatarResourceHelper;

    public AvatarResource(final JiraAuthenticationContext authContext, final AvatarManager avatarManager,
            final AvatarPickerHelper avatarPickerHelper, final AttachmentHelper attachmentHelper, final UserManager userManager)
    {
        this.avatarResourceHelper = new AvatarResourceHelper(authContext, avatarManager, avatarPickerHelper, attachmentHelper, userManager);
    }

    /**
     * Returns all system avatars of the given type.
     *
     * @since v5.0
     *
     * @param type the avatar type
     *
     * @return all system avatars of the given type.
     *
     * @response.representation.200.qname
     *      avatars
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a map containing a list of system avatars. A map is returned to be consistent with the shape of the
     *      project/KEY/avatars REST end point.
     *
     * @response.representation.200.example
     *      {@link AvatarBean#DOC_EXAMPLE_SYSTEM_LIST}
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of avatars.
     */
    @GET
    @Path ("{type}/system")
    public Response getAllSystemAvatars(final @PathParam ("type") String type)
    {
        return avatarResourceHelper.getAllSystemAvatars(Avatar.Type.getByName(type));
    }

    /**
     * Creates temporary avatar
     *
     * @since v5.0
     *
     * @param type the avatar type
     * @param filename name of file being uploaded
     * @param size size of file
     * @param request servlet request
     * @return temporary avatar cropping instructions
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the request does not conain a valid XSRF token
     *
     * @response.representation.400.doc
     *      Valiation failed. For example filesize is beyond max attachment size.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.WILDCARD)
    @Path ("{type}/temporary")
    public Response storeTemporaryAvatar(final @PathParam ("type") String type, final @QueryParam ("filename") String filename,
            final @QueryParam ("size") Long size, final @Context HttpServletRequest request)
    {
        return avatarResourceHelper.storeTemporaryAvatar(Avatar.Type.getByName(type), null, filename, size, request);
    }

    /**
     * Updates the cropping instructions of the temporary avatar.
     *
     * @since v5.0
     *
     * @param type the avatar type
     * @param croppingInstructions cropping instructions
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EDIT_EXAMPLE}
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.400.doc
     *      Returned if the cropping coordinates are invalid
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while cropping the temporary avatar
     */
    @POST
    @Path ("{type}/temporaryCrop")
    public Response createAvatarFromTemporary(final @PathParam ("type") String type, final AvatarCroppingBean croppingInstructions)
    {
        // TODO allow for project key here
        return avatarResourceHelper.cropTemporaryAvatar(Avatar.Type.getByName(type), null, croppingInstructions);
    }
}
