package com.atlassian.jira.rest.v2.issue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarPickerHelperImpl;
import com.atlassian.jira.avatar.Selection;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.plugins.rest.common.multipart.FilePart;

import org.springframework.stereotype.Component;

import static com.atlassian.jira.avatar.Avatar.Type.USER;
import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * A helper resource for Project and User avatars.
 *
 * @since v5.0
 */
@Component
public class AvatarResourceHelper
{

    private JiraAuthenticationContext authContext;
    private AvatarManager avatarManager;
    private AvatarPickerHelper avatarPickerHelper;
    private AttachmentHelper attachmentHelper;
    private final UserManager userManager;

    @Inject
    public AvatarResourceHelper(final JiraAuthenticationContext authContext,
            final AvatarManager avatarManager,
            final AvatarPickerHelper avatarPickerHelper,
            final AttachmentHelper attachmentHelper,
            final UserManager userManager)
    {
        this.authContext = authContext;
        this.avatarManager = avatarManager;
        this.avatarPickerHelper = avatarPickerHelper;
        this.attachmentHelper = attachmentHelper;
        this.userManager = userManager;
    }

    /**
     * Returns all avatars which are visible for the currently logged in user.
     *
     * @param type - Type of avatars (User or Project)
     * @param ownerId - (project id or username)
     * @param selectedAvatarId - id of selected avatar
     * @return all avatars, system and custom for given type
     *
     * @response.representation.200.qname
     *      avatars
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a map containing a list of avatars for both custom an system avatars
     *
     * @response.representation.200.example
     *      {@link AvatarBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of avatars.
     */
    public Map<String, List<AvatarBean>> getAllAvatars(final Avatar.Type type, final String ownerId, Long selectedAvatarId)
    {

        if (selectedAvatarId == null)
        {
            selectedAvatarId = avatarManager.getDefaultAvatarId(type);
        }

        Map<String, List<AvatarBean>> avatars = new HashMap<String, List<AvatarBean>>();

        final ApplicationUser avatarUser = type == USER ? userManager.getUserByKey(ownerId) : null;

        final List<AvatarBean> systemAvatarBeans = AvatarBeanFactory.createAvatarBeans(avatarManager.getAllSystemAvatars(type), avatarUser);
        setSelectedAvatar(selectedAvatarId, systemAvatarBeans);
        avatars.put("system", systemAvatarBeans);

        final List<AvatarBean> customAvatarBeans = AvatarBeanFactory.createAvatarBeans(avatarManager.getCustomAvatarsForOwner(type, ownerId), avatarUser);
        avatars.put("custom", customAvatarBeans);
        setSelectedAvatar(selectedAvatarId, customAvatarBeans);

        return avatars;
    }

    /**
     * Returns all system avatars of the given type.
     *
     * @param type - Type of avatars (User or Project)
     * @return all system avatars for the given type
     *
     * @response.representation.200.qname
     *      avatars
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a map containing a list of system avatars. A map is returned to be consistent with the shape of the
     *      {link #getAllAvatars} method.
     *
     * @response.representation.200.example
     *      {@link AvatarBean#DOC_EXAMPLE_SYSTEM_LIST}
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of avatars.
     */
    public Response getAllSystemAvatars(final Avatar.Type type)
    {
        Map<String, List<AvatarBean>> avatars = new HashMap<String, List<AvatarBean>>();

        final List<AvatarBean> systemAvatarBeans = AvatarBeanFactory.createAvatarBeans(avatarManager.getAllSystemAvatars(type), null);
        avatars.put("system", systemAvatarBeans);

        return Response.ok(avatars).cacheControl(NO_CACHE).build();
    }

    /**
     * Marks selected avatar
     *
     * @param selectedAvatarId - the id of the avatar to be marked
     * @param avatars - list of avatars to find avatar selected
     */
    private void setSelectedAvatar(Long selectedAvatarId, List<AvatarBean> avatars)
    {
        if (selectedAvatarId != null)
        {
            for (AvatarBean avatar : avatars)
            {
                if (avatar.getId().equals(selectedAvatarId.toString()))
                {
                    avatar.setSelected(true);
                    break;
                }
            }
        }
    }

