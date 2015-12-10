package com.atlassian.jira.issue.fields.screen;

import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple mock for {@link com.atlassian.jira.issue.fields.screen.FieldScreenTab}.
 *
 * @since v4.1
 */
public class MockFieldScreenTab implements FieldScreenTab
{
    private Long id;
    private String name;
    private FieldScreen screen;
    private int position;
    private List<FieldScreenLayoutItem> items = new ArrayList<FieldScreenLayoutItem>();

    public Long getId()
    {
        return id;
    }

    public MockFieldScreenTab setId(Long id)
    {
        this.id = id;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(final int position)
    {
        this.position = position;
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems()
    {
        return items;
    }

    public FieldScreenLayoutItem getFieldScreenLayoutItem(final int position)
    {
        return items.get(position);
    }

    public void addFieldScreenLayoutItem(final String fieldId)
    {
        final MockFieldScreenLayoutItem screenLayoutItem = createForFieldId(fieldId);
        items.add(screenLayoutItem);
        resequence();
    }

    public void addFieldScreenLayoutItem(final String fieldId, final int position)
    {
        final MockFieldScreenLayoutItem screenLayoutItem = createForFieldId(fieldId);
        items.add(position, screenLayoutItem);
        resequence();
    }

    public MockFieldScreenLayoutItem addFieldScreenLayoutItem()
    {
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem();
        screenLayoutItem.setFieldScreenTab(this);
        items.add(screenLayoutItem);
        resequence();

        return screenLayoutItem;
    }

    public void moveFieldScreenLayoutItemFirst(final int fieldPosition)
    {
        throw new UnsupportedOperationException();
    }

    public void moveFieldScreenLayoutItemUp(final int fieldPosition)
    {
        throw new UnsupportedOperationException();
    }

    public void moveFieldScreenLayoutItemDown(final int fieldPosition)
    {
        throw new UnsupportedOperationException();
    }

    public void moveFieldScreenLayoutItemLast(final int fieldPosition)
    {
        throw new UnsupportedOperationException();
    }

    public FieldScreenLayoutItem removeFieldScreenLayoutItem(final int fieldPosition)
    {
        final FieldScreenLayoutItem screenLayoutItem = items.remove(fieldPosition);
        resequence();
        return screenLayoutItem;
    }

    public FieldScreenLayoutItem getFieldScreenLayoutItem(final String fieldId)
    {
        for (FieldScreenLayoutItem item : items)
        {
            if (item.getFieldId().equals(fieldId))
            {
                return item;
            }
        }
        return null;
    }

    public boolean isContainsField(final String fieldId)
    {
        return getFieldScreenLayoutItem(fieldId) != null;
    }

    public void moveFieldScreenLayoutItemToPosition(final Map<Integer, FieldScreenLayoutItem> positionsToFields)
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue getGenericValue()
    {
        throw new UnsupportedOperationException();
    }

    public void setGenericValue(final GenericValue genericValue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isModified()
    {
        return false;
    }

    public void setFieldScreen(final FieldScreen fieldScreen)
    {
        this.screen = fieldScreen;
    }

    public FieldScreen getFieldScreen()
    {
        return this.screen;
    }

    @Override
    public void rename(String newName)
    {
        throw new UnsupportedOperationException();
    }

    public void store()
    {
        throw new UnsupportedOperationException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private MockFieldScreenLayoutItem createForFieldId(final String fieldId)
    {
        final MockFieldScreenLayoutItem screenLayoutItem = new MockFieldScreenLayoutItem();
        screenLayoutItem.setFieldScreenTab(this);
        screenLayoutItem.setFieldId(fieldId);
        return screenLayoutItem;
    }

    private void resequence()
    {
        int count = 0;
        for (FieldScreenLayoutItem item : items)
        {
            item.setPosition(count++);
        }
    }
}
