package com.atlassian.jira.lookandfeel;

import java.awt.*;

public class HSBColor
{
    private final int hue;
    private final int saturation;
    private final int brightness;
    private final Color color;
    private final String hexString;

    public HSBColor(final Color color)
    {
        // hue is between 0-359
        // saturation is between 0-100
        // brightness is between 0-100
        final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = Math.round(hsb[0] * 359);
        this.saturation = Math.round(hsb[1] * 100);
        this.brightness = Math.round(hsb[2] * 100);
        this.color = color;
        this.hexString = getHexFromColor(color);
    }

    public HSBColor(String hexString)
    {
        this(Color.decode(hexString.startsWith("#") ? hexString : "#" + hexString));
    }

    public HSBColor(final int hue, final int saturation, final int brightness)
    {
        this(new Color(Color.HSBtoRGB(hue / 359f, saturation / 100f, brightness / 100f)));
    }

    public int getHue()
    {
        return hue;
    }

    public int getSaturation()
    {
        return saturation;
    }

    public int getBrightness()
    {
        return brightness;
    }

    public Color getColor()
    {
        return color;
    }

    public String getHexString()
    {
        return hexString;
    }

    public float getPerceivedBrightness()
    {
        return ((color.getRed() * 299) + (color.getGreen() * 587) + (color.getBlue() * 114)) / 1000f;
    }

    public boolean hasTransparency()
    {
        return color.getAlpha() < 255;
    }

    public HSBColor lightenByPercentage(final float percentage)
    {
        return lightenByAmount(Math.round(brightness * percentage / 100));
    }

    public HSBColor lightenByAmount(final int amount)
    {
        return new HSBColor(hue, saturation, Math.min(100, brightness + amount));
    }

    public HSBColor darkenByPercentage(final float percentage)
    {
        return darkenByAmount(Math.round(brightness * percentage / 100));
    }

    public HSBColor darkenByAmount(final int amount)
    {
        return new HSBColor(hue, saturation, Math.max(0, brightness - amount));
    }

    public HSBColor saturateByPercentage(final float percentage)
    {
        return saturateByAmount(Math.round(saturation * percentage / 100));
    }

    public HSBColor saturateByAmount(final int amount)
    {
        return new HSBColor(hue, Math.min(100, saturation + amount), brightness);
    }

    public HSBColor desaturateByPercentage(final float percentage)
    {
        return desaturateByAmount(Math.round(saturation * percentage / 100));
    }

    public HSBColor desaturateByAmount(final int amount)
    {
        return new HSBColor(hue, Math.max(0, saturation - amount), brightness);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HSBColor hsbColor = (HSBColor) o;

        if (brightness != hsbColor.brightness) return false;
        if (hue != hsbColor.hue) return false;
        if (saturation != hsbColor.saturation) return false;
        if (color.getTransparency() != hsbColor.color.getTransparency()) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = hue;
        result = 31 * result + saturation;
        result = 31 * result + brightness;
        result = 31 * result + color.getTransparency();
        return result;
    }

    @Override
    public String toString()
    {
        return "HSBColor[h=" + hue + ",s=" + saturation + ",b=" + brightness + ",a=" + color.getTransparency() + ",hex=" + getHexString() + "]";
    }

    private static String getHexFromColor(final Color color)
    {
        String result = Integer.toHexString(color.getRGB() & 0xffffff);
        while (result.length() < 6)
        {
            result = "0" + result;
        }

        return "#" + result;
    }
}
