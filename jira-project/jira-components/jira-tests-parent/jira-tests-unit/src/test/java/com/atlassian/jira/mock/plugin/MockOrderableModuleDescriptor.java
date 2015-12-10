package com.atlassian.jira.mock.plugin;

import com.atlassian.jira.plugin.OrderableModuleDescriptor;

public class MockOrderableModuleDescriptor implements OrderableModuleDescriptor
{
    private int order;

    public MockOrderableModuleDescriptor(int order)
    {
        this.order = order;
    }

    public int getOrder()
    {
        return order;
    }

    @Override
    public String toString()
    {
        return "[OrderableModuleDescriptor " + String.valueOf(order) + "]";
    }
}
