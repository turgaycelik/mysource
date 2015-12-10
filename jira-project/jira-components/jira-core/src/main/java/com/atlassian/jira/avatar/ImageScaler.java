package com.atlassian.jira.avatar;

import org.j3d.util.ImageGenerator;

import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.RenderedImage;

/**
 * Helper class for managing image scaling for Avatars.
 *
 * @since v4.0
 */
public class ImageScaler
{

    /**
     * Creates an image consisting of the given selection of the source image rescaled to a square with the given size.
     * If the selection is null, a not-entirely-stupid algorithm for determining a plausible square subselection.
     *
     * @param sourceImage the image source to crop and scale from.
     * @param crop the subrectangle to use, usually square to avoid aspect ratio changes.
     * @param size the desired size of the edge of the resulting square image.
     * @return the new image.
     */
    public RenderedImage getSelectedImageData(final BufferedImage sourceImage, Selection crop, final int size)
    {
        if (crop == null)
        {
            crop = autoCalculateImageCropSelection(sourceImage.getWidth(), sourceImage.getHeight(), size);
        }

        CropImageFilter cropFilter = new CropImageFilter(crop.getTopLeftX(), crop.getTopLeftY(), crop.getWidth(), crop.getHeight());
        AreaAveragingScaleFilter scaleFilter = new AreaAveragingScaleFilter(size, size);

        FilteredImageSource croppedImageProducer = new FilteredImageSource(sourceImage.getSource(), cropFilter);
        FilteredImageSource croppedAndResizedImageProducer = new FilteredImageSource(croppedImageProducer, scaleFilter);

        ImageGenerator generator = new ImageGenerator();
        croppedAndResizedImageProducer.startProduction(generator);
        BufferedImage scaledImage = generator.getImage();
        scaledImage.flush();
        return scaledImage;
    }

    /**
     * Find the largest square that fits inside the destination image.
     *
     * @param imageWidth  original image width.
     * @param imageHeight original image height.
     * @param targetSize  the size of the destination avatar.
     * @return description of where to crop a square out of that image.
     */
    private Selection autoCalculateImageCropSelection(int imageWidth, int imageHeight, int targetSize)
    {
        if (imageWidth <= targetSize && imageHeight <= targetSize)
        {
            return new Selection(0, 0, imageWidth, imageHeight);
        }

        // Find the largest square that can be entirely filled by the source image that is no
        // smaller than the final image.
        int largestSquareWidth = Math.min(imageWidth, imageHeight);

        // Centre that bounding square on the source image. The selection is all of the image
        // that falls inside the square.
        int xOffset = (imageWidth - largestSquareWidth) / 2;
        int yOffset = (imageHeight - largestSquareWidth) / 2;

        //noinspection SuspiciousNameCombination
        return new Selection(xOffset, yOffset, largestSquareWidth, largestSquareWidth);
    }

}
