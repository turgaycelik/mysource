package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.util.StringList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IncludedFieldsTest
{
    @Test
    public void testAllFields()
    {
        IncludedFields fields = IncludedFields.includeAllByDefault(null);
        assertTrue(fields.included("foo", true));
        assertTrue(fields.included("bar", false));
        assertTrue(fields.included("comments", false));
    }

    @Test
    public void testAllNavFields()
    {
        IncludedFields fields = IncludedFields.includeNavigableByDefault(null);
        assertTrue(fields.included("foo", true));
        assertFalse(fields.included("bar", false));
        assertFalse(fields.included("comments", false));
    }

    @Test
    public void testSpecificAllFields()
    {
        IncludedFields fields = IncludedFields.includeAllByDefault(Arrays.asList(StringList.fromQueryParam("comments")));
        assertFalse(fields.included("foo", true));
        assertFalse(fields.included("bar", false));
        assertTrue(fields.included("comments", false));
    }

    @Test
    public void testSpecificNavFields()
    {
        IncludedFields fields = IncludedFields.includeNavigableByDefault(Arrays.asList(StringList.fromQueryParam("comments")));
        assertFalse(fields.included("foo", true));
        assertFalse(fields.included("bar", false));
        assertTrue(fields.included("comments", false));
    }

    @Test
    public void testAllMinusSome()
    {
        IncludedFields fields = IncludedFields.includeAllByDefault(Arrays.asList(StringList.fromQueryParam("-comments,-worklog")));
        assertTrue(fields.included("foo", true));
        assertTrue(fields.included("bar", false));
        assertFalse(fields.included("comments", false));
        assertFalse(fields.included("worklog", false));
    }

    @Test
    public void testExplicitNavigableWhenAllDefault()
    {
        IncludedFields fields = IncludedFields.includeAllByDefault(Arrays.asList(StringList.fromQueryParam("*navigable")));
        assertTrue(fields.included("foo", true));
        assertFalse(fields.included("bar", false));
        assertFalse(fields.included("comments", false));
        assertFalse(fields.included("worklog", false));
    }

    @Test
    public void testExplicitAllWhenNavigableDefault()
    {
        IncludedFields fields = IncludedFields.includeNavigableByDefault(Arrays.asList(StringList.fromQueryParam("*all")));
        assertTrue(fields.included("foo", true));
        assertTrue(fields.included("bar", false));
        assertTrue(fields.included("comments", false));
        assertTrue(fields.included("worklog", false));
    }

    @Test
    public void testExplicitNavigablePlusSome()
    {
        IncludedFields fields = IncludedFields.includeAllByDefault(Arrays.asList(StringList.fromQueryParam("*navigable,comments,worklog")));
        assertTrue(fields.included("foo", true));
        assertFalse(fields.included("bar", false));
        assertTrue(fields.included("comments", false));
        assertTrue(fields.included("worklog", false));
    }
    @Test
    public void testExplicitAllMinusSome()
    {
        IncludedFields fields = IncludedFields.includeAllByDefault(Arrays.asList(StringList.fromQueryParam("*all,-comments,-worklog")));
        assertTrue(fields.included("foo", true));
        assertTrue(fields.included("bar", false));
        assertFalse(fields.included("comments", false));
        assertFalse(fields.included("worklog", false));
    }
    @Test
    public void testExplicitAllMinusSomePlusOthers()
    {
        IncludedFields fields = IncludedFields.includeAllByDefault(Arrays.asList(StringList.fromQueryParam("*all,-comments,+worklog")));
        assertTrue(fields.included("foo", true));
        assertTrue(fields.included("bar", false));
        assertFalse(fields.included("comments", false));
        assertTrue(fields.included("worklog", false));
    }
}
