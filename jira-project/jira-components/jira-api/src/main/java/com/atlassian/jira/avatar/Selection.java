package com.atlassian.jira.avatar;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Represents an absolute subrectangle of an image.
 *
 * @since v4.0
 */
@PublicApi
public final class Selection
{

    private final int topLeftX;

    private final int topLeftY;

    private final int width;

    private final int height;

    public Selection(int topLeftX, int topLeftY, int width, int height)
    {
        Assertions.not("topLeftX must be positive", topLeftX < 0);
        Assertions.not("topLeftY must be positive", topLeftY < 0);
        Assertions.not("width must be positive", width < 0);
        Assertions.not("height must be positive", height < 0);
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
    }

    public int getTopLeftX()
    {
        return topLeftX;
    }

    public int getTopLeftY()
    {
        return topLeftY;
    }

    public int getBottomRightX()
    {
        return topLeftX + width;
    }

    public int getBottomRightY()
    {
        return topLeftY + height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
