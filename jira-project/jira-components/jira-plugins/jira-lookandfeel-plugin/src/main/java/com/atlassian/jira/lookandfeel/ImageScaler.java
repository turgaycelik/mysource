package com.atlassian.jira.lookandfeel;

import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.core.util.thumbnail.Thumbnail;

import java.awt.image.BufferedImage;

/**
 * Scales images
 *
 * @since v4.4
 */
public class ImageScaler
{
    private final Thumber thumber = new Thumber(Thumbnail.MimeType.PNG);


  /**
     *
     * @param image             The {@link BufferedImage} you want to scale
     * @param maxHeight       The maximum height of the scaled image
     * @return  a ratio scaled BufferedImage if the height was greater than the maximum height specified, otherwise the image is unchanged
     */
    public BufferedImage scaleImageToMaxHeight(BufferedImage image, int maxHeight)
    {
        final int imageHeight = image.getHeight(null);
        if (imageHeight <= maxHeight) {
            return image;
        }
        final int imageWidth =  image.getWidth(null);
        final double ratio = (double) imageHeight / maxHeight;
        final int maxWidth = (int) (imageWidth / ratio);

        Thumber.WidthHeightHelper dimensionHelper  = thumber.determineScaleSize(maxWidth, maxHeight, imageWidth, imageHeight);
        return thumber.scaleImage(image,  dimensionHelper);
    }

    /**
       *
       * @param image             The {@link BufferedImage} you want to scale
       * @param maxWidth        The maximum width of the scaled image
       * @return  a ratio scaled BufferedImage if the height was greater than the maximum width specified, otherwise the image is unchanged
       */

    public BufferedImage scaleImageToMaxWidth(BufferedImage image, int maxWidth)
    {
        final int imageWidth =  image.getWidth(null);
        if (imageWidth <= maxWidth) {
            return image;
        }
        final int imageHeight = image.getHeight(null);
        final double ratio = (double) imageWidth / maxWidth;
        final int maxHeight = (int) (imageHeight / ratio);

        Thumber.WidthHeightHelper dimensionHelper  = thumber.determineScaleSize(maxWidth, maxHeight, imageWidth, imageHeight);
        return thumber.scaleImage(image,  dimensionHelper);
    }

    /**
     *
     * @param image           The {@link BufferedImage} you want to scale
     * @param maxSize        The maximum edge size of the scaled image
     * @param crop             If the image is rectangular rather than square, then the image will be cropped.
     * @return   a scaled image
     */
    public BufferedImage scaleImageToSquare(BufferedImage image, int maxSize, boolean crop)
    {
        final int imageWidth =  image.getWidth(null);
        final int imageHeight = image.getHeight(null);
        if (imageWidth <= maxSize && imageHeight <= maxSize) {
            return image;
        }
        BufferedImage scaledImage = image;
        if (crop && imageWidth != imageHeight)
        {
            scaledImage = cropImage(image);
        }
        Thumber.WidthHeightHelper dimensionHelper  = thumber.determineScaleSize(maxSize, maxSize,
                 scaledImage.getWidth(null), scaledImage.getHeight(null));
        return thumber.scaleImage(scaledImage,  dimensionHelper);
    }


    private BufferedImage cropImage(BufferedImage image)
    {
        final int imageWidth =  image.getWidth(null);
        final int imageHeight = image.getHeight(null);
        int largestSquareWidth = Math.min(imageWidth, imageHeight);

        int xOffset = (imageWidth - largestSquareWidth) / 2;
        int yOffset = (imageHeight - largestSquareWidth) / 2;
        return image.getSubimage(xOffset, yOffset, largestSquareWidth, largestSquareWidth);
    }


}
