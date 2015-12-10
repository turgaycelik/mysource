/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import java.util.List;
import java.util.Locale;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.scheduler.SchedulerService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import junit.framework.Assert;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class TestIntegrityCheckManagerImpl
{
    @Rule
    public RuleChain mockAllTheThingsAaaaaa = MockitoMocksInContainer.forTest(this);


    @Mock
    private I18nHelper.BeanFactory i18nHelperFactory;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private SchedulerService schedulerService;

    @Mock
    private I18nHelper i18nHelper;

    private IntegrityCheckManagerImpl integrityCheckManager;

    @Before
    public void setUp() throws Exception
    {
        final Locale locale = Locale.ENGLISH;
        when(applicationProperties.getDefaultLocale()).thenReturn(locale);
        when(i18nHelperFactory.getInstance(locale)).thenReturn(i18nHelper);

        when(i18nHelper.getText(anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                return invocationOnMock.getArguments()[0];
            }
        });

        integrityCheckManager = new IntegrityCheckManagerImpl(null, i18nHelperFactory, applicationProperties, schedulerService);
    }

    @Test
    public void testGetIntegrityChecks()
    {
        final List integrityChecks = integrityCheckManager.getIntegrityChecks();
        assertEquals(6, integrityChecks.size());

        IntegrityCheck integrityCheck = (IntegrityCheck) integrityChecks.get(0);
        List checks = integrityCheck.getChecks();
        assertEquals(3, checks.size());

        Check check = (Check) checks.get(0);
        assertSame(integrityCheck, check.getIntegrityCheck());
        check = (Check) checks.get(1);
        assertSame(integrityCheck, check.getIntegrityCheck());
        integrityCheck = (IntegrityCheck) integrityChecks.get(1);
        checks = integrityCheck.getChecks();
        assertEquals(1, checks.size());
        check = (Check) checks.get(0);
        assertSame(integrityCheck, check.getIntegrityCheck());
    }

    @Test
    public void testGetCheck()
    {
        final Check check = integrityCheckManager.getCheck(new Long(1));
        Assert.assertNotNull(check);
        Assert.assertNotNull(check.getIntegrityCheck());
    }

}
