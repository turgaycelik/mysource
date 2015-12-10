package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Mock implementation of {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab}.
 *
 * @since v4.1
 */
public class MockFieldScreenRendererTab implements FieldScreenRenderTab
{
    private String name;
    private int position;
    private List<FieldScreenRenderLayoutItem> layoutItems;

    public MockFieldScreenRendererTab(final String name, final int position, final List<? extends FieldScreenRenderLayoutItem> layoutItems)
    {
        setName(name);
        setPosition(position);
        setLayoutItems(layoutItems);
    }

    public MockFieldScreenRendererTab()
    {
    }

    public String getName()
    {
        return name;
    }

    public int getPosition()
    {
        return position;
    }

    public List<FieldScreenRenderLayoutItem> getFieldScreenRenderLayoutItems()
    {
        return layoutItems;
    }

    public List<FieldScreenRenderLayoutItem> getFieldScreenRenderLayoutItemsForProcessing()
    {
        return layoutItems;
    }

    public MockFieldScreenRendererTab setName(String name)
    {
        this.name = name;
        return this;
    }

    public MockFieldScreenRendererTab setPosition(int position)
    {
        this.position = position;
        return this;
    }

    public MockFieldScreenRendererTab setLayoutItems(final List<? extends FieldScreenRenderLayoutItem> layoutItems)
    {
        this.layoutItems = new ArrayList<FieldScreenRenderLayoutItem>(layoutItems);
        return this;
    }

    public MockFieldScreenRendererTab addLayoutItem(FieldScreenRenderLayoutItem item)
    {
        if (this.layoutItems == null)
        {
            this.layoutItems = new ArrayList<FieldScreenRenderLayoutItem>();
        }
        this.layoutItems.add(item);
        return this;
    }

    public MockFieldScreenRendererLayoutItem addLayoutItem()
    {
        final MockFieldScreenRendererLayoutItem rendererLayoutItem = new MockFieldScreenRendererLayoutItem();
        addLayoutItem(rendererLayoutItem);
        return rendererLayoutItem;
    }
   
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public int compareTo(final FieldScreenRenderTab o)
    {
        if (o == null)
        {
            return 1;
        }
        else
        {
            return getPosition() - o.getPosition();
        }
    }
}
