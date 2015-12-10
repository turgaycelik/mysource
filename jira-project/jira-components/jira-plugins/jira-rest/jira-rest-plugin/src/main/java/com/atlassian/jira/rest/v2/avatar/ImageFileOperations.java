package com.atlassian.jira.rest.v2.avatar;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.jira.avatar.AvatarFormat;
import com.atlassian.jira.avatar.ImageScaler;
import com.atlassian.jira.avatar.Selection;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import org.springframework.stereotype.*;

@org.springframework.stereotype.Component
class ImageFileOperations
{
    private static final String AVATAR_IMAGE_FORMAT = "png";
    public static final AvatarFormat AVATAR_IMAGE_FORMAT_FULL = new AvatarFormat(AVATAR_IMAGE_FORMAT, "image/png");

    private final Thumber thumber;

    public ImageFileOperations()
    {
        this.thumber = new Thumber();
    }

    public Image getImageFromFile(final File sourceFile) throws IOException
    {
        Image sourceImage = thumber.getImage(sourceFile);

        if (sourceImage == null)
        {
            throw new IOException("invalid image format: " + sourceFile.getName());
        }
        return sourceImage;
    }

    public UploadedAvatar scaleImageToTempFile(final Image sourceImage, final File targetFile, final int edgeSize)
            throws IOException
    {
        Thumber.WidthHeightHelper targetDiemnsion = thumber.determineScaleSize(edgeSize, edgeSize, sourceImage.getWidth(null), sourceImage.getHeight(null));
        BufferedImage scaledImage = thumber.scaleImage(sourceImage, targetDiemnsion);

        ImageIO.write(scaledImage, AVATAR_IMAGE_FORMAT, targetFile);

        return new UploadedAvatar(targetFile, AVATAR_IMAGE_FORMAT_FULL.getContentType(), targetDiemnsion.getWidth(), targetDiemnsion.getHeight());
    }
}
