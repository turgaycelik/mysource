package com.atlassian.jira.web.bean;

public class WorkflowDescriptorInfo
{
    private final String description;
    private final boolean orderable;
    private final boolean deletable;
    private final boolean editable;

    public WorkflowDescriptorInfo(String description, boolean orderable, boolean deletable, boolean editable)
    {
        this.description = description;
        this.orderable = orderable;
        this.deletable = deletable;
        this.editable = editable;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isOrderable()
    {
        return orderable;
    }

    public boolean isDeletable()
    {
        return deletable;
    }

    public boolean isEditable()
    {
        return editable;
    }
}

