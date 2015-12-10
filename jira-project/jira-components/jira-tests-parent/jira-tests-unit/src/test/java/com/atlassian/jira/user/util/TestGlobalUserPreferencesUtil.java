package com.atlassian.jira.user.util;

import java.util.List;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertEquals;

public class TestGlobalUserPreferencesUtil
{
    private GlobalUserPreferencesUtil globalUserPreferencesUtil;
    private MockOfBizDelegator mockOfBizDelegator;

    @Before
    public void setUp() throws Exception
    {
        mockOfBizDelegator = new MockOfBizDelegator(null, null);
        globalUserPreferencesUtil = new GlobalUserPreferencesUtilImpl(mockOfBizDelegator, null);
    }

    @Test
    public void totalCountExternalWithNoLocalUsers()
    {
        final List<GenericValue> externalEntityGenericValues = ImmutableList.of
                (
                        createMockGenericValue(1L, "ApplicationUser", "html"),
                        new MockGenericValue
                                (
                                        "ApplicationUser",
                                        ImmutableMap.of
                                                (
                                                        "id", 1L
                                                )
                                )
                );

        mockOfBizDelegator.setGenericValues(externalEntityGenericValues);

        // There's no local users.
        long userCount = globalUserPreferencesUtil.getTotalUpdateUserCountMailMimeType("text");
        assertEquals("Total count", 1, userCount);


        // all users already have html set.
        userCount = globalUserPreferencesUtil.getTotalUpdateUserCountMailMimeType("html");
        assertEquals("Total count", 0, userCount);
    }

    private GenericValue createMockGenericValue(final Long id, final String entityName, final String propertyValue)
    {
        return new MockGenericValue
                (
                        "OSUserPropertySetView",
                        ImmutableMap.of
                                (
                                        "entityName", entityName,
                                        "entityId", id,
                                        "propertyValue", propertyValue,
                                        "propertyKey", "user.notifications.mimetype"
                                )
                );
    }
}

