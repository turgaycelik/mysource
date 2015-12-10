package com.atlassian.jira.rest.v2.avatar;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import com.atlassian.core.util.FileSize;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LimitedOutputStream;
import com.atlassian.jira.web.util.FileNameCharacterCheckerUtil;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Creates correct avatar from stream uploaded by user
 */
@Component
public class TemporaryAvatarUploader
{
    /**
     * JDK must support processing the types. THESE MUST BE STORED IN lower case. Checked
     * http://reference.sitepoint.com/html/mime-types-full
     */
    private static final java.util.List<String> CONTENT_TYPES = ImmutableList.of("image/jpeg", "image/gif", "image/png", "image/pjpeg", "image/x-png");
    private static final int DEFAULT_MAX_MEGAPIXELS = 5;
    /**
     * Fits the popup window.
     */
    public static final int MAX_SIDE_LENGTH = 500;
    private static final Logger log = Logger.getLogger(TemporaryAvatarUploader.class);
    private static final String TEMP_FILE_PREFIX = "JIRA-avatar";
    private static final String TEMP_FILE_EXTENSION = ".png";


    private final I18nHelper i18nHelper;
    private final ApplicationProperties applicationProperties;
    private final FileUtil fileUtil;
    private final ImageFileOperations imageFileOperations;

    @Inject
    public TemporaryAvatarUploader(final I18nHelper i18nHelper, final ApplicationProperties applicationProperties, FileUtil fileUtil, final ImageFileOperations imageFileOperations)
    {
        this.i18nHelper = i18nHelper;
        this.applicationProperties = applicationProperties;
        this.fileUtil = fileUtil;
        this.imageFileOperations = imageFileOperations;
    }

    public UploadedAvatar createUploadedAvatarFromStream(
            InputStream stream, String fileName, String contentType, long size)
            throws IOException, ValidationException
    {
        validateFile(fileName, contentType);

        File uploadedFile = createTempFileFromBoundedStream(stream, fileName, size);

        UploadedAvatar uploadResult = shrinkImageDimensionsToSize(uploadedFile, contentType, MAX_SIDE_LENGTH);

        return uploadResult;
    }

    public UploadedAvatar createUploadedAvatarFromStream(final InputStream inputStream, final String filename, final String contentType)
            throws IOException, ValidationException
    {
        validateFile( filename, contentType );
        File uploadedFile = fileUtil.createTempFile(inputStream, TEMP_FILE_PREFIX);

        UploadedAvatar uploadResult = shrinkImageDimensionsToSize(uploadedFile, contentType, MAX_SIDE_LENGTH);

        return uploadResult;
    }

    private void validateFile(final String fileName, final String contentType) throws ValidationException
    {
        if (!isImageContent(contentType))
        {
            log.info("Received avatar upload with unsupported content type: " + contentType);
            throw new ValidationException(getText("avatarpicker.upload.contenttype.failure"));
        }
        final String invalidChars = new FileNameCharacterCheckerUtil().assertFileNameDoesNotContainInvalidChars(fileName);
        if (null != invalidChars)
        {
            throw new ValidationException(
                    i18nHelper.getText("avatarpicker.upload.filename.failure", invalidChars));
        }
    }

    private UploadedAvatar shrinkImageDimensionsToSize(final File sourceFile, String originalContentTyppe, int edgeSize)
            throws ValidationException, IOException
    {
        final Image sourceImage;
        try
        {
            sourceImage = imageFileOperations.getImageFromFile(sourceFile);
        }
        catch (IOException e)
        {
            throw new ValidationException(i18nHelper.getText("avatarpicker.upload.image.corrupted"));
        }

        final int sourceHeight = sourceImage.getHeight(null);
        final int sourceWidth = sourceImage.getWidth(null);

        final int maxPixels = getMaxMegaPixels();
        if (sourceHeight * sourceWidth > (maxPixels * 1000000))
        {
            throw new ValidationException(i18nHelper.getText("avatarpicker.upload.too.big", maxPixels));
        }

        if (sourceHeight > edgeSize || sourceWidth > edgeSize)
        {
            try
            {
                File targetFile = fileUtil.createTemporaryFile(TEMP_FILE_PREFIX, TEMP_FILE_EXTENSION);
                return imageFileOperations.scaleImageToTempFile(sourceImage, targetFile, edgeSize);
            }
            catch (IOException x)
            {
                throw new IOException(i18nHelper.getText("avatarpicker.upload.temp.io", x.getMessage()), x);
            }
        }
        else
        {
            return new UploadedAvatar(sourceFile, originalContentTyppe, sourceWidth, sourceHeight);
        }
    }

    private File createTempFileFromBoundedStream(final InputStream stream, final String fileName, final long size)
            throws ValidationException, IOException
    {
        if (0 >= size)
        {
            throw new ValidationException(getText("avatarpicker.upload.size.zero"));
        }

        try
        {
            return fileUtil.createTempFileFromBoundedStream(stream, size, TEMP_FILE_PREFIX);
        }
        catch (FileUtil.StreamSizeMismatchException sizeMismatch)
        {
            throw new ValidationException(getText("avatarpicker.upload.size.wrong"));
        }
        catch (LimitedOutputStream.TooBigIOException tooBigIOException)
        {
            throw new ValidationException(i18nHelper.getText("avatarpicker.upload.size.toobig", fileName, FileSize.format(tooBigIOException.getMaxSize())));
        }
        catch (IOException ioException)
        {
            throw new IOException(i18nHelper.getText("attachfile.error.io.error", fileName, ioException.getMessage()), ioException);
        }
    }

    private boolean isImageContent(final String contentType)
    {
        return contentType != null && CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    private String getText(String key)
    {
        return i18nHelper.getText(key);
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
}