    /**
     * Converts temporary avatar into a real avatar
     *
     * @since v5.0
     * @param type - Type of avatars (User or Project)
     * @param ownerId - (project id or username)
     * @param croppingInstructions - Corrdinates to crop image
     * @return created avatar
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
     * @response.representation.201.doc
     *      Returns created avatar
     *
     * @response.representation.201.example
     *      {@link AvatarBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if the cropping coordinates are invalid
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    public Response createAvatarFromTemporary(final Avatar.Type type, final String ownerId, final AvatarCroppingBean croppingInstructions)
    {
        Selection selection = new Selection(croppingInstructions.getCropperOffsetX(), croppingInstructions.getCropperOffsetY(),
                croppingInstructions.getCropperWidth(), croppingInstructions.getCropperWidth());

        AvatarPickerHelperImpl.Result<Avatar> conversionResult = avatarPickerHelper
                .convertTemporaryToReal(ownerId, type, selection);

        if (!conversionResult.isValid())
        {
            throwWebException(conversionResult.getErrorCollection());
        }

        final AvatarBean avatarBean = AvatarBeanFactory.createAvatarBean(conversionResult.getResult());

        return Response.status(Response.Status.CREATED).entity(avatarBean).cacheControl(never()).build();
    }

    /**
     * Creates temporary avatar and provides instructions for cropping (if needed)
     *
     * @param type - Type of avatars (User or Project)
     * @param ownerId - (project id or username)
     * @param filename - name of file being uploaded
     * @param size - size of file
     * @param request - servlet request
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
     * @response.representation.500.doc
     *      Returned if an error occurs while creating temporary avatar
     */
    public Response storeTemporaryAvatar(final Avatar.Type type, final String ownerId, final String filename, final Long size, final HttpServletRequest request)
    {

        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, filename, size);

        if (!validationResult.isValid())
        {
            throwWebException(validationResult.getErrorMessage(), com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }

        AvatarPickerHelperImpl.Result<AvatarPickerHelperImpl.TemporaryAvatarBean> tempAvatarResult =
                avatarPickerHelper.upload(validationResult.getInputStream(),
                        filename, validationResult.getContentType(),
                        validationResult.getSize(), ownerId, type);

        if (!tempAvatarResult.isValid())
        {
            throwWebException(tempAvatarResult.getErrorCollection());
        }

        final AvatarCroppingBean croppingInstructions = AvatarBeanFactory.createTemporaryAvatarCroppingInstructions(tempAvatarResult.getResult());

        if (tempAvatarResult.getResult().isCroppingNeeded())
        {
            return Response.status(Response.Status.CREATED)
                    .entity(croppingInstructions)
                    .cacheControl(never()).build();
        }
        else
        {
            return createAvatarFromTemporary(type, ownerId, croppingInstructions);
        }
    }

