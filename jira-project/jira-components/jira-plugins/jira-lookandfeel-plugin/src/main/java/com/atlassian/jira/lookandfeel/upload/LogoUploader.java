package com.atlassian.jira.lookandfeel.upload;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.lookandfeel.ImageScaler;
import com.atlassian.jira.lookandfeel.LookAndFeelConstants;
import com.atlassian.jira.lookandfeel.image.ImageDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Uploads logo to well known location Also scales and stores scaled copy If a favicon also stores hires and lores
 * scales
 *
 * @since v4.4
 */
public class LogoUploader
{

    private final ApplicationProperties applicationProperties;
    private final JiraHome jiraHome;
    private final ImageScaler imageScaler;
    private final I18nHelper i18nHelper;
    private final UploadService uploadService;
    private int resizedWidth;
    private int resizedHeight;
    private static final int FAVICON_HIRES_SIZE = 32;
    private static final int FAVICON_SIZE = 16;
    private static final String LOGO_OUTPUT_FORMAT = "png";

    /**
     * We must support processing these types.  This is the same as the supported types in AvatarPicker
     */
    private static final List<String> CONTENT_TYPES = Lists.newArrayList(
            "image/jpeg", "image/gif", "image/png", "image/pjpeg", "image/x-png"
    );

    private final List<String> errorMessages = Lists.newArrayList();

    public LogoUploader(final ApplicationProperties applicationProperties, final JiraHome jiraHome, final ImageScaler imageScaler, final I18nHelper i18nHelper, UploadService uploadService)
    {

        this.applicationProperties = applicationProperties;
        this.jiraHome = jiraHome;
        this.imageScaler = imageScaler;
        this.i18nHelper = i18nHelper;
        this.uploadService = uploadService;
    }

    /**
     * Resize image's height to LOGO_MAX_HEIGHT and scale width accordingly
     *
     * @param image Image to be resized
     * @return scaled Image
     * @throws java.io.IOException when the image file can't be read
     */
    private BufferedImage resizeLogo(BufferedImage image) throws IOException
    {
        BufferedImage scaledImage = imageScaler.scaleImageToMaxHeight(image, LookAndFeelConstants.LOGO_MAX_HEIGHT);

        this.resizedWidth = scaledImage.getWidth();
        this.resizedHeight = scaledImage.getHeight();

        return scaledImage;
    }

    private Map<String, BufferedImage> resizeFavicon(BufferedImage image, String hiResFilename, String scaledFilename)
            throws IOException
    {
        Map favicons = Maps.<String, BufferedImage>newHashMap();
        BufferedImage scaledHiResImage = imageScaler.scaleImageToSquare(image, FAVICON_HIRES_SIZE, true);
        favicons.put(hiResFilename, scaledHiResImage);
        BufferedImage scaledImage = imageScaler.scaleImageToSquare(image, FAVICON_SIZE, true);
        favicons.put(scaledFilename, scaledImage);

        this.resizedWidth = scaledImage.getWidth();
        this.resizedHeight = scaledImage.getHeight();

        return favicons;
    }

    private void writeImageFile(BufferedImage image, String output, String filename) throws IOException
    {
        File logoDirectory = uploadService.getLogoDirectory();
        File logoFile = new File(logoDirectory, filename);

        ImageIO.write(image, output, logoFile);
    }

    private void writeIEFavicon(List<BufferedImage> images, String ieFaviconFilename) throws IOException
    {
        File logoDirectory = uploadService.getLogoDirectory();
        File logoFile = new File(logoDirectory, ieFaviconFilename);
        ICOEncoder.write(images, logoFile);


    }

    /**
     * Read the uploaded file, resize it and save it to the logo directory.
     *
     * @param imageData Image data of the logo to upload
     * @param originalFilename Logo will be saved unaltered to this file path
     * @throws IOException when the file can't be read
     */
    private boolean saveLogoImageData(InputStream imageData, String originalFilename, String rescaledFilename)
            throws IOException
    {
        BufferedImage image = ImageIO.read(imageData);

        if (image != null)
        {
            BufferedImage scaledImage = resizeLogo(image);
            writeImageFile(scaledImage, LOGO_OUTPUT_FORMAT, rescaledFilename);
            writeImageFile(image, LOGO_OUTPUT_FORMAT, originalFilename);
            return true;
        }

        return false;
    }

    /**
     * Read the uploaded file, resize and crop it and save it to the logo directory.
     */
    private boolean saveFaviconImageData(InputStream imageData, String originalFilename, String hiResFilename, String scaledFilename, String ieFaviconFilename)
            throws IOException
    {
        BufferedImage image = ImageIO.read(imageData);

        if (image != null)
        {
            Map<String, BufferedImage> resizedImages = resizeFavicon(image, hiResFilename, scaledFilename);
            for (String path : resizedImages.keySet())
            {
                writeImageFile(resizedImages.get(path), LOGO_OUTPUT_FORMAT, path);
            }
            writeImageFile(image, LOGO_OUTPUT_FORMAT, originalFilename);
            writeIEFavicon(new ArrayList<BufferedImage>(resizedImages.values()), ieFaviconFilename);
            return true;
        }

        return false;
    }


