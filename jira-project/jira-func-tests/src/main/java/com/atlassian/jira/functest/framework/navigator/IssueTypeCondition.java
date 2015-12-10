package com.atlassian.jira.functest.framework.navigator;

/**
 * Navigator condition that can be used to select condition types.
 *
 * @since v3.13
 */
public class IssueTypeCondition extends MultiSelectCondition
{
    public IssueTypeCondition()
    {
        super("type");
    }

    public IssueTypeCondition(IssueTypeCondition copy)
    {
        super(copy);
    }

    public IssueTypeCondition addIssueType(IssueType type)
    {
        if (type != null)
        {
            addOption(type.getName());
        }
        return this;
    }

    public boolean removeIssueType(IssueType type)
    {
        return type != null && removeOption(type.getName());
    }

    public IssueTypeCondition addIssueType(String type)
    {
        addOption(type);
        return this;
    }

    public boolean removeIssueType(String type)
    {
        return removeOption(type);
    }

    public String toString()
    {
        return "Issue Types: " + getOptions();
    }

    public NavigatorCondition copyCondition()
    {
        return new IssueTypeCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new IssueTypeCondition();
    }

    public static class IssueType
    {
        public static final IssueType BUG = new IssueType("Bug");
        public static final IssueType IMPROVEMENT = new IssueType("Improvement");
        public static final IssueType NEW_FEATURE = new IssueType("New Feature");
        public static final IssueType TASK = new IssueType("Task");

        private final String name;

        private IssueType(String name)
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

            IssueType issueType = (IssueType) o;

            if (!name.equals(issueType.name))
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            return name.hashCode();
        }
    }
}
