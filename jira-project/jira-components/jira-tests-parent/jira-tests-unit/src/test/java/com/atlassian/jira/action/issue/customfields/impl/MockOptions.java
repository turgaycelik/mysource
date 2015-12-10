package com.atlassian.jira.action.issue.customfields.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;

public class MockOptions extends AbstractList<Option> implements Options
{
    private final FieldConfig fieldConfig;
    private final Map rootOptions; //Option parent - > Map rootOptions (children)

    public MockOptions(FieldConfig fieldConfig, Map rootOptions)
    {
        this.fieldConfig = fieldConfig;
        this.rootOptions = rootOptions;
    }

    public MockOptions(FieldConfig fieldConfig, List rootOptions)
    {
        this.fieldConfig = fieldConfig;
        this.addAll(rootOptions);
        this.rootOptions = new HashMap();
        for (Iterator iterator = rootOptions.iterator(); iterator.hasNext();)
        {
            Object o = iterator.next();
            this.rootOptions.put(o, o);
        }
    }
    public List<Option> getRootOptions()
    {
        return new ArrayList<Option>(rootOptions.keySet());
    }

    public Option getOptionById(Long optionId)
    {
        return getOptionByKey(optionId, rootOptions);
    }

    public Option getOptionForValue(String value, Long parentOptionId)
    {
        return null;
    }

    private Option getOptionByKey(Long optionId, Map options)
    {
        if (options == null || options.isEmpty())
            return null;

        for (Iterator iterator = options.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            Option parent = (Option) entry.getKey();
            if (optionId.equals(parent.getOptionId()))
                return parent;
            else
            {
                Map children = (Map) entry.getValue();
                return getOptionByKey(optionId, children);
            }
        }
        return null;
    }

    public Option addOption(Option parent, String value)
    {
        throw new UnsupportedOperationException();
    }

    public void removeOption(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void moveToStartSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void incrementSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void decrementSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void moveToLastSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public FieldConfig getRelatedFieldConfig()
    {
        return fieldConfig;
    }

    public void sortOptionsByValue(Option parentOption)
    {
        throw new UnsupportedOperationException();
    }

    public void moveOptionToPosition(Map<Integer, Option> positionsToOptions)
    {
        throw new UnsupportedOperationException();
    }

    public Option get(int index)
    {
        return new ArrayList<Option>(getRootOptions()).get(index);
    }

    public int size()
    {
        return getRootOptions().size();
    }

    public void setValue(final Option option, final String value)
    {
        throw new UnsupportedOperationException();
    }

    public void enableOption(final Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void disableOption(final Option option)
    {
        throw new UnsupportedOperationException();
    }

}
