package com.atlassian.jira.functest.framework.navigator;

/**
 * @since v4.0
 */
public class StatusCondition extends MultiSelectCondition
{
    public StatusCondition()
    {
        super("status");
    }

    public StatusCondition(final StatusCondition statusCondition)
    {
        super(statusCondition);
    }

    public NavigatorCondition copyCondition()
    {
        return new StatusCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new StatusCondition();
    }

    public StatusCondition addStatus(final Type status)
    {
        addOption(status.getName());
        return this;
    }

    @Override
    public String toString()
    {
        return "Status [" + getOptions() + "]";
    }

    public static class Type
    {
        public static final Type OPEN = new Type("Open");
        public static final Type IN_PROGRESS = new Type("In Progress");
        public static final Type REOPENED = new Type("Reopened");
        public static final Type RESOLVED = new Type("Resolved");
        public static final Type CLOSED = new Type("Closed");

        private final String name;

        public Type(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public String toString()
        {
            return getName();
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Type type = (Type) o;

            return name.equals(type.name);
        }

        public int hashCode()
        {
            return name.hashCode();
        }
    }
}
