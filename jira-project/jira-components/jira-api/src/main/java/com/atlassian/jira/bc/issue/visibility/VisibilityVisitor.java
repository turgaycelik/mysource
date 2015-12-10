package com.atlassian.jira.bc.issue.visibility;

import com.atlassian.annotations.PublicSpi;

/**
 * This visitor allows to match {@link Visibility}'s implementations.
 *
 * @since v6.4
 */
@PublicSpi
public interface VisibilityVisitor<T>
{
    T visit(PublicVisibility publicVisibility);
    T visit(RoleVisibility roleVisibility);
    T visit(GroupVisibility groupVisibility);
    T visit(InvalidVisibility invalidVisibility);
}
