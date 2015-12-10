package com.atlassian.jira.plugin.report.impl;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestUserSubTaskIncludeValuesGenerator
{
    @Mock private I18nHelper.BeanFactory i18nHelperFactory;
    @Mock private JiraAuthenticationContext authContext;

    @Test
    public void testSubTasksEnabled() throws Exception
    {
        UserSubTaskIncludeValuesGenerator generator = new UserSubTaskIncludeValuesGenerator(authContext, i18nHelperFactory);
        final MockUser user = new MockUser("TestUserSubTaskIncludeValuesGenerator");
        when(i18nHelperFactory.getInstance(user)).thenReturn(new MockI18nBean());
        Map generatorParams = EasyMap.build("User", user);
        Assert.assertEquals(2, generator.getValues(generatorParams).size());
    }
}