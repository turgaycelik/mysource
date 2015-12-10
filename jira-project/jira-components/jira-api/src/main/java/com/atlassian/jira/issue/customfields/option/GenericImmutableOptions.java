package com.atlassian.jira.issue.customfields.option;

import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

public class GenericImmutableOptions <E> extends AbstractList<E>
{

    FieldConfig fieldConfig;
    List<E> originalList;

    public GenericImmutableOptions(List<E> originalList, FieldConfig fieldConfig)
    {
        this.originalList = originalList;
        this.fieldConfig = fieldConfig;
    }

    public E get(int index)
    {
        return originalList.get(index);
    }

    public int size()
    {
        return originalList.size();
    }

    public FieldConfig getRelatedFieldConfig()
    {
        return fieldConfig;
    }

}
