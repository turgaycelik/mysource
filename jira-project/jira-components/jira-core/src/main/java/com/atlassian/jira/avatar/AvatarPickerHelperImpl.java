package com.atlassian.jira.avatar;

import com.atlassian.core.util.FileSize;
import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.LimitedOutputStream;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.util.FileNameCharacterCheckerUtil;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @since v5.0
 */
public class AvatarPickerHelperImpl implements AvatarPickerHelper
{
    private static final Logger log = Logger.getLogger(AvatarPickerHelperImpl.class);
    /**
     * JDK must support processing the types. THESE MUST BE STORED IN lower case. Checked
     * http://reference.sitepoint.com/html/mime-types-full
     */
    private static final List<String> CONTENT_TYPES = Lists.newArrayList("image/jpeg", "image/gif", "image/png", "image/pjpeg", "image/x-png");

    /**
     * Fits the popup window.
     */
    private static final int MAX_SIDE_LENGTH = 500;

    /**
     * ImageIO format name used to write the image to disk.
     */
    private static final String AVATAR_IMAGE_FORMAT = "png";

    private static final String TEMP_FILE_PREFIX = "JIRA-avatar";

    private static final String TEMP_FILE_EXTENSION = ".png";

    private static final int DEFAULT_MAX_MEGAPIXELS = 5;


    private AvatarManager avatarManager;
    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory requestContextFactory;
    private final ProjectManager projectManager;
    private final UserUtil userUtil;
    private Thumber thumber;

    public AvatarPickerHelperImpl(AvatarManager avatarManager, JiraAuthenticationContext authenticationContext,
            ApplicationProperties properties, final VelocityRequestContextFactory requestContextFactory,
            ProjectManager projectManager, UserUtil userUtil)
    {
        this.avatarManager = avatarManager;
        this.authenticationContext = authenticationContext;
        applicationProperties = properties;
        this.requestContextFactory = requestContextFactory;
        this.projectManager = projectManager;
        this.userUtil = userUtil;
        thumber = new Thumber(Thumbnail.MimeType.PNG);
    }

