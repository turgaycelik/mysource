package com.atlassian.jira.mock.ofbiz.matchers;

import com.google.common.base.Objects;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.ofbiz.core.entity.EntityExpr;

/**
 * Mockito/hamcrest matcher factory for an {@code EntityExpr}.
 *
 * @since v6.2
 */
public class EntityExprMatcher extends ArgumentMatcher<EntityExpr>
{
    private final EntityExpr expected;

    private EntityExprMatcher(final EntityExpr expected)
    {
        this.expected = expected;
    }

    public static EntityExprMatcher entityExpr(EntityExpr expected)
    {
        return new EntityExprMatcher(expected);
    }

    @Override
    public boolean matches(final Object o)
    {
        return o instanceof EntityExpr && matches((EntityExpr)o);
    }

    private boolean matches(EntityExpr other)
    {
        return Objects.equal(expected.getOperator(), other.getOperator()) &&
                expected.isLUpper() == other.isLUpper() &&
                expected.isRUpper() == other.isRUpper() &&
                Objects.equal(expected.getLhs(), other.getLhs()) &&
                Objects.equal(expected.getRhs(), other.getRhs());
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("entity expression [");
        describeArgumentTo(description, expected.isLUpper(), expected.getLhs());
        description.appendText(" ").appendText(expected.getOperator().getCode()).appendText(" ");
        describeArgumentTo(description, expected.isRUpper(), expected.getRhs());
        description.appendText("]");
    }

    private static void describeArgumentTo(final Description description, boolean upper, Object value)
    {
        if (upper)
        {
            description.appendText("UPPER(");
        }
        description.appendValue(value);
        if (upper)
        {
            description.appendText(")");
        }
    }
}
