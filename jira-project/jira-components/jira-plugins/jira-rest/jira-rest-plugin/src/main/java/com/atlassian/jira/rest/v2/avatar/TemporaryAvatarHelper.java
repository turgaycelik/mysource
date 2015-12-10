package com.atlassian.jira.rest.v2.avatar;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImageDataProvider;
import com.atlassian.jira.avatar.AvatarPickerHelperImpl;
import com.atlassian.jira.avatar.TypeAvatarService;
import com.atlassian.jira.avatar.CroppingAvatarImageDataProviderFactory;
import com.atlassian.jira.avatar.Selection;
import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.avatar.TemporaryAvatars;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.rest.v2.issue.AvatarBean;
import com.atlassian.jira.rest.v2.issue.AvatarBeanFactory;
import com.atlassian.jira.rest.v2.issue.AvatarCroppingBean;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.multipart.FilePart;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Component
public class TemporaryAvatarHelper
{

    private final TemporaryAvatarUploader avatarUploader;
    private final AttachmentHelper attachmentHelper;
    private final I18nHelper i18nHelper;
    private final CroppingAvatarImageDataProviderFactory croppingAvatarImageDataProviderFactory;
    // can i remove this dep?
    private final VelocityRequestContextFactory requestContextFactory;
    private final UniversalAvatarsService avatars;
    private final TemporaryAvatars temporaryAvatars;

    @Inject
    // to many paramters...
    public TemporaryAvatarHelper(
            final TemporaryAvatarUploader avatarUploader,
            final AttachmentHelper attachmentHelper,
            final TemporaryAvatars temporaryAvatars,
            final VelocityRequestContextFactory requestContextFactory,
            final UniversalAvatarsService avatars,
            final CroppingAvatarImageDataProviderFactory croppingAvatarImageDataProviderFactory,
            final I18nHelper i18nHelper)
    {
        this.avatarUploader = avatarUploader;
        this.attachmentHelper = attachmentHelper;
        this.temporaryAvatars = temporaryAvatars;
        this.requestContextFactory = requestContextFactory;
        this.avatars = avatars;
        this.croppingAvatarImageDataProviderFactory = croppingAvatarImageDataProviderFactory;
        this.i18nHelper = i18nHelper;
    }

    public Response storeTemporaryAvatar(final ApplicationUser remoteUser, final Avatar.Type type, final String ownerId, Avatar.Size targetSize, final String filename,
            final Long size, final HttpServletRequest request)
    {

        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, filename, size);

