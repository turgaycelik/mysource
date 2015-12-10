package com.atlassian.jira.issue.fields.option;

import java.util.List;

/**
 * Text option with children
 * @since 5.2
 */
public class GroupTextOption extends TextOption
{
    private List<Option> childOptions;

    public GroupTextOption(String id, String name, List<Option> childOptions)
    {
        super(id, name);
        this.childOptions = childOptions;
    }

    public GroupTextOption(String id, String name, String cssClass, List<Option> childOptions)
    {
        super(id, name, cssClass);
        this.childOptions = childOptions;
    }

    @Override
    public List<Option> getChildOptions()
    {
        return this.childOptions;
    }

    public void setChildOptions(List<Option> childOptions)
    {
        this.childOptions = childOptions;
    }
}
