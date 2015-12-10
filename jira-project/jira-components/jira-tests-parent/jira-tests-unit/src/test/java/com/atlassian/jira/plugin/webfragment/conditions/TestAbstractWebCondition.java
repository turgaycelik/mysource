
package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.Collections;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.web.Condition;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestAbstractWebCondition
{
    private static final Map<String,Object> ANONYMOUS = Collections.emptyMap();

    private User expectedUser;
    private ApplicationUser expectedAppUser;

    @Mock private UserUtil mockUserUtil;

    @After
    public void tearDown()
    {
        expectedUser = null;
        expectedAppUser = null;
        mockUserUtil = null;
    }

    @Test
    public void testShouldDisplayWithUser()
    {
        asFred();
        checkShouldDisplay(true, context("user", expectedUser));
    }

    @Test
    public void testShouldDisplayWithUsername()
    {
        asFred();
        checkShouldDisplay(true, context("username", "fred"));
    }

    @Test
    public void testShouldNotDisplayWithUser()
    {
        checkShouldDisplay(false, context("username", "fred"));
    }

    @Test
    public void testShouldDisplayAnonymous()
    {
        checkShouldDisplay(true, ANONYMOUS);
    }

    @Test
    public void testShouldNotDisplayAnonymous()
    {
        checkShouldDisplay(false, ANONYMOUS);
    }


    private ApplicationUser asFred()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        expectedUser = new MockUser("fred");
        expectedAppUser = new DelegatingApplicationUser("fred", expectedUser);
        when(mockUserUtil.getUserByName("fred")).thenReturn(expectedAppUser);
        return expectedAppUser;
    }

    private void checkShouldDisplay(final boolean expectedResult, Map<String,Object> context)
    {
        final Condition condition = new AbstractWebCondition()
        {
            @Override
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }

            @Override
            public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper)
            {
                assertEquals(expectedAppUser, user);
                return expectedResult;
            }
        };
        assertEquals(expectedResult, condition.shouldDisplay(context));
    }

    private Map<String,Object> context(String key, Object value)
    {
        return ImmutableMap.of(key, value);
    }
}
