package com.atlassian.jira.web.action.issue.navigator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.0
 */
@XmlRootElement
public class ToolOptionGroup
{
    @XmlElement
    private List<ToolOptionItem> items = new ArrayList<ToolOptionItem>();

    @XmlElement
    private String label;

    @XmlElement
    private String id;

    public ToolOptionGroup()
    {
    }

    public ToolOptionGroup(final String label)
    {
        this.label = label;
    }

    public ToolOptionGroup(final String id, final String label)
    {
        this.id = id;
        this.label = label;
    }

    public void addItem(ToolOptionItem item)
    {
        items.add(item);
    }

    public String getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public List<ToolOptionItem> getItems()
    {
        return items;
    }

    public boolean isEmpty()
    {
        return items.isEmpty();
    }

}
