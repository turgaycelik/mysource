package com.atlassian.jira.issue.search.searchers.transformer;

public class ComponentSearchInput extends SearchInput
{
    private static class ComponentInputType extends InputType
    {
        static ComponentInputType NO_COMPONENT = new ComponentInputType("NO_COMPONENT");
        static ComponentInputType COMPONENT = new ComponentInputType("COMPONENT");

        private ComponentInputType(String name)
        {
            super(name);
        }
    }

    private ComponentSearchInput(ComponentInputType type, String value)
    {
        super(type, value);
    }

    public static ComponentSearchInput noComponents()
    {
        return new ComponentSearchInput(ComponentInputType.NO_COMPONENT, null);
    }

    public static ComponentSearchInput component(String value)
    {
        return new ComponentSearchInput(ComponentInputType.COMPONENT, value);
    }

    public boolean isNoComponent()
    {
        return ComponentInputType.NO_COMPONENT.equals(type);
    }

    public boolean isComponent()
    {
        return ComponentInputType.COMPONENT.equals(type);
    }
}
