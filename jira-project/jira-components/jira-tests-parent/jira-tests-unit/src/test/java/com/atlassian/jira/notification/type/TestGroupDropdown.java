/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.List;
import java.util.Map;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestGroupDropdown
{
    @Mock
    @AvailableInContainer
    private UserUtil userUtil;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer
    private UserPreferencesManager userPreferencesManager;


    @Rule
    public RuleChain mocks = MockitoMocksInContainer.forTest(this);

    private GroupDropdown groupDropdown;

    @Before
    public void setup()
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());

        groupDropdown = new GroupDropdown(jiraAuthenticationContext);
    }


    @Test
    public void assertConstants()
    {
        assertEquals("group", groupDropdown.getType());
        assertEquals("admin.notification.types.group", groupDropdown.getDisplayName());
    }

    @Test
    public void validationShouldCheckNonEmptyStringExistenceInParams()
    {
        Map<String, String> parameters = ImmutableMap.of(
                "blank" , "",
                "valid" , "parameter"
        );
        assertFalse(groupDropdown.doValidation("nonexisting", parameters));
        assertFalse(groupDropdown.doValidation("blank", parameters));
        assertTrue(groupDropdown.doValidation("valid", parameters));
    }

    @Test
    public void shouldAddRecipientsBasingOffGroupName()
    {
        ApplicationUser user1 = new MockApplicationUser("uname1", "dname1", "user1@somewhere.com" );
        ApplicationUser user2 = new MockApplicationUser("uname2", "dname2", "user2@somewhere.com" );
        ApplicationUser user3 = new MockApplicationUser("uname3", "dname3", "user3@somewhere.com" );

        ExtendedPreferences prefs = mock(ExtendedPreferences.class);
        when(prefs.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn("html");
        when(userPreferencesManager.getExtendedPreferences(any(ApplicationUser.class))).thenReturn(prefs);

        when(userUtil.getAllUsersInGroupNames(ImmutableList.of("crazy-group"))).thenReturn(ImmutableSortedSet.of(user1.getDirectoryUser(), user2.getDirectoryUser(), user3.getDirectoryUser()));

        final List<NotificationRecipient> recipients = groupDropdown.getRecipients(null, "crazy-group");

        assertThat(recipients, Matchers.containsInAnyOrder(
                new NotificationRecipient(user1),
                new NotificationRecipient(user2),
                new NotificationRecipient(user3)
        ));

    }

}
