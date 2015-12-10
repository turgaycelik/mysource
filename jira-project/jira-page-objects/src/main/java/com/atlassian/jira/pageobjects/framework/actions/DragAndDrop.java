package com.atlassian.jira.pageobjects.framework.actions;

import com.atlassian.jira.pageobjects.framework.util.JiraLocators;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementActions;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.google.inject.Inject;

/**
 * Injectable Drag and drop component, with support for PageElements, useful abstractions (below, above, left, right)
 * and better error handling.
 *
 * @since 5.2
 */
public class DragAndDrop
{

    @Inject private PageElementActions actions;
    @Inject private PageElementFinder finder;

    public Builder dragAndDrop(PageElement source)
    {
        return new Builder(source);
    }

    public final class Builder
    {
        private final PageElement source;

        private PageElement directTarget;
        private int xOffset = -1;
        private int yOffset = -1;
        private long windowHeight = -2;
        private long windowWidth = -2;


        private Builder(PageElement source)
        {
            this.source = Assertions.notNull("source", source);
        }

        public Builder to(PageElement target)
        {
            this.directTarget = Assertions.notNull("target", target);
            return this;
        }

        public Builder below(PageElement target)
        {
            resetDirectTarget();
            setY(target.getLocation().getY() + target.getSize().getHeight() - source.getLocation().getY());
            return this;
        }

        public Builder above(PageElement target)
        {
            resetDirectTarget();
            setY(target.getLocation().getY() - source.getLocation().getY());
            return this;
        }

        public Builder toLeftOf(PageElement target)
        {
            resetDirectTarget();
            setX(target.getLocation().getX() - source.getLocation().getX());
            return this;
        }

        public Builder toRightOf(PageElement target)
        {
            resetDirectTarget();
            setX(target.getLocation().getX() + target.getSize().getWidth() - source.getLocation().getX());
            return this;
        }

        private void setY(int y)
        {
            this.yOffset = y;
            this.xOffset = xOffset >= 0 ? xOffset : 0;
        }

        private void setX(int x)
        {
            this.xOffset = x;
            this.yOffset = yOffset >= 0 ? yOffset : 0;
        }

        private int halfOf(int dim)
        {
            return (int) (dim / 2.0);
        }

        private void resetDirectTarget()
        {
            this.directTarget = null;
        }

        private void resetXY()
        {
            this.xOffset = -1;
            this.yOffset = -1;
        }

        public boolean canExecute()
        {
            final PageElement body = finder.find(JiraLocators.body());
            windowWidth = body.javascript().execute(Long.class, "return jQuery(window).width();");
            windowHeight = body.javascript().execute(Long.class, "return jQuery(window).height();");
            if (yOffset() > windowHeight)
            {
                return false;
            }
            if (xOffset() > windowWidth)
            {
                return false;
            }
            return true;
        }

        private int yOffset()
        {
            if (directTarget != null)
            {
                return directTarget.getLocation().getY() - source.getLocation().getY();
            }
            else
            {
                return yOffset;
            }
        }

        private int xOffset()
        {
            if (directTarget != null)
            {
                return directTarget.getLocation().getX() - source.getLocation().getX();
            }
            else
            {
                return xOffset;
            }
        }

        public void execute()
        {
            ensureViewport();
            if (directTarget != null)
            {
                actions.dragAndDrop(source, directTarget).perform();
            }
            else if (xOffset >= 0)
            {
                actions.dragAndDropBy(source, xOffset, yOffset).perform();
            }
            else
            {
                throw new IllegalStateException("Drag and drop action not specified");
            }
        }

        private void ensureViewport()
        {
            if (!canExecute())
            {
                throw new IllegalStateException(String.format("Drag and drop source or target out of viewport. "
                        + "Viewport is (%d, %d), target is at (%d, %d)."
                        + "Cannot perform drag and drop.",
                            windowWidth, windowHeight, xOffset(), yOffset()));
            }
        }
    }

}
