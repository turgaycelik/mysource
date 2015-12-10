/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestApplicationProperties
{
    @Mock
    ApplicationPropertiesStore applicationPropertiesStore;

    @Test
    public void testGettingProperties()
    {
        Mockito.when(applicationPropertiesStore.getStringFromDb(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK)).thenReturn(null);
        Mockito.when(applicationPropertiesStore.getStringFromDb(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY)).thenReturn(null);

        Mockito.when(applicationPropertiesStore.getString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK)).thenReturn("7");
        Mockito.when(applicationPropertiesStore.getString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY)).thenReturn("24");

        final ApplicationProperties applicationProperties = new ApplicationPropertiesImpl(applicationPropertiesStore);
        Assert.assertNull(applicationProperties.getString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        Assert.assertNull(applicationProperties.getString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));

        Assert.assertEquals("7", applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        Assert.assertEquals("24", applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
    }
}
