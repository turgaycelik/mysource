package com.atlassian.jira.bc.issue.visibility;

import com.atlassian.fugue.Option;

/**
 * Static factory methods to create {@link VisibilityVisitor}
 * which return groupLevel or roleLevelId from given {@link Visibility} or validate it.
 *
 * @since v6.4
 */
public class VisibilityVisitors
{
    private VisibilityVisitors() {}

    private static final VisibilityVisitor<Option<String>> GROUP_LEVEL_VISITOR = new VisibilityVisitor<Option<String>>()
    {
        @Override
        public Option<String> visit(final PublicVisibility publicVisibility)
        {
            return Option.none();
        }

        @Override
        public Option<String> visit(final RoleVisibility roleVisibility)
        {
            return Option.none();
        }

        @Override
        public Option<String> visit(final GroupVisibility groupVisibility)
        {
            return Option.some(groupVisibility.getGroupLevel());
        }

        @Override
        public Option<String> visit(final InvalidVisibility invalidVisibility)
        {
            return Option.none();
        }
    };

    private static final VisibilityVisitor<Option<Long>> ROLE_LEVEL_VISITOR = new VisibilityVisitor<Option<Long>>()
    {
        @Override
        public Option<Long> visit(final PublicVisibility publicVisibility)
        {
            return Option.none();
        }

        @Override
        public Option<Long> visit(final RoleVisibility roleVisibility)
        {
            return Option.some(roleVisibility.getRoleLevelId());
        }

        @Override
        public Option<Long> visit(final GroupVisibility groupVisibility)
        {
            return Option.none();
        }

        @Override
        public Option<Long> visit(final InvalidVisibility invalidVisibility)
        {
            return Option.none();
        }
    };

    /**
     * @return the visitor which returns roleId level restriction if the visibility is represent by {@link com.atlassian.jira.bc.issue.visibility.RoleVisibility}.
     * In other way it returns Option.NONE.
     * WARNING This visitor returns Option.NONE for invalid visibility too, so if you want to transform empty option
     * to null and then pass it to worklog or comment you have to validate this visibility first.
     * If you don't valid it, the invalid visibility make this comment or worklog public.
     */
    public static VisibilityVisitor<Option<String>> returningGroupLevelVisitor()
    {
        return GROUP_LEVEL_VISITOR;
    }

    /**
     * @return the visitor which returns group level restriction if the visibility is represent by {@link com.atlassian.jira.bc.issue.visibility.GroupVisibility}.
     * In other way it returns Option.NONE.
     * WARNING This visitor returns Option.NONE for invalid visibility too, so if you want to transform empty option
     * to null and then pass it to worklog or comment you have to validate this visibility first.
     * If you don't valid it, the invalid visibility make this comment or worklog public.
     */
    public static VisibilityVisitor<Option<Long>> returningRoleLevelIdVisitor()
    {
        return ROLE_LEVEL_VISITOR;
    }
}