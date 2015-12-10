package com.atlassian.jira.issue.fields.option;

import java.util.ArrayList;
import java.util.List;

/**
 * Same as an HTML optgroup. Holds <options>
 *
 * @since v4.4
 */
public class OptionGroup implements SelectChild
{
    /**
     * The id to define this option group, particularly for JS.
     */
    private String id;

    /**
     * The text to be displayed for this option group.
     */
    private String display;

    /**
     * The text to be displayed at the bottom of group
     */
    private String footer;
    /**
     * The index of this group in a list of select items. If -1, gets appended to the end of the list.
     */
    private int weight = -1;

    /**
     * The options to be displayed nested inside of this group.
     */
    private List<AssigneeOption> groupOptions = new ArrayList<AssigneeOption>();

    public OptionGroup(String id, String display, String footer, int weight)
    {
        this.id = id;
        this.display = display;
        this.footer = footer;
        this.weight = weight;
    }

    public OptionGroup(String id, String display, int weight)
    {
        this.id = id;
        this.display = display;
        this.weight = weight;
    }

    public void add(AssigneeOption option)
    {
        groupOptions.add(option);
    }

    public String getId()
    {
        return id;
    }

    public String getFooter()
    {
        return footer;
    }

    public String getDisplay()
    {
        return display;
    }

    public int getWeight()
    {
        return weight;
    }

    public List<AssigneeOption> getGroupOptions()
    {
        return groupOptions;
    }

    /**
     * Used to differentiate a map representation of this object from an AssigneeOption.
     */
    public boolean isOptionGroup()
    {
        return true;
    }
}
