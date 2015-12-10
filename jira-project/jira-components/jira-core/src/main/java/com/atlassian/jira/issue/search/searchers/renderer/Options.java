package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.fields.option.Option;

import java.util.List;

abstract class Options
{
    protected Option no;

    public void no(Option option)
    {
        this.no = option;
    }

    public Option getNo()
    {
        return no;
    }

    abstract List<Option> all();
}
