package com.atlassian.jira.issue.thumbnail;

/**
 * The height and width of an image or thumbnail.
 * <p/>
 * This code was taken originally from our friends in Confluence
 *
 * @since v4.4
 */
public class Dimensions
{
    private final int width;
    private final int height;

    public Dimensions(final int width, final int height)
    {
        this.height = height;
        this.width = width;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final Dimensions that = (Dimensions) o;
        return height == that.height && width == that.width;
    }

    public int hashCode()
    {
        int result;
        result = width;
        result = 31 * result + height;
        return result;
    }

    public String toString()
    {
        return width + "x" + height;
    }
}
