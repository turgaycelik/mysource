package com.atlassian.jira.auditing;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


public class TestAuditingCategory
{
    @Test
    public void testEnumSynchronizedWithStable() throws Exception
    {
        final ImmutableList<String> expectedNames = ImmutableList.of(
                "AUDITING",
                "USER_MANAGEMENT",
                "GROUP_MANAGEMENT",
                "PERMISSIONS",
                "WORKFLOWS",
                "NOTIFICATIONS",
                "FIELDS",
                "PROJECTS",
                "SYSTEM");
        final ImmutableList<String> actualNames = ImmutableList.copyOf(
                Iterables.transform(
                        Arrays.asList(AuditingCategory.values()),
                        new Function<AuditingCategory, String>()
                        {
                            @Override
                            public String apply(final AuditingCategory input)
                            {
                                return input.name();
                            }
                        }));
        assertEquals("ACHTUNG! Any additions to AuditingCategory enum should be BACK PORTED to stable "
                        + "or otherwise there are problems downgrading from OnDemand - JRA-38083!"
                        + " (WARNING! It is not enough just to add new token to list above, you "
                        + " must go and change enum on current stable branch. Now!)",
                expectedNames, actualNames);
    }
}

