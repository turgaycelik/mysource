package com.atlassian.jira.functest.framework.navigator;

/**
 * @since v4.0
 */
public class ResolutionCondition extends MultiSelectCondition
{
    public ResolutionCondition()
    {
        super("resolution");
    }

    public ResolutionCondition(final ResolutionCondition resolutionCondition)
    {
        super(resolutionCondition);
    }

    public NavigatorCondition copyCondition()
    {
        return new ResolutionCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new ResolutionCondition();
    }

    public ResolutionCondition addResolution(final Type resolution)
    {
        addOption(resolution.getName());
        return this;
    }

    @Override
    public String toString()
    {
        return "Resolution [" + getOptions() + "]";
    }

    public static class Type
    {
        public static final Type UNRESOLVED = new Type("Unresolved");
        public static final Type FIXED = new Type("Fixed");
        public static final Type WONT_FIX = new Type("Won't Fix");
        public static final Type DUPLICATE = new Type("Duplicate");
        public static final Type INCOMPLETE = new Type("Incomplete");
        public static final Type CANNOT_REPRODUCE = new Type("Cannot Reproduce");

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