    @Override
    public Result<TemporaryAvatarBean> upload(InputStream stream, String fileName, String contentType, long size, String ownerId, Avatar.Type type)
    {
        if (!isAllowedToPickAvatar(ownerId, type))
        {
            return new Result<TemporaryAvatarBean>().addError(null, ErrorCollection.Reason.FORBIDDEN);
        }

        if (!isImageContent(contentType))
        {
            if (fileName != null && !fileName.contains("."))
            {
                log.info("Received avatar upload with unsupported content type: " + contentType + " and no extension");
                return new Result<TemporaryAvatarBean>().addError(getText("avatarpicker.upload.contenttype.no.ext.failure"), ErrorCollection.Reason.VALIDATION_FAILED);
            }

            log.info("Received avatar upload with unsupported content type: " + contentType);
            return new Result<TemporaryAvatarBean>().addError(getText("avatarpicker.upload.contenttype.failure"), ErrorCollection.Reason.VALIDATION_FAILED);
        }

        final Result<File> copyResult = copyStreamToTempFile(stream, fileName, size);
        if (!copyResult.isValid())
        {
            return new Result<TemporaryAvatarBean>(copyResult);
        }

        final File file = copyResult.getResult();
        final String invalidChars = new FileNameCharacterCheckerUtil().assertFileNameDoesNotContainInvalidChars(fileName);
        if (invalidChars == null)
        {
            ResultAndContentTypeChanged scaledFileAndContentType = clampSize(file, MAX_SIDE_LENGTH);
            Result<File> scaledFile = scaledFileAndContentType.getResult();

            if (!scaledFile.isValid())
            {
                // error should have been added before
                return new Result<TemporaryAvatarBean>(scaledFile);
            }
            final Image image;
            try
            {
                image = thumber.getImage(scaledFile.getResult());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            // save a temporary avatar so we can do scaling
            String temporaryContentType = scaledFileAndContentType.isContentTypeChanged() ? AvatarManager.AVATAR_IMAGE_FORMAT_FULL.getContentType() : contentType;
            TemporaryAvatar tempAvatar = new TemporaryAvatar(contentType, temporaryContentType, fileName, scaledFile.getResult(), null);
            ExecutingHttpRequest.get().getSession().setAttribute(SessionKeys.TEMP_AVATAR, tempAvatar);

            return new Result<TemporaryAvatarBean>(new TemporaryAvatarBean(getTemporaryAvatarUrl(),
                        image.getWidth(null), image.getHeight(null), isCroppingNeeded(image)));
        }
        else
        {
            return new Result<TemporaryAvatarBean>().addError(getText("avatarpicker.upload.filename.failure", invalidChars), ErrorCollection.Reason.VALIDATION_FAILED);
        }
    }

    @Override
    public Result<Avatar> convertTemporaryToReal(String ownerId, Avatar.Type type, Selection selection)
    {
        if (!isAllowedToPickAvatar(ownerId, type))
        {
            return new Result<Avatar>().addError(null, ErrorCollection.Reason.FORBIDDEN);
        }

        TemporaryAvatar temporaryAvatar = (TemporaryAvatar) ExecutingHttpRequest.get().getSession()
                .getAttribute(SessionKeys.TEMP_AVATAR);

        if (temporaryAvatar == null)
        {
            return new Result<Avatar>().addError(getText("avatarpicker.upload.failure"), ErrorCollection.Reason.SERVER_ERROR);
        }
        // check image crop insets make sense
        int maxOffset = MAX_SIDE_LENGTH - AvatarManager.ImageSize.LARGE.getPixels();
        if (selection.getTopLeftX() < 0 || selection.getTopLeftY() < 0 || selection.getTopLeftX() > maxOffset || selection.getTopLeftY() > maxOffset)
        {
            return new Result<Avatar>().addError(getText("avatarpicker.upload.failure"), ErrorCollection.Reason.VALIDATION_FAILED);
        }

        Avatar newAvatar =
                type == Avatar.Type.PROJECT ?
                    AvatarImpl.createCustomAvatar(temporaryAvatar.getOriginalFilename(), temporaryAvatar.getContentType(), projectManager.getProjectObj(Long.valueOf(ownerId)))
                    : AvatarImpl.createCustomAvatar(temporaryAvatar.getOriginalFilename(), temporaryAvatar.getContentType(), userUtil.getUserByKey(ownerId));
        try
        {
            newAvatar = avatarManager.create(newAvatar, new FileInputStream(temporaryAvatar.getFile()), selection);
        }
        catch (IOException e)
        {
            log.error("Unable to create avatar.", e);
            return new Result<Avatar>().addError(getText("avatarpicker.upload.temp.io", e.getMessage()), ErrorCollection.Reason.SERVER_ERROR);
        }

        if (temporaryAvatar.getFile().delete())
        {
            log.debug("Deleted temporary avatar file " + temporaryAvatar.getFile().getAbsolutePath());
        }
        else
        {
            log.info("Couldn't delete temporary avatar file. Will retry on shutdown. " + temporaryAvatar.getFile().getAbsolutePath());
        }

        return new Result<Avatar>(newAvatar);
    }

    @Override
    public Result<TemporaryAvatarBean> cropTemporary(String ownerId, Avatar.Type type, Selection selection)
    {
        if (!isAllowedToPickAvatar(ownerId, type))
        {
            return new Result<TemporaryAvatarBean>().addError(null, ErrorCollection.Reason.FORBIDDEN);
        }
        
        TemporaryAvatar temporaryAvatar = (TemporaryAvatar) ExecutingHttpRequest.get().getSession()
                .getAttribute(SessionKeys.TEMP_AVATAR);

        if (temporaryAvatar == null)
        {
            return new Result<TemporaryAvatarBean>().addError(getText("avatarpicker.upload.failure"), ErrorCollection.Reason.SERVER_ERROR);
        }

        TemporaryAvatar croppedAvatar = new TemporaryAvatar(temporaryAvatar.getContentType(), temporaryAvatar.getTemporaryContentType(), temporaryAvatar.getOriginalFilename(), temporaryAvatar.getFile(), selection);
        ExecutingHttpRequest.get().getSession().setAttribute(SessionKeys.TEMP_AVATAR, croppedAvatar);

        return new Result<TemporaryAvatarBean>(new TemporaryAvatarBean(getTemporaryAvatarUrl(), selection));
    }

    private boolean isAllowedToPickAvatar(String ownerId, Avatar.Type type)
    {
        return StringUtils.isBlank(ownerId)
                || (type == Avatar.Type.PROJECT && avatarManager.hasPermissionToEdit(authenticationContext.getUser(), projectManager.getProjectObj(Long.parseLong(ownerId))))
                || (type == Avatar.Type.USER && avatarManager.hasPermissionToEdit(authenticationContext.getUser(), userUtil.getUserByKey(ownerId)));
    }
    
    private boolean isCroppingNeeded(final Image image)
    {
        return !(image.getWidth(null) <= AvatarManager.ImageSize.LARGE.getPixels() && image.getHeight(null) == image.getWidth(null));
    }

    private Result<File> copyStreamToTempFile(InputStream stream, String fileName, long size)
    {
        final Result<File> fileResult = createTemporaryFile(null);
        if (!fileResult.isValid())
        {
            return fileResult;
        }
        File tempFile = fileResult.getResult();

        LimitedOutputStream limitedOutput = null;
        OutputStream outputStream = null;
        try
        {
            outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            if (size >= 0)
            {
                outputStream = limitedOutput = new LimitedOutputStream(new BufferedOutputStream(outputStream), size);
            }

            IOUtils.copy(stream, outputStream);

            //We want the close here. If we get an error flusing to the disk then we really need to know.
            outputStream.close();

            //This can only happen when the stream is too small. If the stream is too big we will get a
            //TooBigIOException which is caught below.
            if (limitedOutput != null && limitedOutput.getCurrentLength() != size)
            {
                deleteFileIfExists(tempFile);
                String text;
                if (limitedOutput.getCurrentLength() == 0)
                {
                    text = getText("avatarpicker.upload.size.zero");
                }
                else
                {
                    text = getText("avatarpicker.upload.size.wrong");
                }
                return new Result<File>().addError(text, ErrorCollection.Reason.VALIDATION_FAILED);
            }
            return fileResult;
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(outputStream);
            deleteFileIfExists(tempFile);

            if (e instanceof LimitedOutputStream.TooBigIOException)
            {
                LimitedOutputStream.TooBigIOException tooBigIOException = (LimitedOutputStream.TooBigIOException) e;
                return new Result<File>().addError(getText("avatarpicker.upload.size.toobig", fileName, FileSize.format(tooBigIOException.getMaxSize())), ErrorCollection.Reason.VALIDATION_FAILED);
            }
            else
            {
                //JRADEV-5540: This is probably caused by some kind of client i/o error (e.g. disconnect). Not much point of logging it as we send
                // back an error reason anyways.
                log.debug("I/O error occured while attaching file.", e);
                return new Result<File>().addError(getText("attachfile.error.io.error", fileName, e.getMessage()), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
    }

    private String getBaseUrl()
    {
        return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

    private boolean isImageContent(final String contentType)
    {
        return contentType != null && CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    private ResultAndContentTypeChanged clampSize(final File sourceFile, int edgeSize)
    {
        Image sourceImage = null;
        try
        {
            sourceImage = thumber.getImage(sourceFile);
        }
        catch (IOException e)
        {
            log.error(e);
            new Result<File>().addError(getText("avatarpicker.upload.image.corrupted"), ErrorCollection.Reason.VALIDATION_FAILED);
        }

        if (sourceImage == null)
        {
            return new ResultAndContentTypeChanged(new Result<File>().addError(getText("avatarpicker.upload.image.corrupted"), ErrorCollection.Reason.VALIDATION_FAILED), false);
        }

        final int sourceHeight = sourceImage.getHeight(null);
        final int sourceWidth = sourceImage.getWidth(null);

        final int maxPixels = getMaxMegaPixels();
        if (sourceHeight * sourceWidth > (maxPixels * 1000000))
        {
            return new ResultAndContentTypeChanged(new Result<File>().addError(getText("avatarpicker.upload.too.big", maxPixels), ErrorCollection.Reason.VALIDATION_FAILED), false);
        }

        if (sourceHeight > edgeSize || sourceWidth > edgeSize)
        {
            return cropImageToFile(edgeSize, sourceImage);
        }
        else
        {
            return new ResultAndContentTypeChanged(new Result<File>(sourceFile), false);
        }
    }

    static class ResultAndContentTypeChanged
    {
        private final Result<File> result;
        private final boolean contentTypeChanged;

        ResultAndContentTypeChanged(Result<File> result, boolean contentTypeChanged) {
            this.result = result;
            this.contentTypeChanged = contentTypeChanged;
        }

        public Result<File> getResult()
        {
            return result;
        }

        public boolean isContentTypeChanged()
        {
            return contentTypeChanged;
        }
    }

    private ResultAndContentTypeChanged cropImageToFile(final int edgeSize, final Image sourceImage)
    {
        Result<File> prescaledFile = createTemporaryFile(TEMP_FILE_EXTENSION);
        if (!prescaledFile.isValid())
        {
            return new ResultAndContentTypeChanged(prescaledFile, false);
        }

        Thumber.WidthHeightHelper dimensions = thumber.determineScaleSize(edgeSize, edgeSize, sourceImage.getWidth(null), sourceImage.getHeight(null));
        BufferedImage scaledImage = thumber.scaleImage(sourceImage, dimensions);
        try
        {
            ImageIO.write(scaledImage, AVATAR_IMAGE_FORMAT, prescaledFile.getResult());
            return new ResultAndContentTypeChanged(prescaledFile, true);
        }
        catch (IOException e)
        {
            return new ResultAndContentTypeChanged(new Result<File>().addError(getText("avatarpicker.upload.temp.io", e.getMessage()), ErrorCollection.Reason.SERVER_ERROR), false);
        }
    }


    private int getMaxMegaPixels()
    {
        final String megaPixels = applicationProperties.getDefaultBackedString("jira.avatar.megapixels");
        if (StringUtils.isNotBlank(megaPixels) && StringUtils.isNumeric(megaPixels))
        {
            return Integer.parseInt(megaPixels);
        }

        return DEFAULT_MAX_MEGAPIXELS;
    }

//    public

    public String getTemporaryAvatarUrl()
    {
        // if the user chooses a new temporary avatar we need to keep making this url unique so that the javascript that
        // ajaxly retrieves this url always gets a unique one, forcing the browser to keep the image fresh
        return getBaseUrl() + "/secure/temporaryavatar?cropped=true&magic=" + System.currentTimeMillis();
    }

    private static void deleteFileIfExists(final File file)
    {
        if (file.exists() && !file.delete())
        {
            log.warn("Unable to delete file '" + file + "'.");
        }
    }

    private String getText(String text)
    {
        return authenticationContext.getI18nHelper().getText(text);
    }

    private String getText(String text, Object... args)
    {
        return authenticationContext.getI18nHelper().getText(text, args);
    }

    private Result<File> createTemporaryFile(String suffix)
    {
        try
        {
            final File tempFile = File.createTempFile(TEMP_FILE_PREFIX, suffix);
            tempFile.deleteOnExit();
            return new Result<File>(tempFile);
        }
        catch (IOException e)
        {
            return new Result<File>().addError(getText("avatarpicker.upload.temp.fail"), ErrorCollection.Reason.SERVER_ERROR);
        }
    }

    public static class Result<T>
    {

        ErrorCollection errorCollection = new SimpleErrorCollection();

        private T result;

        Result()
        {
        }

        Result(T t)
        {
            this.result = t;
        }

        Result(Result<?> result)
        {
            errorCollection = result.getErrorCollection();
        }

        public ErrorCollection getErrorCollection()
        {
            return errorCollection;
        }

        public T getResult()
        {
            return result;
        }

        public boolean isValid()
        {
            return !errorCollection.hasAnyErrors();
        }

        Result<T> setResult(T t)
        {
            this.result = t;
            return this;
        }

        Result<T> addError(String message, ErrorCollection.Reason reason)
        {
            errorCollection.addErrorMessage(message, reason);
            return this;
        }
    }

    public static class TemporaryAvatarBean
    {

        private final String url;
        private final boolean needsCropping;
        private int cropperWidth = -1;
        private int cropperOffsetX = -1;
        private int cropperOffsetY = -1;

        public TemporaryAvatarBean(final String url, int width, final int height, final boolean needsCropping)
        {
            this.url = url;
            this.needsCropping = needsCropping;
            this.setCroppingCoordinates(width, height);
        }
        
        public TemporaryAvatarBean(final String url, final Selection selection)
        {
            this.url = url;
            this.needsCropping = true;
            this.setCroppingCoordinates(selection);
        }

        public boolean isCroppingNeeded()
        {
            return needsCropping;
        }

        public String getUrl()
        {
            return url;
        }

        public int getCropperWidth()
        {
            return cropperWidth;
        }

        public int getCropperOffsetX()
        {
            return cropperOffsetX;
        }

        public int getCropperOffsetY()
        {
            return cropperOffsetY;
        }

        private void setCroppingCoordinates(final int width, final int height)
        {
            final int baseSize = AvatarManager.ImageSize.LARGE.getPixels();

            if (width <= baseSize)
            {
                cropperOffsetX = 0;
            }
            else
            {
                cropperOffsetX = (width - baseSize) / 5;
            }
            if (height <= baseSize)
            {
                cropperOffsetY = 0;
            }
            else
            {
                cropperOffsetY = (height - baseSize) / 5;
            }

            if (this.cropperWidth == -1)
            {
                final int minWidth = Math.min(width, height);

                if (minWidth <= baseSize)
                {
                    this.cropperWidth = minWidth;
                }
                else
                {
                    this.cropperWidth = baseSize + ((minWidth - baseSize) / 3);
                }
            }
        }
        
        private void setCroppingCoordinates(final Selection selection)
        {
            cropperWidth = selection.getWidth();
            cropperOffsetX = selection.getTopLeftX();
            cropperOffsetY = selection.getTopLeftY();
        }
    }
}



