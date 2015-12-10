package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;

/**
 * Simple mock implementation of {@link FieldScreen}.
 *
 * @since v4.1
 */
public class MockFieldScreen implements FieldScreen
{
    private List<FieldScreenTab> tabs = new ArrayList<FieldScreenTab>();
    private Long id;
    private String name;
    private String description;

    public MockFieldScreen()
    {

    }

    public MockFieldScreen(long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public MockFieldScreen name(final String name)
    {
        this.name = name;
        return this;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public List<FieldScreenTab> getTabs()
    {
        return tabs;
    }

    public FieldScreenTab getTab(final int tabPosition)
    {
        return tabs.get(tabPosition);
    }

    public MockFieldScreen addTab(FieldScreenTab tab)
    {
        tab.setPosition(tabs.size());
        tabs.add(tab);
        tab.setFieldScreen(this);

        return this;
    }

    public MockFieldScreenTab addMockTab()
    {
        final MockFieldScreenTab tab = new MockFieldScreenTab();
        addTab(tab);
        return tab;
    }

    public FieldScreenTab addTab(final String tabName)
    {
        final MockFieldScreenTab tab = addMockTab();
        tab.setName(tabName);
        return tab;
    }

    public void removeTab(final int tabPosition)
    {
        tabs.remove(tabPosition);
        for (int i = tabPosition; i < tabs.size(); i++)
        {
            tabs.get(i).setPosition(i);
        }
    }

    @Override
    public void moveFieldScreenTabToPosition(int tabPosition, int newPosition)
    {
    }

    public void moveFieldScreenTabLeft(final int tabPosition)
    {
        throw new UnsupportedOperationException();
    }

    public void moveFieldScreenTabRight(final int tabPosition)
    {
        throw new UnsupportedOperationException();
    }

    public void resequence()
    {
        int count = 0;
        for (FieldScreenTab fieldScreenTab : getTabs())
        {
            fieldScreenTab.setPosition(count++);
        }
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

    public void store()
    {
        throw new UnsupportedOperationException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    public boolean containsField(final String fieldId)
    {
        for (FieldScreenTab tab : getTabs())
        {
            if (tab.getFieldScreenLayoutItem(fieldId) != null)
            {
                return true;
            }
        }
        return false;
    }

    public void removeFieldScreenLayoutItem(final String fieldId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockFieldScreen that = (MockFieldScreen) o;

        if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
        if (tabs != null ? !tabs.equals(that.tabs) : that.tabs != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = tabs != null ? tabs.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