        if (!validationResult.isValid())
        {
            throwWebException(validationResult.getErrorMessage(), com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }

        try
        {
            UploadedAvatar uploadedAvatar = avatarUploader.createUploadedAvatarFromStream(
                    validationResult.getInputStream(),
                    filename,
                    validationResult.getContentType(),
                    validationResult.getSize());

            final TemporaryAvatar temporaryAvatar = new TemporaryAvatar(
                    validationResult.getContentType(),
                    uploadedAvatar.getContentType(),
                    filename,
                    uploadedAvatar.getImageFile(),
                    null);

            temporaryAvatars.storeTemporaryAvatar(remoteUser, temporaryAvatar, type, ownerId);

            final AvatarPickerHelperImpl.TemporaryAvatarBean temporaryAvatarBean =
                    new AvatarPickerHelperImpl.TemporaryAvatarBean(
                            getTemporaryAvatarUrl(),
                            uploadedAvatar.getWidth(),
                            uploadedAvatar.getHeight(),
                            isCroppingNeeded(uploadedAvatar, targetSize));

            final AvatarCroppingBean croppingInstructions =
                    AvatarBeanFactory.createTemporaryAvatarCroppingInstructions(temporaryAvatarBean);

            if (temporaryAvatarBean.isCroppingNeeded())
            {
                return Response.status(Response.Status.CREATED)
                        .entity(croppingInstructions)
                        .cacheControl(never()).build();
            }
            else
            {
                final AvatarBean avatarBean = createAvatarFromTemporary(remoteUser, type, ownerId, croppingInstructions);
                return Response.status(Response.Status.CREATED).entity(avatarBean).cacheControl(never()).build();
            }
        }
        catch (ValidationException e)
        {
            throwWebException(e.getMessage(), com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }
        catch (IOException e)
        {
            throwWebException(e.getMessage(), com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR);
        }
        catch (IllegalAccessException e)
        {
            throwWebException(null, com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN);
        }

        throw new AssertionError("unreachable!");
    }

    public Response storeTemporaryAvatar(final ApplicationUser remoteUser, final Avatar.Type type, final String ownerId, final Avatar.Size targetSize, final FilePart filePart, final HttpServletRequest request)
    {
        final String fullPath = filePart.getName();
        final String filename = fullPath.substring(fullPath.lastIndexOf("\\") + 1, fullPath.length());
        AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, filename, null);

        if (!validationResult.isValid())
        {
            throwWebException(validationResult.getErrorMessage(), com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }

        try
        {
            UploadedAvatar uploadedAvatar = avatarUploader.createUploadedAvatarFromStream(
                    filePart.getInputStream(),
                    filename,
                    filePart.getContentType());

            final TemporaryAvatar temporaryAvatar = new TemporaryAvatar(
                    validationResult.getContentType(),
                    uploadedAvatar.getContentType(),
                    filename,
                    uploadedAvatar.getImageFile(),
                    null);

            temporaryAvatars.storeTemporaryAvatar(remoteUser, temporaryAvatar, type, ownerId);

            final AvatarPickerHelperImpl.TemporaryAvatarBean temporaryAvatarBean =
                    new AvatarPickerHelperImpl.TemporaryAvatarBean(
                            getTemporaryAvatarUrl(),
                            uploadedAvatar.getWidth(),
                            uploadedAvatar.getHeight(),
                            isCroppingNeeded(uploadedAvatar, targetSize));

            final AvatarCroppingBean croppingInstructions =
                    AvatarBeanFactory.createTemporaryAvatarCroppingInstructions(temporaryAvatarBean);

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
                final AvatarBean avatarBean = createAvatarFromTemporary(remoteUser, type, ownerId, croppingInstructions);


                return Response.status(Response.Status.CREATED)
                        .entity("<html><body>"
                                + "<textarea>"
                                + "{"
                                + "\"id\": \"" + avatarBean.getId() + "\""
                                + "}"
                                + "</textarea>"
                                + "</body></html>")
                        .cacheControl(never()).build();
            }
        }
        catch (ValidationException e)
        {
            throwWebException(e.getMessage(), com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }
        catch (IOException e)
        {
            throwWebException(e.getMessage(), com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR);
        }
        catch (IllegalAccessException e)
        {
            throwWebException(null, com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN);
        }

        throw new AssertionError("unreachable!");
    }

    public AvatarBean createAvatarFromTemporary(final ApplicationUser remoteUser, final Avatar.Type type, final String ownerId, final AvatarCroppingBean croppingInstructions)
    {
        Selection selection = new Selection(croppingInstructions.getCropperOffsetX(), croppingInstructions.getCropperOffsetY(),
                croppingInstructions.getCropperWidth(), croppingInstructions.getCropperWidth());

        Avatar conversionResult =
                convertTemporaryToReal(remoteUser, ownerId, type, selection);

        final AvatarBean avatarBean = AvatarBeanFactory.createAvatarBean(conversionResult);

        return avatarBean;
    }

    private boolean isCroppingNeeded(final UploadedAvatar image, final Avatar.Size targetSize)
    {
        final boolean isSquare = image.getHeight() == image.getWidth();
        final boolean widthWithinBounds = image.getWidth() <= targetSize.getPixels();

        return !widthWithinBounds || !isSquare;
    }

    public String getTemporaryAvatarUrl()
    {
        // if the user chooses a new temporary avatar we need to keep making this url unique so that the javascript that
        // ajaxly retrieves this url always gets a unique one, forcing the browser to keep the image fresh
        return getBaseUrl() + "/secure/temporaryavatar?cropped=true&magic=" + System.currentTimeMillis();
    }

    private String getBaseUrl()
    {
        return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }


    public Avatar convertTemporaryToReal(final ApplicationUser remoteUser, String ownerId, Avatar.Type type, Selection selection)
    {

        TemporaryAvatar temporaryAvatar = temporaryAvatars.getCurrentTemporaryAvatar();

        if (temporaryAvatar == null)
        {
            throwWebException(i18nHelper.getText("avatarpicker.upload.failure"), com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR);
        }

        try
        {
            final InputStream imageDataStream = temporaryAvatar.getImageData();
            try
            {
                final AvatarImageDataProvider imageDataProvider = croppingAvatarImageDataProviderFactory.createStreamsFrom(imageDataStream, selection);
                final TypeAvatarService typeAvatars = avatars.getAvatars(type);
                if (typeAvatars == null)
                {
                    throwWebException(i18nHelper.getText("rest.error.invalid.avatar.type", type.getName()), com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
                }
                Avatar newAvatar = typeAvatars.createAvatar(remoteUser, ownerId, imageDataProvider);

                temporaryAvatars.dispose(temporaryAvatar);
                return newAvatar;
            }
            finally
            {
                IOUtils.closeQuietly(imageDataStream);
            }
        }
        catch (IOException e)
        {
            throwWebException(i18nHelper.getText("avatarpicker.upload.temp.io", e.getMessage()), com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR);
        }
        catch (IllegalAccessException e)
        {
            throwWebException(i18nHelper.getText("avatarpicker.upload.temp.io", e.getMessage()), com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN);
        }

        throw new AssertionError("ureachable");
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
