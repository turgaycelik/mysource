package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.fields.option.TextOption;

public class OptionWithValidity extends TextOption
{
    private final boolean valid;

    public OptionWithValidity(String id, String name)
    {
        this(id, name, true);
    }

    OptionWithValidity(String id, String name, boolean valid)
    {
        super(id, name);

        this.valid = valid;

        if (!valid)
        {
            setCssClass("invalid_sel");
        }
    }

    public boolean isValid()
    {
        return valid;
    }
}