    /**
     * Creates temporary avatar using multipart. The cropping instructions (or validation errors) are sent back as JSON
     * stored in a textarea. This is because the client uses remote iframing to submit avatars using multipart. So we
     * must send them a valid HTML page back from which the client parses the JSON.
     *
     * @param type - Type of avatars (User or Project)
     * @param ownerId - (project id or username)
     * @param filePart - File body
     * @param request - servlet request
     * @return html fragment that will be added to iframe (client will parse this out into JSON)
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      text/html
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions embeded in HTML page. Error messages will also be embeded in the page.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    public Response storeTemporaryAvatarUsingMultiPart(final Avatar.Type type, final String ownerId, final FilePart filePart, final HttpServletRequest request)
    {

        String filename = filePart.getName();
        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, filename, null);

        if (!validationResult.isValid())
        {
            Collection<String> errorMessages = new ArrayList<String>();
            errorMessages.add(validationResult.getErrorMessage());
            return returnErrorMessagesAsHtml(errorMessages);
        }
        else
        {
            final String fullPath = filePart.getName();
            final String fileName = fullPath.substring(fullPath.lastIndexOf("\\") + 1, fullPath.length());

            try
            {
                AvatarPickerHelperImpl.Result<AvatarPickerHelperImpl.TemporaryAvatarBean> tempAvatarResult =
                        avatarPickerHelper.upload(filePart.getInputStream(), fileName, filePart.getContentType(), -1,
                                ownerId, type);

                if (!tempAvatarResult.isValid())
                {
                    return returnErrorMessagesAsHtml(tempAvatarResult.getErrorCollection().getErrorMessages());
                }

                final AvatarPickerHelperImpl.TemporaryAvatarBean temporaryAvatarBean = tempAvatarResult.getResult();

                if (temporaryAvatarBean.isCroppingNeeded())
                {

                    return Response.status(Response.Status.CREATED)
                            .entity("<html><body>"
                                    + "<textarea>"
                                    + "{"
                                    + "\"url\": \"" + temporaryAvatarBean.getUrl() + "\","
                                    + "\"cropperWidth\": \"" + temporaryAvatarBean.getCropperWidth() + "\","
                                    + "\"cropperOffsetX\": \"" + temporaryAvatarBean.getCropperOffsetX() + "\","
                                    + "\"cropperOffsetY\": \"" + temporaryAvatarBean.getCropperOffsetY() + "\","
                                    + "\"isCroppingNeeded\": \"" + temporaryAvatarBean.isCroppingNeeded() + "\""
                                    + "}"
                                    + "</textarea>"
                                    + "</body></html>")
                            .cacheControl(never()).build();
                }
                else
                {
                    Selection selection = new Selection(temporaryAvatarBean.getCropperOffsetX(), temporaryAvatarBean.getCropperOffsetY(),
                            temporaryAvatarBean.getCropperWidth(), temporaryAvatarBean.getCropperWidth());

                    AvatarPickerHelperImpl.Result<Avatar> conversionResult = avatarPickerHelper
                            .convertTemporaryToReal(ownerId, type, selection);

                    if (!conversionResult.isValid())
                    {
                        return returnErrorMessagesAsHtml(conversionResult.getErrorCollection().getErrorMessages());
                    }

                    return Response.status(Response.Status.CREATED)
                             .entity("<html><body>"
                                    + "<textarea>"
                                    + "{"
                                    + "\"id\": \"" + conversionResult.getResult().getId() + "\""
                                    + "}"
                                    + "</textarea>"
                                    + "</body></html>")
                            .cacheControl(never()).build();
                }

            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public Response cropTemporaryAvatar(final Avatar.Type type, final String ownerId, final AvatarCroppingBean croppingInstructions)
    {
        Selection selection = new Selection(croppingInstructions.getCropperOffsetX(), croppingInstructions.getCropperOffsetY(),
                croppingInstructions.getCropperWidth(), croppingInstructions.getCropperWidth());

        AvatarPickerHelperImpl.Result<AvatarPickerHelperImpl.TemporaryAvatarBean> cropResult = avatarPickerHelper
                .cropTemporary(ownerId, type, selection);

        if (!cropResult.isValid())
        {
            throwWebException(cropResult.getErrorCollection());
        }

        return Response.status(Response.Status.OK).cacheControl(never()).build();
    }

    /**
     * Deletes avatar
     *
     * @since v5.0
     * @param id database id for avatar
     * @return temporary avatar cropping instructions
     *
     * @response.representation.204.mediaType
     *      application/json
     *
     * @response.representation.204.doc
     *      Returned if the avatar is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to delete the component.
     *
     * @response.representation.404.doc
     *      Returned if the avatar does not exist or the currently authenticated user does not have permission to
     *      delete it.
     */
    public Response deleteAvatar(final @PathParam ("id") Long id)
    {
        Avatar avatar = avatarManager.getById(id);
        if (avatar == null)
        {
            throw new NotFoundWebException();
        }
        if (!avatarManager.hasPermissionToEdit(authContext.getLoggedInUser(), avatar.getAvatarType(), avatar.getOwner()))
        {
            throw new NotAuthorisedWebException();
        }
        boolean deleted = avatarManager.delete(id, true);

        if (deleted)
        {
            return Response.noContent().cacheControl(never()).build();
        }
        else
        {
            return Response.status(Response.Status.FORBIDDEN).cacheControl(never()).build();
        }
    }

    private AttachmentHelper.ValidationResult validateAttachment(final HttpServletRequest request, @Nullable final String filename, @Nullable final Long size)
    {
        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, filename, size);

        if (!validationResult.isValid())
        {

            if (validationResult.getErrorType() == AttachmentHelper.ValidationError.XSRF_TOKEN_INVALID)
            {
                throwWebException(authContext.getI18nHelper().getText("xsrf.error.title"),
                        com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN);
            }
            else if (validationResult.getErrorType() == AttachmentHelper.ValidationError.ATTACHMENT_IO_SIZE)
            {
                String message = authContext.getI18nHelper().getText("attachfile.error.io.size", filename);
                throwWebException(message, com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR);
            }
            else
            {
                String message = authContext.getI18nHelper().getText("attachfile.error.io.error", filename,
                        validationResult.getErrorMessage());
                throwWebException(message, com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR);
            }
        }

        return validationResult;
    }

    private Response returnErrorMessagesAsHtml(Collection<String> errorMessages)
    {

        String errorMsgs = "";
        String sep = "";

        for (String errorMessage : errorMessages)
        {
            errorMsgs += "\"" + JSONEscaper.escape(errorMessage) + "\"" + sep;
            sep = ",";
        }

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



    private void throwWebException(String message, com.atlassian.jira.util.ErrorCollection.Reason reason)
    {
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(message, reason);
        throwWebException(errorCollection);
    }

    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }
}