    public String saveLogo(InputStream imageData, String originalFilename, String rescaledFilename)
    {
        try
        {
            if (saveLogoImageData(imageData, originalFilename, rescaledFilename))
            {
                return rescaledFilename;
            }
        }
        catch (IOException e)
        {
            errorMessages.add(e.getMessage());
        }

        return null;
    }

    public String saveFavicon(InputStream imageData, String originalFilename, String hiResFilename, String scaledFilename, String ieFaviconFilename)
    {

        try
        {
            if (saveFaviconImageData(imageData, originalFilename, hiResFilename, scaledFilename, ieFaviconFilename))
            {
                return scaledFilename;
            }
        }
        catch (IOException e)
        {
            errorMessages.add(e.getMessage());
        }

        return null;
    }

    public void saveDefaultLogo(URL url, String originalFilename, String scaledFilename)
    {
        BufferedImage image = null;
        try
        {
            InputStream imageData = url.openStream();
            image = ImageIO.read(imageData);

            saveDefaultLogo(image, originalFilename, scaledFilename);
        }
        catch (IOException e)
        {
            errorMessages.add(e.getMessage());
        }

    }

    public Dimension saveDefaultLogo(BufferedImage image, String originalFilename, String scaledFilename)
    {
        Dimension dimension = new Dimension(image.getWidth(), image.getHeight());
        try
        {
            if (image != null)
            {
                BufferedImage scaledImage = imageScaler.scaleImageToMaxHeight(image, LookAndFeelConstants.LOGO_MAX_HEIGHT);
                writeImageFile(scaledImage, LOGO_OUTPUT_FORMAT, scaledFilename);
                writeImageFile(image, LOGO_OUTPUT_FORMAT, originalFilename);
                dimension.setSize(scaledImage.getWidth(), scaledImage.getHeight());
            }
        }
        catch (IOException e)
        {
            errorMessages.add(e.getMessage());
        }
        return dimension;

    }

    public Dimension saveDefaultFavicOn(BufferedImage image, String originalFilename, String scaledFilename)
    {

        Dimension dimension = new Dimension(FAVICON_HIRES_SIZE, FAVICON_HIRES_SIZE);
        try
        {
            if (image != null)
            {
                BufferedImage scaledImage = imageScaler.scaleImageToSquare(image, FAVICON_HIRES_SIZE, true);
                writeImageFile(scaledImage, LOGO_OUTPUT_FORMAT, scaledFilename);
                writeImageFile(image, LOGO_OUTPUT_FORMAT, originalFilename);
            }
        }
        catch (IOException e)
        {
            errorMessages.add(e.getMessage());
        }
        return dimension;
    }


    public int getResizedWidth()
    {
        return resizedWidth;
    }

    public int getResizedHeight()
    {
        return resizedHeight;
    }

    public boolean validate(ImageDescriptor imageDescriptor)
    {
        if (StringUtils.isNotEmpty(imageDescriptor.getImageName()))
        {
            if (!isContentTypeSupported(imageDescriptor.getContentType()))
            {
                addErrorMessage(i18nHelper.getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), imageDescriptor.getImageName()));
                addErrorMessage(i18nHelper.getText("jira.lookandfeel.upload.mimetype.unsupported", imageDescriptor.getContentType()));
            }
            if (imageDescriptor.getInputStream() == null)
            {
                addErrorMessage(i18nHelper.getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), imageDescriptor.getImageName()));
            }
        }
        else
        {
            addErrorMessage(i18nHelper.getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), null));
        }
        return errorMessages.isEmpty();
    }


    /**
     * <p>Returns true if and only if contentType is not empty and contentType.toLowerCase() contains at least one member of the CONTENT_TYPES collection.</p>
     * <ol>
     *     <li> isContentTypeSupported("image/png") = true </li>
     *     <li> isContentTypeSupported("charset=utf8; image/png") = true </li>
     *     <li> isContentTypeSupported("charset=utf8") = false </li>
     * </ol>
     * @param contentType
     * @return
     */
    private boolean isContentTypeSupported(final String contentType)
    {
        if (StringUtils.isNotBlank(contentType))
        {
            final String lowerCaseContentType = contentType.toLowerCase();
            return Iterables.any(CONTENT_TYPES, new Predicate<String>()
            {
                @Override
                public boolean apply(@Nullable String validContentType)
                {
                    return lowerCaseContentType.contains(validContentType);
                }
            });
        }
        else {
            return false;
        }
    }

    public List<String> getErrorMessages()
    {
        return errorMessages;
    }

    private void addErrorMessage(String errorMessage)
    {
        errorMessages.add(errorMessage);
    }
}
