package com.atlassian.jira.auditing;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TestAssociatedItem
{
    @Test
    public void testTypeEnumSynchronizedWithStable() throws Exception
    {
        final ImmutableList<String> expectedNames = ImmutableList.of(
                "USER",
                "PROJECT",
                "GROUP",
                "SCHEME",
                "REMOTE_DIRECTORY",
                "WORKFLOW",
                "PERMISSIONS",
                "VERSION",
                "CUSTOM_FIELD",
                "PROJECT_CATEGORY",
                "PROJECT_COMPONENT",
                "PROJECT_ROLE",
                "LICENSE");
        final ImmutableList<String> actualNames = ImmutableList.copyOf(
                Iterables.transform(
                        Arrays.asList(AssociatedItem.Type.values()),
                        new Function<AssociatedItem.Type, String>()
                        {
                            @Override
                            public String apply(final AssociatedItem.Type input)
                            {
                                return input.name();
                            }
                        }));
        assertEquals("ACHTUNG! Any additions to AssociatedItem.Type enum should be BACK PORTED to stable (otherwise there are problems downgrading from OnDemand - JRA-38083)",
                expectedNames, actualNames);
    }
}