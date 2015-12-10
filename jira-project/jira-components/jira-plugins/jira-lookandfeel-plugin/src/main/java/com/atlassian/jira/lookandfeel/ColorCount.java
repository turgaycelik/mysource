package com.atlassian.jira.lookandfeel;

public class ColorCount implements Comparable<ColorCount>
{
    private final HSBColor color;
    private final int count;
    private final float percentage;

    public ColorCount(final HSBColor color, final int count, final float percentage)
    {
        this.color = color;
        this.count = count;
        this.percentage = percentage;
    }

    public HSBColor getColor()
    {
        return color;
    }

    public int getCount()
    {
        return count;
    }

    public float getPercentage()
    {
        return percentage;
    }

    @Override
    public int compareTo(final ColorCount colorCount)
    {
        // Sort in reverse order so that ones with a higher count appear first in the list
        return colorCount.count - count;
    }

    @Override
    public String toString()
    {
        return "ColorCount[color=" + color + ",count=" + count + ",percentage=" + percentage + "]";
    }
}
