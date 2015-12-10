package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.fields.option.GroupTextOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.google.common.collect.Lists;

import java.util.List;

class ComponentOptions extends Options
{
    private GroupTextOption options;

    public void options(GroupTextOption options)
    {
        this.options = options;
    }

    public GroupTextOption getOptions()
    {
        return options;
    }

    @Override
    List<Option> all()
    {
        List<Option> all = Lists.newArrayList();
        if (null != no)
        {
            all.add(no);
        }
        if (options != null)
        {
            all.addAll(options.getChildOptions());
        }
        return all;
    }
}
