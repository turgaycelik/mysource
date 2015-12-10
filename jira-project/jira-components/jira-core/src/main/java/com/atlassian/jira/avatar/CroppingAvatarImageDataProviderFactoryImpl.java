package com.atlassian.jira.avatar;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.atlassian.core.util.thumbnail.Thumber;

public class CroppingAvatarImageDataProviderFactoryImpl implements CroppingAvatarImageDataProviderFactory
{
    private final AvatarTagger avatarTagger;
    private final String targetFormat;
    private final Thumber thumber;
    private final com.atlassian.jira.avatar.ImageScaler scaler;

    public CroppingAvatarImageDataProviderFactoryImpl(final AvatarTagger avatarTagger, final ImageScaler scaler)
    {
        this.scaler = scaler;
        thumber = new Thumber();
        this.avatarTagger = avatarTagger;
        this.targetFormat = "png";
    }

    @Override
    public AvatarImageDataProvider createStreamsFrom(final InputStream uploadedImage, final Selection cropping)
            throws IOException
    {
        return new AvatarImageDataProviderImpl(uploadedImage, cropping);
    }

    private class AvatarImageDataProviderImpl implements AvatarImageDataProvider
    {
        final BufferedImage uploadedImage;
        final Selection cropping;

        private AvatarImageDataProviderImpl(final InputStream uploadedImage, final Selection cropping)
                throws IOException
        {
            // rather should accept image?
            this.uploadedImage = thumber.getImage(uploadedImage);
            this.cropping = cropping;
        }

        @Override
        public void storeImage(final Avatar.Size requestSize, final OutputStream output) throws IOException
        {
            cropImageToSize(uploadedImage, cropping, requestSize, output);
        }
    }

    private void cropImageToSize(final BufferedImage imageData, final Selection croppingSelection, final Avatar.Size size, OutputStream target)
            throws IOException
    {

        final int pixels = size.getPixels();
        RenderedImage image = cropImage(imageData, croppingSelection, pixels);

        avatarTagger.saveTaggedAvatar(image, targetFormat, target);
    }

    private RenderedImage cropImage(final BufferedImage image, final Selection croppingSelection, final int pixels)
            throws IOException
    {
        return scaler.getSelectedImageData(image, croppingSelection, pixels);
    }
}
