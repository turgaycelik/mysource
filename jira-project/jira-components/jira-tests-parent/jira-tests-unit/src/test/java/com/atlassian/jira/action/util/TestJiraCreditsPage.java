package com.atlassian.jira.action.util;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.action.util.JiraCreditsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestJiraCreditsPage
{
    @Mock
    private BuildUtilsInfo buildUtilsInfo;

    @Test
    public void getBuildVersionReturnsTheVersionOnBuildUtilsInfo()
    {
        String expectedVersion = "1.0";
        when(buildUtilsInfo.getVersion()).thenReturn(expectedVersion);

        String actualVersionVersion = new JiraCreditsPage(buildUtilsInfo).getBuildVersion();

        assertThat(actualVersionVersion, is(expectedVersion));
    }
}
