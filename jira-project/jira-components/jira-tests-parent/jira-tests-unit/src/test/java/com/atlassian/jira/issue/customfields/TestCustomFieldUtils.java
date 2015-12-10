/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.customfields;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.FieldVisibilityManager;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCustomFieldUtils
{

    @Test
    public void testParseCustomFieldId()
    {
        assertEquals(123, CustomFieldUtils.getCustomFieldId("customfield_123").longValue());
        assertEquals(123, CustomFieldUtils.getCustomFieldId("customfield_123:after").longValue());
        assertEquals(123, CustomFieldUtils.getCustomFieldId("customfield_123:before").longValue());
    }

    @Test
    public void testIsShownAndVisibleOld()
    {
        Mock customFieldMock = new Mock(CustomField.class);
        FieldVisibilityManager fieldVisibilityManager = new FieldVisibilityManager() {

            public boolean isFieldHidden(final User remoteUser, final String id)
            {
                return false;
            }

            public boolean isFieldHidden(final String fieldId, final Issue issue)
            {
                return false;
            }

            public boolean isCustomFieldHidden(final Long projectId, final Long customFieldId, final String issueTypeId)
            {
                return false;
            }

            public boolean isFieldHidden(final Long projectId, final String fieldId, final Long issueTypeId)
            {
                return false;
            }

            public boolean isFieldHidden(final Long projectId, final String fieldId, final String issueTypeId)
            {
                return false;
            }

            public boolean isFieldHiddenInAllSchemes(final Long projectId, final String fieldId, final List issueTypes)
            {
                return false;
            }

            public boolean isFieldHiddenInAllSchemes(final Long projectId, final String fieldId)
            {
                return false;
            }

            public boolean isFieldHiddenInAllSchemes(String fieldId, SearchContext context, User user)
            {
                return Boolean.valueOf(fieldId).booleanValue();
            }

            @Override
            public boolean isFieldVisible(User remoteUser, String id)
            {
                return true;
            }

            @Override
            public boolean isFieldVisible(String fieldId, Issue issue)
            {
                return true;
            }

            @Override
            public boolean isFieldVisible(Long projectId, String fieldId, Long issueTypeId)
            {
                return true;
            }

            @Override
            public boolean isFieldVisible(Long projectId, String fieldId, String issueTypeId)
            {
                return true;
            }

            @Override
            public boolean isCustomFieldVisible(Long projectId, Long customFieldId, String issueTypeId)
            {
                return true;
            }
        };

        //false false
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.FALSE);
        customFieldMock.expectAndReturn("getId", "true");
        assertFalse(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), null, null, fieldVisibilityManager));

        //false, true
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.FALSE);
        customFieldMock.expectAndReturn("getId", "false");
        assertFalse(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), null, null, fieldVisibilityManager));

        //true false
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.TRUE);
        customFieldMock.expectAndReturn("getId", "true");
        assertFalse(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), null, null, fieldVisibilityManager));

        //true true
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.TRUE);
        customFieldMock.expectAndReturn("getId", "false");
        assertTrue(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), null, null, fieldVisibilityManager));
    }

    @Test
    public void testIsShownAndVisibleNew()
    {
        Mock customFieldMock = new Mock(CustomField.class);
        FieldVisibilityManager fieldVisibilityManager = new FieldVisibilityManager() {

            public boolean isFieldHidden(final User remoteUser, final String id)
            {
                return false;
            }

            public boolean isFieldHidden(final String fieldId, final Issue issue)
            {
                return false;
            }

            public boolean isCustomFieldHidden(final Long projectId, final Long customFieldId, final String issueTypeId)
            {
                return false;
            }

            public boolean isFieldHidden(final Long projectId, final String fieldId, final Long issueTypeId)
            {
                return false;
            }

            public boolean isFieldHidden(final Long projectId, final String fieldId, final String issueTypeId)
            {
                return false;
            }

            public boolean isFieldHiddenInAllSchemes(final Long projectId, final String fieldId, final List issueTypes)
            {
                return false;
            }

            public boolean isFieldHiddenInAllSchemes(final Long projectId, final String fieldId)
            {
                return false;
            }

            public boolean isFieldHiddenInAllSchemes(String fieldId, SearchContext context, User user)
            {
                return Boolean.valueOf(fieldId).booleanValue();
            }

            @Override
            public boolean isFieldVisible(User remoteUser, String id)
            {
                return true;
            }

            @Override
            public boolean isFieldVisible(String fieldId, Issue issue)
            {
                return true;
            }

            @Override
            public boolean isFieldVisible(Long projectId, String fieldId, Long issueTypeId)
            {
                return true;
            }

            @Override
            public boolean isFieldVisible(Long projectId, String fieldId, String issueTypeId)
            {
                return true;
            }

            @Override
            public boolean isCustomFieldVisible(Long projectId, Long customFieldId, String issueTypeId)
            {
                return true;
            }
        };
        final User user = new MockUser("dave");

        //false false
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.FALSE);
        customFieldMock.expectAndReturn("getId", "true");
        assertFalse(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), user, null, fieldVisibilityManager));

        //false, true
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.FALSE);
        customFieldMock.expectAndReturn("getId", "false");
        assertFalse(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), user, null, fieldVisibilityManager));

        //true false
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.TRUE);
        customFieldMock.expectAndReturn("getId", "true");
        assertFalse(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), user, null, fieldVisibilityManager));

        //true true
        customFieldMock.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.TRUE);
        customFieldMock.expectAndReturn("getId", "false");
        assertTrue(CustomFieldUtils.isShownAndVisible((CustomField) customFieldMock.proxy(), user, null, fieldVisibilityManager));
    }
}
