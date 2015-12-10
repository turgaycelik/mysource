package com.atlassian.jira.jql.resolver;

import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestWorkRatioIndexInfoResolver
{
    @Test
    public void testConvertToIndexValue() throws Exception
    {
        final WorkRatioIndexInfoResolver resolver = new WorkRatioIndexInfoResolver();
        assertEquals(Collections.singletonList("00050"), resolver.getIndexedValues(50L));
        assertEquals(Collections.singletonList("00040"), resolver.getIndexedValues("40"));
    }
}
