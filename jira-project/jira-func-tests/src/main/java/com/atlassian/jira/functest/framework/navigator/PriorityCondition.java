package com.atlassian.jira.functest.framework.navigator;

/**
 * @since v4.0
 */
public class PriorityCondition extends MultiSelectCondition
{
    public PriorityCondition()
    {
        super("priority");
    }

    public PriorityCondition(final PriorityCondition priorityCondition)
    {
        super(priorityCondition);
    }

    public NavigatorCondition copyCondition()
    {
        return new PriorityCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new PriorityCondition();
    }

    public PriorityCondition addPriority(final Type priority)
    {
        addOption(priority.getName());
        return this;
    }

    @Override
    public String toString()
    {
        return "Priority [" + getOptions() + "]";
    }

    public static class Type
    {
        public static final Type BLOCKER = new Type("Blocker");
        public static final Type CRITICAL = new Type("Critical");
        public static final Type MAJOR = new Type("Major");
        public static final Type MINOR = new Type("Minor");
        public static final Type TRIVIAL = new Type("Trivial");

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
