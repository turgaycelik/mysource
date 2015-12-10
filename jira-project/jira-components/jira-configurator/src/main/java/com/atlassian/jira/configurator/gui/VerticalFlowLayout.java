package com.atlassian.jira.configurator.gui;

import java.awt.*;

import com.atlassian.jira.cluster.ClusterSafe;

public class VerticalFlowLayout implements LayoutManager
{
    private int vgap = 4;

    /**
     * Gets the vertical gap between components.
     *
     * @return the vertical gap between components
     */
    public int getVgap()
    {
        return vgap;
    }

    /**
     * Sets the vertical gap between components to the specified value.
     *
     * @param vgap the vertical gap between components
     */
    public void setVgap(int vgap)
    {
        this.vgap = vgap;
    }


    public void addLayoutComponent(final String name, final Component comp)
    {
        System.out.println("addLayoutComponent " + name + " , " + comp);
    }

    public void removeLayoutComponent(final Component comp)
    {
        System.out.println("removeLayoutComponent " + comp);
    }

    @ClusterSafe ("This is not part of the JIRA web app.")
    public Dimension preferredLayoutSize(final Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();
            int maxWidth = 0;
            int totalHeight = insets.top + insets.bottom;

            for (int i = 0; i < componentCount; i++)
            {
                Component comp = parent.getComponent(i);
                Dimension preferred = comp.getPreferredSize();
                if (i > 0)
                {
                    totalHeight += vgap;
                }
                totalHeight += preferred.height;
                if (maxWidth < preferred.width)
                {
                    maxWidth = preferred.width;
                }
            }

            return new Dimension(insets.left + maxWidth + insets.right, totalHeight);            
        }
    }

    public Dimension minimumLayoutSize(final Container parent)
    {
        return preferredLayoutSize(parent);
    }

    @ClusterSafe ("This is not part of the JIRA web app.")
    public void layoutContainer(final Container parent)
    {
        synchronized (parent.getTreeLock())
        {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();
            final int x = insets.left;
            int y = insets.top;

            for (int c = 0; c < componentCount; c++)
            {
                Component component = parent.getComponent(c);
                Dimension preferred = component.getPreferredSize();
                component.setBounds(x, y, preferred.width, preferred.height);
                y = y + component.getHeight() + vgap;
            }
        }
    }
}
