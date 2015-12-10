package com.atlassian.query.clause;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 *
 * @since v6.2
 */
public class PropertyTest
{

    @Test
    public void shouldSplitKeyAndObjectReferencesIfContainsDots()
    {
        final Property property = new Property(ImmutableList.of("key1.key2","key3"),ImmutableList.of("prop1.prop2","prop3"));

        assertThat(property.getKeys(), Matchers.contains("key1", "key2", "key3"));
        assertThat(property.getObjectReferences(), Matchers.contains("prop1", "prop2", "prop3"));
    }

}
