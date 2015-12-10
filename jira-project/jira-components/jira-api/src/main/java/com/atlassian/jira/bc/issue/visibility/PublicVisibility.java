package com.atlassian.jira.bc.issue.visibility;

import javax.annotation.concurrent.Immutable;

/**
 * This class means that there is no visibility restrictions.
 *
 * @since v6.4
 */
@Immutable
public final class PublicVisibility implements Visibility
{
    final static PublicVisibility INSTANCE = new PublicVisibility();

    private PublicVisibility() {}

    @Override
    public <T> T accept(final VisibilityVisitor<T> visitor)
    {
        return visitor.visit(this);
    }
}
