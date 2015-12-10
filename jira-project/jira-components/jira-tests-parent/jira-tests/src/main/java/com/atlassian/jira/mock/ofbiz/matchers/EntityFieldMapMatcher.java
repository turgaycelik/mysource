package com.atlassian.jira.mock.ofbiz.matchers;

import java.util.Iterator;
import java.util.Map;

import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.base.Objects;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.fail;

/**
 * Mockito/hamcrest matcher factory for an {@code EntityFieldMap}.
 *
 * @since v6.2
 */
public class EntityFieldMapMatcher extends ArgumentMatcher<EntityFieldMap>
{
    private final EntityFieldMap expected;

    private EntityFieldMapMatcher(final EntityFieldMap expected)
    {
        this.expected = expected;
    }

    public static EntityFieldMapMatcher entityFieldMap(Map<String,?> expected)
    {
        return new EntityFieldMapMatcher(new EntityFieldMap(expected, EntityOperator.AND));
    }

    public static EntityFieldMapMatcher entityFieldMap(EntityFieldMap expected)
    {
        return new EntityFieldMapMatcher(expected);
    }

    @Override
    public boolean matches(final Object o)
    {
        return o instanceof EntityFieldMap && matches((EntityFieldMap)o);
    }

    private boolean matches(EntityFieldMap other)
    {
        return Objects.equal(expected.getOperator(), other.getOperator()) &&
                toMap(expected).equals(toMap(other));
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendValue(expected);
    }

    private static Map<String,Object> toMap(EntityFieldMap entityFieldMap)
    {
        final Map<String,Object> map = newHashMap();
        final Iterator<? extends Map.Entry<String,?>> iter = entityFieldMap.getFieldEntryIterator();
        while (iter.hasNext())
        {
            final Map.Entry<String,?> entry = iter.next();
            if (map.put(entry.getKey(), entry.getValue()) != null)
            {
                fail("EntityFieldMap has multiple values for field '" + entry.getKey() + "': " + entityFieldMap);
            }
        }
        return map;
    }
}
