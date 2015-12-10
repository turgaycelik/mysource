package com.atlassian.jira.web.ui.model;

import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A builder of {@link DropDownModel}s
 *
 * @since v4.4.1
 */
public class DropDownModelBuilder
{
    private List<DropDownModel.DropDownSection> sections = null;
    private String topText = null;
    private DropDownSectionImpl currentSection = null;


    public static DropDownModelBuilder builder()
    {
        return new DropDownModelBuilder();
    }

    public DropDownModelBuilder()
    {
        this.sections = new ArrayList<DropDownModel.DropDownSection>();
    }

    public DropDownModelBuilder setTopText(final String topText)
    {
        this.topText = topText;
        return this;
    }

    public DropDownModelBuilder startSection()
    {
        if (currentSection != null)
        {
            throw new IllegalStateException("You are currently in a section");
        }
        currentSection = new DropDownSectionImpl();
        return this;
    }

    public DropDownModelBuilder endSection()
    {
        if (currentSection == null)
        {
            throw new IllegalStateException("You are NOT currently in a section");
        }
        sections.add(currentSection);
        currentSection = null;
        return this;
    }

    public DropDownModelBuilder addItem(DropDownItemImpl item)
    {
        if (currentSection == null)
        {
            throw new IllegalStateException("You are currently NOT in a section");
        }
        currentSection.add(Assertions.notNull("item", item));
        return this;
    }

    public DropDownItemImpl item()
    {
        return new DropDownItemImpl();
    }

    public DropDownModel build()
    {
        if (currentSection != null)
        {
            throw new IllegalStateException("You are currently in a section");
        }
        return new DropDownModelImpl(topText, sections);
    }

    private class DropDownModelImpl implements DropDownModel
    {

        private final String topText;
        private final List<DropDownSection> sections;


        private DropDownModelImpl(String topText, List<DropDownSection> sections)
        {
            this.topText = topText;
            this.sections = sections;
        }

        @Override
        public String getTopText()
        {
            return topText;
        }

        @Override
        public List<DropDownSection> getSections()
        {
            return sections;
        }

        @Override
        public int getTotalItems()
        {
            int count = 0;
            for (DropDownSection section : sections)
            {
                count += section.getItems().size();
            }
            return count;
        }
    }


    private class DropDownSectionImpl implements DropDownModel.DropDownSection
    {
        private List<DropDownModel.DropDownItem> items = new ArrayList<DropDownModel.DropDownItem>();

        private void add(DropDownItemImpl item)
        {
            items.add(item);
        }

        @Override
        public List<DropDownModel.DropDownItem> getItems()
        {
            return items;
        }


    }

    public static class DropDownItemImpl implements DropDownModel.DropDownItem
    {
        private String itemText = "";
        private Map<String, String> attrs = new HashMap<String, String>();

        public DropDownItemImpl()
        {
        }

        @Override
        public String getText()
        {
            return itemText;
        }

        @Override
        public String getAttr(String name)
        {
            return attrs.get(name);
        }

        @Override
        public String getAttrAndRemove(String name)
        {
            final String value = attrs.get(name);
            attrs.remove(name);
            return value;
        }

        @Override
        public Set<String> getAttrs()
        {
            return attrs.keySet();
        }

        public DropDownItemImpl setText(String itemText)
        {
            this.itemText = itemText;
            return this;
        }

        public DropDownItemImpl setAttr(String attrName, String attrValue)
        {
            attrs.put(attrName, attrValue);
            return this;
        }
    }

}
