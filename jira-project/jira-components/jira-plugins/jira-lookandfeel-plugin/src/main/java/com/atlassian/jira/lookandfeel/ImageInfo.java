package com.atlassian.jira.lookandfeel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageInfo
{
    private final BufferedImage image;

    public ImageInfo(final BufferedImage image)
    {
        this.image = image;
    }

    public HSBColor getTopLeftPixel()
    {
        return getPixel(0, 0);
    }

    public HSBColor getTopRightPixel()
    {
        return getPixel(image.getWidth() - 1, 0);
    }

    public HSBColor getBottomLeftPixel()
    {
        return getPixel(0, image.getHeight() - 1);
    }

    public HSBColor getBottomRightPixel()
    {
        return getPixel(image.getWidth() - 1, image.getHeight() - 1);
    }

    public List<ColorCount> getColorRatio()
    {
        final ColorCounter counter = new ColorCounter();

        for (int x = 0; x < image.getWidth(); x++)
        {
            for (int y = 0; y < image.getHeight(); y++)
            {
                counter.addColor(getPixel(x, y));
            }
        }

        return counter.getColorRatio();
    }

    public Map<HSBColor, Integer> getColorRatioAsMap()
    {
        final ColorCounter counter = new ColorCounter();

        for (int x = 0; x < image.getWidth(); x++)
        {
            for (int y = 0; y < image.getHeight(); y++)
            {
                counter.addColor(getPixel(x, y));
            }
        }

        return counter.getColorRatioAsMap();
    }

    public static List<ColorCount> getSimilarColorRatio(final HSBColor ... colors)
    {
        final ColorCounter counter = new ColorCounter();

        for (final HSBColor color : colors)
        {
            counter.addColor(color);
        }

        return counter.getSimilarColorRatio();
    }

    public HSBColor getFirstNonMonochromeColor(final float minPercentage)
    {
        final List<ColorCount> colorRatio = getColorRatio();
        for (final ColorCount colorCount : colorRatio)
        {
            if (!isMonochrome(colorCount.getColor()))
            {
                if (colorCount.getPercentage() < minPercentage)
                {
                    return null;
                }
                return colorCount.getColor();
            }
        }

        return null;
    }

    public Integer getMostCommonHue()
    {
        final Map<Integer, HueCounterPair> hues = getHueRatio();

        // Rank the hues from most common to least common
        final List<HueCounterPair> orderedHues = new ArrayList<HueCounterPair>(hues.values());
        Collections.sort(orderedHues);
        Collections.reverse(orderedHues);

        // Add together hues that are close together
        final Map<Integer, Counter> commonHues = new HashMap<Integer, Counter>();

        for (final HueCounterPair pair : orderedHues)
        {
            Counter counter = commonHues.get(pair.getHue());
            if (counter == null)
            {
                // Find a hue close to this one
                for (final Integer commonHue : commonHues.keySet())
                {
                    if (Math.abs(pair.getHue() - commonHue) < 15)
                    {
                        counter = commonHues.get(commonHue);
                        break;
                    }
                }
            }

            if (counter == null)
            {
                // No similar hue found
                counter = new Counter();
                commonHues.put(pair.getHue(), counter);
            }

            counter.increment(pair.getCounter().value());
        }

        // Get hue with the highest count
        Integer hueWithMax = null;
        for (final Map.Entry<Integer, Counter> entry : commonHues.entrySet())
        {
            if (hueWithMax == null || entry.getValue().value() > commonHues.get(hueWithMax).value())
            {
                hueWithMax = entry.getKey();
            }
        }

        // Make sure the most common hue appears in more than 3 pixels
        if (hueWithMax != null && commonHues.get(hueWithMax).value() > 3)
        {
            return hueWithMax;
        }

        return null;
    }

    private Map<Integer, HueCounterPair> getHueRatio()
    {
        final Map<Integer, HueCounterPair> hues = new HashMap<Integer, HueCounterPair>();
        final List<ColorCount> colorRatio = getColorRatio();
        for (final ColorCount colorCount : colorRatio)
        {
            if (!isMonochrome(colorCount.getColor()) && !colorCount.getColor().hasTransparency())
            {
                final int hue = colorCount.getColor().getHue();
                HueCounterPair pair = hues.get(hue);
                if (pair == null)
                {
                    pair = new HueCounterPair(hue, new Counter());
                    hues.put(hue, pair);
                }
                pair.getCounter().increment(colorCount.getCount());
            }
        }

        return hues;
    }

    public HSBColor getMostInterestingColorWithHue(final int hue)
    {
        HSBColor color = null;

        final List<ColorCount> colorRatio = getColorRatio();
        for (final ColorCount colorCount : colorRatio)
        {
            if (colorCount.getColor().getHue() == hue)
            {
                if (color == null)
                {
                    color = colorCount.getColor();
                }
                else if (colorCount.getColor().getSaturation() > color.getSaturation())
                {
                    color = colorCount.getColor();
                }
            }
        }

        return color;
    }

    public HSBColor getAverageColorWithHue(final int hue)
    {
        long saturationSum = 0;
        long brightnessSum = 0;
        int nbPixels = 0;

        final List<ColorCount> colorRatio = getColorRatio();
        for (final ColorCount colorCount : colorRatio)
        {
            if (colorCount.getColor().getHue() == hue)
            {
                saturationSum += (colorCount.getColor().getSaturation() * colorCount.getCount());
                brightnessSum += (colorCount.getColor().getBrightness() * colorCount.getCount());
                nbPixels += colorCount.getCount();
            }
        }

        final int saturationAverage = Math.round((float) saturationSum / nbPixels);
        final int brightnessAverage = Math.round((float) brightnessSum / nbPixels);
        return new HSBColor(hue, saturationAverage, brightnessAverage);
    }

    public HSBColor getAverageColour()
    {
        long hueSum = 0;
        long saturationSum = 0;
        long brightnessSum = 0;
        int nbPixels = 0;

        for(int x = 0; x < image.getWidth(); x++)
        {
            for(int y = 0; y < image.getHeight(); y++)
            {
                final HSBColor color = getPixel(x, y);
                if (!color.hasTransparency())
                {
                    hueSum += color.getHue();
                    saturationSum += color.getSaturation();
                    brightnessSum += color.getBrightness();
                    nbPixels++;
                }
            }
        }

        final int hueAverage = Math.round((float) hueSum / nbPixels);
        final int saturationAverage = Math.round((float) saturationSum / nbPixels);
        final int brightnessAverage = Math.round((float) brightnessSum / nbPixels);
        return new HSBColor(hueAverage, saturationAverage, brightnessAverage);
    }

    public static HSBColor getAverageColor(final HSBColor ... colors)
    {
        long hueSum = 0;
        long saturationSum = 0;
        long brightnessSum = 0;
        int nbPixels = 0;

        for (final HSBColor color : colors)
        {
            if (!color.hasTransparency())
            {
                hueSum += color.getHue();
                saturationSum += color.getSaturation();
                brightnessSum += color.getBrightness();
                nbPixels++;
            }
        }

        final int hueAverage = Math.round((float) hueSum / nbPixels);
        final int saturationAverage = Math.round((float) saturationSum / nbPixels);
        final int brightnessAverage = Math.round((float) brightnessSum / nbPixels);
        return new HSBColor(hueAverage, saturationAverage, brightnessAverage);
    }

    public float getPercentageTransparent()
    {
        int nbPixels = 0;

        for(int x = 0; x < image.getWidth(); x++)
        {
            for(int y = 0; y < image.getHeight(); y++)
            {
                final HSBColor color = getPixel(x, y);
                if (color.hasTransparency())
                {
                    nbPixels++;
                }
            }
        }

        return (float) nbPixels / (image.getWidth() * image.getHeight()) * 100;
    }

    public boolean isTransparentBackground()
    {
        // Check if any of the corner pixels are transparent
        // If they are, check there is at least some transparency in the rest of the image
        // This handles the case of random transparent corner pixels
        return (getTopLeftPixel().hasTransparency()
                || getTopRightPixel().hasTransparency()
                || getBottomLeftPixel().hasTransparency()
                || getBottomRightPixel().hasTransparency())
                && getPercentageTransparent() > 2;
    }

    /*
     * Assumes that isTransparentBackground() is false.
     */
    public HSBColor getBackgroundColor()
    {
        final HSBColor topLeft = getTopLeftPixel();
        final HSBColor topRight = getTopRightPixel();
        final HSBColor bottomLeft = getBottomLeftPixel();
        final HSBColor bottomRight = getBottomRightPixel();

        // If all corner pixels have some transparency, the logo most likely has rounded transparent corners.
        // Under the assumption that isTransparentBackground() is false, it must have failed the criteria of having a
        // certain percentage of transparent pixels in the image.
        // Since we can't tell from the corner pixels, the most common colour is a good guess at the background colour.
        if (topLeft.hasTransparency() && topRight.hasTransparency()
                && bottomLeft.hasTransparency() && bottomRight.hasTransparency())
        {
            return getColorRatio().get(0).getColor();
        }

        final List<ColorCount> colorCounts = getSimilarColorRatio(topLeft, topRight, bottomLeft, bottomRight);

        // If at least three colours are similar, use it
        if (colorCounts.get(0).getCount() >= 3)
        {
            return colorCounts.get(0).getColor();
        }

        // If two colours are the same, we will use it if the other two colours are different
        if (colorCounts.get(0).getCount() == 2 && colorCounts.size() == 3)
        {
            return colorCounts.get(0).getColor();
        }

        // Can't find a logical background colour, choose corner colour that occurs most in the image
        final Map<HSBColor, Integer> colorRatio = getColorRatioAsMap();
        HSBColor maxColor = null;

        for (final HSBColor testColor : Arrays.asList(topLeft, topRight, bottomLeft, bottomRight))
        {
            final Integer count = colorRatio.get(testColor);
            if (count != null)
            {
                if (maxColor == null || count > colorRatio.get(maxColor))
                {
                    maxColor = testColor;
                }
            }
        }

        // Since we know there is at least one opaque corner pixel, this will never be null
        return maxColor;
    }

    public boolean isMostlyWhite()
    {
        final List<ColorCount> colorRatio = getColorRatio();
        float ratio = 0;
        for (int i = 0; i < colorRatio.size() && ratio <= 90; i++)
        {
            if (!isWhiteOrCloseEnough(colorRatio.get(i).getColor()))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean isWhiteOrCloseEnough(final HSBColor color)
    {
        final int r = color.getColor().getRed();
        final int g = color.getColor().getGreen();
        final int b = color.getColor().getBlue();

        return (r > 245) && (g > 245) && (b > 245);
    }

    public boolean isTooBrightForWhiteBackground()
    {
        final HSBColor color = getAverageColour();
        return (color.getBrightness() > 80 && color.getSaturation() < 20)
                || (color.getBrightness() > 90 && color.getSaturation() < 40);
    }

    public HSBColor getPredominantColor()
    {
        HSBColor result = getFirstNonMonochromeColor(10);
        if (result != null)
        {
            return result;
        }

        final Integer hue = getMostCommonHue();
        if (hue != null)
        {
            return getMostInterestingColorWithHue(hue);
        }

        return null;
    }

    private HSBColor getPixel(int x, int y)
    {
        final int color = image.getRGB(x, y);

        final int red = (color & 0x00ff0000) >> 16;
        final int green = (color & 0x0000ff00) >> 8;
        final int blue = color & 0x000000ff;
        final int alpha = (color >> 24) & 0xff;

        return new HSBColor(new Color(red, green, blue, alpha));
    }

    public static boolean isMonochrome(final HSBColor color)
    {
        final int red = color.getColor().getRed();
        final int green = color.getColor().getGreen();
        final int blue = color.getColor().getBlue();
        final float average = (red + green + blue) / 3f;
        final int forgiveness = 7;

        return Math.abs(red - average) <= forgiveness
                && Math.abs(green - average) <= forgiveness
                && Math.abs(blue - average) <= forgiveness;
    }

    public static boolean isSimilar(final HSBColor c1, final HSBColor c2)
    {
        final int forgiveness = 7;

        return Math.abs(c1.getColor().getRed() - c2.getColor().getRed()) <= forgiveness
                && Math.abs(c1.getColor().getGreen() - c2.getColor().getGreen()) <= forgiveness
                && Math.abs(c1.getColor().getBlue() - c2.getColor().getBlue()) <= forgiveness;
    }

    private static class HueCounterPair implements Comparable<HueCounterPair>
    {
        private final int hue;
        private final Counter counter;

        private HueCounterPair(final int hue, final Counter counter)
        {
            this.hue = hue;
            this.counter = counter;
        }

        public int getHue()
        {
            return hue;
        }

        public Counter getCounter()
        {
            return counter;
        }

        @Override
        public int compareTo(final HueCounterPair other)
        {
            return counter.value() - other.counter.value();
        }
    }
}
